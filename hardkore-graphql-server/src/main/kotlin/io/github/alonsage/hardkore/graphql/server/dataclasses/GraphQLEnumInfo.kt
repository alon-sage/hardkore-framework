package io.github.alonsage.hardkore.graphql.server.dataclasses

import kotlin.reflect.KClass

data class GraphQLEnumInfo<T : Enum<T>>(
    val kClass: KClass<T>,
    val valuesByName: Map<String, T>,
)