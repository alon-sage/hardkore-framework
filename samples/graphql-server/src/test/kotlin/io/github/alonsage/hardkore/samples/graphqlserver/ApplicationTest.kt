package io.github.alonsage.hardkore.samples.graphqlserver

import io.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import io.github.alonsage.hardkore.graphql.server.testing.graphql
import io.github.alonsage.hardkore.ktor.server.testing.webApplicationTest
import io.github.alonsage.hardkore.samples.graphqlserver.type.EventSource
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApplicationTest {
    @Test
    fun `returns user with events`() {
        webApplicationTest(GraphQLServerDiProfile::class) { client ->
            val query = GetUserWithEventsQuery(
                userId = UUID.randomUUID().toString(),
                eventsNumber = 3
            )
            val data = client.graphql().query(query).execute().let { result ->
                assertNull(result.exception)
                assertNull(result.errors)
                assertNotNull(result.data)
            }
            assertNotNull(data.user) { user ->
                assertEquals(query.userId, user.id)
                assertEquals(query.eventsNumber, user.lastEvents?.size)
            }
        }
    }

    @Test
    fun `emits event`() {
        webApplicationTest(GraphQLServerDiProfile::class) { client ->
            val mutation = EmitEventMutation(
                userId = UUID.randomUUID().toString(),
                message = "Foo bar",
                source = EventSource.entries.filter { it != EventSource.UNKNOWN__ }.random()
            )
            val data = client.graphql().mutation(mutation).execute().let { result ->
                assertNull(result.exception)
                assertNull(result.errors)
                assertNotNull(result.data)
            }
            assertEquals(mutation.message, data.emitEvent?.message)
            assertEquals(mutation.source, data.emitEvent?.source)
        }
    }

    @Test
    fun `returns watched events`() {
        webApplicationTest(GraphQLServerDiProfile::class) { baseClient ->
            val client = baseClient.config { install(WebSockets) }
            val subscription = WatchEventsSubscription(userId = UUID.randomUUID().toString())
            val results = client.graphql().subscription(subscription).toFlow().take(10).toList()
            results.forEach { result ->
                assertNull(result.exception)
                assertNull(result.errors)
                assertNotNull(result.data) { data ->
                    assertNotNull(data.userEvents) { event ->
                        assertContains(event.message!!, subscription.userId!!)
                    }
                }
            }
        }
    }
}