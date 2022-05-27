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
import dev.kord.common.DiscordBitSet
import dev.kord.common.Locale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import sh.nino.towa.slash.commands.annotations.ApplicationCommand
import sh.nino.towa.slash.commands.annotations.DeferEphemeral
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents the base class for executing slash commands.
 */
abstract class AbstractApplicationCommand {
    private val options = mutableListOf<CommandOption<*>>()
    private val log by logging<AbstractApplicationCommand>()

    /**
     * Returns the list of localisations for this command's description, mapped by
     * a [Locale].
     */
    private val descriptionLocalisations: MutableMap<Locale, String> = mutableMapOf()

    /**
     * Returns the list of localisations for this command's name, mapped by
     * a [Locale].
     */
    private val nameLocalisations: MutableMap<Locale, String> = mutableMapOf()

    /**
     * Returns the list of subcommands connected to this [AbstractApplicationCommand].
     */
    private val subcommandsList = mutableListOf<ApplicationSubcommand>()

    /**
     * Returns the list of subcommand groups connected to this top-level [AbstractApplicationCommand].
     */
    private val groupsList = mutableListOf<ApplicationSubcommandGroup>()

    /**
     * If this command should be deferred ephemerally.
     */
    val shouldDeferEphemeral: Boolean = this::class.findAnnotation<DeferEphemeral>()?.let { true } ?: false

    /**
     * Returns the metadata about this [command][AbstractApplicationCommand].
     */
    val info: ApplicationCommand = this::class.findAnnotation() ?: error("Missing `@ApplicationCommand` annotation!")

    init {
        log.debug("Finding options in declared member properties in this class!")
        val properties = this::class.declaredMemberProperties.filter {
            it.returnType.jvmErasure.java.isAssignableFrom(CommandOption::class.java)
        }

        log.debug("Found ${properties.size} properties that are command options for command ${info.name}!")
        for (prop in properties) {
            val result = prop.call(this) as? CommandOption<*> ?: continue

            log.debug("  | -> Option ${result.name} - ${result.description}")
            options.add(result)
        }
    }

    /**
     * Adds a localisation of this subcommand group's name.
     * @param locale The [Locale] object to use for the localisation.
     * @param name The name of the subcommand group in that locale.
     * @return This [command][AbstractApplicationCommand] to chain methods.
     */
    fun localiseName(locale: Locale, name: String): AbstractApplicationCommand {
        this.nameLocalisations.computeIfAbsent(locale) { name }
        return this
    }

    /**
     * Adds a localisation of this subcommand group's description.
     * @param locale The [Locale] object to use for the localisation.
     * @param description The description of the subcommand group in that locale.
     * @return This [command][AbstractApplicationCommand] to chain methods.
     */
    fun localiseDescription(locale: Locale, description: String): AbstractApplicationCommand {
        this.descriptionLocalisations.computeIfAbsent(locale) { description }
        return this
    }

    /**
     * Returns this [command][AbstractApplicationCommand] as a raw request object.
     */
    fun toRequest(): ApplicationCommandCreateRequest = ApplicationCommandCreateRequest(
        info.name,
        Optional.invoke(nameLocalisations.toMap()),
        ApplicationCommandType.ChatInput,
        Optional.invoke(info.description),
        Optional.invoke(descriptionLocalisations.toMap()),
        Optional.invoke(options.map { it.toKordBuilder().toRequest() } + subcommandsList.map { it.toRawValue() } + groupsList.map { it.toRawValue() }),
        Optional.invoke(Permissions.PermissionsBuilder(DiscordBitSet(info.defaultMemberPermissions)).permissions()),
        OptionalBoolean.Value(info.dmPermission),
        OptionalBoolean.Value(false)
    )

    /**
     * Executes the command.
     * @param context The command's context.
     */
    abstract suspend fun execute(context: ApplicationCommandContext)
}
