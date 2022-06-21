/*
 * 👾 Towa: Powerful and advanced command handling library made for Discord.
 * Copyright © 2022 Nino Team <https://nino.sh>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sh.nino.towa.slash.commands.application

import dev.floofy.utils.slf4j.logging
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import sh.nino.towa.slash.commands.SlashCommandExtension
import sh.nino.towa.slash.commands.applicationCommands
import sh.nino.towa.slash.commands.events.application.*

internal class ApplicationCommandHandler(private val extension: SlashCommandExtension) {
    private val log by logging<ApplicationCommandHandler>()

    suspend fun onApplicationCommand(event: ApplicationCommandInteractionCreateEvent) {
        log.debug("Received slash command /${event.interaction.invokedCommandName} (${event.interaction.invokedCommandId})")
        val command = extension.applicationCommands.firstOrNull { it.info.name == event.interaction.invokedCommandName }
        if (command == null) {
            val context = ApplicationCommandPipelineContext(extension.pipeline)
            extension.pipeline.emit(ApplicationCommandNotFound::class, context)

            return
        }

        val pipelineCtx = ApplicationCommandPipelineContext(extension.pipeline)
        pipelineCtx.attributes["command"] = command

        extension
            .kord
            .rest
            .interaction
            .deferMessage(event.interaction.id, event.interaction.token, command.shouldDeferEphemeral)

        if (event.interaction.data.data.options.value == null) {
            extension.pipeline.emit(ApplicationCommandPropagation::class, pipelineCtx)

            val context = ApplicationCommandContext(event, emptyMap())
            try {
                command.execute(context)
                extension.pipeline.emit(
                    ApplicationCommandExecuted::class,
                    pipelineCtx
                )
            } catch (e: Throwable) {
                pipelineCtx.attributes["exception"] = e
                extension.pipeline.emit(
                    ApplicationCommandException::class,
                    pipelineCtx
                )
            }

            return
        }

        val options = mutableMapOf<CommandOption<*>, Any?>()
        var executer: Executable = command

        for (option in event.interaction.data.data.options.value!!) {
            if (option.value.value != null) {
                log.debug("Received command argument, finding argument ${option.name}...")

                val argument = command.options.firstOrNull {
                    it.name == option.value.value!!.name && it.toKordBuilder().type == option.value.value!!.type
                }

                if (argument == null) {
                    log.debug("Command argument ${option.name} was not found, skipping")
                    continue
                }

                log.trace("ARGUMENT FOUND :: ${option.name} -> ${option.value.value!!.value}")
                options[argument] = option.value.value!!.value
            }

            val subcommand = command.subcommandsList.firstOrNull {
                it.name == option.name
            }

            if (subcommand != null) {
                executer = subcommand
                for (arg in option.values.value!!) {
                    log.debug("Received command argument, finding argument ${arg.name}...")

                    val argument = command.options.firstOrNull {
                        it.name == arg.name && it.toKordBuilder().type == arg.type
                    }

                    if (argument == null) {
                        log.debug("Command argument ${arg.name} was not found, skipping")
                        continue
                    }

                    log.trace("ARGUMENT FOUND :: ${arg.name} -> ${arg.value}")
                    options[argument] = arg.value
                }
            }

            val group = command.groupsList.firstOrNull { it.name == option.name }
            if (option.subCommands.value != null && group != null) {
                val subcmd = option.subCommands.value!!.first()
                val subcommand = group.subcommands.firstOrNull { it.name == subcmd.name }

                if (subcommand != null) {
                    log.info("Found subcommand ${subcmd.name} in subcommand group ${option.name}")
                    executer = subcommand

                    for (arg in subcmd.options.value!!) {
                        log.debug("Received command argument, finding argument ${arg.name}...")

                        val argument = command.options.firstOrNull {
                            it.name == arg.name && it.toKordBuilder().type == arg.type
                        }

                        if (argument == null) {
                            log.debug("Command argument ${arg.name} was not found, skipping")
                            continue
                        }

                        log.trace("ARGUMENT FOUND :: ${arg.name} -> ${arg.value}")
                        options[argument] = arg.value
                    }
                }
            }
        }

        val context = ApplicationCommandContext(event, options)
        when (executer) {
            is AbstractApplicationCommand -> {
                extension.pipeline.emit(ApplicationCommandPropagation::class, pipelineCtx)

                try {
                    executer.execute(context)
                    extension.pipeline.emit(ApplicationCommandExecuted::class, pipelineCtx)
                } catch (e: Throwable) {
                    pipelineCtx.attributes["exception"] = e
                    extension.pipeline.emit(ApplicationCommandException::class, pipelineCtx)
                }
            }

            is AbstractApplicationSubcommand -> {
                extension.pipeline.emit(ApplicationSubcommandPropagation::class, pipelineCtx)

                try {
                    executer.execute(context)
                    extension.pipeline.emit(ApplicationSubcommandExecuted::class, pipelineCtx)
                } catch (e: Throwable) {
                    pipelineCtx.attributes["exception"] = e
                    extension.pipeline.emit(ApplicationSubcommandException::class, pipelineCtx)
                }
            }
        }
    }
}
