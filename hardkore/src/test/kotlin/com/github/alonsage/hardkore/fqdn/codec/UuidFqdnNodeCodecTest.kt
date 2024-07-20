package com.github.alonsage.hardkore.fqdn.codec

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class UuidFqdnNodeCodecTest {
    @Test
    fun `encode works`() {
        val codec = UuidFqdnNodeCodec()
        val value = UUID.randomUUID()
        val encoded = codec.encode(value)
        assertEquals(value.toString(), encoded)
    }

    @Test
    fun `decode works`() {
        val codec = UuidFqdnNodeCodec()
        val value = UUID.randomUUID()
        val decoded = codec.decode(value.toString())
        assertEquals(value, decoded)
    }
}