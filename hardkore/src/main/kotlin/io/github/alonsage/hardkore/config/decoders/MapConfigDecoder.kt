package io.github.alonsage.hardkore.config.decoders

import io.github.alonsage.hardkore.config.AbstractConfigDecoder
import io.github.alonsage.hardkore.config.ConfigBeanFactory
import io.github.alonsage.hardkore.utils.MapEntry
import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil
import kotlin.jvm.internal.TypeReference
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

class MapConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == Map::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any {
        val keyType = type.arguments[0].type!!
        val valueType = type.arguments[1].type!!

        return if (keyType.classifier == String::class) {
            val pathItems = ConfigUtil.splitPath(path)
            config.getObject(path).keys.associateWith { key ->
                val valuePath = ConfigUtil.joinPath(pathItems + key)
                beanFactory.bean(config, valuePath, valueType)
            }
        } else {
            val listItemType = TypeReference(
                classifier = MapEntry::class,
                arguments = listOf(
                    KTypeProjection(KVariance.INVARIANT, keyType),
                    KTypeProjection(KVariance.INVARIANT, valueType)
                ),
                isMarkedNullable = false
            )
            val listType = TypeReference(
                classifier = List::class,
                arguments = listOf(
                    KTypeProjection(KVariance.INVARIANT, listItemType)
                ),
                isMarkedNullable = false
            )
            @Suppress("UNCHECKED_CAST")
            (beanFactory.bean(config, path, listType) as List<MapEntry<*, *>>)
                .associate { it.key to it.value }
        }
    }
}