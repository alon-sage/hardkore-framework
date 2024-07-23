package io.github.alonsage.hardkore.ktor.server.starter

import io.github.alonsage.hardkore.runtime.runApplication
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorServerStarterTest {
    @Test
    fun `web-server command works`() {
        withConfiguredHostPort { (host, port) ->
            val command = thread(start = true, isDaemon = true) {
                runApplication(arrayOf("web-server"))
            }
            try {
                runBlocking {
                    flow {
                        val client = HttpClient()
                        val response = client.get("http://$host:$port/health")
                        assertTrue(response.status.isSuccess())
                        assertEquals("OK", response.bodyAsText())
                        emit(Unit)
                    }.retry(20) {
                        delay(250)
                        true
                    }.collect()
                }
            } finally {
                command.interrupt()
                command.join()
            }
        }
    }
}