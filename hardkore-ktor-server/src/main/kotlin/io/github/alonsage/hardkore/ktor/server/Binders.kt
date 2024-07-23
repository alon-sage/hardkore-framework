package io.github.alonsage.hardkore.ktor.server

import io.github.alonsage.hardkore.di.BeanFactory
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.bindSet

fun Binder.bindKtorModule(factory: BeanFactory<KtorModule>) =
    bindSet {
        bindFactory(factory = factory)
    }