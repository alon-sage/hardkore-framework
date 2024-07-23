package io.github.alonsage.hardkore.config

import com.typesafe.config.Config
import kotlin.reflect.KType

abstract class AbstractConfigDecoder : ConfigDecoder {
    override fun decodedOrNull(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any? =
        if (supports(type)) decoded(config, path, type, beanFactory) else null

    protected abstract fun supports(type: KType): Boolean

    protected abstract fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any
}