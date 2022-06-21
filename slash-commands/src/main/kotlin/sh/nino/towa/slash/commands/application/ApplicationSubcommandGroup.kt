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

import dev.kord.common.Locale
import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional

/**
 * Creates a subcommand group with a builder DSL.
 * @param name The name of the subcommand group
 * @param description The description of the subcommand group.
 * @return The [ApplicationSubcommandGroup] that was created.
 */
fun ApplicationSubcommandGroup(name: String, description: String, builder: ApplicationSubcommandGroup.() -> Unit = {}): ApplicationSubcommandGroup =
    ApplicationSubcommandGroup(name, description).apply(builder)

/**
 * Represents a subcommand group, which can have nested subcommands and groups.
 * @param name The name of the subcommand group
 * @param description The description of the subcommand group.
 */
class ApplicationSubcommandGroup(val name: String, val description: String) {
    private val descriptionLocalisations = mutableMapOf<Locale, String>()
    private val nameLocalisations = mutableMapOf<Locale, String>()
    internal val subcommands = mutableListOf<AbstractApplicationSubcommand>()
    private val groups = mutableListOf<ApplicationSubcommandGroup>()

    /**
     * Appends multiple subcommand objects to this subcommand group.
     * @param subcommands The subcommands to insert.
     * @return This [subcommand group][ApplicationSubcommandGroup] to chain methods.
     */
    fun addSubcommands(vararg subcommands: AbstractApplicationSubcommand): ApplicationSubcommandGroup {
        for (subcommand in subcommands) addSubcommand(subcommand)

        return this
    }

    /**
     * Appends a singular subcommand into this subcommand group
     * @param subcommand The subcommand to insert.
     * @return This [subcommand group][ApplicationSubcommandGroup] to chain methods.
     */
    fun addSubcommand(subcommand: AbstractApplicationSubcommand): ApplicationSubcommandGroup {
        this.subcommands.add(subcommand)
        return this
    }

    /**
     * Adds a nested group to this subcommand group
     * @param name The name of the newly, nested subcommand group
     * @param description The description of the newly, nested subcommand group.
     * @param builder The builder DSL block to add more subcommands!
     * @return This [subcommand group][ApplicationSubcommandGroup] to chain methods.
     */
    fun addGroup(name: String, description: String, builder: ApplicationSubcommandGroup.() -> Unit = {}): ApplicationSubcommandGroup {
        groups.add(ApplicationSubcommandGroup(name, description).apply(builder))
        return this
    }

    /**
     * Adds a localisation of this subcommand group's name.
     * @param locale The [Locale] object to use for the localisation.
     * @param name The name of the subcommand group in that locale.
     * @return This [subcommand group][ApplicationSubcommandGroup] to chain methods.
     */
    fun localiseName(locale: Locale, name: String): ApplicationSubcommandGroup {
        this.nameLocalisations.computeIfAbsent(locale) { name }
        return this
    }

    /**
     * Adds a localisation of this subcommand group's description.
     * @param locale The [Locale] object to use for the localisation.
     * @param description The description of the subcommand group in that locale.
     * @return This [subcommand group][ApplicationSubcommandGroup] to chain methods.
     */
    fun localiseDescription(locale: Locale, description: String): ApplicationSubcommandGroup {
        this.descriptionLocalisations.computeIfAbsent(locale) { description }
        return this
    }

    /**
     * Returns the raw value of this subcommand group.
     */
    fun toRawValue(): ApplicationCommandOption = ApplicationCommandOption(
        ApplicationCommandOptionType.SubCommandGroup,
        name,
        Optional.invoke(nameLocalisations),
        description,
        Optional.invoke(descriptionLocalisations),
        options = Optional.invoke(subcommands.map { it.toRawValue() } + groups.map { it.toRawValue() })
    )
}
