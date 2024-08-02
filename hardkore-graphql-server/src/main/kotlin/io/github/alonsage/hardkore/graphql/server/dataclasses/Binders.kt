package io.github.alonsage.hardkore.graphql.server.dataclasses

import io.github.alonsage.hardkore.di.BeanFactory
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.bindMap
import io.github.alonsage.hardkore.di.bindSet
import io.github.alonsage.hardkore.graphql.server.DataLoaderFactory
import io.github.alonsage.hardkore.graphql.server.FederationDataLoaderFactory
import kotlin.reflect.KClass

inline fun <reified T : Enum<T>> Binder.bindGraphQLEnum(
    typeName: String = checkNotNull(T::class.simpleName),
    nameOverrides: Map<T, String> = emptyMap()
) =
    bindMap<String, GraphQLEnumInfo<*>>(GraphQLDataClassesDiModule.Qualifier) {
        bindInstance(typeName, GraphQLEnumInfo(T::class, enumValues<T>().associateBy { nameOverrides[it] ?: it.name }))
    }

inline fun <reified T> Binder.bindGraphQLDataClass(typeName: String = checkNotNull(T::class.simpleName)) =
    bindMap<KClass<*>, String>(GraphQLDataClassesDiModule.Qualifier) {
        bindInstance(T::class, typeName)
    }

fun Binder.bindGraphQLResolver(factory: BeanFactory<Any>) =
    bindSet(GraphQLDataClassesDiModule.Qualifier) {
        bindFactory(factory)
    }

inline fun <reified T> Binder.bindGraphQLFederationDataLoader(
    noinline factory: BeanFactory<FederationDataLoaderFactory<T>>
) =
    bindMap<KClass<*>, DataLoaderFactory<*, *>>(GraphQLDataClassesDiModule.Qualifier) {
        bindFactory(T::class, factory)
    }