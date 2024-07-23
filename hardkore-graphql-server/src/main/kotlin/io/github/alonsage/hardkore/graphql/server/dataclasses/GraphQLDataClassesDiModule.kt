package io.github.alonsage.hardkore.graphql.server.dataclasses

import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.bean
import io.github.alonsage.hardkore.di.bindMap
import io.github.alonsage.hardkore.di.bindSet
import io.github.alonsage.hardkore.graphql.server.DataLoaderFactory
import io.github.alonsage.hardkore.graphql.server.FederationTypeResolver
import io.github.alonsage.hardkore.graphql.server.bindGraphQLFederationTypeResolver
import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberProperties

class GraphQLDataClassesDiModule : DiModule {
    object Qualifier

    override fun Binder.install() {
        bindMap<KClass<*>, String>(Qualifier) {
            bindFactory(Query::class) { operationTypeName(bean(), "query") }
            bindFactory(Mutation::class) { operationTypeName(bean(), "mutation") }
            bindFactory(Subscription::class) { operationTypeName(bean(), "subscription") }
        }
        bindSet<Any>(Qualifier) {}
        bindMap<KClass<*>, DataLoaderFactory<*, *>>(Qualifier) {}

        bindSet {
            bindBatchFactory { typeRuntimeWirings(bean(), bean(Qualifier), bean(Qualifier)) }
        }
        bindMap {
            bindBatchFactory { dataLoaders(bean(Qualifier), bean(Qualifier)) }
        }
        bindGraphQLFederationTypeResolver {
            federationTypeResolver(bean(Qualifier))
        }
    }

    private fun operationTypeName(registry: TypeDefinitionRegistry, operation: String): String {
        val operations = registry.schemaDefinition().getOrNull()?.operationTypeDefinitions.orEmpty() +
                registry.schemaExtensionDefinitions.flatMap { it.operationTypeDefinitions }
        return try {
            operations.single { it.name == operation }.typeName.name
        } catch (_: NoSuchElementException) {
            when (operation) {
                "query" -> "Query"
                "mutation" -> "Mutation"
                "subscription" -> "Subscription"
                else -> error("Unknown operation: $operation")
            }
        }
    }

    private fun dataLoaders(
        dataClasses: Map<KClass<*>, String>,
        dataLoaders: Map<KClass<*>, DataLoaderFactory<*, *>>
    ): Map<String, DataLoaderFactory<*, *>> =
        dataLoaders.mapKeys { (dataClass, _) -> "Federation[${dataClasses[dataClass]}]" }

    private fun typeRuntimeWirings(
        typeDefinitionRegistry: TypeDefinitionRegistry,
        dataClasses: Map<KClass<*>, String>,
        resolvers: Set<Any>
    ): Set<TypeRuntimeWiring> {
        val allSources = dataClasses.keys.associateWith { dataClassSources(it).toMutableSet() }
        resolvers.forEach { resolver ->
            resolverSources(resolver).forEach { source ->
                val dataClassSources = allSources[source.dataClass]
                    ?: error("Unbound GraphQL data class: ${source.dataClass}")
                dataClassSources.add(source)
            }
        }

        val wirings = mutableSetOf<TypeRuntimeWiring>()
        for ((dataClass, typeName) in dataClasses) {
            val typeDefinition = typeDefinitionRegistry.getType(typeName).getOrNull() as? ObjectTypeDefinition
                ?: if (dataClass != Query::class && dataClass != Mutation::class && dataClass != Subscription::class) {
                    error("GraphQL data class bound to unknown object type: $dataClass to $typeName")
                } else {
                    continue
                }
            val wiring = TypeRuntimeWiring.newTypeWiring(typeName)
            val dataClassSources = checkNotNull(allSources[dataClass])
            typeDefinition.fieldDefinitions.forEach { fieldDefinition ->
                val fieldSources = dataClassSources.filter { it.matches(fieldDefinition) }
                when {
                    fieldSources.size == 1 -> wiring.dataFetcher(fieldDefinition.name, fieldSources.single())
                    fieldSources.size > 1 -> error(
                        "Too many sources for field: ${typeDefinition.name}.${fieldDefinition.name}. Candidates:" +
                                fieldSources.joinToString("\n  - ", "\n  - ")
                    )
                }
            }
            wirings.add(wiring.build())
        }
        return wirings
    }

    private fun dataClassSources(dataClass: KClass<*>): List<DataSource> =
        dataClass.memberProperties.map { DataSource.DataClassProperty(dataClass, it) }

    private fun resolverSources(resolver: Any): List<DataSource> =
        resolver::class.memberExtensionFunctions.mapNotNull { resolverSource(resolver, it) }

    private fun resolverSource(resolver: Any, function: KFunction<*>): DataSource? {
        var extensionParam: KParameter? = null
        var instanceParam: KParameter? = null
        var environmentParam: KParameter? = null
        val argumentParams = mutableMapOf<String, KParameter>()
        function.parameters.forEach { param ->
            when (param.kind) {
                KParameter.Kind.INSTANCE -> instanceParam = param
                KParameter.Kind.EXTENSION_RECEIVER -> extensionParam = param
                KParameter.Kind.VALUE ->
                    if (param.isVararg) {
                        return null
                    } else if (isEnvironmentParam(param)) {
                        if (environmentParam == null) {
                            environmentParam = param
                        } else {
                            return null
                        }
                    } else {
                        argumentParams[checkNotNull(param.name)] = param
                    }
            }
        }
        return DataSource.ResolverFunction(
            dataClass = checkNotNull(extensionParam).type.classifier as? KClass<*> ?: return null,
            function = function,
            resolver = resolver,
            extensionParam = checkNotNull(extensionParam),
            instanceParam = checkNotNull(instanceParam),
            environmentParam = environmentParam,
            argumentParams = argumentParams
        )
    }

    private fun isEnvironmentParam(param: KParameter): Boolean =
        param.type.classifier == DataFetchingEnvironment::class

    private fun federationTypeResolver(dataClasses: Map<KClass<*>, String>): FederationTypeResolver =
        FederationTypeResolver { env ->
            dataClasses[env.getObject<Any?>()::class]
                ?.let { typeName -> env.schema.getObjectType(typeName) }
        }
}