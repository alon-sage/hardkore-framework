package com.github.alonsage.hardkore.runtime

import com.github.ajalt.clikt.core.CliktCommand
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiContext

abstract class ApplicationCommand(
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
) : CliktCommand(
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
    override fun run() {
        val diContext = DiContext { setup() }
        runWithContext(diContext)
    }

    protected abstract fun Binder.setup()

    protected abstract fun runWithContext(context: DiContext)
}
