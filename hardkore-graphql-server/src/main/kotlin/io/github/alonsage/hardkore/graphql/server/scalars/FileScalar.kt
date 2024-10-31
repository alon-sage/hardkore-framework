package io.github.alonsage.hardkore.graphql.server.scalars

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import io.ktor.http.content.PartData
import java.util.*

object FileScalar {
    private const val NAME = "File"
    private const val DESCRIPTION = "GraphQL type for file uploads"

    val type: GraphQLScalarType = GraphQLScalarType.newScalar()
        .name(NAME)
        .description(DESCRIPTION)
        .coercing(SecretCoercing)
        .build()

    internal object SecretCoercing : Coercing<PartData.FileItem, Unit> {
        override fun serialize(dataFetcherResult: Any, graphQLContext: GraphQLContext, locale: Locale) {
            throw CoercingSerializeException("File is an input-only type and cannot be serialized");
        }

        override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): PartData.FileItem {
            if (input is PartData.FileItem) {
                return input
            }
            throw CoercingParseValueException("Expected type PartData.FileItem but was " + input::class::simpleName);
        }

        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): PartData.FileItem {
            throw CoercingParseLiteralException("File is an input-only type and cannot be parsed from literals");
        }
    }
}