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
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.ComponentType
import dev.kord.common.entity.InteractionType
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.interaction.*
import dev.kord.core.on
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import kotlinx.coroutines.Job
import sh.nino.towa.core.Towa
import sh.nino.towa.core.TowaBuilder
import sh.nino.towa.core.annotations.InjectKord
import sh.nino.towa.core.extensionOrNull
import sh.nino.towa.core.extensions.AbstractExtension
import sh.nino.towa.core.pipeline.PipelineContext
import sh.nino.towa.core.pipeline.PipelineEvent
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.application.ApplicationCommandHandler
import sh.nino.towa.slash.commands.events.application.ApplicationCommandPipelineContext
import sh.nino.towa.slash.commands.events.application.ApplicationCommandRegistered
import sh.nino.towa.slash.commands.events.message.MessageCommandPipelineContext
import sh.nino.towa.slash.commands.events.message.MessageCommandRegistered
import sh.nino.towa.slash.commands.events.user.UserContextCommandRegistered
import sh.nino.towa.slash.commands.events.user.UserContextPipelineContext
import sh.nino.towa.slash.commands.extensions.name
import sh.nino.towa.slash.commands.interactables.AutocompleteManager
import sh.nino.towa.slash.commands.interactables.ButtonInteractableManager
import sh.nino.towa.slash.commands.interactables.ModalInteractableManager
import sh.nino.towa.slash.commands.interactables.SelectMenuInteractableManager
import sh.nino.towa.slash.commands.message.MessageCommand
import sh.nino.towa.slash.commands.message.MessageCommandHandler
import sh.nino.towa.slash.commands.user.UserContextCommand
import sh.nino.towa.slash.commands.user.UserContextHandler
import java.util.concurrent.CancellationException
import kotlin.reflect.*

class SlashCommandExtension(internal val config: SlashCommandExtensionConfiguration): AbstractExtension() {
    @InjectKord
    internal lateinit var kord: Kord

    private val selectMenuInteractableManager = SelectMenuInteractableManager(this)
    private val applicationCommandHandler = ApplicationCommandHandler(this)
    private val buttonInteractableManager = ButtonInteractableManager(this)
    private val messageCommandHandler = MessageCommandHandler(this)
    private val autocompleteManager = AutocompleteManager(this)
    private val modalSubmitManager = ModalInteractableManager(this)
    private val userContextHandler = UserContextHandler(this)
    private val eventJob: Job? = null
    private val log by logging<SlashCommandExtension>()

    init {
        runInitLifecycleEvents()
    }

    /**
     * Returns the key of this extension.
     */
    override val key: String = SLASH_COMMANDS_EXTENSION_KEY

    /**
     * Represents the pipeline to intercept events.
     */
    val pipeline: SlashCommandPipeline = SlashCommandPipeline()

    private fun runInitLifecycleEvents() {
        for (init in config.onInitLifecycleEvents) {
            this.init()
        }
    }

    /**
     * Loads the extension if `Towa.start` was called.
     */
    override suspend fun load() {
        log.info("Registering ${applicationCommands.size} application commands, ${messageCommands.size} message commands, and ${userContextCommands.size} user context commands")

        if (config.devGuildId != null) {
            log.debug("Registering all application commands in dev guild [${config.devGuildId}]. If you want the commands to respect the `onlyGuildsIn` property in @SlashCommand, or as global commands, then do not set the devGuildId property!")
            val requests = mutableListOf<ApplicationCommandCreateRequest>()
            for (command in userContextCommands) {
                val context = UserContextPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(UserContextCommandRegistered::class, context)
                requests.add(command.toRequest())
            }

            for (command in messageCommands) {
                val context = MessageCommandPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(MessageCommandRegistered::class, context)
                requests.add(command.toRequest())
            }

            for (command in applicationCommands) {
                val context = ApplicationCommandPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(ApplicationCommandRegistered::class, context)
                command.registerOptions()

                requests.add(command.toRequest())
            }

            kord.rest.interaction.createGuildApplicationCommands(
                kord.selfId,
                Snowflake(config.devGuildId),
                requests
            )
        } else {
            val globalRequests = mutableListOf<ApplicationCommandCreateRequest>()
            val guildSpecificRequests = mutableMapOf<Snowflake, ApplicationCommandCreateRequest>()

            for (command in userContextCommands) {
                if (command.onlyInGuilds.isEmpty()) {
                    log.debug("User context command [${command.name}] is represented as a global command since it didn't add the @OnlyInGuilds annotation!")

                    val context = UserContextPipelineContext(pipeline)
                    context.attributes["command"] = command

                    pipeline.emit(UserContextCommandRegistered::class, context)
                    globalRequests.add(command.toRequest())
                    continue
                }

                log.debug("User context command [${command.name}] is a guild command and will be registered in guilds [${command.onlyInGuilds.joinToString(", ")}]")
                for (guildId in command.onlyInGuilds) {
                    guildSpecificRequests[Snowflake(guildId)] = command.toRequest()
                }

                val context = UserContextPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(UserContextCommandRegistered::class, context)
            }

            for (command in messageCommands) {
                if (command.onlyInGuilds.isEmpty()) {
                    log.debug("Message command [${command.name}] is represented as a global command since it didn't add the @OnlyInGuilds annotation!")

                    val context = MessageCommandPipelineContext(pipeline)
                    context.attributes["command"] = command

                    pipeline.emit(MessageCommandRegistered::class, context)
                    globalRequests.add(command.toRequest())
                    continue
                }

                log.debug("Message command [${command.name}] is a guild command and will be registered in guilds [${command.onlyInGuilds.joinToString(", ")}]")
                for (guildId in command.onlyInGuilds) {
                    guildSpecificRequests[Snowflake(guildId)] = command.toRequest()
                }

                val context = MessageCommandPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(MessageCommandRegistered::class, context)
            }

            for (command in applicationCommands) {
                if (command.onlyInGuilds.isEmpty()) {
                    log.debug("Application command [${command.info.name}] is represented as a global command since it didn't add the @OnlyInGuilds annotation!")

                    val context = ApplicationCommandPipelineContext(pipeline)
                    context.attributes["command"] = command

                    pipeline.emit(ApplicationCommandRegistered::class, context)
                    globalRequests.add(command.toRequest())
                    continue
                }

                for (guildId in command.onlyInGuilds) {
                    command.registerOptions()
                    guildSpecificRequests[Snowflake(guildId)] = command.toRequest()
                }

                val context = ApplicationCommandPipelineContext(pipeline)
                context.attributes["command"] = command

                pipeline.emit(ApplicationCommandRegistered::class, context)
            }
        }

        kord.on<InteractionCreateEvent> {
            onInteraction(this)
        }

        log.info("Successfully initialized slash commands extension! I'll do all the heavy lifting~")
    }

    override suspend fun unload() {
        log.info("Shutting down slash commands extension...")

        eventJob?.cancel(CancellationException("Told by SlashCommandExtension#unload()"))
        log.warn("Done! :(")
    }

    private suspend fun onInteraction(event: InteractionCreateEvent) {
        log.debug("Received interaction [${event.interaction.type.name}]")

        when (event.interaction.type) {
            InteractionType.ApplicationCommand -> {
                log.debug("Received application command type ${event.interaction.data.data.type.value!!.name}")
                val ev = event as ApplicationCommandInteractionCreateEvent
                when (ev.interaction.invokedCommandType) {
                    ApplicationCommandType.User -> userContextHandler.onApplicationCommand(ev)
                    ApplicationCommandType.Message -> messageCommandHandler.onApplicationCommand(ev)
                    ApplicationCommandType.ChatInput -> applicationCommandHandler.onApplicationCommand(ev)
                    else -> log.debug("Application command type [${event.interaction.data.data.type.value!!.name}] is not supported at this given moment.")
                }
            }

            InteractionType.AutoComplete -> {
                log.debug("Received autocomplete request!")
                autocompleteManager.manage(event as AutoCompleteInteractionCreateEvent)
            }

            InteractionType.ModalSubmit -> {
                log.debug("Received modal submit request!")
                modalSubmitManager.manage(event as ModalSubmitInteractionCreateEvent)
            }

            InteractionType.Component -> {
                val ev = event as ComponentInteractionCreateEvent

                log.debug("Received component interactable event [${ev.interaction.componentType.name}]")
                when (ev.interaction.componentType) {
                    is ComponentType.SelectMenu -> selectMenuInteractableManager.manage(ev as SelectMenuInteractionCreateEvent)
                    is ComponentType.Button -> buttonInteractableManager.manage(ev as ButtonInteractionCreateEvent)
                    else -> log.debug("Component type [${ev.interaction.componentType.name}] is not supported.")
                }
            }

            else -> {} // do nothing
        }
    }

    companion object {
        const val SLASH_COMMANDS_EXTENSION_KEY: String = "towa:slash:commands"
    }
}

/**
 * Returns this [extension][SlashCommandExtension] if it was properly loaded, or
 * `null` if nothing was found.
 */
val Towa.slashCommands: SlashCommandExtension?
    get() = extensionOrNull(SlashCommandExtension.SLASH_COMMANDS_EXTENSION_KEY)

/**
 * Creates and registers the [SlashCommandExtension].
 * @param builder The builder object to use.
 */
fun TowaBuilder.useSlashCommands(builder: SlashCommandExtensionBuilder.() -> Unit) {
    if (extensions.containsKey(SlashCommandExtension.SLASH_COMMANDS_EXTENSION_KEY))
        throw IllegalStateException("You already registered the slash commands extension!")

    register(SlashCommandExtension(SlashCommandExtensionBuilder().apply(builder).build()))
}

/**
 * Adds an interceptor point to a specific [event][E] and will be emitted
 * when the event is fired.
 *
 * @param event The [KClass] for the event.
 * @param interceptor The interceptor logic.
 */
fun <C: PipelineContext<SlashCommandPipeline>, E: PipelineEvent<SlashCommandPipeline, C>> SlashCommandExtension.intercept(
    event: KClass<E>,
    interceptor: suspend C.() -> Unit
): SlashCommandPipeline = pipeline.intercept(event, interceptor) as SlashCommandPipeline

/**
 * Adds an interceptor point to a specific [event][E] and will be emitted
 * when the event is fired.
 *
 * @param interceptor The interceptor logic.
 */
inline fun <C: PipelineContext<SlashCommandPipeline>, reified E: PipelineEvent<SlashCommandPipeline, C>> SlashCommandExtension.intercept(
    noinline interceptor: suspend C.() -> Unit
): SlashCommandPipeline = pipeline.intercept(E::class, interceptor) as SlashCommandPipeline

/**
 * Returns all the application commands registered from the [SlashCommandExtensionBuilder].
 */
val SlashCommandExtension.applicationCommands: List<AbstractApplicationCommand>
    get() = config.applicationCommands

/**
 * Returns all the message commands registered from the [SlashCommandExtensionBuilder].
 */
val SlashCommandExtension.messageCommands: List<MessageCommand>
    get() = config.messageCommands

/**
 * Returns all the user context commands registered from the [SlashCommandExtensionBuilder].
 */
val SlashCommandExtension.userContextCommands: List<UserContextCommand>
    get() = config.userCommands
