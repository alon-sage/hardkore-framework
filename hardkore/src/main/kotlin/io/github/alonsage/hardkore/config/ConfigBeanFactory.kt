package io.github.alonsage.hardkore.config

import com.typesafe.config.Config
import kotlin.reflect.KType

fun interface ConfigBeanFactory {
    fun bean(config: Config, path: String, type: KType): Any?
}