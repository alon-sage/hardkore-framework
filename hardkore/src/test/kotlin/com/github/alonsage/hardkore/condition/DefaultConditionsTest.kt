package com.github.alonsage.hardkore.condition

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultConditionsTest {
    val condition1 = object : Condition {}
    val condition2 = object : Condition {}

    @Test
    fun `set abd clear work and don't affect other conditions`() {
        val conditions = DefaultConditions()

        assertFalse(conditions.get(condition1))
        assertFalse(conditions.get(condition2))
        conditions.set(condition1)
        assertTrue(conditions.get(condition1))
        assertFalse(conditions.get(condition2))
        conditions.clear(condition1)
        assertFalse(conditions.get(condition1))
        assertFalse(conditions.get(condition2))
    }

    @Test
    fun `wait works`() {
        val conditions = DefaultConditions()

        runBlocking {
            val job = launch { conditions.wait(condition1) }

            conditions.set(condition2)
            yield()
            assertFalse(job.isCompleted)

            conditions.set(condition1)
            yield()
            assertTrue(job.isCompleted)
        }
    }

    @Test
    fun `watch works`() {
        val conditions = DefaultConditions()

        runBlocking {
            val result = async { conditions.watch(condition1).take(2).toList() }

            conditions.set(condition2)
            yield()
            assertFalse(result.isCompleted)

            conditions.set(condition1)
            yield()
            assertTrue(result.isCompleted)

            assertEquals(result.await(), listOf(false, true))
        }
    }

    @Test
    fun `all works`() {
        val conditions = DefaultConditions()

        assertFalse(conditions.all(condition1, condition2))

        conditions.set(condition1)
        assertFalse(conditions.all(condition1, condition2))

        conditions.set(condition2)
        assertTrue(conditions.all(condition1, condition2))
    }

    @Test
    fun `waitAll works`() {
        val conditions = DefaultConditions()

        runBlocking {
            val job = launch { conditions.waitAll(condition1, condition2) }

            conditions.set(condition1)
            yield()
            assertFalse(job.isCompleted)

            conditions.set(condition2)
            yield()
            assertTrue(job.isCompleted)
        }
    }

    @Test
    fun `watchAll works`() {
        val conditions = DefaultConditions()

        runBlocking {
            val result = async { conditions.watchAll(condition1, condition2).take(2).toList() }

            conditions.set(condition1)
            yield()
            assertFalse(result.isCompleted)

            conditions.set(condition2)
            yield()
            assertTrue(result.isCompleted)

            assertEquals(result.await(), listOf(false, true))
        }
    }
}