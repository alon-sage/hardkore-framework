package com.github.alonsage.hardkore.graphql.server

import com.apollographql.federation.graphqljava.Federation
import com.apollographql.federation.graphqljava._Entity
import com.github.alonsage.hardkore.config.bindConfigBean
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.DiProfiles
import com.github.alonsage.hardkore.di.Prototype
import com.github.alonsage.hardkore.di.bean
import com.github.alonsage.hardkore.di.bindFactory
import com.github.alonsage.hardkore.di.bindList
import com.github.alonsage.hardkore.di.bindMap
import com.github.alonsage.hardkore.di.bindSet
import com.github.alonsage.hardkore.graphql.server.dataclasses.GraphQLDataClassesDiModule
import com.github.alonsage.hardkore.graphql.server.scalars.SecretScalar
import com.github.alonsage.hardkore.ktor.server.KtorServerDiModule
import com.github.alonsage.hardkore.ktor.server.bindKtorModule
import com.google.auto.service.AutoService
import graphql.ErrorType
import graphql.GraphQL
import graphql.GraphQLError
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.AsyncSerialExecutionStrategy
import graphql.execution.SubscriptionExecutionStrategy
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import graphql.schema.DataFetcher
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLUnionType
import graphql.schema.TypeResolver
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.idl.errors.SchemaProblem
import graphql.schema.impl.SchemaUtil
import org.dataloader.BatchLoaderContextProvider
import org.dataloader.DataLoaderRegistry
import java.util.concurrent.CompletableFuture

@AutoService(DiModule::class)
@DiProfiles(GraphQLServerDiProfile::class)
class GraphQLServerDiModule : DiModule {
    override fun Binder.install() {
        install(KtorServerDiModule())
        install(GraphQLDataClassesDiModule())

        bindConfigBean<GraphQLProperties>("graphql")

        bindSet<GraphQLScalarType> {}
        bindSet<TypeRuntimeWiring> {}
        bindMap<String, SchemaDirectiveWiring> {}
        bindList<Instrumentation> {}
        bindSet<FederationTypeResolver> {}
        bindMap<String, DataLoaderFactory<*, *>> {}

        bindFactory { typeDefinitionRegistry(bean()) }
        bindFactory { runtimeWiring(bean(), bean(), bean()) }
        bindFactory { graphQLSchema(bean(), bean(), bean(), bean(), bean()) }
        bindFactory(scope = Prototype) { dataLoaderRegistryFactory(bean()) }
        bindFactory { graphQL(bean(), bean()) }

        bindKtorModule { GraphQLKtorModule(bean(), bean(), bean()) }

        bindGraphQLScalarType { SecretScalar.type }
    }

    private fun typeDefinitionRegistry(
        properties: GraphQLProperties
    ): TypeDefinitionRegistry =
        TypeDefinitionRegistry().also { registry ->
            val schemaParser = SchemaParser()
            properties.schemas.forEach { schema ->
                val stream = javaClass.classLoader.getResourceAsStream(schema)
                    ?: error("Missing GraphQL schema resource: $schema")
                registry.merge(schemaParser.parse(stream))
            }
        }

    private fun runtimeWiring(
        scalars: Set<GraphQLScalarType>,
        types: Set<TypeRuntimeWiring>,
        directives: Map<String, SchemaDirectiveWiring>
    ): RuntimeWiring =
        RuntimeWiring.newRuntimeWiring()
            .apply { types.forEach { type -> type(type) } }
            .apply { scalars.forEach { scalar -> scalar(scalar) } }
            .apply { directives.forEach { (name, wiring) -> directive(name, wiring) } }
            .build()

    private fun dataLoaderRegistryFactory(dataLoaders: Map<String, DataLoaderFactory<*, *>>): DataLoaderRegistryFactory =
        DataLoaderRegistryFactory { coroutineScope ->
            val contextProvider = BatchLoaderContextProvider { coroutineScope }
            DataLoaderRegistry().apply {
                dataLoaders.forEach { (name, factory) -> register(name, factory.dataLoader(contextProvider)) }
            }
        }

    private fun graphQLSchema(
        properties: GraphQLProperties,
        registry: TypeDefinitionRegistry,
        wiring: RuntimeWiring,
        typeResolvers: Set<FederationTypeResolver>,
        dataLoaders: Map<String, DataLoaderFactory<*, *>>
    ): GraphQLSchema =
        if (properties.federation) {
            Federation.transform(registry, wiring)
                .resolveEntityType(federationTypeResolver(typeResolvers))
                .fetchEntities(federationDataFetcher())
                .build()
                .apply { validate(wiring, dataLoaders) }
        } else {
            SchemaGenerator()
                .makeExecutableSchema(registry, wiring)
                .apply { validate(wiring, dataLoaders) }
        }

    private fun federationTypeResolver(resolvers: Set<FederationTypeResolver>): TypeResolver =
        TypeResolver { environment ->
            resolvers.firstNotNullOfOrNull { it.getType(environment) }
                ?: error("Can resolve type of ${environment.getObject<Any>()::class}")
        }

    private fun federationDataFetcher(): DataFetcher<*> =
        DataFetcher { environment ->
            val representations = environment.getArgument<List<Map<String, Any?>>>(_Entity.argumentName)
            val futures = representations
                ?.map {
                    val typeName = it["__typename"]
                    val dataLoader = environment.getDataLoader<Map<String, Any?>, Any>("Federation[${typeName}]")
                        ?: error("Missing federation data loader: Federation[${typeName}]")
                    dataLoader.load(it)
                }
                .orEmpty()
            CompletableFuture.allOf(*futures.toTypedArray())
                .thenApply { futures.map { it.get() } }
        }

    private fun GraphQLSchema.validate(
        wiring: RuntimeWiring,
        dataLoaders: Map<String, DataLoaderFactory<*, *>>
    ) {
        val errors = mutableListOf<GraphQLError>()
        val validator = MissingDataFetchersValidator(wiring, queryType, errors)
        SchemaUtil.visitPartiallySchema(this, validator)

        val entityType = getType(_Entity.typeName) as? GraphQLUnionType
        entityType?.types.orEmpty().filter { "Federation[${it.name}]" !in dataLoaders }.forEach {
            errors.add(
                GraphQLError.newError()
                    .message("Missing federation data loader for object type: ${it.name}")
                    .errorType(ErrorType.OperationNotSupported)
                    .location(it.definition?.sourceLocation)
                    .build()
            )
        }

        if (errors.isNotEmpty()) {
            throw SchemaProblem(errors)
        }
    }

    private fun graphQL(
        schema: GraphQLSchema,
        instrumentations: List<Instrumentation>
    ): GraphQL =
        GraphQL.newGraphQL(schema)
            .queryExecutionStrategy(AsyncExecutionStrategy())
            .mutationExecutionStrategy(AsyncSerialExecutionStrategy())
            .subscriptionExecutionStrategy(SubscriptionExecutionStrategy())
            .instrumentation(ChainedInstrumentation(instrumentations))
            .build()
}