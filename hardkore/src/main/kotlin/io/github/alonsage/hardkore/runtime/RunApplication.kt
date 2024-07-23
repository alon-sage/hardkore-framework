package io.github.alonsage.hardkore.runtime

import com.github.ajalt.clikt.completion.CompletionCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import java.util.*

fun runApplication(
    args: Array<String>,
    commands: List<CliktCommand> = emptyList(),
    autoLoadCommands: Boolean = true
) {
    val loadedCommands = if (autoLoadCommands) ServiceLoader.load(CliktCommand::class.java).toList() else emptyList()
    val rootCommand = NoOpCliktCommand(printHelpOnEmptyArgs = true)
    rootCommand.subcommands(CompletionCommand())
    rootCommand.subcommands(commands)
    rootCommand.subcommands(loadedCommands)
    rootCommand.main(args)
}