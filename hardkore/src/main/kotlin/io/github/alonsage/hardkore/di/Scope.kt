package io.github.alonsage.hardkore.di

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentListOf

interface Scope

interface ScopeContext<T : Scope> {
    val scope: T
    val beanRegistry: BeanRegistry
}

data object Prototype : Scope {
    internal object Context : ScopeContext<Prototype> {
        override val scope = Prototype
        override val beanRegistry = PrototypeBeanRegistry
    }
}

data object Singleton : Scope {
    internal class Context : ScopeContext<Singleton> {
        override val scope = Singleton
        override val beanRegistry = SingletonBeanRegistry()
    }
}

data object BoundedScope : Scope {
    operator fun invoke(builder: SingletonBeanRegistry.BuildScope.() -> Unit): Context =
        Context(builder)

    class Context(
        builder: SingletonBeanRegistry.BuildScope.() -> Unit
    ) : ScopeContext<BoundedScope> {
        override val scope: BoundedScope = BoundedScope
        override val beanRegistry: BeanRegistry = SingletonBeanRegistry(builder)
    }
}

internal class ScopeContextStacks
private constructor(
    private val stacks: PersistentMap<Scope, PersistentList<ScopeContext<*>>>
) {
    constructor() : this(
        stacks = persistentHashMapOf(
            Prototype to persistentListOf(Prototype.Context),
            Singleton to persistentListOf(Singleton.Context())
        )
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Scope> top(scope: T): ScopeContext<T>? =
        stacks[scope]?.last() as ScopeContext<T>?

    fun push(context: ScopeContext<*>): ScopeContextStacks {
        val stack = stacks[context.scope] ?: persistentListOf(context)
        return ScopeContextStacks(stacks.put(context.scope, stack.add(context)))
    }
}