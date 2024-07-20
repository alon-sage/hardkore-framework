package com.github.alonsage.hardkore.condition

import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DefaultConditions : Conditions {
    private val store = MutableStateFlow(persistentHashSetOf<Condition>())

    override fun set(condition: Condition) {
        store.update { it.add(condition) }
    }

    override fun clear(condition: Condition) {
        store.update { it.remove(condition) }
    }

    override fun get(condition: Condition): Boolean =
        store.value.contains(condition)

    override fun watch(condition: Condition): Flow<Boolean> =
        store.map { condition in it }.distinctUntilChanged()

    override suspend fun wait(condition: Condition) {
        store.first { condition in it }
    }

    override fun all(conditions: Collection<Condition>): Boolean =
        store.value.containsAll(conditions)

    override fun watchAll(conditions: Collection<Condition>): Flow<Boolean> =
        store.map { it.containsAll(conditions) }.distinctUntilChanged()

    override suspend fun waitAll(conditions: Collection<Condition>) {
        store.first { it.containsAll(conditions) }
    }
}