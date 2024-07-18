package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil
import kotlin.reflect.KType

class ListConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == List::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any {
        val pathItems = ConfigUtil.splitPath(path)
        val itemType = type.arguments[0].type!!
        return config.getList(path).mapIndexed { index, value ->
            val itemPath = ConfigUtil.joinPath(pathItems + index.toString())
            beanFactory.bean(value.atPath(itemPath), itemPath, itemType)
        }
    }
}