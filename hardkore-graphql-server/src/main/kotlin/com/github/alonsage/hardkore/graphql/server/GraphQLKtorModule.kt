package com.github.alonsage.hardkore.graphql.server

import com.github.alonsage.hardkore.graphql.server.dto.GraphQLErrorDto
import com.github.alonsage.hardkore.graphql.server.dto.GraphQLRequestDto
import com.github.alonsage.hardkore.graphql.server.dto.GraphQLResponseDto
import com.github.alonsage.hardkore.graphql.server.dto.GraphQLSubscriptionMessageDto
import com.github.alonsage.hardkore.graphql.server.dto.GraphQLSubscriptionStatus
import com.github.alonsage.hardkore.ktor.server.KtorModule
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.GraphQLContext
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.reactivestreams.Publisher
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.set

class GraphQLKtorModule(
    private val properties: GraphQLProperties,
    private val graphQL: GraphQL,
    private val dataLoaderRegistryFactory: DataLoaderRegistryFactory
) : KtorModule {
    private val json = Json {
        ignoreUnknownKeys = true

        @OptIn(ExperimentalSerializationApi::class)
        explicitNulls = false
    }

    override fun Application.configure() {
        routing {
            if (properties.authentication == GraphQLAuthentication.NONE) {
                graphQLPostEndpoint()
                graphQLSubscriptionsEndpoint()
            } else {
                val optional = properties.authentication == GraphQLAuthentication.OPTIONAL
                if (properties.authProviders.isEmpty()) {
                    authenticate(optional = optional) {
                        graphQLPostEndpoint()
                        graphQLSubscriptionsEndpoint()
                    }
                } else {
                    authenticate(*properties.authProviders.toTypedArray(), optional = optional) {
                        graphQLPostEndpoint()
                        graphQLSubscriptionsEndpoint()
                    }
                }
            }
        }
    }

    private fun Route.graphQLPostEndpoint() {
        val route = post("/graphql") {
            supervisorScope {
                val request: GraphQLRequestDto = call.receive()
                val executionInput = request.executionInput(
                    dataLoaderRegistry = dataLoaderRegistryFactory.dataLoaderRegistry(this),
                    coroutineScope = this,
                    context = mapOf(ApplicationCall::class to call)
                )
                val executionResult = graphQL.executeAsync(executionInput).await()
                call.respond(GraphQLResponseDto(executionResult))
            }
        }
        route.install(ContentNegotiation) {
            json(json)
        }
    }

    private fun Route.graphQLSubscriptionsEndpoint() {
        application.install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        webSocket("/graphql", protocol = "graphql-transport-ws") {
            val graphQLContext = AtomicReference<GraphQLContext?>(null)
            val subscriptions = ConcurrentHashMap<String, Job>()

            suspend fun closeSession(status: GraphQLSubscriptionStatus) {
                close(CloseReason(status.code.toShort(), status.reason))
            }

            launch {
                delay(properties.subscriptionTimeoutMillis)
                if (graphQLContext.get() == null) {
                    closeSession(GraphQLSubscriptionStatus.CONNECTION_INIT_TIMEOUT)
                }
            }

            try {
                while (isActive) {
                    when (val message = receiveDeserialized<GraphQLSubscriptionMessageDto>()) {
                        is GraphQLSubscriptionMessageDto.ConnectionInit -> {
                            val newContext = GraphQLContext.newContext()
                                .build()
                            if (graphQLContext.compareAndSet(null, newContext)) {
                                sendSerialized(GraphQLSubscriptionMessageDto.ConnectionAck())
                            } else {
                                closeSession(GraphQLSubscriptionStatus.TOO_MANY_REQUESTS)
                            }
                        }

                        is GraphQLSubscriptionMessageDto.Ping -> {
                            sendSerialized(GraphQLSubscriptionMessageDto.Pong())
                        }

                        is GraphQLSubscriptionMessageDto.Subscribe -> when {
                            graphQLContext.get() == null -> {
                                closeSession(GraphQLSubscriptionStatus.UNAUTHORIZED)
                            }

                            subscriptions.containsKey(message.id) -> {
                                closeSession(GraphQLSubscriptionStatus.conflict(message.id))
                            }

                            else -> {
                                val job = launch {
                                    val executionInput = message.payload.executionInput(
                                        dataLoaderRegistry = dataLoaderRegistryFactory.dataLoaderRegistry(this),
                                        coroutineScope = this,
                                        context = mapOf(ApplicationCall::class to call)
                                    )
                                    val executionResult = graphQL.executeAsync(executionInput).await()
                                    executionResult.getData<Publisher<ExecutionResult>>().asFlow()
                                        .onEach { result ->
                                            if (result.errors.isNullOrEmpty()) {
                                                sendSerialized(
                                                    GraphQLSubscriptionMessageDto.Next(
                                                        message.id,
                                                        GraphQLResponseDto(result)
                                                    )
                                                )
                                            } else {
                                                sendSerialized(
                                                    GraphQLSubscriptionMessageDto.Error(
                                                        message.id,
                                                        result.errors.map { GraphQLErrorDto(it) }
                                                    )
                                                )
                                            }
                                        }
                                        .onCompletion {
                                            if (it == null) {
                                                sendSerialized(GraphQLSubscriptionMessageDto.Complete(message.id))
                                            } else {
                                                closeSession(GraphQLSubscriptionStatus.SERVER_ERROR)
                                            }
                                        }
                                        .collect()
                                }
                                subscriptions[message.id] = job
                                job.invokeOnCompletion { subscriptions.remove(message.id) }
                            }
                        }

                        is GraphQLSubscriptionMessageDto.Complete -> {
                            subscriptions.remove(message.id)
                                ?.cancel()
                                ?: closeSession(GraphQLSubscriptionStatus.INVALID_MESSAGE)
                        }

                        else -> {
                            closeSession(GraphQLSubscriptionStatus.INVALID_MESSAGE)
                        }
                    }
                }
            } catch (e: Exception) {
                closeSession(GraphQLSubscriptionStatus.SERVER_ERROR)
            }
        }
    }
}

