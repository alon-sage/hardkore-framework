package io.github.alonsage.hardkore.di

internal sealed interface Binding<T> {
    val beanRef: BeanRef<T>
}

internal class InstanceBinding<T>(
    override val beanRef: BeanRef<T>,
    val instance: T
) : Binding<T>

internal sealed interface ScopedBinding<T> : Binding<T> {
    val scope: Scope
}

internal class FactoryBinding<T>(
    override val beanRef: BeanRef<T>,
    override val scope: Scope,
    val factory: BeanFactory<T>
) : ScopedBinding<T>

internal class SetBinding<T>(
    override val beanRef: BeanRef<Set<T>>,
    override val scope: Scope,
    val instances: Set<T>,
    val factories: Set<BeanFactory<T>>,
    val batchFactories: Set<BeanFactory<Set<T>>>
) : ScopedBinding<Set<T>>

internal class ListBinding<T>(
    override val beanRef: BeanRef<List<T>>,
    override val scope: Scope,
    val instances: Map<T, Int>,
    val factories: Map<BeanFactory<T>, Int>,
    val batchFactories: Set<BeanFactory<Map<T, Int>>>
) : ScopedBinding<List<T>>

internal class MapBinding<K, T>(
    override val beanRef: BeanRef<Map<K, T>>,
    override val scope: Scope,
    val instances: Map<K, T>,
    val factories: Map<K, BeanFactory<T>>,
    val batchFactories: Set<BeanFactory<Map<K, T>>>
) : ScopedBinding<Map<K, T>>