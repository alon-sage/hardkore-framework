package io.github.alonsage.hardkore.config.decoders

import io.github.alonsage.hardkore.config.ConfigBeanFactory
import io.github.alonsage.hardkore.config.ConfigDecoder
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigUtil
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class DataClassConfigDecoder : ConfigDecoder {
    override fun decodedOrNull(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any? {
        val pathItems = ConfigUtil.splitPath(path)
        val clazz = type.classifier as? KClass<*> ?: return null
        if (!clazz.isData) return null
        val constructor = clazz.primaryConstructor ?: return null
        val parameters = constructor.parameters
            .mapNotNull {
                try {
                    it to beanFactory.bean(config, ConfigUtil.joinPath(pathItems + it.name), it.type)
                } catch (e: ConfigException.Missing) {
                    if (it.isOptional) null
                    else throw e
                }
            }
            .toMap()
        return constructor.callBy(parameters)
    }
}