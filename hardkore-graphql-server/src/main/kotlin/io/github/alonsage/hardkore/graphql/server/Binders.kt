package io.github.alonsage.hardkore.graphql.server

import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.instrumentation.Instrumentation
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.TypeRuntimeWiring
import io.github.alonsage.hardkore.di.BeanFactory
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DEFAULT_PRECEDENCE
import io.github.alonsage.hardkore.di.bindFactory
import io.github.alonsage.hardkore.di.bindList
import io.github.alonsage.hardkore.di.bindMap
import io.github.alonsage.hardkore.di.bindSet
import java.util.function.Consumer

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

fun Binder.bindGraphQLExceptionHandler(factory: BeanFactory<DataFetcherExceptionHandler>) =
    bindFactory<DataFetcherExceptionHandler?>(factory = factory)

fun Binder.bindGraphQLSchemaTransformer(factory: BeanFactory<Consumer<GraphQLSchema.Builder>>) =
    bindSet {
        bindFactory(factory)
    }