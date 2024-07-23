package io.github.alonsage.hardkore.fqdn.codec

import java.util.*
import kotlin.reflect.KClass

object FqdnNodeCodecs {
    private val codecs = ServiceLoader
        .load(FqdnNodeCodec::class.java)
        .associateBy { codec ->
            val supertype = codec::class.supertypes.single { it.classifier == FqdnNodeCodec::class }
            checkNotNull(supertype.arguments.single().type?.classifier as? KClass<*>) {
                "Can't determine codec supported type: $codec"
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> codec(kClass: KClass<out T>): FqdnNodeCodec<T> =
        codecs[kClass] as FqdnNodeCodec<T>?
            ?: error("Missing codec for type: $kClass")
}