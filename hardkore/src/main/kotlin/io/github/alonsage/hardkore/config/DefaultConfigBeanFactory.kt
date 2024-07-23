package io.github.alonsage.hardkore.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import kotlin.reflect.KType

internal class DefaultConfigBeanFactory(
    private val decoders: Set<ConfigDecoder>
) : ConfigBeanFactory {
    override fun bean(config: Config, path: String, type: KType): Any? =
        when {
            runCatching { config.getValue(path) }.isSuccess ->
                try {
                    decoders.firstNotNullOf { decoder ->
                        try {
                            decoder.decodedOrNull(config, path, type, this)
                        } catch (e: ConfigException) {
                            throw e
                        } catch (cause: Exception) {
                            throw ConfigException.BugOrBroken(
                                "Configuration value of '$path' failed to be decoded as $type",
                                cause
                            )
                        }
                    }
                } catch (_: NoSuchElementException) {
                    throw ConfigException.BadValue(
                        config.origin(),
                        path,
                        "Configuration value of '$path' can't be decoded as $type"
                    )
                }

            type.isMarkedNullable -> null

            else -> throw ConfigException.Missing(config.origin(), path)
        }
}