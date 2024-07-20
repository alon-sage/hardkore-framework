package com.github.alonsage.hardkore.ktor.server

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

class KtorServerCommandTest {

    @Test
    fun `ktor server started by command responsible`() {
        withConfiguredHostPort { (host, port) ->
            val command = thread(start = true, isDaemon = true) {
                KtorServerCommand().main(emptyArray())
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