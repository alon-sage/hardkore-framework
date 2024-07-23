package io.github.alonsage.hardkore.utils

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals

class SecretTest {
    @Test
    fun `value is not leaked by toString`() {
        val value = UUID.randomUUID().toString()
        val secret = Secret(value)
        assertEquals("********", secret.toString())
    }

    @Test
    fun `value accessible by property`() {
        val value = UUID.randomUUID().toString()
        val secret = Secret(value)
        assertEquals(value, secret.value)
    }

    @Test
    @OptIn(ExperimentalEncodingApi::class)
    fun `value encoded properly`() {
        val value = UUID.randomUUID().toString()
        val encoded = Base64.getEncoder().encodeToString(value.toByteArray())
        val secret = Secret.base64encoded(value.toByteArray())
        assertEquals(encoded, secret.value)
    }

    @Test
    @OptIn(ExperimentalEncodingApi::class)
    fun `value decoded properly`() {
        val value = UUID.randomUUID().toString()
        val encoded = Base64.getEncoder().encodeToString(value.toByteArray())
        val secret = Secret(encoded)
        assertEquals(value, String(secret.base64decoded()))
    }
}