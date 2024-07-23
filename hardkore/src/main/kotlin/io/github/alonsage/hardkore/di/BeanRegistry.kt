package io.github.alonsage.hardkore.di

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf

interface BeanRegistry {
    fun <T> bean(ref: BeanRef<T>, factory: () -> T): T
}

object PrototypeBeanRegistry : BeanRegistry {
    override fun <T> bean(ref: BeanRef<T>, factory: () -> T): T =
        factory()
}

class SingletonBeanRegistry
private constructor(
    initial: PersistentMap<BeanRef<*>, Any?>
) : SynchronizedObject(), BeanRegistry {
    private val instances = atomic(initial)

    constructor() : this(persistentHashMapOf())

    constructor(builder: BuildScope.() -> Unit) : this(
        persistentHashMapOf<BeanRef<*>, Any?>().builder()
            .also { BuildScope(it).apply(builder) }
            .build()
    )

    override fun <T> bean(ref: BeanRef<T>, factory: () -> T): T {
        if (ref !in instances.value) {
            synchronized(this) {
                if (ref !in instances.value) {
                    val bean = factory()
                    instances.update { it.put(ref, bean) }
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return instances.value[ref] as T
    }

    class BuildScope
    internal constructor(
        private val builder: PersistentMap.Builder<BeanRef<*>, Any?>
    ) {
        fun <T> add(ref: BeanRef<T>, instance: T) {
            require(ref !in builder) { "Bean already added: $ref" }
            builder[ref] = instance
        }
    }
}

inline fun <reified T> SingletonBeanRegistry.BuildScope.add(instance: T, qualifier: Any? = null) =
    add(beanRef(qualifier), instance)