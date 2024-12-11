package io.github.alonsage.hardkore.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface Opt<out T> {
    data object Missing : Opt<Any?>

    @JvmInline
    value class Present<out T>
    internal constructor(val value: T) : Opt<T>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> missing() =
            Missing as Opt<T>

        fun <T> of(value: T): Opt<T> =
            Opt.Present(value)

        fun <T> ofNotNull(value: T): Opt<T & Any> =
            if (value == null) missing() else of(value)

        @Deprecated("", replaceWith = ReplaceWith("Opt.missing<T>()"))
        @Suppress("FunctionName")
        fun <T> Missing() =
            missing<T>()

        @Deprecated("", replaceWith = ReplaceWith("Opt.of<T>(value)"))
        @Suppress("FunctionName")
        fun <T> Present(value: T) =
            of(value)
    }
}

@OptIn(ExperimentalContracts::class)
fun <T> Opt<T>.isMissing(): Boolean {
    contract { returns(false) implies (this@isMissing is Opt.Present<T>) }
    return this is Opt.Missing
}

@OptIn(ExperimentalContracts::class)
fun <T> Opt<T>.isPresent(): Boolean {
    contract { returns(true) implies (this@isPresent is Opt.Present<T>) }
    return this is Opt.Present<T>
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Opt<T>.onMissing(block: () -> Unit): Opt<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return also { if (isMissing()) block() }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Opt<T>.onPresent(block: (T) -> Unit): Opt<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return also { if (isPresent()) block(value) }
}

fun <T> Opt<T>.getOrNull(): T? =
    if (isPresent()) value else null

fun <T> Opt<T>.getOrDefault(default: T): T =
    if (isPresent()) value else default

@OptIn(ExperimentalContracts::class)
inline fun <T> Opt<T>.getOrElse(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent()) value else block()
}

@OptIn(ExperimentalContracts::class)
inline fun <T, R> Opt<T>.map(block: (T) -> R): Opt<R> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isMissing()) Opt.missing() else Opt.of(block(value))
}

@OptIn(ExperimentalContracts::class)
inline fun <T, R> Opt<T>.flatMap(block: (T) -> Opt<R>): Opt<R> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isMissing()) Opt.missing() else block(value)
}

@OptIn(ExperimentalContracts::class)
inline fun <T, R> Opt<T>.mapNotNull(block: (T) -> R): Opt<R & Any> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isMissing()) Opt.missing() else Opt.ofNotNull(block(value))
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Opt<T>.filter(block: (T) -> Boolean): Opt<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isMissing() || !block(value)) Opt.missing() else Opt.of(value)
}

fun <T> Opt<T>.filterNotNull(): Opt<T & Any> =
    if (isMissing()) Opt.missing() else Opt.ofNotNull(value)

inline fun <reified T> Opt<Any?>.filterIsInstance(): Opt<T> =
    if (isMissing() || value !is T) Opt.missing() else Opt.of(value as T)

fun <T> Opt<T>.or(alt: Opt<T>): Opt<T> =
    if (isPresent()) this else alt

@OptIn(ExperimentalContracts::class)
inline fun <T> Opt<T>.orElse(block: () -> T): Opt<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent()) this else Opt.of(block())
}

fun <T, R> Opt<T>.zip(other: Opt<R>): Opt<Pair<T, R>> =
    if (isPresent() && other.isPresent()) {
        Opt.of(value to other.value)
    } else {
        Opt.missing()
    }
