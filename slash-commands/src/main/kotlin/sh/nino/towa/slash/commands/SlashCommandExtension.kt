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

package sh.nino.towa.slash.commands

import dev.floofy.utils.slf4j.logging
import dev.kord.core.Kord
import sh.nino.towa.core.Towa
import sh.nino.towa.core.extension
import sh.nino.towa.core.extensions.AbstractExtension
import sh.nino.towa.slash.commands.locator.ILocator
import sh.nino.towa.slash.commands.message.MessageCommand

/**
 * Represents the [extension][AbstractExtension] that is used to control the
 */
class SlashCommandExtension(configuration: SlashCommandsConfiguration, private val kord: Kord): AbstractExtension() {
    /**
     * Returns all the registered message commands.
     */
    internal val registeredMessageCommands: MutableList<MessageCommand> = mutableListOf()
    private val log by logging<SlashCommandExtension>()

    /**
     * Returns the locator object to load in commands.
     */
    val locator: ILocator = configuration.locator

    companion object {
        const val KEY_NAME: String = "towa.slash.commands"
    }

    /**
     * Returns the key of this extension.
     */
    override val key: String = KEY_NAME

    /**
     * Loads the extension if `Towa.start` was called.
     */
    override suspend fun load() {
        kord.createGlobalApplicationCommands {
            input("owo", "uwu") {
            }
        }
    }

    /**
     * Unloads this extension, if we can.
     */
    override suspend fun unload() {
    }
}

/**
 * Registers the slash commands extension to this Towa builder.
 * @param configure The configuration builder to configure slash command use.
 */
fun Towa.useSlashCommands(configure: SlashCommandConfigBuilder.() -> Unit = {}) {
    val config = SlashCommandConfigBuilder().apply(configure).build()
    register(SlashCommandExtension(config, kord))
}

/**
 * Retrieves this [SlashCommandExtension] from the current Towa object.
 * @throws IllegalStateException If the extension wasn't loaded.
 */
val Towa.slashCommands: SlashCommandExtension
    get() = extension(SlashCommandExtension.KEY_NAME)

/**
 * Returns all the registered message commands.
 */
val SlashCommandExtension.messageCommands: List<MessageCommand>
    get() = registeredMessageCommands.toList()
