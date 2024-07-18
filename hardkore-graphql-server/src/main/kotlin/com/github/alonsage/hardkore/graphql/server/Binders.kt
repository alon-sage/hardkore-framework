package com.github.alonsage.hardkore.graphql.server

import com.github.alonsage.hardkore.di.BeanFactory
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DEFAULT_PRECEDENCE
import com.github.alonsage.hardkore.di.bindList
import com.github.alonsage.hardkore.di.bindMap
import com.github.alonsage.hardkore.di.bindSet
import graphql.execution.instrumentation.Instrumentation
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.TypeRuntimeWiring

fun Binder.bindGraphQLScalarType(factory: BeanFactory<GraphQLScalarType>) =
    bindSet {
        bindFactory(factory)
    }

fun Binder.bindGraphQLType(factory: BeanFactory<TypeRuntimeWiring>) =
    bindSet {
        bindFactory(factory)
    }

fun Binder.bindGraphQLDirective(name: String, factory: BeanFactory<SchemaDirectiveWiring>) =
    bindMap {
        bindFactory(name, factory)
    }

fun Binder.bindGraphQLInstrumentation(
    precedence: Int = DEFAULT_PRECEDENCE,
    factory: BeanFactory<Instrumentation>
) =
    bindList {
        bindFactory(precedence, factory)
    }

fun Binder.bindGraphQLDataLoader(name: String, factory: BeanFactory<DataLoaderFactory<*, *>>) =
    bindMap {
        bindFactory(name, factory)
    }

inline fun <reified T : DataLoaderFactory<*, *>> Binder.bindGraphQLDataLoader(noinline factory: BeanFactory<T>) =
    bindGraphQLDataLoader(checkNotNull(T::class.qualifiedName), factory)

fun Binder.bindGraphQLFederationDataLoader(
    typeName: String,
    factory: BeanFactory<DataLoaderFactory<*, *>>
) =
    bindGraphQLDataLoader("Federation[$typeName]", factory)

fun Binder.bindGraphQLFederationTypeResolver(factory: BeanFactory<FederationTypeResolver>) =
    bindSet {
        bindFactory(factory)
    }