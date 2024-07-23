package io.github.alonsage.hardkore.ktor.server.testing

import io.github.alonsage.hardkore.ktor.server.KtorServerDiProfile
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebApplicationTest {
    @Test
    fun `web application test works`() =
        webApplicationTest(KtorServerDiProfile::class) { client ->
            val response = client.get("/health")
            assertTrue(response.status.isSuccess())
            assertEquals("OK", response.bodyAsText())
        }
}