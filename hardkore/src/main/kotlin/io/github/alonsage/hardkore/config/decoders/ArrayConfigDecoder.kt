package io.github.alonsage.hardkore.config.decoders

import io.github.alonsage.hardkore.config.AbstractConfigDecoder
import io.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil
import java.lang.reflect.Array
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ArrayConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        (type.classifier as? KClass<*>)?.java?.isArray == true

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any {
        val pathItems = ConfigUtil.splitPath(path)
        val itemType = type.arguments[0].type!!
        val items = config.getList(path).mapIndexed { index, value ->
            val itemPath = ConfigUtil.joinPath(pathItems + index.toString())
            beanFactory.bean(value.atPath(itemPath), itemPath, itemType)
        }
        return items.stream().toArray { size ->
            @Suppress("UNCHECKED_CAST")
            Array.newInstance((itemType.classifier as KClass<*>).java, size) as kotlin.Array<out Any>
        }
    }
}