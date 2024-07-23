package io.github.alonsage.hardkore.graphql.server

import kotlinx.coroutines.CoroutineScope
import org.dataloader.DataLoaderRegistry

fun interface DataLoaderRegistryFactory {
    fun dataLoaderRegistry(coroutineScope: CoroutineScope): DataLoaderRegistry
}