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
import dev.kord.common.entity.InteractionType
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.Job
import sh.nino.towa.slash.commands.SlashCommandExtension

/**
 * Represents the execution handler for executing application commands.
 */
class ApplicationCommandHandler(private val extension: SlashCommandExtension, private val kord: Kord): AutoCloseable {
    private val eventJob: Job
    private val log by logging<ApplicationCommandHandler>()

    init {
        log.info("Found ${extension.applicationCommands.size} application commands, 0 message commands, and 0 user commands to use!")

        eventJob = kord.on<InteractionCreateEvent> { onInteraction(this) }
    }

    override fun close() {
        eventJob.cancel()
    }

    private fun onInteraction(event: InteractionCreateEvent) = when (event.interaction.data.type) {
        InteractionType.Ping -> {}
        InteractionType.ApplicationCommand -> onApplicationCommand(event as ApplicationCommandInteractionCreateEvent)
        else -> {
            log.warn("Interaction type ${event.interaction.data.type.type} is not implemented.")
        }
    }

    private fun onApplicationCommand(event: ApplicationCommandInteractionCreateEvent) {
        log.debug("Checking if command /${event.interaction.invokedCommandName} exists in tree...")

        val command = extension.applicationCommands.firstOrNull {
            it.info.name == event.interaction.invokedCommandName
        }

        if (command == null) {
            log.debug("Cannot find top-level command with name /${event.interaction.invokedCommandName}! Skipping...")
            return
        }

        // val optionsMap = mutableMapOf<CommandOption<*>, Any?>()
        println(event.interaction.data)
    }
}
