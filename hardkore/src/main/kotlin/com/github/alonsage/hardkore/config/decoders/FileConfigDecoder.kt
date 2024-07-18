package com.github.alonsage.hardkore.config.decoders

import com.github.alonsage.hardkore.config.AbstractConfigDecoder
import com.github.alonsage.hardkore.config.ConfigBeanFactory
import com.typesafe.config.Config
import java.io.File
import kotlin.reflect.KType

class FileConfigDecoder : AbstractConfigDecoder() {
    override fun supports(type: KType): Boolean =
        type.classifier == File::class

    override fun decoded(config: Config, path: String, type: KType, beanFactory: ConfigBeanFactory): Any =
        File(config.getString(path))
}