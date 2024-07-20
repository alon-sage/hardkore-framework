package com.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BeanRefTest {
    @Test
    fun `default qualifier is null`() {
        val beanRef = beanRef<String>()
        assertEquals(typeOf<String>(), beanRef.kType)
        assertNull(beanRef.qualifier)
    }

    @Test
    fun `qualifier is stored correctly`() {
        val qualifier = UUID.randomUUID()
        val beanRef = beanRef<Int>(qualifier)
        assertEquals(typeOf<Int>(), beanRef.kType)
        assertEquals(qualifier, beanRef.qualifier)
    }

    @Test
    fun `nullability is stored correctly`() {
        val beanRef = beanRef<Boolean?>()
        assertTrue(beanRef.kType.isMarkedNullable)
        assertEquals(typeOf<Boolean?>(), beanRef.kType)
        assertNull(beanRef.qualifier)
    }

    @Test
    fun `object qualifier works`() {
        val qualifier = object {}
        val beanRef = beanRef<UUID>(qualifier)
        assertEquals(typeOf<UUID>(), beanRef.kType)
        assertEquals(qualifier, beanRef.qualifier)
    }
}