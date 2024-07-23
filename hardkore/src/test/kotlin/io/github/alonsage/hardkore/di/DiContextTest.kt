package io.github.alonsage.hardkore.di

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class DiContextTest {
    @Test
    fun `context was created and it resolves bean`() {
        val bean = UUID.randomUUID()
        val context = DiContext { bindInstance(bean) }
        assertEquals(bean, context.bean<UUID>())
    }

    @Test
    fun `context with parent was created and it resolves beans from both`() {
        val parentBean = Random.nextInt()
        val parentContext = DiContext { bindInstance(parentBean) }

        val bean = UUID.randomUUID()
        val context = DiContext(parentContext) { bindInstance(bean) }

        assertEquals(parentBean, context.bean<Int>())
        assertEquals(bean, context.bean<UUID>())
    }

    @Test
    fun `resolves optional bean`() {
        val context = DiContext { bindInstance<UUID?>(null) }
        assertNull(context.bean<UUID?>())
    }

    @Test
    fun `fails if no bean defined`() {
        val context = DiContext { }
        val exception = assertFailsWith<NoSuchElementException> { context.bean<UUID>() }
        assertEquals(assertNotNull(exception.message), "Bean is not found: ${beanRef<UUID>()}")
    }

    @Test
    fun `fails if no bean defined in both parent and child`() {
        val parentContext = DiContext {}
        val context = DiContext(parentContext) { }
        val exception = assertFailsWith<NoSuchElementException> { context.bean<UUID>() }
        assertEquals(assertNotNull(exception.message), "Bean is not found: ${beanRef<UUID>()}")
    }

    @Test
    fun `bean resolutions fails on direct self referencing`() {
        val context = DiContext { bindFactory { SelfReferencingObject(bean()) } }
        val exception = assertFailsWith<RuntimeException> { context.bean<SelfReferencingObject>() }
        assertIs<IllegalStateException>(exception.cause)
        assertContains(assertNotNull(exception.cause?.message), "Bean dependencies cycle:")
    }

    @Test
    fun `bean resolutions fails on indirect self referencing`() {
        val context = DiContext {
            bindFactory { SelfReferencingObject1(bean()) }
            bindFactory { SelfReferencingObject2(bean()) }
        }
        val exception = assertFailsWith<RuntimeException> { context.bean<SelfReferencingObject1>() }
        assertIs<RuntimeException>(exception.cause)
        assertIs<IllegalStateException>(exception.cause?.cause)
        assertContains(assertNotNull(exception.cause?.cause?.message), "Bean dependencies cycle:")
    }

    @Test
    fun `overriding works`() {
        val bean = UUID.randomUUID()
        val context = DiContext { bindInstance(bean) }

        val overriddenBean = UUID.randomUUID()
        val overriddenContext = context.overridden { bindInstance(overriddenBean) }

        assertEquals(bean, context.bean<UUID>())
        assertEquals(overriddenBean, overriddenContext.bean<UUID>())
    }

    @Test
    fun `prototype beans are different`() {
        val context = DiContext { bindFactory(scope = Prototype) { UUID.randomUUID() } }
        val bean1 = context.bean<UUID>()
        val bean2 = context.bean<UUID>()
        assertNotEquals(bean1, bean2)
    }

    @Test
    fun `singleton beans are the same`() {
        val context = DiContext { bindFactory(scope = Singleton) { UUID.randomUUID() } }
        val bean1 = context.bean<UUID>()
        val bean2 = context.bean<UUID>()
        assertEquals(bean1, bean2)
        assertSame(bean1, bean2)
    }

    @Test
    fun `scoped beans works`() {
        val context = DiContext {
            bindFactory<UUID>(scope = BoundedScope) { throw IllegalStateException("Bean is missing") }
        }

        val scopedBean = UUID.randomUUID()
        val scopedContext = context.enterScope(BoundedScope { add(scopedBean) })

        val exception = assertFailsWith<IllegalStateException> { context.bean<UUID>() }
        assertEquals(
            assertNotNull(exception.message),
            "Scoped bean can not be created out of scope: ${beanRef<UUID>()} in $BoundedScope scope"
        )
        assertEquals(scopedBean, scopedContext.bean<UUID>())
    }

    @Test
    fun `scoped beans don't cross scope boundaries`() {
        val context = DiContext {
            bindFactory<UUID>(scope = BoundedScope) { throw IllegalStateException("Bean is missing") }
        }

        val scopedBean1 = UUID.randomUUID()
        val scopedContext1 = context.enterScope(BoundedScope { add(scopedBean1) })

        val scopedBean2 = UUID.randomUUID()
        val scopedContext2 = context.enterScope(BoundedScope { add(scopedBean2) })

        assertNotEquals(scopedContext1.bean<UUID>(), scopedContext2.bean<UUID>())
    }

    @Test
    fun `empty set bean resolved`() {
        val context = DiContext { bindSet<UUID> { } }
        assertEquals(emptySet(), context.bean<Set<UUID>>())
    }

    @Test
    fun `set bean resolved`() {
        val bean1 = UUID.randomUUID()
        val bean2 = UUID.randomUUID()
        val bean3 = UUID.randomUUID()
        val context = DiContext {
            bindSet {
                bindInstance(bean1)
                bindFactory { bean2 }
                bindBatchFactory { setOf(bean3) }
            }
        }
        assertEquals(setOf(bean1, bean2, bean3), context.bean<Set<UUID>>())
    }

    @Test
    fun `set bean resolution fails on duplicating items`() {
        val bean = UUID.randomUUID()
        val context = DiContext {
            bindSet {
                bindInstance(bean)
                bindFactory { bean }
            }
        }
        val exception = assertFailsWith<RuntimeException> { context.bean<Set<UUID>>() }
        assertIs<IllegalStateException>(exception.cause)
        assertEquals(
            assertNotNull(exception.cause?.message),
            "Set bean contains duplicating elements: ${beanRef<Set<UUID>>()} already has $bean"
        )
    }

    @Test
    fun `empty list bean resolved`() {
        val context = DiContext { bindList<UUID> { } }
        assertEquals(emptyList(), context.bean<List<UUID>>())
    }

    @Test
    fun `list bean resolved`() {
        val bean1 = UUID.randomUUID()
        val bean2 = UUID.randomUUID()
        val bean3 = UUID.randomUUID()
        val context = DiContext {
            bindList {
                bindInstance(bean1, precedence = 2)
                bindFactory(1) { bean2 }
                bindBatchFactory { mapOf(bean3 to 0) }
            }
        }
        assertEquals(listOf(bean3, bean2, bean1), context.bean<List<UUID>>())
    }

    @Test
    fun `list bean resolution fails on duplicating items`() {
        val bean = UUID.randomUUID()
        val context = DiContext {
            bindList {
                bindInstance(bean)
                bindFactory { bean }
            }
        }
        val exception = assertFailsWith<RuntimeException> { context.bean<List<UUID>>() }
        assertIs<IllegalStateException>(exception.cause)
        assertEquals(
            assertNotNull(exception.cause?.message),
            "List bean contains duplicating elements: ${beanRef<List<UUID>>()} already has $bean"
        )
    }

    @Test
    fun `empty map bean resolved`() {
        val context = DiContext { bindMap<String, UUID> { } }
        assertEquals(emptyMap(), context.bean<Map<String, UUID>>())
    }

    @Test
    fun `map bean resolved`() {
        val bean1 = UUID.randomUUID()
        val bean2 = UUID.randomUUID()
        val bean3 = UUID.randomUUID()
        val context = DiContext {
            bindMap {
                bindInstance("foo", bean1)
                bindFactory("bar") { bean2 }
                bindBatchFactory { mapOf("baz" to bean3) }
            }
        }
        assertEquals(
            mapOf("foo" to bean1, "bar" to bean2, "baz" to bean3),
            context.bean<Map<String, UUID>>()
        )
    }

    @Test
    fun `map bean resolution fails on duplicating keys`() {
        val context = DiContext {
            bindMap {
                bindInstance("foo", UUID.randomUUID())
                bindBatchFactory { mapOf("foo" to UUID.randomUUID()) }
            }
        }
        val exception = assertFailsWith<RuntimeException> { context.bean<Map<String, UUID>>() }
        assertIs<IllegalStateException>(exception.cause)
        assertEquals(
            assertNotNull(exception.cause?.message),
            "Map bean contains duplicating keys: ${beanRef<Map<String, UUID>>()} already has foo"
        )
    }

    class SelfReferencingObject(val dependency: SelfReferencingObject)

    class SelfReferencingObject1(val dependency: SelfReferencingObject2)

    class SelfReferencingObject2(val dependency: SelfReferencingObject1)
}