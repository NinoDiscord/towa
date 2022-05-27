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
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import io.ktor.http.*
import sh.nino.towa.core.Towa
import sh.nino.towa.core.extension
import sh.nino.towa.core.extensions.AbstractExtension
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.application.ApplicationCommandHandler
import sh.nino.towa.slash.commands.locator.ILocator

/**
 * Represents the [extension][AbstractExtension] that is used to control the
 */
class SlashCommandExtension(private val configuration: SlashCommandsConfiguration, private val kord: Kord): AbstractExtension() {
    private val log by logging<SlashCommandExtension>()

    internal var applicationCommands: MutableList<AbstractApplicationCommand> = mutableListOf()

    /**
     * Represents the handler for executing application commands.
     */
    val applicationCommandHandler = ApplicationCommandHandler(this, kord)

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
        log.info("Loading all application commands...")

        val appCommands = locator.findCommands()
        log.info("Found ${appCommands.size} application commands to register!")

        if (configuration.devServerId != null) {
            log.debug("Found development server ID pointing to ${configuration.devServerId}!")
            for (command in appCommands) {
                log.debug("   | -> Found command /${command.info.name} - ${command.info.description}!")

                for (id in (command.info.useInGuilds + configuration.devServerId)) {
                    kord.rest.interaction.createGuildApplicationCommand(
                        kord.selfId,
                        Snowflake(id),
                        command.toRequest()
                    )

                    log.debug("   | -> Registered command /${command.info.name} - ${command.info.description} to guild ID $id!")
                }
            }
        } else {
            log.debug("Creating global application commands...")
            for (command in appCommands) {
                log.debug("   | -> Found command /${command.info.name} - ${command.info.description}!")

                if (command.info.useInGuilds.isNotEmpty()) {
                    log.debug("   | -> Command ${command.info.name} has a list of guilds (${command.info.useInGuilds.joinToString(", ")}) it needs to be registered in!")
                    for (id in command.info.useInGuilds) {
                        kord.rest.interaction.createGuildApplicationCommand(
                            kord.selfId,
                            Snowflake(id),
                            command.toRequest()
                        )

                        log.info("   | -> Registered command /${command.info.name} - ${command.info.description} to guild ID $id!")
                    }
                } else {
                    log.debug("   | -> Command ${command.info.name} is a global command!")
                    kord.rest.interaction.createGlobalApplicationCommand(
                        kord.selfId,
                        command.toRequest()
                    )
                }
            }
        }

        applicationCommands = appCommands.toMutableList()
        log.info("Done!")
    }

    /**
     * Unloads this extension, if we can.
     */
    override suspend fun unload() {
        log.warn("Destroying handlers...")
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
