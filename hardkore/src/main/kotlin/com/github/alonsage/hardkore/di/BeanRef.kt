package com.github.alonsage.hardkore.di

import kotlin.reflect.KType
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

data class BeanRef<T>(
    internal val kType: KType,
    internal val qualifier: Any?
)

inline fun <reified T> beanRef(qualifier: Any? = null): BeanRef<T> =
    BeanRef(typeOf<T>().let { it.withNullability(it.isMarkedNullable) }, qualifier)