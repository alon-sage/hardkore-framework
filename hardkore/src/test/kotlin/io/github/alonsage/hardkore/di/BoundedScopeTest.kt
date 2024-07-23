package io.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertSame

class BoundedScopeTest {
    @Test
    fun `initializer works`() {
        val bean = UUID.randomUUID()
        val beanRef = beanRef<UUID>()
        val scopeContext = BoundedScope { add(bean) }

        val instance = scopeContext.beanRegistry.bean(beanRef, UUID::randomUUID)

        assertSame(bean, instance)
        assertEquals(bean, instance)
    }
}