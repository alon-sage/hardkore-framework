package com.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

class PrototypeBeanRegistryTest {
    @Test
    fun `doesn't cache instances`() {
        val beanRef = beanRef<UUID>()
        val instance1 = PrototypeBeanRegistry.bean(beanRef, UUID::randomUUID)
        val instance2 = PrototypeBeanRegistry.bean(beanRef, UUID::randomUUID)
        assertNotSame(instance1, instance2)
        assertNotEquals(instance1, instance2)
    }
}