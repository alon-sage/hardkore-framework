package com.github.alonsage.hardkore.graphql.server

import graphql.TypeResolutionEnvironment
import graphql.schema.GraphQLObjectType

fun interface FederationTypeResolver {
    fun getType(env: TypeResolutionEnvironment): GraphQLObjectType?
}