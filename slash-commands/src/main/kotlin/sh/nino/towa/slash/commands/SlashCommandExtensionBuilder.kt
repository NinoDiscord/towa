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

import sh.nino.towa.core.TowaDsl
import sh.nino.towa.core.locator.Locator
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.extensions.locateApplicationCommands
import sh.nino.towa.slash.commands.extensions.locateMessageCommands
import sh.nino.towa.slash.commands.extensions.locateUserContextCommands
import sh.nino.towa.slash.commands.message.MessageCommand
import sh.nino.towa.slash.commands.user.UserContextCommand

typealias SlashCommandExtensionInit = SlashCommandExtension.() -> Unit

/**
 * Represents the configuration for configuring the [SlashCommandExtension].
 */
data class SlashCommandExtensionConfiguration(
    val onInitLifecycleEvents: List<SlashCommandExtensionInit>,
    val shouldErrorIfOptionIsMissing: Boolean = false,
    val applicationCommands: List<AbstractApplicationCommand>,
    val messageCommands: List<MessageCommand>,
    val userCommands: List<UserContextCommand>,
    val devGuildId: Long? = null
)

/**
 * Represents the builder to create a [SlashCommandExtensionConfiguration] object.
 */
@TowaDsl
class SlashCommandExtensionBuilder {
    private val initLifecycleEvents: MutableList<SlashCommandExtensionInit> = mutableListOf()
    private val userContextCommands: MutableList<UserContextCommand> = mutableListOf()
    private val messageCommands: MutableList<MessageCommand> = mutableListOf()
    private val appCommands: MutableList<AbstractApplicationCommand> = mutableListOf()
    private var locator: Locator? = null

    /**
     * If the application command handler should treat required, but missing
     * options as errors before it executes your command code, so you don't
     * need to have explicit errors.
     */
    var shouldErrorIfOptionIsMissing: Boolean = false

    /**
     * The development guild ID to test your slash commands before it hits production. This is useful
     * for debugging your slash commands in a dedicated server rather than all guilds the bot is
     * present in.
     */
    var devGuildId: Long? = null

    /**
     * Appends a new [Locator] to this builder to locate application, message,
     * and user context commands that is usable.
     *
     * @param locator The locator object to use.
     * @return This [builder][SlashCommandExtensionBuilder] for chaining methods.
     */
    fun <T: Locator> useLocator(locator: T): SlashCommandExtensionBuilder {
        if (this.locator != null)
            throw IllegalStateException("Cannot override locator if it was already set!")

        this.locator = locator
        return this
    }

    /**
     * Adds an array of application commands to this [builder][SlashCommandExtensionBuilder]. This is useful
     * if you don't use dependency injection, or you have under <10 commands.
     *
     * @param commands The commands to register
     * @return This [builder][SlashCommandExtensionBuilder] to chain methods.
     */
    fun addApplicationCommands(vararg commands: AbstractApplicationCommand): SlashCommandExtensionBuilder {
        for (command in commands) {
            addApplicationCommand(command)
        }

        return this
    }

    /**
     * Adds a single application command to this [builder][SlashCommandExtensionBuilder]. This is useful
     * if you don't really use dependency injection.
     *
     * @param command The commands to register
     * @throws IllegalStateException If the command was already registered in this [builder][SlashCommandExtensionBuilder].
     * @return This [builder][SlashCommandExtensionBuilder] to chain methods.
     */
    fun addApplicationCommand(command: AbstractApplicationCommand): SlashCommandExtensionBuilder {
        if (appCommands.contains(command))
            throw IllegalStateException("Cannot register the same command twice.")

        appCommands.add(command)
        return this
    }

    fun addUserContextCommand(command: UserContextCommand): SlashCommandExtensionBuilder {
        if (userContextCommands.contains(command))
            throw IllegalStateException("Cannot register the same command twice.")

        userContextCommands.add(command)
        return this
    }

    fun addUserContextCommands(vararg commands: UserContextCommand): SlashCommandExtensionBuilder {
        for (command in commands) {
            addUserContextCommand(command)
        }

        return this
    }

    fun addMessageCommand(command: MessageCommand): SlashCommandExtensionBuilder {
        if (messageCommands.contains(command))
            throw IllegalStateException("Cannot register the same command twice.")

        messageCommands.add(command)
        return this
    }

    fun addMessageCommands(vararg commands: MessageCommand): SlashCommandExtensionBuilder {
        for (command in commands) {
            addMessageCommand(command)
        }

        return this
    }

    fun onInitLifecycle(init: SlashCommandExtensionInit): SlashCommandExtensionBuilder {
        initLifecycleEvents += init
        return this
    }

    /**
     * Creates a new [SlashCommandExtensionConfiguration] with the given
     * builder configuration.
     */
    fun build(): SlashCommandExtensionConfiguration {
        val allAppCommands = (locator?.locateApplicationCommands() ?: emptyList()) + appCommands
        val allMessageCommands = (locator?.locateMessageCommands() ?: emptyList()) + messageCommands
        val allUserContextCommands = (locator?.locateUserContextCommands() ?: emptyList()) + userContextCommands

        return SlashCommandExtensionConfiguration(
            initLifecycleEvents.toList(),
            shouldErrorIfOptionIsMissing,
            allAppCommands,
            allMessageCommands,
            allUserContextCommands,
            devGuildId
        )
    }
}
