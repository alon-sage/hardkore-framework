package com.github.alonsage.hardkore.runtime

import com.github.alonsage.hardkore.di.DiContext
import com.github.alonsage.hardkore.di.bean
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

abstract class RunServicesApplicationCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    treatUnknownOptionsAsArgs: Boolean = false,
    hidden: Boolean = false
) : ApplicationCommand(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    autoCompleteEnvvar,
    allowMultipleSubcommands,
    treatUnknownOptionsAsArgs,
    hidden
) {
    override fun runWithContext(context: DiContext) {
        val services: Set<RuntimeService> = context.bean()
        val runtime = Runtime.getRuntime()
        runBlocking {
            launch {
                val shutdownHook = thread(start = false, name = "ShutdownHook") { cancel() }
                runtime.addShutdownHook(shutdownHook)
                services.forEach { launch { it.run() } }
            }
        }
    }
}