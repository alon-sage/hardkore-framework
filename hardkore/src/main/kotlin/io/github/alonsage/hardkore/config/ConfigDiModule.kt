package io.github.alonsage.hardkore.config

import io.github.alonsage.hardkore.config.decoders.ArrayConfigDecoder
import io.github.alonsage.hardkore.config.decoders.BooleanConfigDecoder
import io.github.alonsage.hardkore.config.decoders.ClassConfigDecoder
import io.github.alonsage.hardkore.config.decoders.DataClassConfigDecoder
import io.github.alonsage.hardkore.config.decoders.DurationConfigDecoder
import io.github.alonsage.hardkore.config.decoders.EnumClassConfigDecoder
import io.github.alonsage.hardkore.config.decoders.FileConfigDecoder
import io.github.alonsage.hardkore.config.decoders.KotlinDurationConfigDecoder
import io.github.alonsage.hardkore.config.decoders.ListConfigDecoder
import io.github.alonsage.hardkore.config.decoders.MapConfigDecoder
import io.github.alonsage.hardkore.config.decoders.NumberConfigDecoder
import io.github.alonsage.hardkore.config.decoders.StringConfigDecoder
import io.github.alonsage.hardkore.config.decoders.ValueClassConfigDecoder
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.MIN_PRECEDENCE
import io.github.alonsage.hardkore.di.bean
import io.github.alonsage.hardkore.di.bindFactory
import com.google.auto.service.AutoService
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

@AutoService(DiModule::class)
class ConfigDiModule : DiModule {
    override fun Binder.install() {
        bindConfig(REFERENCE_CONFIG_PRECEDENCE) { ConfigFactory.defaultReferenceUnresolved() }
        bindConfig(DEFAULT_APPLICATION_CONFIG_PRECEDENCE) { ConfigFactory.defaultApplication() }
        bindConfig(SYSTEM_PROPERTIES_CONFIG_PRECEDENCE) { ConfigFactory.systemProperties() }
        bindConfig(SYSTEM_ENVIRONMENT_CONFIG_PRECEDENCE) { ConfigFactory.systemEnvironmentOverrides() }

        bindConfigDecoder { ArrayConfigDecoder() }
        bindConfigDecoder { BooleanConfigDecoder() }
        bindConfigDecoder { ClassConfigDecoder() }
        bindConfigDecoder { DataClassConfigDecoder() }
        bindConfigDecoder { DurationConfigDecoder() }
        bindConfigDecoder { KotlinDurationConfigDecoder() }
        bindConfigDecoder { EnumClassConfigDecoder() }
        bindConfigDecoder { FileConfigDecoder() }
        bindConfigDecoder { ListConfigDecoder() }
        bindConfigDecoder { MapConfigDecoder() }
        bindConfigDecoder { NumberConfigDecoder() }
        bindConfigDecoder { StringConfigDecoder() }
        bindConfigDecoder { ValueClassConfigDecoder() }

        bindFactory { config(bean()) }
        bindFactory<ConfigBeanFactory> { DefaultConfigBeanFactory(bean()) }
    }

    private fun config(configSources: List<ConfigSource>): Config =
        configSources
            .map(ConfigSource::config)
            .reduceOrNull(Config::withFallback)
            ?.resolve()
            ?: ConfigFactory.empty()

    companion object {
        const val REFERENCE_CONFIG_PRECEDENCE = MIN_PRECEDENCE
        const val DEFAULT_APPLICATION_CONFIG_PRECEDENCE = 1200
        const val SYSTEM_PROPERTIES_CONFIG_PRECEDENCE = 1100
        const val SYSTEM_ENVIRONMENT_CONFIG_PRECEDENCE = 1000
    }
}