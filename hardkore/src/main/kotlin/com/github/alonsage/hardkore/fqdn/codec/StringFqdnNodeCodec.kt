package com.github.alonsage.hardkore.fqdn.codec

import com.google.auto.service.AutoService

@AutoService(FqdnNodeCodec::class)
@SuppressWarnings("rawtypes")
class StringFqdnNodeCodec : FqdnNodeCodec<String> {
    override fun encode(value: String): String = value
    override fun decode(value: String): String = value
}