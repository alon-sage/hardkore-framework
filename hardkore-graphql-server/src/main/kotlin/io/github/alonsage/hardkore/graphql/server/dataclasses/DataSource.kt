package io.github.alonsage.hardkore.graphql.server.dataclasses

import io.github.alonsage.hardkore.fqdn.Fqdn
import io.github.alonsage.hardkore.graphql.server.coroutineScope
import graphql.language.FieldDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.future.future
import kotlinx.coroutines.reactive.asPublisher
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

internal sealed interface DataSource : DataFetcher<Any?> {
    val dataClass: KClass<*>
    fun matches(fieldDefinition: FieldDefinition): Boolean

    class DataClassProperty(
        override val dataClass: KClass<*>,
        private val property: KProperty<*>
    ) : DataSource {
        override fun matches(fieldDefinition: FieldDefinition): Boolean =
            fieldDefinition.name == property.name &&
                    fieldDefinition.inputValueDefinitions.isEmpty()

        override fun get(environment: DataFetchingEnvironment): Any? =
            property.call(environment.getSource())
    }

    class ResolverFunction(
        override val dataClass: KClass<*>,
        private val function: KFunction<*>,
        private val resolver: Any,
        private val extensionParam: KParameter,
        private val instanceParam: KParameter,
        private val environmentParam: KParameter?,
        private val argumentParams: Map<String, KParameter>
    ) : DataSource {
        private val isFlowSubscription =
            dataClass == Subscription::class && function.returnType.classifier == Flow::class

        override fun matches(fieldDefinition: FieldDefinition): Boolean =
            fieldDefinition.name == function.name &&
                    fieldDefinition.inputValueDefinitions.all { it.name in argumentParams }

        override fun get(environment: DataFetchingEnvironment): Any? {
            val arguments = buildMap {
                put(extensionParam, environment.getSource() ?: dataClass.objectInstance)
                put(instanceParam, resolver)
                if (environmentParam != null) {
                    put(environmentParam, environment)
                }
                argumentParams.forEach { (name, param) ->
                    put(param, convertValue(environment.getArgument(name), param.type))
                }
            }
            val result = if (function.isSuspend) {
                environment.coroutineScope.future(start = CoroutineStart.UNDISPATCHED) {
                    function.callSuspendBy(arguments)
                }
            } else {
                function.callBy(arguments)
            }
            return if (isFlowSubscription) {
                @Suppress("UNCHECKED_CAST")
                (result as Flow<Any>).asPublisher(environment.coroutineScope.coroutineContext)
            } else {
                result
            }
        }

        private fun convertValue(value: Any?, targetType: KType): Any? =
            when {
                value == null ->
                    if (targetType.isMarkedNullable) null else error("Can not convert null to $targetType")

                targetType.jvmErasure.isInstance(value) ->
                    value

                targetType.classifier == UUID::class ->
                    UUID.fromString(value.toString())

                targetType.jvmErasure.isSubclassOf(Fqdn::class) && value is String ->
                    makeFqdn(value, targetType.jvmErasure)

                targetType.jvmErasure == List::class && value is List<*> ->
                    makeList(value, targetType)

                targetType.jvmErasure.isData && value is Map<*, *> ->
                    makeDataClass(value, targetType.jvmErasure)

                else ->
                    error("Can not convert $value to $targetType")
            }

        @Suppress("UNCHECKED_CAST")
        private fun makeFqdn(value: String, fqdnClass: KClass<*>): Any =
            Fqdn.fromString(value, fqdnClass as KClass<out Fqdn<*>>)

        private fun makeList(value: List<*>, targetType: KType): Any =
            targetType.arguments[0].type
                ?.let { itemType -> value.map { item -> convertValue(item, itemType) } }
                ?: error("Can not convert $value to $targetType")

        private fun makeDataClass(value: Map<*, *>, dataClass: KClass<*>): Any {
            val constructor = checkNotNull(dataClass.primaryConstructor)
            val arguments = constructor.parameters.associateWith {
                convertValue(value[it.name], it.type)
            }
            return constructor.callBy(arguments)
        }
    }
}