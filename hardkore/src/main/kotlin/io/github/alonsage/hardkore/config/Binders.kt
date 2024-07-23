package io.github.alonsage.hardkore.config

import io.github.alonsage.hardkore.di.BeanFactory
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DEFAULT_PRECEDENCE
import io.github.alonsage.hardkore.di.bean
import io.github.alonsage.hardkore.di.bindFactory
import io.github.alonsage.hardkore.di.bindList
import io.github.alonsage.hardkore.di.bindSet
import com.typesafe.config.Config
import kotlin.reflect.typeOf

fun Binder.bindConfig(precedence: Int = DEFAULT_PRECEDENCE, factory: BeanFactory<Config>) =
    bindList {
        bindFactory(precedence) { ConfigSource { factory() } }
    }

fun Binder.bindConfigDecoder(factory: BeanFactory<ConfigDecoder>) =
    bindSet {
        bindFactory(factory = factory)
    }

inline fun <reified T> Binder.bindConfigBean(path: String) =
    bindFactory {
        val config: Config = bean()
        val beanFactory: ConfigBeanFactory = bean()
        beanFactory.bean(config, path, typeOf<T>()) as T
    }