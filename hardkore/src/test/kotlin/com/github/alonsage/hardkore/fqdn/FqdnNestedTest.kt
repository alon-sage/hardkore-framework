package com.github.alonsage.hardkore.fqdn

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FqdnNestedTest {
    @Test
    fun `same identifiers equal`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val value = UUID.randomUUID()
        val fqdn1 = NestedFqdn(value, rootFqdn)
        val fqdn2 = NestedFqdn(value, rootFqdn)
        assertEquals(fqdn1, fqdn2)
    }

    @Test
    fun `same identifiers have same hashCode`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val value = UUID.randomUUID()
        val fqdn1 = NestedFqdn(value, rootFqdn)
        val fqdn2 = NestedFqdn(value, rootFqdn)
        assertEquals(fqdn1.hashCode(), fqdn2.hashCode())
    }

    @Test
    fun `different identifiers do not equal`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn1 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        val fqdn2 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertNotEquals(fqdn1, fqdn2)
    }

    @Test
    fun `different identifiers have different hash`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn1 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        val fqdn2 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertNotEquals(fqdn1.hashCode(), fqdn2.hashCode())
    }

    @Test
    fun `different parents do not equal`() {
        val value = UUID.randomUUID()
        val fqdn1 = NestedFqdn(value, RootFqdn(UUID.randomUUID().toString()))
        val fqdn2 = NestedFqdn(value, RootFqdn(UUID.randomUUID().toString()))
        assertNotEquals(fqdn1, fqdn2)
    }

    @Test
    fun `different parents have different hash`() {
        val value = UUID.randomUUID()
        val fqdn1 = NestedFqdn(value, RootFqdn(UUID.randomUUID().toString()))
        val fqdn2 = NestedFqdn(value, RootFqdn(UUID.randomUUID().toString()))
        assertNotEquals(fqdn1.hashCode(), fqdn2.hashCode())
    }

    @Test
    fun `not equal to null`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertFalse(fqdn.equals(null))
    }

    @Test
    fun `equal to it self`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = NestedFqdn(UUID.randomUUID(), rootFqdn)
        @Suppress("ReplaceCallWithBinaryOperator")
        assertTrue(fqdn.equals(fqdn))
    }

    @Test
    fun `not equal to other instance`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertFalse(fqdn.equals(fqdn.toString()))
    }

    @Test
    fun `contains it self`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertTrue(fqdn in fqdn)
    }

    @Test
    fun `doesn't contains different`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn1 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        val fqdn2 = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertFalse(fqdn1 in fqdn2)
    }

    @Test
    fun `name generation works correctly`() {
        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = NestedFqdn(UUID.randomUUID(), rootFqdn)
        assertContains(fqdn.toString(), "/nested:")
    }

    @Test
    fun `name annotation works correctly`() {
        @FqdnNodeName("foobar")
        class BarBazFqdn(
            override val id: UUID,
            override val owner: RootFqdn
        ) : Fqdn.Nested<UUID, RootFqdn>()

        val rootFqdn = RootFqdn(UUID.randomUUID().toString())
        val fqdn = BarBazFqdn(UUID.randomUUID(), rootFqdn)
        assertContains(fqdn.toString(), "/foobar:")
    }

    @Test
    fun `decoded correctly`() {
        val rootId = UUID.randomUUID().toString()
        val nestedId = UUID.randomUUID()
        val encoded = "fqdn://root:$rootId/nested:$nestedId"
        val fqdn = Fqdn.fromString<NestedFqdn>(encoded)
        assertEquals(nestedId, fqdn.id)
        assertEquals(rootId, fqdn.owner.id)
    }
}