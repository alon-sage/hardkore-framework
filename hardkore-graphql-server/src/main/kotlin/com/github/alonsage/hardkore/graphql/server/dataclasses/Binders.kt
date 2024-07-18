package com.github.alonsage.hardkore.graphql.server.dataclasses

import com.github.alonsage.hardkore.di.BeanFactory
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.bindMap
import com.github.alonsage.hardkore.di.bindSet
import com.github.alonsage.hardkore.graphql.server.DataLoaderFactory
import com.github.alonsage.hardkore.graphql.server.FederationDataLoaderFactory
import kotlin.reflect.KClass


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