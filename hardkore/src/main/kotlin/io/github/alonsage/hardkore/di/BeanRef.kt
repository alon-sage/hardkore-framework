package io.github.alonsage.hardkore.di

import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection.Companion.invariant
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

data class BeanRef<T>(
    internal val kType: KType,
    internal val qualifier: Any?
)

inline fun <reified T> beanRef(qualifier: Any? = null): BeanRef<T> =
    BeanRef(cleanPlatformType(typeOf<T>()), qualifier)

inline fun <reified T> setBeanRef(qualifier: Any? = null): BeanRef<Set<T>> =
    BeanRef(Set::class.createType(listOf(invariant(cleanPlatformType(typeOf<T>())))), qualifier)

inline fun <reified T> listBeanRef(qualifier: Any? = null): BeanRef<List<T>> =
    BeanRef(List::class.createType(listOf(invariant(cleanPlatformType(typeOf<T>())))), qualifier)

inline fun <reified K, reified V> mapBeanRef(qualifier: Any? = null): BeanRef<Map<K, V>> =
    BeanRef(
        Map::class.createType(
            listOf(
                invariant(cleanPlatformType(typeOf<K>())),
                invariant(cleanPlatformType(typeOf<V>()))
            )
        ), qualifier
    )

@Suppress("NOTHING_TO_INLINE")
inline fun cleanPlatformType(kType: KType): KType =
    kType.withNullability(kType.isMarkedNullable)
