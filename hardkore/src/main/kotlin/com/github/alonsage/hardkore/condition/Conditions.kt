package com.github.alonsage.hardkore.condition

import kotlinx.coroutines.flow.Flow

interface Conditions {
    fun set(condition: Condition)
    fun clear(condition: Condition)

    fun get(condition: Condition): Boolean
    fun watch(condition: Condition): Flow<Boolean>
    suspend fun wait(condition: Condition)

    fun all(conditions: Collection<Condition>): Boolean
    fun watchAll(conditions: Collection<Condition>): Flow<Boolean>
    suspend fun waitAll(conditions: Collection<Condition>)

    fun all(vararg conditions: Condition): Boolean =
        all(conditions.toList())

    fun watchAll(vararg conditions: Condition): Flow<Boolean> =
        watchAll(conditions.toList())

    suspend fun waitAll(vararg conditions: Condition) =
        waitAll(conditions.toList())
}