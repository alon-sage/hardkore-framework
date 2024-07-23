package io.github.alonsage.hardkore.samples.webserver

import io.github.alonsage.hardkore.ktor.server.KtorServerDiProfile
import io.github.alonsage.hardkore.ktor.server.testing.webApplicationTest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `default greeting`() {
        webApplicationTest(KtorServerDiProfile::class) { client ->
            val response = client.get("/greeting")
            assertEquals("Hello World!",  response.bodyAsText())
        }
    }

    @Test
    fun `custom greeting`() {
        webApplicationTest(KtorServerDiProfile::class) { client ->
            val response = client.get("/greeting?name=Universe")
            assertEquals("Hello Universe!",  response.bodyAsText())
        }
    }
}