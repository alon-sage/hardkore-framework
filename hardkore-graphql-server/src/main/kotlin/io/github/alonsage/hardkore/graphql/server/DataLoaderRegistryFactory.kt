package io.github.alonsage.hardkore.graphql.server

import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.CoroutineScope
import org.dataloader.DataLoaderRegistry

fun interface DataLoaderRegistryFactory {
    fun dataLoaderRegistry(applicationCall: ApplicationCall, coroutineScope: CoroutineScope): DataLoaderRegistry
}