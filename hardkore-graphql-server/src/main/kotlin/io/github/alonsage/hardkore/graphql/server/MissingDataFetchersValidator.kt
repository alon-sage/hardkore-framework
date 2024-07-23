package io.github.alonsage.hardkore.graphql.server

import com.apollographql.federation.graphqljava._Entity
import com.apollographql.federation.graphqljava._Service
import graphql.ErrorType
import graphql.GraphQLError
import graphql.introspection.Introspection
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLTypeUtil
import graphql.schema.GraphQLTypeVisitorStub
import graphql.schema.idl.RuntimeWiring
import graphql.util.TraversalControl
import graphql.util.TraverserContext

internal class MissingDataFetchersValidator(
    private val wiring: RuntimeWiring,
    private val queryType: GraphQLObjectType,
    private val errors: MutableList<GraphQLError>
) : GraphQLTypeVisitorStub() {
    override fun visitGraphQLFieldDefinition(
        node: GraphQLFieldDefinition,
        context: TraverserContext<GraphQLSchemaElement>
    ): TraversalControl {
        val parentNode = context.parentNode as? GraphQLObjectType
            ?: return TraversalControl.CONTINUE

        if (Introspection.isIntrospectionTypes(parentNode)) {
            return TraversalControl.CONTINUE
        }

        if (parentNode.name == _Service.typeName) {
            return TraversalControl.CONTINUE
        }

        if (parentNode.name == queryType.name) {
            if (node.name == _Service.fieldName) {
                return TraversalControl.CONTINUE
            }
            if (node.name == _Entity.fieldName) {
                return TraversalControl.CONTINUE
            }
        }

        val coordinates = FieldCoordinates.coordinates(parentNode, node)
        if (
            !wiring.codeRegistry.hasDataFetcher(coordinates) &&
            node.name !in wiring.getDataFetchersForType(parentNode.name) &&
            wiring.getDefaultDataFetcherForType(parentNode.name) == null
        ) {
            errors.add(
                GraphQLError.newError()
                    .message("${formatField(parentNode, node)} is missing data fetcher")
                    .errorType(ErrorType.OperationNotSupported)
                    .location(node.definition?.sourceLocation)
                    .build()
            )
        }
        return TraversalControl.CONTINUE
    }

    private fun formatField(type: GraphQLObjectType, field: GraphQLFieldDefinition): String =
        field.arguments.joinToString(
            separator = ", ",
            prefix = "${type.name}.${field.name}(",
            postfix = "): ${GraphQLTypeUtil.simplePrint(field.type)}"
        ) { "${it.name}: ${GraphQLTypeUtil.simplePrint(it.type)}" }
}