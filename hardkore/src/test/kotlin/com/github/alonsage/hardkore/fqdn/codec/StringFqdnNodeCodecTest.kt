package com.github.alonsage.hardkore.fqdn.codec

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class StringFqdnNodeCodecTest {
    @Test
    fun `encode works`() {
        val codec = StringFqdnNodeCodec()
        val value = UUID.randomUUID().toString()
        val encoded = codec.encode(value)
        assertEquals(value, encoded)
    }

    @Test
    fun `decode works`() {
        val codec = StringFqdnNodeCodec()
        val value = UUID.randomUUID().toString()
        val decoded = codec.decode(value)
        assertEquals(value, decoded)
    }
}