package io.github.alonsage.hardkore.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface Opt<out T> {
    data object Missing : Opt<Any?>

    @JvmInline
    value class Present<out T>(val value: T) : Opt<T>

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

fun <T, R> Opt<T>.mapNotNull(block: (T) -> R): Opt<R & Any> =
    if (isMissing()) {
        Opt.Missing()
    } else {
        val transformed = block(value)
        if (transformed == null) {
            Opt.Missing()
        } else {
            Opt.Present(transformed)
        }
    }

fun <T> Opt<T>.filter(block: (T) -> Boolean): Opt<T> =
    if (isMissing() || !block(value)) Opt.Missing() else Opt.Present(value)

fun <T> Opt<T>.filterNotNull(): Opt<T & Any> =
    if (isMissing() || value == null) Opt.Missing() else Opt.Present(value)

inline fun <reified T> Opt<Any?>.filterIsInstance(): Opt<T> =
    if (isMissing() || value !is T) {
        Opt.Missing()
    } else {
        Opt.Present(value as T)
    }
