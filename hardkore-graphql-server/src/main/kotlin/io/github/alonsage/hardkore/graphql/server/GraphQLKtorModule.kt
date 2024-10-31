package io.github.alonsage.hardkore.graphql.server

import graphql.ExecutionResult
import graphql.GraphQL
import graphql.GraphQLContext
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLErrorDto
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLRequestDto
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLResponseDto
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLSubscriptionMessageDto
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLSubscriptionStatus
import io.github.alonsage.hardkore.ktor.server.KtorModule
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.contentType
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.util.pipeline.PipelineContext
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
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
        route("/graphql") {
            contentType(ContentType.MultiPart.FormData) {
                post { handleMultipart() }
            }
            contentType(ContentType.Application.Json) {
                post { handleJson() }
                    .install(ContentNegotiation) { json(json) }
            }
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
        webSocket("/graphql", protocol = "graphql-transport-ws") { handleWebSocket() }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleMultipart() {
        supervisorScope {
            var request: GraphQLRequestDto? = null
            var filesMapping: Map<String, List<String>>? = null
            val fileItems = mutableMapOf<String, PartData.FileItem>()

            val data = call.receiveMultipart()
            while (true) {
                val part = data.readPart() ?: break
                when {
                    part.name == "operations" && part is PartData.FormItem ->
                        request = try {
                            json.decodeFromString(part.value)
                        } catch (e: SerializationException) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@supervisorScope
                        }

                    part.name == "map" && part is PartData.FormItem ->
                        filesMapping = try {
                            json.decodeFromString(part.value)
                        } catch (e: SerializationException) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@supervisorScope
                        }

                    part.name != null && part is PartData.FileItem ->
                        fileItems[part.name!!] = part

                    else -> {}
                }
            }

            if (request == null || filesMapping == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@supervisorScope
            }

            val fileOverrides = filesMapping.entries
                .fold(emptyMap<String, PartData.FileItem>()) { mapping, (fileRef, paths) ->
                    val fileItem = fileItems[fileRef] ?: run {
                        call.respond(HttpStatusCode.BadRequest)
                        return@supervisorScope
                    }
                    mapping + paths.map { it to fileItem }
                }

            val executionInput = request.executionInput(
                dataLoaderRegistry = dataLoaderRegistryFactory.dataLoaderRegistry(call, this),
                coroutineScope = this,
                context = mapOf(ApplicationCall::class to call),
                fileOverrides = fileOverrides
            )

            val executionResult = graphQL.executeAsync(executionInput).await()
            call.respondText(json.encodeToString(GraphQLResponseDto(executionResult)), ContentType.Application.Json)
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleJson() {
        supervisorScope {
            val request: GraphQLRequestDto = call.receive()
            val executionInput = request.executionInput(
                dataLoaderRegistry = dataLoaderRegistryFactory.dataLoaderRegistry(call, this),
                coroutineScope = this,
                context = mapOf(ApplicationCall::class to call)
            )
            val executionResult = graphQL.executeAsync(executionInput).await()
            call.respond(GraphQLResponseDto(executionResult))
        }
    }

    private suspend fun DefaultWebSocketServerSession.handleWebSocket() {
        val graphQLContext = AtomicReference<GraphQLContext?>(null)
        val subscriptions = ConcurrentHashMap<String, Job>()

        suspend fun closeSession(status: GraphQLSubscriptionStatus) {
            close(CloseReason(status.code.toShort(), status.reason))
        }

        suspend fun sendMessage(message: GraphQLSubscriptionMessageDto) {
            sendSerialized<GraphQLSubscriptionMessageDto>(message)
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
                        val newContext = GraphQLContext.newContext().build()
                        if (graphQLContext.compareAndSet(null, newContext)) {
                            sendMessage(GraphQLSubscriptionMessageDto.ConnectionAck())
                        } else {
                            closeSession(GraphQLSubscriptionStatus.TOO_MANY_REQUESTS)
                        }
                    }

                    is GraphQLSubscriptionMessageDto.Ping -> {
                        sendMessage(GraphQLSubscriptionMessageDto.Pong())
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
                                    dataLoaderRegistry = dataLoaderRegistryFactory.dataLoaderRegistry(call, this),
                                    coroutineScope = this,
                                    context = mapOf(ApplicationCall::class to call)
                                )
                                val executionResult = graphQL.executeAsync(executionInput).await()
                                executionResult.getData<Publisher<ExecutionResult>>().asFlow()
                                    .onEach { result ->
                                        if (result.errors.isNullOrEmpty()) {
                                            sendMessage(
                                                GraphQLSubscriptionMessageDto.Next(
                                                    message.id,
                                                    GraphQLResponseDto(result)
                                                )
                                            )
                                        } else {
                                            sendMessage(
                                                GraphQLSubscriptionMessageDto.Error(
                                                    message.id,
                                                    result.errors.map { GraphQLErrorDto(it) }
                                                )
                                            )
                                        }
                                    }
                                    .onCompletion {
                                        if (it == null) {
                                            sendMessage(GraphQLSubscriptionMessageDto.Complete(message.id))
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

