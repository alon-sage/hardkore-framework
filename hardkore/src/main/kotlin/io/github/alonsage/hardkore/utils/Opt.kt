package io.github.alonsage.hardkore.utils

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

inline fun <T> Opt<T>.onMissing(block: () -> Unit): Opt<T> =
    also {
        if (isMissing()) {
            block()
        }
    }

inline fun <T> Opt<T>.onPresent(block: (T) -> Unit): Opt<T> =
    also {
        if (!isMissing()) {
            block(value)
        }
    }

fun <T> Opt<T>.getOrNull(): T? =
    if (isMissing()) null else value

fun <T> Opt<T>.getOrDefault(default: T): T =
    if (isMissing()) default else value

inline fun <T> Opt<T>.getOrElse(block: () -> T): T =
    if (isMissing()) block() else value

inline fun <T, R> Opt<T>.map(block: (T) -> R): Opt<R> =
    if (isMissing()) Opt.Missing() else Opt.Present(block(value))