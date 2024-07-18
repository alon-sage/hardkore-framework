package com.github.alonsage.hardkore.di

import kotlin.reflect.KProperty

interface InjectionContext {
    val context: DiContext
    fun <T> bean(ref: BeanRef<T>): T
    fun <T> provider(ref: BeanRef<T>): BeanProvider<T> = BeanProvider { bean(ref) }
}

fun interface BeanProvider<T> {
    fun bean(): T
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = bean()
}

inline fun <reified T> InjectionContext.bean(qualifier: Any? = null): T =
    bean(beanRef(qualifier))

inline fun <reified T> InjectionContext.provider(qualifier: Any? = null): BeanProvider<T> =
    provider(beanRef(qualifier))
