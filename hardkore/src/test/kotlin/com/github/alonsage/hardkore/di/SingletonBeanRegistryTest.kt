package com.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class SingletonBeanRegistryTest {
    @Test
    fun `cache instances`() {
        val beanRef = beanRef<UUID>()
        val registry = SingletonBeanRegistry()
        val instance1 = registry.bean(beanRef, UUID::randomUUID)
        val instance2 = registry.bean(beanRef, UUID::randomUUID)
        assertSame(instance1, instance2)
        assertEquals(instance1, instance2)
    }

    @Test
    fun `concurrency works correctly`() {
        val beanRef = beanRef<UUID>()
        val registry = SingletonBeanRegistry()

        val results = ConcurrentSkipListSet<UUID>()
        val numThreads = 32
        val barrier = CyclicBarrier(numThreads)
        val countDownLatch = CountDownLatch(numThreads)
        repeat(numThreads) {
            thread(start = true, isDaemon = true) {
                barrier.await()
                repeat(1000) {
                    results.add(registry.bean(beanRef, UUID::randomUUID))
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()

        assertEquals(1, results.distinct().size)
    }

    @Test
    fun `predefined bean is returned`() {
        val beanRef = beanRef<UUID>()
        val bean = UUID.randomUUID()
        val registry = SingletonBeanRegistry {
            add(bean)
        }

        val instance = registry.bean(beanRef, UUID::randomUUID)
        assertSame(bean, instance)
        assertEquals(bean, instance)
    }

    @Test
    fun `redefinition is restricted`() {
        SingletonBeanRegistry {
            add(UUID.randomUUID())
            val exception = assertThrows<IllegalArgumentException> {
                add(UUID.randomUUID())
            }
            assertContains(assertNotNull(exception.message), "Bean already added:")
        }
    }
}