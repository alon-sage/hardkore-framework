package io.github.alonsage.hardkore.graphql.server

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope

internal data class BatchContext(
    val applicationCall: ApplicationCall,
    val coroutineScope: CoroutineScope,
)
