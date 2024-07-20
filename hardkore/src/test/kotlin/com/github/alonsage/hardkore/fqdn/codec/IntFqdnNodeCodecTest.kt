package com.github.alonsage.hardkore.fqdn.codec

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class IntFqdnNodeCodecTest {
    @Test
    fun `encode works`() {
        val codec = IntFqdnNodeCodec()
        val value = Random.nextInt(Int.MAX_VALUE)
        val encoded = codec.encode(value)
        assertEquals(value.toString(), encoded)
    }

    @Test
    fun `decode works`() {
        val codec = IntFqdnNodeCodec()
        val value = Random.nextInt(Int.MAX_VALUE)
        val decoded = codec.decode(value.toString())
        assertEquals(value, decoded)
    }
}