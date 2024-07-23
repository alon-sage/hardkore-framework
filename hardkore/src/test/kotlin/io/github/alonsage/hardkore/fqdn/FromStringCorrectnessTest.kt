package io.github.alonsage.hardkore.fqdn

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FromStringCorrectnessTest {
    @Test
    fun `missing schema fails`() {
        val rootId = UUID.randomUUID().toString()
        val nestedId = UUID.randomUUID()
        val invalid = "root:$rootId/nested:$nestedId"
        val exception = assertFailsWith<IllegalArgumentException> {
            Fqdn.fromString<NestedFqdn>(invalid)
        }
        assertEquals("Fqdn is missing schema", exception.message)
    }

    @Test
    fun `missing identifier fails`() {
        val nestedId = UUID.randomUUID()
        val invalid = "fqdn://root/nested:$nestedId"
        val exception = assertFailsWith<IllegalArgumentException> {
            Fqdn.fromString<NestedFqdn>(invalid)
        }
        assertEquals("Fqdn contains invalid node: root", exception.message)
    }

    @Test
    fun `less nodes number fails`() {
        val rootId = UUID.randomUUID().toString()
        val invalid = "fqdn://root:$rootId"
        val exception = assertFailsWith<IllegalArgumentException> {
            Fqdn.fromString<NestedFqdn>(invalid)
        }
        assertEquals("Fqdn has wrong number of nodes: 1", exception.message)
    }

    @Test
    fun `greater nodes number fails`() {
        val rootId = UUID.randomUUID().toString()
        val nestedId = UUID.randomUUID()
        val invalid = "fqdn://root:$rootId/nested:$nestedId/test:100500"
        val exception = assertFailsWith<IllegalArgumentException> {
            Fqdn.fromString<NestedFqdn>(invalid)
        }
        assertEquals("Fqdn has wrong number of nodes: 3", exception.message)
    }

    @Test
    fun `incorrect node name fails`() {
        val rootId = UUID.randomUUID().toString()
        val nestedId = UUID.randomUUID()
        val invalid = "fqdn://root:$rootId/test:$nestedId"
        val exception = assertFailsWith<IllegalArgumentException> {
            Fqdn.fromString<NestedFqdn>(invalid)
        }
        assertEquals("Fqdn contains unexpected node: test", exception.message)
    }
}