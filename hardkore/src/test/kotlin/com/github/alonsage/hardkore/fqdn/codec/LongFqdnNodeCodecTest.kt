package com.github.alonsage.hardkore.fqdn.codec

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class LongFqdnNodeCodecTest {
    @Test
    fun `encode works`() {
        val codec = LongFqdnNodeCodec()
        val value = Random.nextLong(Long.MAX_VALUE)
        val encoded = codec.encode(value)
        assertEquals(value.toString(), encoded)
    }

    @Test
    fun `decode works`() {
        val codec = LongFqdnNodeCodec()
        val value = Random.nextLong(Long.MAX_VALUE)
        val decoded = codec.decode(value.toString())
        assertEquals(value, decoded)
    }
}