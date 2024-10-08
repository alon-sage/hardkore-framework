package io.github.alonsage.hardkore.config.decoders

import io.github.alonsage.hardkore.config.AbstractConfigDecoder
import io.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import kotlin.reflect.KType

class BooleanConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == Boolean::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        config.getBoolean(path)
}