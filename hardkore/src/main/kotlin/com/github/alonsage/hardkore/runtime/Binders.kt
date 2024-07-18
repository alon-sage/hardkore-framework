package com.github.alonsage.hardkore.runtime

import com.github.alonsage.hardkore.di.BeanFactory
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.bindSet

fun Binder.bindRuntimeService(factory: BeanFactory<RuntimeService>) =
    bindSet {
        bindFactory(factory = factory)
    }