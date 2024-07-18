package com.github.alonsage.hardkore.graphql.server

fun interface FederationDataLoaderFactory<T> : DataLoaderFactory<Map<String, Any?>, T> {
    fun interface Linear<T> :
        FederationDataLoaderFactory<T>,
        DataLoaderFactory.Linear<Map<String, Any?>, T>

    fun interface Associative<T> :
        FederationDataLoaderFactory<T>,
        DataLoaderFactory.Associative<Map<String, Any?>, T>
}