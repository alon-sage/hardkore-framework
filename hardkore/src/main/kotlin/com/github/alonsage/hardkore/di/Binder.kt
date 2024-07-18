package com.github.alonsage.hardkore.di

import java.util.*
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@DslMarker
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class BinderDsl

@BinderDsl
interface Binder {
    fun <T> bindInstance(ref: BeanRef<T>, instance: T)
    fun <T> bindFactory(ref: BeanRef<T>, scope: Scope, factory: BeanFactory<T>)
    fun <T> bindSet(ref: BeanRef<Set<T>>, scope: Scope, block: SetBinder<T>.() -> Unit)
    fun <T> bindList(ref: BeanRef<List<T>>, scope: Scope, block: ListBinder<T>.() -> Unit)
    fun <K, T> bindMap(ref: BeanRef<Map<K, T>>, scope: Scope, block: MapBinder<K, T>.() -> Unit)
    fun requireBean(ref: BeanRef<*>)
    fun install(module: DiModule)
    fun autoInstall(vararg requiredProfiles: KClass<out DiProfile>)
}

typealias BeanFactory<T> = (@BinderDsl InjectionContext).() -> T

@BinderDsl
interface SetBinder<T> {
    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindInstance(instance: T)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindFactory(factory: BeanFactory<T>)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindBatchFactory(factory: BeanFactory<Set<T>>)
}

const val MAX_PRECEDENCE = Int.MIN_VALUE

const val MIN_PRECEDENCE = Int.MAX_VALUE

const val DEFAULT_PRECEDENCE = 0

@BinderDsl
interface ListBinder<T> {
    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindInstance(precedence: Int = DEFAULT_PRECEDENCE, instance: T)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindFactory(precedence: Int = DEFAULT_PRECEDENCE, factory: BeanFactory<T>)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindBatchFactory(factory: BeanFactory<Map<T, Int>>)
}

@BinderDsl
interface MapBinder<K, T> {
    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindInstance(key: K, instance: T)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindFactory(key: K, factory: BeanFactory<T>)

    @BuilderInference
    @OptIn(ExperimentalTypeInference::class)
    fun bindBatchFactory(factory: BeanFactory<Map<K, T>>)
}

fun interface DiModule {
    fun Binder.install()
}

inline fun <reified T> Binder.bindInstance(instance: T, qualifier: Any? = null) =
    bindInstance(beanRef(qualifier), instance)

inline fun <reified T> Binder.bindFactory(
    qualifier: Any? = null,
    scope: Scope = Singleton,
    noinline factory: BeanFactory<T>
) =
    bindFactory(beanRef(qualifier), scope, factory)

inline fun <reified T> Binder.bindSet(
    qualifier: Any? = null,
    scope: Scope = Singleton,
    noinline block: SetBinder<T>.() -> Unit
) =
    bindSet(beanRef(qualifier), scope, block)

inline fun <reified T> Binder.bindList(
    qualifier: Any? = null,
    scope: Scope = Singleton,
    noinline block: ListBinder<T>.() -> Unit
) =
    bindList(beanRef(qualifier), scope, block)

inline fun <reified K, reified T> Binder.bindMap(
    qualifier: Any? = null,
    scope: Scope = Singleton,
    noinline block: MapBinder<K, T>.() -> Unit
) =
    bindMap(beanRef(qualifier), scope, block)

inline fun <reified T> Binder.requireBean(qualifier: Any? = null) =
    requireBean(beanRef<T>(qualifier))

interface DiProfile

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DiProfiles(vararg val profiles: KClass<out DiProfile>)

internal class DefaultBinder(
    private val bindings: MutableMap<BeanRef<*>, Binding<*>>,
    private val modules: MutableSet<KClass<out DiModule>>
) : Binder {
    private val requirements = mutableListOf<BeanRef<*>>()

    override fun <T> bindInstance(ref: BeanRef<T>, instance: T) {
        require(ref !in bindings) { "Bean is already bound: $ref" }
        bindings[ref] = InstanceBinding(ref, instance)
    }

    override fun <T> bindFactory(ref: BeanRef<T>, scope: Scope, factory: BeanFactory<T>) {
        require(ref !in bindings) { "Bean is already bound: $ref" }
        bindings[ref] = FactoryBinding(ref, scope, factory)
    }

    override fun <T> bindSet(ref: BeanRef<Set<T>>, scope: Scope, block: SetBinder<T>.() -> Unit) {
        if (ref in bindings) {
            val found = bindings[ref]
            require(found is SetBinding<*>) { "Bean is already bound: $ref" }
            require(found.scope == scope) { "Set bean is already bound into another scope: $ref in ${found.scope} scope" }

            @Suppress("UNCHECKED_CAST")
            found as SetBinding<T>

            bindings[ref] = DefaultSetBinder(found).apply(block).binding()
        } else {
            bindings[ref] = DefaultSetBinder(ref, scope).apply(block).binding()
        }
    }

    override fun <T> bindList(ref: BeanRef<List<T>>, scope: Scope, block: ListBinder<T>.() -> Unit) {
        if (ref in bindings) {
            val found = bindings[ref]
            require(found is ListBinding<*>) { "Bean is already bound: $ref" }
            require(found.scope == scope) { "Set bean is already bound into another scope: $ref in ${found.scope} scope" }

            @Suppress("UNCHECKED_CAST")
            found as ListBinding<T>

            bindings[ref] = DefaultListBinder(found).apply(block).binding()
        } else {
            bindings[ref] = DefaultListBinder(ref, scope).apply(block).binding()
        }
    }

    override fun <K, T> bindMap(ref: BeanRef<Map<K, T>>, scope: Scope, block: MapBinder<K, T>.() -> Unit) {
        if (ref in bindings) {
            val found = bindings[ref]
            require(found is MapBinding<*, *>) { "Bean is already bound: $ref" }
            require(found.scope == scope) { "Map bean is already bound into another scope: $ref in ${found.scope} scope" }

            @Suppress("UNCHECKED_CAST")
            found as MapBinding<K, T>

            bindings[ref] = DefaultMapBinder(found).apply(block).binding()
        } else {
            bindings[ref] = DefaultMapBinder(ref, scope).apply(block).binding()
        }
    }

    override fun requireBean(ref: BeanRef<*>) {
        requirements.add(ref)
    }

    override fun install(module: DiModule) {
        if (modules.add(module::class)) {
            with(module) { install() }
        }
    }

    override fun autoInstall(vararg requiredProfiles: KClass<out DiProfile>) {
        ServiceLoader.load(DiModule::class.java)
            .filter { module ->
                val annotation = module::class.findAnnotation<DiProfiles>()
                annotation == null || requiredProfiles.intersect(annotation.profiles.toSet()).isNotEmpty()
            }
            .forEach { install(it) }
    }

    fun checkRequirements() {
        requirements.forEach { ref ->
            require(ref in bindings) { "Missing required bean: $ref" }
        }
    }
}

internal class DefaultSetBinder<T>
private constructor(
    private val beanRef: BeanRef<Set<T>>,
    private val scope: Scope,
    private val instances: MutableSet<T>,
    private val factories: MutableSet<BeanFactory<T>>,
    private val batchFactories: MutableSet<BeanFactory<Set<T>>>,
) : SetBinder<T> {
    constructor(beanRef: BeanRef<Set<T>>, scope: Scope) : this(
        beanRef = beanRef,
        scope = scope,
        instances = mutableSetOf(),
        factories = mutableSetOf(),
        batchFactories = mutableSetOf()
    )

    constructor(binding: SetBinding<T>) : this(
        beanRef = binding.beanRef,
        scope = binding.scope,
        instances = binding.instances.toMutableSet(),
        factories = binding.factories.toMutableSet(),
        batchFactories = binding.batchFactories.toMutableSet()
    )

    override fun bindInstance(instance: T) {
        require(instances.add(instance)) {
            "Set bean contains duplicating instances: $beanRef already has $instance"
        }
    }

    override fun bindFactory(factory: BeanFactory<T>) {
        require(factories.add(factory)) {
            "Set bean contains duplicating factories: $beanRef already has $factory"
        }
    }

    override fun bindBatchFactory(factory: BeanFactory<Set<T>>) {
        require(batchFactories.add(factory)) {
            "Set bean contains duplicating batch factories: $beanRef already has $factory"
        }
    }

    fun binding(): SetBinding<T> =
        SetBinding(beanRef, scope, instances.toSet(), factories.toSet(), batchFactories.toSet())
}

internal class DefaultListBinder<T>
private constructor(
    private val beanRef: BeanRef<List<T>>,
    private val scope: Scope,
    private val instances: MutableMap<T, Int>,
    private val factories: MutableMap<BeanFactory<T>, Int>,
    private val batchFactories: MutableSet<BeanFactory<Map<T, Int>>>,
) : ListBinder<T> {
    constructor(beanRef: BeanRef<List<T>>, scope: Scope) : this(
        beanRef = beanRef,
        scope = scope,
        instances = mutableMapOf(),
        factories = mutableMapOf(),
        batchFactories = mutableSetOf()
    )

    constructor(binding: ListBinding<T>) : this(
        beanRef = binding.beanRef,
        scope = binding.scope,
        instances = binding.instances.toMutableMap(),
        factories = binding.factories.toMutableMap(),
        batchFactories = binding.batchFactories.toMutableSet()
    )

    override fun bindInstance(precedence: Int, instance: T) {
        require(instances.put(instance, precedence) == null) {
            "Set bean contains duplicating instances: $beanRef already has $instance"
        }
    }

    override fun bindFactory(precedence: Int, factory: BeanFactory<T>) {
        require(factories.put(factory, precedence) == null) {
            "Set bean contains duplicating factories: $beanRef already has $factory"
        }
    }

    override fun bindBatchFactory(factory: BeanFactory<Map<T, Int>>) {
        require(batchFactories.add(factory)) {
            "Set bean contains duplicating batch factories: $beanRef already has $factory"
        }
    }

    fun binding(): ListBinding<T> =
        ListBinding(beanRef, scope, instances.toMap(), factories.toMap(), batchFactories.toSet())
}

internal class DefaultMapBinder<K, T>
private constructor(
    private val beanRef: BeanRef<Map<K, T>>,
    private val scope: Scope,
    private val instances: MutableMap<K, T>,
    private val factories: MutableMap<K, BeanFactory<T>>,
    private val batchFactories: MutableSet<BeanFactory<Map<K, T>>>
) : MapBinder<K, T> {
    constructor(beanRef: BeanRef<Map<K, T>>, scope: Scope) : this(
        beanRef = beanRef,
        scope = scope,
        instances = mutableMapOf(),
        factories = mutableMapOf(),
        batchFactories = mutableSetOf()
    )

    constructor(binding: MapBinding<K, T>) : this(
        beanRef = binding.beanRef,
        scope = binding.scope,
        instances = binding.instances.toMutableMap(),
        factories = binding.factories.toMutableMap(),
        batchFactories = binding.batchFactories.toMutableSet()
    )

    override fun bindInstance(key: K, instance: T) {
        require(key !in instances && key !in factories) {
            "Map bean contains duplicating keys: $beanRef already has $key"
        }
        instances[key] = instance
    }

    override fun bindFactory(key: K, factory: BeanFactory<T>) {
        require(key !in instances && key !in factories) {
            "Map bean contains duplicating keys: $beanRef already has $key"
        }
        factories[key] = factory
    }

    override fun bindBatchFactory(factory: BeanFactory<Map<K, T>>) {
        require(batchFactories.add(factory)) {
            "Map bean contains duplicating batch factories: $beanRef already has $factory"
        }
    }

    fun binding(): MapBinding<K, T> =
        MapBinding(beanRef, scope, instances.toMap(), factories.toMap(), batchFactories.toSet())
}