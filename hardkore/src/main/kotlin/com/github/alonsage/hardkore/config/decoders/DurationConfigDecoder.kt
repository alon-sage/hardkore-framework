package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import java.time.Duration
import kotlin.reflect.KType

class DurationConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == Duration::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        config.getDuration(path)
}