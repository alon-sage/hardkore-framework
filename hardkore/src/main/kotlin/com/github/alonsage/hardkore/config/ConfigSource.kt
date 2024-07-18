package com.github.alonsage.hardkore.config

import com.typesafe.config.Config

fun interface ConfigSource {
    fun config(): Config
}