package com.github.alonsage.hardkore.fqdn

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FqdnRootTest {
    @Test
    fun `same identifiers equal`() {
        val value = UUID.randomUUID().toString()
        val fqdn1 = RootFqdn(value)
        val fqdn2 = RootFqdn(value)
        assertEquals(fqdn1, fqdn2)
    }

    @Test
    fun `same identifiers have same hashCode`() {
        val value = UUID.randomUUID().toString()
        val fqdn1 = RootFqdn(value)
        val fqdn2 = RootFqdn(value)
        assertEquals(fqdn1.hashCode(), fqdn2.hashCode())
    }

    @Test
    fun `different identifiers do not equal`() {
        val fqdn1 = RootFqdn(UUID.randomUUID().toString())
        val fqdn2 = RootFqdn(UUID.randomUUID().toString())
        assertNotEquals(fqdn1, fqdn2)
    }

    @Test
    fun `different identifiers have different hash`() {
        val fqdn1 = RootFqdn(UUID.randomUUID().toString())
        val fqdn2 = RootFqdn(UUID.randomUUID().toString())
        assertNotEquals(fqdn1.hashCode(), fqdn2.hashCode())
    }

    @Test
    fun `not equal to null`() {
        val fqdn = RootFqdn(UUID.randomUUID().toString())
        assertFalse(fqdn.equals(null))
    }

    @Test
    fun `equal to it self`() {
        val fqdn = RootFqdn(UUID.randomUUID().toString())
        @Suppress("ReplaceCallWithBinaryOperator")
        assertTrue(fqdn.equals(fqdn))
    }

    @Test
    fun `not equal to other instance`() {
        val fqdn = RootFqdn(UUID.randomUUID().toString())
        assertFalse(fqdn.equals(fqdn.toString()))
    }

    @Test
    fun `contains it self`() {
        val fqdn = RootFqdn(UUID.randomUUID().toString())
        assertTrue(fqdn in fqdn)
    }

    @Test
    fun `doesn't contains different`() {
        val fqdn1 = RootFqdn(UUID.randomUUID().toString())
        val fqdn2 = RootFqdn(UUID.randomUUID().toString())
        assertFalse(fqdn1 in fqdn2)
    }

    @Test
    fun `name generation works correctly`() {
        val fqdn = RootFqdn(UUID.randomUUID().toString())
        assertContains(fqdn.toString(), "/root:")
    }

    @Test
    fun `name annotation works correctly`() {
        @FqdnNodeName("foobar")
        class BarBazFqdn(override val id: String) : Fqdn.Root<String>()

        val fqdn = BarBazFqdn(UUID.randomUUID().toString())
        assertContains(fqdn.toString(), "/foobar:")
    }

    @Test
    fun `decoded correctly`() {
        val rootId = UUID.randomUUID().toString()
        val encoded = "fqdn://root:$rootId"
        val fqdn = Fqdn.fromString<RootFqdn>(encoded)
        assertEquals(rootId, fqdn.id)
    }
}