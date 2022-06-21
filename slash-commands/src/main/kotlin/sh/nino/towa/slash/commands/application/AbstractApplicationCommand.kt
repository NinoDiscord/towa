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

import dev.kord.common.DiscordBitSet
import dev.kord.common.Locale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import sh.nino.towa.slash.commands.annotations.DeferEphemeral
import sh.nino.towa.slash.commands.annotations.OnlyInGuilds
import sh.nino.towa.slash.commands.annotations.SlashCommand
import sh.nino.towa.slash.commands.message.MessageCommand
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Represents the base class for executing slash commands.
 */
abstract class AbstractApplicationCommand: Executable {
    internal val options = mutableListOf<CommandOption<*>>()

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
    internal val subcommandsList = mutableListOf<AbstractApplicationSubcommand>()

    /**
     * Returns the list of subcommand groups connected to this top-level [AbstractApplicationCommand].
     */
    internal val groupsList = mutableListOf<ApplicationSubcommandGroup>()

    /**
     * Returns the list of guilds that this [MessageCommand] is only available in.
     */
    val onlyInGuilds: List<String> = this::class.findAnnotation<OnlyInGuilds>()?.guilds?.toList() ?: emptyList()

    /**
     * If this command should be deferred ephemerally.
     */
    val shouldDeferEphemeral: Boolean = this::class.findAnnotation<DeferEphemeral>()?.let { true } ?: false

    /**
     * Returns the metadata about this [command][AbstractApplicationCommand].
     */
    val info: SlashCommand = this::class.findAnnotation() ?: error("Missing `@SlashCommand` annotation!")

    internal fun registerOptions() {
        val properties = this::class.declaredMemberProperties.filter {
            it.returnType.isSubtypeOf(typeOf<CommandOption<*>>())
        }

        for (prop in properties) {
            prop.isAccessible = true

            val option = prop.call(this) as? CommandOption<*> ?: continue
            options.add(option)
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
     * Registers a subcommand into this [AbstractApplicationCommand].
     * @param subcommand The subcommand to register
     * @return This [command][AbstractApplicationCommand] to chain methods.
     */
    fun addSubcommand(subcommand: AbstractApplicationSubcommand): AbstractApplicationCommand {
        subcommand.registerOptions()
        this.subcommandsList.add(subcommand)
        return this
    }

    /**
     * Registers a list of subcommands into this [AbstractApplicationCommand].
     * @param subcommand The subcommands to register in bulk
     * @return This [command][AbstractApplicationCommand] to chain methods.
     */
    fun addSubcommands(vararg subcommands: AbstractApplicationSubcommand): AbstractApplicationCommand {
        for (subcommand in subcommands) addSubcommand(subcommand)

        return this
    }

    /**
     * Registers a subcommand group into this [AbstractApplicationCommand].
     * @param name The name of the subcommand group
     * @param description The description of the subcommand
     * @param group The group DSL builder to construct a [ApplicationSubcommandGroup].
     * @return This [command][AbstractApplicationCommand] to chain methods.
     */
    fun addSubcommandGroup(
        name: String,
        description: String,
        group: ApplicationSubcommandGroup.() -> Unit = {}
    ): AbstractApplicationCommand {
        groupsList.add(ApplicationSubcommandGroup(name, description, group))
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
        Optional.invoke(
            options.map {
                it.toKordBuilder().toRequest()
            } + subcommandsList.map {
                it.toRawValue()
            } + groupsList.map {
                it.toRawValue()
            }
        ),
        Optional.invoke(
            if (info.defaultMemberPermissions.isEmpty())
                Permissions()
            else
                Permissions.PermissionsBuilder(DiscordBitSet(info.defaultMemberPermissions)).permissions()
        ),
        OptionalBoolean.Value(info.dmPermission),
        OptionalBoolean.Value(false)
    )
}
