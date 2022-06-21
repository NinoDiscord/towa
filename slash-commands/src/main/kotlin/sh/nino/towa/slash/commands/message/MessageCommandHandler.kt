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

package sh.nino.towa.slash.commands.message

import dev.floofy.utils.slf4j.logging
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import sh.nino.towa.slash.commands.SlashCommandExtension
import sh.nino.towa.slash.commands.events.message.*

internal class MessageCommandHandler(private val extension: SlashCommandExtension) {
    private val log by logging<MessageCommandHandler>()

    suspend fun onApplicationCommand(event: ApplicationCommandInteractionCreateEvent) {
        val ev = event as MessageCommandInteractionCreateEvent

        log.debug("Received message command [${ev.interaction.invokedCommandName} (${ev.interaction.invokedCommandId})]")
        val command = extension.config.messageCommands.firstOrNull {
            it.name == ev.interaction.invokedCommandName
        }

        if (command == null) {
            val context = MessageCommandPipelineContext(extension.pipeline)
            extension.pipeline.emit(MessageCommandNotFound::class, context)

            return
        }

        val pipelineCtx = MessageCommandPipelineContext(extension.pipeline)
        pipelineCtx.attributes["command"] = command

        extension.pipeline.emit(MessageCommandPropagation::class, pipelineCtx)
        extension
            .kord
            .rest
            .interaction
            .deferMessage(event.interaction.id, event.interaction.token, command.deferEphemeral)

        val context = MessageCommandContext(ev)
        try {
            command.execute(context)
            extension.pipeline.emit(
                MessageCommandExecuted::class,
                pipelineCtx
            )
        } catch (e: Throwable) {
            pipelineCtx.attributes["exception"] = e
            extension.pipeline.emit(
                MessageCommandException::class,
                pipelineCtx
            )
        }
    }
}
