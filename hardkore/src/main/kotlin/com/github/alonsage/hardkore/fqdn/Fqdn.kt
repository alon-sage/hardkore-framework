package com.github.alonsage.hardkore.fqdn

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

sealed class Fqdn<T : Any> {
    abstract val id: T

    operator fun contains(other: Fqdn<*>): Boolean {
        var current: Fqdn<*>? = other
        while (current != null) {
            if (current == this) return true
            current = (current as? Nested<*, *>)?.owner
        }
        return false
    }

    abstract override fun toString(): String

    abstract class Root<T : Any> : Fqdn<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun equals(other: Any?): Boolean =
            when {
                other == null -> false
                other === this -> true
                other::class != this::class -> false
                else -> (other as Root<T>).id == id
            }

        override fun hashCode(): Int =
            id.hashCode()

        override fun toString(): String =
            SCHEMA + spec(this::class).run { nodeName + ID_DELIMITER + idCodec.encode(id) }
    }

    abstract class Nested<T : Any, O : Fqdn<*>> : Fqdn<T>() {
        abstract val owner: O

        @Suppress("UNCHECKED_CAST")
        override fun equals(other: Any?): Boolean =
            when {
                other == null -> false
                other === this -> true
                other::class != this::class -> false
                else -> (other as Nested<T, O>).let { it.id == id && it.owner == owner }
            }

        override fun hashCode(): Int =
            31 * owner.hashCode() + id.hashCode()

        override fun toString(): String =
            owner.toString() + NODE_DELIMITER + spec(this::class).run { nodeName + ID_DELIMITER + idCodec.encode(id) }
    }

    companion object {
        private const val SCHEMA = "fqdn://"
        private const val ID_DELIMITER = ":"
        private const val NODE_DELIMITER = "/"

        private val specsCache = ConcurrentHashMap<KClass<out Fqdn<*>>, FqdnNodeSpec<*>>()

        @Suppress("UNCHECKED_CAST")
        internal fun <T : Fqdn<*>> spec(fqdnClass: KClass<T>): FqdnNodeSpec<T> =
            specsCache.computeIfAbsent(fqdnClass) { FqdnNodeSpec(it) } as FqdnNodeSpec<T>

        fun <F : Fqdn<*>> fromString(encoded: String, fqdnClass: KClass<F>): F {
            require(encoded.startsWith(SCHEMA)) { "Fqdn is missing schema" }

            val nodes = encoded.removePrefix(SCHEMA).split(NODE_DELIMITER).map { node ->
                node.split(ID_DELIMITER, limit = 2).also {
                    require(it.size == 2) { "Fqdn contains invalid node: $node" }
                }
            }

            val nodeSpecs = spec(fqdnClass).completeSpecs
            require(nodes.size == nodeSpecs.size) { "Fqdn has wrong number of nodes: ${nodes.size}" }

            @Suppress("UNCHECKED_CAST")
            return nodeSpecs.zip(nodes).fold<_, Fqdn<*>?>(null) { fqdn, (nodeSpec, node) ->
                require(nodeSpec.nodeName == node[0]) { "Fqdn contains unexpected node: ${node[0]}" }
                val id = nodeSpec.idCodec.decode(node[1])
                if (fqdn == null) {
                    nodeSpec.constructor.newInstance(id)
                } else {
                    nodeSpec.constructor.newInstance(id, fqdn)
                }
            } as F
        }

        inline fun <reified F : Fqdn<*>> fromString(encoded: String): F =
            fromString(encoded, F::class)
    }
}