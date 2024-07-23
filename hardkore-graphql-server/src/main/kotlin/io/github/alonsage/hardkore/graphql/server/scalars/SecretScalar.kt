package io.github.alonsage.hardkore.graphql.server.scalars

import io.github.alonsage.hardkore.utils.Secret
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.util.*

object SecretScalar {
    private const val NAME = "Secret"
    private const val DESCRIPTION = "GraphQL type for sensitive data"

    val type: GraphQLScalarType = GraphQLScalarType.newScalar()
        .name(NAME)
        .description(DESCRIPTION)
        .coercing(SecretCoercing)
        .build()

    internal object SecretCoercing : Coercing<Secret, String> {
        override fun serialize(dataFetcherResult: Any, graphQLContext: GraphQLContext, locale: Locale): String =
            if (dataFetcherResult is Secret) dataFetcherResult.value
            else throw CoercingSerializeException("Expected a 'Secret' but was '${dataFetcherResult::class}'.")

        override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): Secret =
            if (input is String) Secret(input)
            else throw CoercingParseValueException("Expected a 'String' but was '${input::class}'.")

        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): Secret =
            if (input is StringValue) Secret(input.value)
            else throw CoercingParseLiteralException("Expected AST type 'StringValue' but was '${input::class}'.")

        override fun valueToLiteral(input: Any, graphQLContext: GraphQLContext, locale: Locale): Value<*> =
            StringValue.newStringValue(serialize(input, graphQLContext, locale)).build()
    }
}