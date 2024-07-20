package com.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class ScopeContextStacksTest {
    @Test
    fun `stack works`() {
        val stack = ScopeContextStacks()
        val scopeContext = BoundedScope {}

        val updatedStack = stack.push(scopeContext)

        assertNull(stack.top(BoundedScope))
        assertSame(scopeContext, updatedStack.top(BoundedScope))
    }
}