package io.github.alonsage.hardkore.graphql.server.testing

import io.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import io.github.alonsage.hardkore.graphql.server.dto.GraphQLSubscriptionMessageDto
import io.github.alonsage.hardkore.ktor.server.testing.webApplicationTest
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GraphQLClientTest {
    @Test
    fun `query works`() =
        webApplicationTest(GraphQLServerDiProfile::class) { client ->
            val value = UUID.randomUUID().toString()
            val result = client.graphql().query(EchoQuery(value)).execute()
            assertNull(result.exception)
            assertNull(result.errors)
            assertNotNull(result.data) {
                assertEquals(value, it.echo)
            }
        }

    @Test
    fun `mutation works`() =
        webApplicationTest(GraphQLServerDiProfile::class) { client ->
            val value = UUID.randomUUID().toString()
            val result = client.graphql().mutation(EchoMutation(value)).execute()
            assertNull(result.exception)
            assertNull(result.errors)
            assertNotNull(result.data) {
                assertEquals(value, it.echo)
            }
        }

    @Test
    fun `subscription works`() =
        webApplicationTest(GraphQLServerDiProfile::class) { baseClient ->
            val client = baseClient.config { install(WebSockets) }
            val value = UUID.randomUUID().toString()
            val result = client.graphql().subscription(EchoSubscription(value)).toFlow()
            val items = result.take(11).onEach { println(1) }.toList()
            assertEquals(10, items.size)
            items.forEach { item ->
                assertNull(item.exception)
                assertNull(item.errors)
                assertNotNull(item.data) {
                    assertEquals(value, it.echo)
                }
            }
            println(2)
        }
}

