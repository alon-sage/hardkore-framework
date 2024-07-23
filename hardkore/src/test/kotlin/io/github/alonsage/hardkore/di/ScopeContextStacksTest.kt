package io.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class ScopeContextStacksTest {
    @Test
    fun `push works`() {
        val stack = ScopeContextStacks()
        val scopeContext = BoundedScope {}

        val updatedStack = stack.push(scopeContext)

        assertNull(stack.top(BoundedScope))
        assertSame(scopeContext, updatedStack.top(BoundedScope))
    }
    @Test
    fun `push the same type works`() {
        val stack = ScopeContextStacks()
        val scopeContext1 = BoundedScope {}
        val scopeContext2 = BoundedScope {}

        val updatedStack = stack.push(scopeContext1).push(scopeContext2)

        assertNull(stack.top(BoundedScope))
        assertSame(scopeContext2, updatedStack.top(BoundedScope))
    }
}