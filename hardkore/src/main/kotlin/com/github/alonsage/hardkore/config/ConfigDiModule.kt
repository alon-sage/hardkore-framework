package com.github.alonsage.hardkore.config

import com.github.alonsage.hardkore.config.decoders.ArrayConfigDecoder
import com.github.alonsage.hardkore.config.decoders.BooleanConfigDecoder
import com.github.alonsage.hardkore.config.decoders.ClassConfigDecoder
import com.github.alonsage.hardkore.config.decoders.DataClassConfigDecoder
import com.github.alonsage.hardkore.config.decoders.DurationConfigDecoder
import com.github.alonsage.hardkore.config.decoders.EnumClassConfigDecoder
import com.github.alonsage.hardkore.config.decoders.FileConfigDecoder
import com.github.alonsage.hardkore.config.decoders.KotlinDurationConfigDecoder
import com.github.alonsage.hardkore.config.decoders.ListConfigDecoder
import com.github.alonsage.hardkore.config.decoders.MapConfigDecoder
import com.github.alonsage.hardkore.config.decoders.NumberConfigDecoder
import com.github.alonsage.hardkore.config.decoders.StringConfigDecoder
import com.github.alonsage.hardkore.config.decoders.ValueClassConfigDecoder
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.MIN_PRECEDENCE
import com.github.alonsage.hardkore.di.bean
import com.github.alonsage.hardkore.di.bindFactory
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