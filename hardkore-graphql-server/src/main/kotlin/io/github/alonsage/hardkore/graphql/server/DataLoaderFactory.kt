package io.github.alonsage.hardkore.graphql.server

import kotlinx.coroutines.future.future
import org.dataloader.BatchLoaderContextProvider
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.BatchLoaderWithContext
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory.newDataLoader
import org.dataloader.DataLoaderFactory.newMappedDataLoader
import org.dataloader.DataLoaderOptions
import org.dataloader.MappedBatchLoaderWithContext
import java.util.concurrent.CompletionStage

fun interface DataLoaderFactory<K, V> {
    fun dataLoader(contextProvider: BatchLoaderContextProvider): DataLoader<K, V>

    fun options(contextProvider: BatchLoaderContextProvider): DataLoaderOptions =
        DataLoaderOptions.newOptions()
            .setBatchLoaderContextProvider(contextProvider)

    fun interface Linear<K, V> : DataLoaderFactory<K, V>, BatchLoaderWithContext<K, V> {
        override fun dataLoader(contextProvider: BatchLoaderContextProvider): DataLoader<K, V> =
            newDataLoader(this, options(contextProvider))

        override fun load(keys: List<K>, environment: BatchLoaderEnvironment): CompletionStage<List<V?>> =
            environment.coroutineScope.future { loadSuspend(keys, environment) }

        suspend fun loadSuspend(keys: List<K>, environment: BatchLoaderEnvironment): List<V?>
    }

    fun interface Associative<K, V> : DataLoaderFactory<K, V>, MappedBatchLoaderWithContext<K, V> {
        override fun dataLoader(contextProvider: BatchLoaderContextProvider): DataLoader<K, V> =
            newMappedDataLoader(this, options(contextProvider))

        override fun load(keys: Set<K>, environment: BatchLoaderEnvironment): CompletionStage<Map<K, V?>> =
            environment.coroutineScope.future { loadSuspend(keys, environment) }

        suspend fun loadSuspend(keys: Set<K>, environment: BatchLoaderEnvironment): Map<K, V?>
    }
}