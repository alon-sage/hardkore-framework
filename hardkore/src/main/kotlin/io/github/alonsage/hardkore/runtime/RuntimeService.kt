package io.github.alonsage.hardkore.runtime

fun interface RuntimeService {
    suspend fun run()
}