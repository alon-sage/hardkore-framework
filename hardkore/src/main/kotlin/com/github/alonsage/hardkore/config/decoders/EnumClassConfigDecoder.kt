package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import kotlin.reflect.KClass
import kotlin.reflect.KType

class EnumClassConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        (type.classifier as? KClass<*>)?.java?.isEnum == true

    @Suppress("UNCHECKED_CAST")
    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        config.getEnum((type.classifier as KClass<out Enum<*>>).java, path)
}