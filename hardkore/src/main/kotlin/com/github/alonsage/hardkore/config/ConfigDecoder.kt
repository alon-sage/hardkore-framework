package com.github.alonsage.hardkore.config

import com.typesafe.config.Config
import kotlin.reflect.KType

interface ConfigDecoder {
    fun decodedOrNull(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any?
}
