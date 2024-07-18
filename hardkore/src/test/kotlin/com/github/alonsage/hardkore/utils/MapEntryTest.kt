package com.github.alonsage.hardkore.utils

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class MapEntryTest {
    @Test
    fun `values the same as provided`() {
        val key = UUID.randomUUID()
        val value = UUID.randomUUID()
        val entry = MapEntry(key, value)
        assertEquals(key, entry.key)
        assertEquals(value, entry.value)
    }
}