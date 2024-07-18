package com.github.alonsage.hardkore.fqdn.codec

import com.google.auto.service.AutoService

@AutoService(FqdnNodeCodec::class)
@SuppressWarnings("rawtypes")
class IntFqdnNodeCodec : FqdnNodeCodec<Int> {
    override fun encode(value: Int): String = value.toString()
    override fun decode(value: String): Int = value.toInt()
}