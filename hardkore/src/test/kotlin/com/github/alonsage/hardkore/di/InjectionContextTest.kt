package com.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertSame

class InjectionContextTest {
    @Test
    fun `provider works`() {
        val context = DiContext {
            bindFactory { UUID.randomUUID() }
        }

        val bean1: UUID = context.bean()

        val beanProvider: BeanProvider<UUID> = context.provider()
        val bean2 = beanProvider.bean()

        val bean3: UUID by context.provider()

        assertSame(bean1, bean2)
        assertEquals(bean1, bean2)

        assertSame(bean1, bean3)
        assertEquals(bean1, bean3)
    }
}