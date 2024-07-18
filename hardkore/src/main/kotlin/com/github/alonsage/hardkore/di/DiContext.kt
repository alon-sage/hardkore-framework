package com.github.alonsage.hardkore.di

import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.reflect.KClass

interface DiContext : InjectionContext {
    fun enterScope(context: ScopeContext<*>): DiContext
    fun overridden(block: Binder.() -> Unit): DiContext
}

internal class DefaultDiContext
private constructor(
    private val bindings: Map<BeanRef<*>, Binding<*>>,
    private val modules: Set<KClass<out DiModule>>,
    private val parent: DiContext?,
    private val scopeContextStacks: ScopeContextStacks
) : DiContext {
    constructor(bindings: Map<BeanRef<*>, Binding<*>>, modules: Set<KClass<out DiModule>>, parent: DiContext?) : this(
        bindings = bindings,
        modules = modules,
        parent = parent,
        scopeContextStacks = ScopeContextStacks()
    )

    override val context: DiContext = this

    override fun <T> bean(ref: BeanRef<T>): T =
        Injector().bean(ref)

    override fun enterScope(context: ScopeContext<*>): DiContext =
        DefaultDiContext(bindings, modules, parent, scopeContextStacks.push(context))

    override fun overridden(block: Binder.() -> Unit): DiContext {
        val bindingOverrides = mutableMapOf<BeanRef<*>, Binding<*>>()
        val moduleOverrides = mutableSetOf<KClass<out DiModule>>()
        DefaultBinder(bindingOverrides, moduleOverrides).apply(block)
        return DefaultDiContext(bindings + bindingOverrides, modules + moduleOverrides, parent)
    }

    inner class Injector : InjectionContext {
        private val path = mutableSetOf<BeanRef<*>>()

        override val context: DiContext = this@DefaultDiContext

        override fun <T> bean(ref: BeanRef<T>): T =
            withDependencyCycleChecking(ref) {
                @Suppress("UNCHECKED_CAST")
                when (val binding = bindings[ref] as Binding<T>?) {
                    null -> parent?.bean(ref) ?: run {
                        if (ref.kType.isMarkedNullable) null as T
                        else throw NoSuchElementException("Bean is not found: $ref")
                    }

                    is InstanceBinding -> binding.instance

                    is ScopedBinding -> {
                        val beanRegistry = scopeContextStacks.top(binding.scope)?.beanRegistry
                            ?: error("Scoped bean can not be created out of scope: $ref in ${binding.scope} scope")

                        try {
                            when (binding) {
                                is FactoryBinding -> beanRegistry.bean(ref) {
                                    binding.factory(this)
                                }

                                is SetBinding<*> -> beanRegistry.bean(ref) {
                                    @Suppress("UNCHECKED_CAST")
                                    binding.constructBean() as T
                                }

                                is ListBinding<*> -> beanRegistry.bean(ref) {
                                    @Suppress("UNCHECKED_CAST")
                                    binding.constructBean() as T
                                }

                                is MapBinding<*, *> -> beanRegistry.bean(ref) {
                                    @Suppress("UNCHECKED_CAST")
                                    binding.constructBean() as T
                                }
                            }
                        } catch (e: Exception) {
                            throw RuntimeException("Bean creation failed: $ref", e)
                        }
                    }
                }
            }

        private fun <T> withDependencyCycleChecking(ref: BeanRef<*>, block: () -> T): T {
            if (!path.add(ref)) {
                val path = (path.toList() + ref).joinToString("\n") { "  - $it" }
                error("Bean dependencies cycle:\n$path")
            }
            try {
                return block()
            } finally {
                path.remove(ref)
            }
        }

        private fun <T> SetBinding<T>.constructBean(): Set<T> =
            instances.toList()
                .plus(factories.map { factory -> factory(this@Injector) })
                .plus(batchFactories.flatMap { batchFactory -> batchFactory(this@Injector) })
                .fold(persistentSetOf()) { result, element ->
                    check(element !in result) { "Set bean contains duplicating elements: $beanRef already has $element" }
                    result.add(element)
                }

        private fun <T> ListBinding<T>.constructBean(): List<T> =
            instances.toList()
                .plus(factories.map { (factory, precedence) -> factory(this@Injector) to precedence })
                .plus(batchFactories.flatMap { batchFactory -> batchFactory(this@Injector).toList() })
                .fold(mutableMapOf<T, Int>()) { result, (element, precedence) ->
                    check(element !in result) { "List bean contains duplicating elements: $beanRef already has $element" }
                    result[element] = precedence
                    result
                }
                .toList()
                .sortedBy { it.second }
                .map { it.first }

        private fun <K, T> MapBinding<K, T>.constructBean(): Map<K, T> =
            instances.toList()
                .plus(factories.map { (key, factory) -> key to factory(this@Injector) })
                .plus(batchFactories.flatMap { batchFactory -> batchFactory(this@Injector).toList() })
                .fold(persistentHashMapOf()) { result, (key, element) ->
                    check(key !in result) { "Map bean contains duplicating keys: $beanRef already has $key" }
                    result.put(key, element)
                }
    }
}

fun DiContext(
    parent: DiContext? = null,
    block: Binder.() -> Unit
): DiContext {
    val bindings = mutableMapOf<BeanRef<*>, Binding<*>>()
    val modules = mutableSetOf<KClass<out DiModule>>()
    DefaultBinder(bindings, modules).apply(block).checkRequirements()
    return DefaultDiContext(bindings, modules, parent)
}