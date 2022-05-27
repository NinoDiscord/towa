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

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.interaction.InteractionCreateEvent

/**
 * Represents the context object of a top-level command or subcommand.
 * @param event The raw interaction event, which you can receive from [interaction].
 * @param options The raw options that was collected.
 * @param command The top-level command that this context object belongs to.
 * @param group The subcommand group object, if it was executed in one.
 * @param subcommand The subcommand that owns this context object, if it was executed as one.
 */
class ApplicationCommandContext(
    private val event: InteractionCreateEvent,
    private val options: Map<CommandOption<*>, Any?>,
    val command: AbstractApplicationCommand,
    val group: ApplicationSubcommandGroup? = null,
    val subcommand: ApplicationSubcommand? = null
) {
    /**
     * Returns the raw interaction object from the [InteractionCreateEvent].
     */
    val interaction: Interaction = event.interaction

    /**
     * Returns a [command option][CommandOption] by the option object.
     * @param key The option object to search through
     * @return The result casted to [U].
     * @throws IllegalStateException If the option was required and the result was `null`.
     */
    @Suppress("UNCHECKED_CAST")
    fun <U, T: CommandOption<U>> option(key: T): U {
        val result = options[key]
        if (result !is NullableOption && result == null)
            throw IllegalStateException("Required option with name ${key.name} was not provided.")

        return result as U
    }

    /**
     * Returns the [Guild] that this command was executed in. Returns `null`
     * if it was in a DM channel as a globally-scoped command.
     */
    suspend fun getGuild(): Guild? {
        if (event.interaction.data.guildId.value == null)
            return null

        return event.kord.getGuild(event.interaction.data.guildId.value!!)
    }

    /**
     * Returns the channel that this context was built upon, casted as [T].
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T: Channel> getChannel(): T? = event.kord.getChannel(event.interaction.data.channelId) as? T
}
