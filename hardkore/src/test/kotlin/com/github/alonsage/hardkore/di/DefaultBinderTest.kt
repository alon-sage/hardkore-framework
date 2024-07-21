package com.github.alonsage.hardkore.di

import com.google.auto.service.AutoService
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.*
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DefaultBinderTest {
    @Test
    fun `install works`() {
        withFreshBindingsAndModules { bindings, modules ->
            val beanRef = beanRef<UUID>()
            val bean = UUID.randomUUID()
            val module = DiModule { bindInstance(bean) }
            val binder = DefaultBinder(bindings, modules)
            binder.install(module)

            assertEquals(1, bindings.size)
            assertNotNull(bindings[beanRef]) {
                assertIs<InstanceBinding<*>>(it)
                assertEquals(beanRef, it.beanRef)
                assertEquals(bean, it.instance)
                assertSame(bean, it.instance)
            }
            assertEquals(setOf(module::class), modules)
        }
    }

    @Test
    fun `install ignores module duplication`() {
        withFreshBindingsAndModules { bindings, modules ->
            val beanRef = beanRef<UUID>()
            val bean = UUID.randomUUID()
            val module = DiModule { bindInstance(bean) }
            val binder = DefaultBinder(bindings, modules)
            binder.install(module)
            binder.install(module)

            assertEquals(1, bindings.size)
            assertNotNull(bindings[beanRef]) {
                assertIs<InstanceBinding<*>>(it)
                assertEquals(beanRef, it.beanRef)
                assertEquals(bean, it.instance)
                assertSame(bean, it.instance)
            }
            assertEquals(setOf(module::class), modules)
        }
    }

    @Test
    fun `checkRequirements completes if no requirements`() {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            binder.checkRequirements()
        }
    }

    @Test
    fun `checkRequirements completes if requirement provided`() {
        withFreshBindingsAndModules { bindings, modules ->
            val beanRef = beanRef<UUID>()
            val bean = UUID.randomUUID()
            val binder = DefaultBinder(bindings, modules)
            binder.bindInstance(beanRef, bean)
            binder.requireBean(beanRef)

            binder.checkRequirements()
        }
    }

    @Test
    fun `checkRequirements fails if no requirement provided`() {
        withFreshBindingsAndModules { bindings, modules ->
            val beanRef = beanRef<UUID>()
            val binder = DefaultBinder(bindings, modules)
            binder.requireBean(beanRef)

            val exception = assertFailsWith<IllegalStateException> { binder.checkRequirements() }
            assertEquals(assertNotNull(exception.message), "Missing required bean: $beanRef")
        }
    }

    @Test
    fun `inline requireBean works`() {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            binder.bindInstance(UUID.randomUUID())
            binder.requireBean<UUID>()
            binder.checkRequirements()
        }
    }

    @Test
    fun `autoInstall works`() {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            binder.autoInstall()

            assertTrue(TestAutoDiModule::class in modules)
            assertTrue(beanRef<String>("bar") in bindings)

            assertFalse(ProfileTestAutoDiModule::class in modules)
            assertFalse(beanRef<String>("baz") in bindings)
        }
    }

    @Test
    fun `autoInstall with profile works`() {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            binder.autoInstall(TestDiProfile::class)

            assertTrue(TestAutoDiModule::class in modules)
            assertTrue(beanRef<String>("bar") in bindings)

            assertTrue(ProfileTestAutoDiModule::class in modules)
            assertTrue(beanRef<String>("baz") in bindings)
        }
    }

    @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2}")
    @ArgumentsSource(BeanTypeQualifierArgumentsProvider::class)
    fun `bindInstance works`(bean: Any?, kType: KType, qualifier: Any?) {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            val beanRef = BeanRef<Any?>(kType, qualifier)
            binder.bindInstance(beanRef, bean)

            assertEquals(1, bindings.size)
            assertNotNull(bindings[beanRef]) {
                assertIs<InstanceBinding<*>>(it)
                assertEquals(beanRef, it.beanRef)
                assertEquals(bean, it.instance)
                assertSame(bean, it.instance)
            }
            assertTrue(modules.isEmpty())
        }
    }

    @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2}")
    @ArgumentsSource(BeanTypeQualifierArgumentsProvider::class)
    fun `bindInstance duplicate binding fails`(bean: Any?, kType: KType, qualifier: Any?) {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            val beanRef = BeanRef<Any?>(kType, qualifier)
            binder.bindInstance(beanRef, bean)

            val exception = assertFailsWith<IllegalArgumentException> {
                binder.bindInstance(beanRef, bean)
            }
            assertEquals(assertNotNull(exception.message), "Bean is already bound: $beanRef")
        }
    }

    @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
    @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
    fun `bindFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            val beanRef = BeanRef<Any?>(kType, qualifier)
            val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
            binder.bindFactory(beanRef, scope, beanFactory)

            assertEquals(1, bindings.size)
            assertNotNull(bindings[beanRef]) {
                assertIs<FactoryBinding<*>>(it)
                assertEquals(beanRef, it.beanRef)
                assertEquals(scope, it.scope)
                assertEquals(beanFactory, it.factory)
                assertSame(beanFactory, it.factory)
            }
            assertTrue(modules.isEmpty())
        }
    }

    @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
    @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
    fun `bindFactory duplicate binding fails`(kType: KType, qualifier: Any?, scope: Scope) {
        withFreshBindingsAndModules { bindings, modules ->
            val binder = DefaultBinder(bindings, modules)
            val beanRef = BeanRef<Any?>(kType, qualifier)
            val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
            binder.bindFactory(beanRef, scope, beanFactory)

            val exception = assertFailsWith<IllegalArgumentException> {
                binder.bindFactory(beanRef, scope, beanFactory)
            }
            assertEquals(assertNotNull(exception.message), "Bean is already bound: $beanRef")
        }
    }

    @Nested
    inner class BindSetTest {
        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound with other binding`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                binder.bindInstance(beanRef, setOf(bean))

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindSet(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Bean is already bound: $beanRef"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound to other scope`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                binder.bindSet(beanRef, CustomScope) { }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindSet(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Set bean is already bound into another scope: $beanRef in $CustomScope scope"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance works`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                binder.bindSet(beanRef, scope) { bindInstance(bean) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<SetBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertEquals(setOf(bean), it.instances)
                    assertSame(bean, it.instances.single())
                    assertTrue(it.factories.isEmpty())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance fails on duplicate bindings`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                binder.bindSet(beanRef, scope) { bindInstance(bean) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindSet(beanRef, scope) { bindInstance(bean) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Set bean contains duplicating instances: $beanRef already has $bean"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                binder.bindSet(beanRef, scope) { bindFactory(beanFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<SetBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertEquals(setOf(beanFactory), it.factories)
                    assertSame(beanFactory, it.factories.single())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory fails on duplicate bindings`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                binder.bindSet(beanRef, scope) { bindFactory(beanFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindSet(beanRef, scope) { bindFactory(beanFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Set bean contains duplicating factories: $beanRef already has $beanFactory"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                val beansFactory: BeanFactory<Set<Any?>> = { throw NotImplementedError() }
                binder.bindSet(beanRef, scope) { bindBatchFactory(beansFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<SetBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertTrue(it.factories.isEmpty())
                    assertEquals(setOf(beansFactory), it.batchFactories)
                    assertSame(beansFactory, it.batchFactories.single())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory fails on duplicate bindings`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Set<Any?>>(setType(kType), qualifier)
                val beansFactory: BeanFactory<Set<Any?>> = { throw NotImplementedError() }
                binder.bindSet(beanRef, scope) { bindBatchFactory(beansFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindSet(beanRef, scope) { bindBatchFactory(beansFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Set bean contains duplicating batch factories: $beanRef already has $beansFactory"
                )
            }
        }
    }

    @Nested
    inner class BindListTest {
        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound with other binding`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                binder.bindInstance(beanRef, listOf(bean))

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindList(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Bean is already bound: $beanRef"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound to other scope`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                binder.bindList(beanRef, CustomScope) { }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindList(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "List bean is already bound into another scope: $beanRef in $CustomScope scope"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance works`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beanPrecedence = Random.nextInt()
                binder.bindList(beanRef, scope) { bindInstance(bean, beanPrecedence) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<ListBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertEquals(mapOf(bean to beanPrecedence), it.instances)
                    assertSame(bean, it.instances.keys.single())
                    assertTrue(it.factories.isEmpty())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance defaults works`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                binder.bindList(beanRef, scope) { bindInstance(bean) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<ListBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertEquals(mapOf(bean to DEFAULT_PRECEDENCE), it.instances)
                    assertSame(bean, it.instances.keys.single())
                    assertTrue(it.factories.isEmpty())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance fails on duplicate bindings`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beanPrecedence = Random.nextInt()
                binder.bindList(beanRef, scope) { bindInstance(bean, beanPrecedence) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindList(beanRef, scope) { bindInstance(bean, beanPrecedence) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "List bean contains duplicating instances: $beanRef already has $bean"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beanPrecedence = Random.nextInt()
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                binder.bindList(beanRef, scope) { bindFactory(beanPrecedence, beanFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<ListBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertEquals(mapOf(beanFactory to beanPrecedence), it.factories)
                    assertSame(beanFactory, it.factories.keys.single())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory defaults works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                binder.bindList(beanRef, scope) { bindFactory(factory = beanFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<ListBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertEquals(mapOf(beanFactory to DEFAULT_PRECEDENCE), it.factories)
                    assertSame(beanFactory, it.factories.keys.single())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory fails on duplicate bindings`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beanPrecedence = Random.nextInt()
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                binder.bindList(beanRef, scope) { bindFactory(beanPrecedence, beanFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindList(beanRef, scope) { bindFactory(beanPrecedence, beanFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "List bean contains duplicating factories: $beanRef already has $beanFactory"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beansFactory: BeanFactory<Map<Any?, Int>> = { throw NotImplementedError() }
                binder.bindList(beanRef, scope) { bindBatchFactory(beansFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<ListBinding<*>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertTrue(it.factories.isEmpty())
                    assertEquals(setOf(beansFactory), it.batchFactories)
                    assertSame(beansFactory, it.batchFactories.single())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory fails on duplicate bindings`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<List<Any?>>(listType(kType), qualifier)
                val beansFactory: BeanFactory<Map<Any?, Int>> = { throw NotImplementedError() }
                binder.bindList(beanRef, scope) { bindBatchFactory(beansFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindList(beanRef, scope) { bindBatchFactory(beansFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "List bean contains duplicating batch factories: $beanRef already has $beansFactory"
                )
            }
        }
    }

    @Nested
    inner class BindMapTest {
        @Test
        fun `inline bindMap works`() {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = beanRef<Map<Int, UUID>>()
                val bean = UUID.randomUUID()
                val beanKey = Random.nextInt()

                binder.bindMap { bindInstance(beanKey, bean) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<MapBinding<*, *>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(mapOf(beanKey to bean), it.instances)
                }
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound with other binding`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                binder.bindInstance(beanRef, mapOf(UUID.randomUUID() to bean))

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Bean is already bound: $beanRef"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `fails if already bound to other scope`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                binder.bindMap(beanRef, CustomScope) { }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean is already bound into another scope: $beanRef in $CustomScope scope"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance works`(bean: Any?, kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindInstance(beanKey, bean) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<MapBinding<*, *>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertEquals(mapOf(beanKey to bean), it.instances)
                    assertSame(bean, it.instances.values.single())
                    assertTrue(it.factories.isEmpty())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance fails on duplicate bindings from instances`(
            bean: Any?,
            kType: KType,
            qualifier: Any?,
            scope: Scope
        ) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindInstance(beanKey, bean) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { bindInstance(beanKey, bean) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean contains duplicating keys: $beanRef already has $beanKey"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindInstance fails on duplicate bindings from factories`(
            bean: Any?,
            kType: KType,
            qualifier: Any?,
            scope: Scope
        ) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindFactory(beanKey, beanFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { bindInstance(beanKey, bean) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean contains duplicating keys: $beanRef already has $beanKey"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindFactory(beanKey, beanFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<MapBinding<*, *>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertEquals(mapOf(beanKey to beanFactory), it.factories)
                    assertSame(beanFactory, it.factories.values.single())
                    assertTrue(it.batchFactories.isEmpty())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory fails on duplicate bindings from factories`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindFactory(beanKey, beanFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { bindFactory(beanKey, beanFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean contains duplicating keys: $beanRef already has $beanKey"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(BeanTypeQualifierScopeArgumentsProvider::class)
        fun `bindFactory fails on duplicate bindings from instances`(
            bean: Any?,
            kType: KType,
            qualifier: Any?,
            scope: Scope
        ) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beanFactory: BeanFactory<Any?> = { throw NotImplementedError() }
                val beanKey = UUID.randomUUID()
                binder.bindMap(beanRef, scope) { bindInstance(beanKey, bean) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { bindFactory(beanKey, beanFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean contains duplicating keys: $beanRef already has $beanKey"
                )
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory works`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beansFactory: BeanFactory<Map<UUID, Any?>> = { throw NotImplementedError() }
                binder.bindMap(beanRef, scope) { bindBatchFactory(beansFactory) }

                assertEquals(1, bindings.size)
                assertNotNull(bindings[beanRef]) {
                    assertIs<MapBinding<*, *>>(it)
                    assertEquals(beanRef, it.beanRef)
                    assertEquals(scope, it.scope)
                    assertTrue(it.instances.isEmpty())
                    assertTrue(it.factories.isEmpty())
                    assertEquals(setOf(beansFactory), it.batchFactories)
                    assertSame(beansFactory, it.batchFactories.single())
                }
                assertTrue(modules.isEmpty())
            }
        }

        @ParameterizedTest(name = "{index}: b={0} | t={1} | q={2} | s={3}")
        @ArgumentsSource(TypeQualifierScopeArgumentsProvider::class)
        fun `bindBatchFactory fails on duplicate bindings`(kType: KType, qualifier: Any?, scope: Scope) {
            withFreshBindingsAndModules { bindings, modules ->
                val binder = DefaultBinder(bindings, modules)
                val beanRef = BeanRef<Map<UUID, Any?>>(mapType(typeOf<UUID>(), kType), qualifier)
                val beansFactory: BeanFactory<Map<UUID, Any?>> = { throw NotImplementedError() }
                binder.bindMap(beanRef, scope) { bindBatchFactory(beansFactory) }

                val exception = assertFailsWith<IllegalArgumentException> {
                    binder.bindMap(beanRef, scope) { bindBatchFactory(beansFactory) }
                }
                assertEquals(
                    assertNotNull(exception.message),
                    "Map bean contains duplicating batch factories: $beanRef already has $beansFactory"
                )
            }
        }
    }

    companion object {
        val beanAndTypeArgs = listOf(
            beanAndType<String?>(null),
            beanAndType("foobar"),
            beanAndType<UUID>(UUID.randomUUID())
        )

        val qualifierArgs = listOf(null, 1, "string", TestObjectQualifier, TestDataClassQualifier())

        val scopeArgs = listOf(Prototype, Singleton, BoundedScope)

        private inline fun <reified T> beanAndType(value: T): Pair<Any?, KType> =
            value to typeOf<T>()

        private fun setType(kType: KType): KType =
            Set::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, kType)))

        private fun listType(kType: KType): KType =
            List::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, kType)))

        private fun mapType(keyKType: KType, valueKType: KType): KType =
            Map::class.createType(
                listOf(
                    KTypeProjection(KVariance.INVARIANT, keyKType),
                    KTypeProjection(KVariance.INVARIANT, valueKType)
                )
            )

        private inline fun <T> withFreshBindingsAndModules(
            block: (
                bindings: MutableMap<BeanRef<*>, Binding<*>>,
                modules: MutableSet<KClass<out DiModule>>
            ) -> T
        ): T =
            block(mutableMapOf(), mutableSetOf())
    }

    data object TestObjectQualifier

    data class TestDataClassQualifier(
        val name: String = "test",
        val id: Int = 346334
    )

    class BeanTypeQualifierArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            beanAndTypeArgs.flatMap { (bean, kType) ->
                qualifierArgs.map { qualifier ->
                    Arguments.of(bean, kType, qualifier)
                }
            }.stream()
    }

    class BeanTypeQualifierScopeArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            beanAndTypeArgs.flatMap { (bean, kType) ->
                qualifierArgs.flatMap { qualifier ->
                    scopeArgs.map { scope ->
                        Arguments.of(bean, kType, qualifier, scope)
                    }
                }
            }.stream()
    }

    class TypeQualifierScopeArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            beanAndTypeArgs.flatMap { (_, kType) ->
                qualifierArgs.flatMap { qualifier ->
                    scopeArgs.map { scope ->
                        Arguments.of(kType, qualifier, scope)
                    }
                }
            }.stream()
    }

    @AutoService(DiModule::class)
    class TestAutoDiModule : DiModule {
        override fun Binder.install() {
            bindInstance("foo", "bar")
        }
    }

    sealed interface TestDiProfile : DiProfile

    @AutoService(DiModule::class)
    @DiProfiles(TestDiProfile::class)
    class ProfileTestAutoDiModule : DiModule {
        override fun Binder.install() {
            bindInstance("bar", "baz")
        }
    }

    data object CustomScope : Scope
}