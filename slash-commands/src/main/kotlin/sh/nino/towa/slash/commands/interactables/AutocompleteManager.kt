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

package sh.nino.towa.slash.commands.interactables

import dev.floofy.utils.slf4j.logging
import dev.kord.common.entity.optional.value
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import sh.nino.towa.slash.commands.SlashCommandExtension
import sh.nino.towa.slash.commands.annotations.AutocompleteAction
import sh.nino.towa.slash.commands.application.StringCommandOption
import sh.nino.towa.slash.commands.applicationCommands
import sh.nino.towa.slash.commands.events.application.ApplicationCommandNotFound
import sh.nino.towa.slash.commands.events.application.ApplicationCommandPipelineContext
import sh.nino.towa.slash.commands.events.interactables.autocomplete.AutocompleteFunctionNotFound
import sh.nino.towa.slash.commands.events.interactables.autocomplete.AutocompletePipelineContext
// import sh.nino.towa.slash.commands.interactables.context.AutocompleteContext
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation

internal class AutocompleteManager(private val extension: SlashCommandExtension) {
    private val log by logging<AutocompleteManager>()

    suspend fun manage(event: AutoCompleteInteractionCreateEvent) {
        val commandName = event.interaction.data.data.name.value!!
        log.info("Received autocomplete request for slash command /$commandName")

        val command = extension.applicationCommands.firstOrNull {
            it.info.name == commandName
        }

        if (command == null) {
            val context = ApplicationCommandPipelineContext(extension.pipeline)
            extension.pipeline.emit(ApplicationCommandNotFound::class, context)

            return
        }

        val focusedOption = event.interaction.data.data.options.value!!.singleOrNull {
            it.focused.value == true
        }

        val context = AutocompletePipelineContext(extension.pipeline)
        if (focusedOption == null) {
            extension.pipeline.emit(AutocompleteFunctionNotFound::class, context)
            return
        }

        context.attributes["option:name"] = focusedOption.name
        context.attributes["option:value"] = focusedOption.value

        val towaOption = command.options.singleOrNull {
            it is StringCommandOption && it.autocompleteActionName == focusedOption.name
        }

        if (towaOption == null) {
            extension.pipeline.emit(AutocompleteFunctionNotFound::class, context)
            return
        }

        val autocompleteMethod = command::class.declaredMemberFunctions.singleOrNull { it.hasAnnotation<AutocompleteAction>() }
        if (autocompleteMethod == null) {
            extension.pipeline.emit(AutocompleteFunctionNotFound::class, context)
            return
        }

//        try {
//            val autocompleteContext = AutocompleteContext(event, towaOption as StringCommandOption)
//
//            extension
//                .kord
//                .rest
//                .interaction
//                .deferMessage(event.interaction.id, event.interaction.token, false)
//
//            autocompleteMethod.call(command, autocompleteContext)
//            extension.pipeline.emit(AutocompleteExecute::class, context)
//        } catch (e: Throwable) {
//            context.attributes["exception"] = e
//            extension.pipeline.emit(AutocompleteException::class, context)
//        }
    }
}
