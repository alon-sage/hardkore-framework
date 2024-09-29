package io.github.alonsage.hardkore.graphql.server

import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.await
import org.dataloader.BatchLoaderEnvironment
import kotlin.reflect.KClass

val GraphQLContext.coroutineScope: CoroutineScope
    get() = checkNotNull(get(CoroutineScope::class)) { "Missing coroutine scope" }

val DataFetchingEnvironment.coroutineScope: CoroutineScope
    get() = graphQlContext.coroutineScope

val BatchLoaderEnvironment.applicationCall: ApplicationCall
    get() = (getContext() as BatchContext).applicationCall

val BatchLoaderEnvironment.coroutineScope: CoroutineScope
    get() = (getContext() as BatchContext).coroutineScope


val GraphQLContext.applicationCall: ApplicationCall
    get() = checkNotNull(get(ApplicationCall::class)) { "Missing application call" }

val DataFetchingEnvironment.applicationCall: ApplicationCall
    get() = graphQlContext.applicationCall


suspend fun <K, V> DataFetchingEnvironment.loadData(kClass: KClass<out DataLoaderFactory<K, V>>, key: K): V? {
    val dataLoaderName = checkNotNull(kClass.qualifiedName)
    val dataLoader = checkNotNull(getDataLoader<K, V>(dataLoaderName)) { "Dataloader not found: $dataLoaderName" }
    return dataLoader.load(key).await()
}

suspend fun <K, V> DataFetchingEnvironment.loadData(kClass: KClass<out DataLoaderFactory<K, V>>, key: K, ctx: Any): V? {
    val dataLoaderName = checkNotNull(kClass.qualifiedName)
    val dataLoader = checkNotNull(getDataLoader<K, V>(dataLoaderName)) { "Dataloader not found: $dataLoaderName" }
    return dataLoader.load(key, ctx).await()
}