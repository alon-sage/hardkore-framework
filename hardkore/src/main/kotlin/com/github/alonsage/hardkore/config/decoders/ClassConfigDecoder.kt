package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import kotlin.reflect.KType

class ClassConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == Class::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        Thread.currentThread().contextClassLoader.loadClass(config.getString(path))
}