package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.github.alonsage.hardkore.config.ConfigDecoder
import com.typesafe.config.Config
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ValueClassConfigDecoder : ConfigDecoder {
    override fun decodedOrNull(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any? {
        val clazz = type.classifier as? KClass<*> ?: return null
        if (!clazz.isValue) return null
        val constructor = clazz.primaryConstructor ?: return null
        return constructor.call(beanFactory.bean(config, path, constructor.parameters[0].type))
    }
}