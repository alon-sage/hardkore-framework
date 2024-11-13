package io.github.alonsage.hardkore.graphql.server.dataclasses

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface Opt<T> {
    data object Missing : Opt<Any?>

    @JvmInline
    value class Present<T>(val value: T) : Opt<T>

    companion object {
        @Suppress("UNCHECKED_CAST", "FunctionName")
        fun <T> Missing() = Missing as Opt<T>
    }
}

@OptIn(ExperimentalContracts::class)
fun <T> Opt<T>.isMissing(): Boolean {
    contract { returns(false) implies (this@isMissing is Opt.Present<T>) }
    return this is Opt.Missing
}