package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import kotlin.reflect.KType

class NumberConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        when (type.classifier) {
            Number::class -> true
            Byte::class -> true
            Short::class -> true
            Int::class -> true
            Long::class -> true
            Float::class -> true
            Double::class -> true
            else -> false
        }

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        when (type.classifier) {
            Number::class -> config.getNumber(path)
            Byte::class -> config.getInt(path).toByte()
            Short::class -> config.getInt(path).toShort()
            Int::class -> config.getInt(path)
            Long::class -> config.getLong(path)
            Float::class -> config.getDouble(path).toFloat()
            Double::class -> config.getDouble(path)
            else -> error("Should never rich this branch")
        }
}