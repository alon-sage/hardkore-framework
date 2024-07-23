package io.github.alonsage.hardkore.runtime

import io.github.alonsage.hardkore.di.BeanFactory
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.bindSet

fun Binder.bindRuntimeService(factory: BeanFactory<RuntimeService>) =
    bindSet {
        bindFactory(factory = factory)
    }