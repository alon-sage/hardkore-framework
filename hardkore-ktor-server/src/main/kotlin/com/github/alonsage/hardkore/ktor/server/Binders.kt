package com.github.alonsage.hardkore.ktor.server

import com.github.alonsage.hardkore.di.BeanFactory
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.bindSet

fun Binder.bindKtorModule(factory: BeanFactory<KtorModule>) =
    bindSet {
        bindFactory(factory = factory)
    }