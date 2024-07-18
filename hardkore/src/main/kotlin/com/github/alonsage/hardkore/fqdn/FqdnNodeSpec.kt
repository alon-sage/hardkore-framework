package com.github.alonsage.hardkore.fqdn

import com.github.alonsage.hardkore.fqdn.codec.FqdnNodeCodecs
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

internal data class FqdnNodeSpec<F : Fqdn<*>>(
    val fqdnClass: KClass<F>
) {
    private val idClass by lazy {
        when {
            fqdnClass.isSubclassOf(Fqdn.Root::class) ->
                fqdnClass.supertypes.single { it.classifier == Fqdn.Root::class }
                    .arguments.first()
                    .type?.classifier
                    .let { checkNotNull(it) as KClass<*> }

            fqdnClass.isSubclassOf(Fqdn.Nested::class) ->
                fqdnClass.supertypes.single { it.classifier == Fqdn.Nested::class }
                    .arguments.first()
                    .type?.classifier
                    .let { checkNotNull(it) as KClass<*> }

            else -> error("Unreachable state")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val ownerClass by lazy {
        when {
            fqdnClass.isSubclassOf(Fqdn.Root::class) -> null

            fqdnClass.isSubclassOf(Fqdn.Nested::class) ->
                fqdnClass.supertypes.single { it.classifier == Fqdn.Nested::class }
                    .arguments[1].type?.classifier
                    .let { checkNotNull(it) as KClass<out Fqdn<*>> }

            else -> error("Unreachable state")
        }

    }

    val nodeName by lazy {
        fqdnClass.findAnnotation<FqdnNodeName>()?.value?.takeIf { it.isNotEmpty() }
            ?: fqdnClass.simpleName?.replace(CLASS_SUFFIX_REGEX, "")?.replaceFirstChar { it.lowercase() }
            ?: error("Can find Fqdn node name for $fqdnClass")
    }

    val idCodec by lazy { FqdnNodeCodecs.codec(idClass) }

    val constructor by lazy {
        if (ownerClass == null) {
            fqdnClass.java.getConstructor(idClass.java)
        } else {
            fqdnClass.java.getConstructor(idClass.java, ownerClass!!.java)
        }
    }

    val completeSpecs: List<FqdnNodeSpec<*>> by lazy {
        ownerClass?.let { Fqdn.spec(it).completeSpecs }.orEmpty() + this
    }

    companion object {
        private val CLASS_SUFFIX_REGEX = "fqdn$".toRegex(RegexOption.IGNORE_CASE)
    }
}