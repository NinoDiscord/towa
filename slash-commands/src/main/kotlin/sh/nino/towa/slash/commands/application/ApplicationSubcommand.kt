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
import dev.kord.common.Locale
import dev.kord.common.entity.ApplicationCommandOption
import dev.kord.common.entity.ApplicationCommandOptionType
import dev.kord.common.entity.optional.Optional
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents a subcommand within an [AbstractApplicationCommand].
 * @param name The name of the subcommand, 1-32 chars in length.
 * @param description The description of the subcommand, 1-32 chars in length.
 * @param options The options that will be registered.
 */
abstract class ApplicationSubcommand(
    val name: String,
    val description: String,
    private val options: MutableList<CommandOption<*>> = mutableListOf()
) {
    private val descriptionLocalisations = mutableMapOf<Locale, String>()
    private val nameLocalisations = mutableMapOf<Locale, String>()
    private val log by logging<ApplicationSubcommand>()

    init {
        val properties = this::class.declaredMemberProperties.filter {
            it.returnType.jvmErasure.java.isAssignableFrom(CommandOption::class.java)
        }

        log.debug("Found ${properties.size} properties that are command options for subcommand $name!")
        for (prop in properties) {
            val result = prop.call(this) as? CommandOption<*> ?: continue
            options.add(result)
        }
    }

    /**
     * Adds a localisation of this subcommand's name.
     * @param locale The [Locale] object to use for the localisation.
     * @param name The name of the subcommand group in that locale.
     * @return This [subcommand][ApplicationSubcommand] to chain methods.
     */
    fun localiseName(locale: Locale, name: String): ApplicationSubcommand {
        this.nameLocalisations.computeIfAbsent(locale) { name }
        return this
    }

    /**
     * Adds a localisation of this subcommand group's description.
     * @param locale The [Locale] object to use for the localisation.
     * @param description The description of the subcommand group in that locale.
     * @return This [subcommand][ApplicationSubcommand] to chain methods.
     */
    fun localiseDescription(locale: Locale, description: String): ApplicationSubcommand {
        this.descriptionLocalisations.computeIfAbsent(locale) { description }
        return this
    }

    /**
     * Returns the raw value of this subcommand.
     */
    fun toRawValue(): ApplicationCommandOption = ApplicationCommandOption(
        ApplicationCommandOptionType.SubCommand,
        name,
        Optional.invoke(null),
        description,
        options = Optional.invoke(options.map { it.toKordBuilder().toRequest() })
    )

    /**
     * Executes this subcommand
     * @param context The context object.
     */
    abstract suspend fun execute(context: ApplicationCommandContext)
}
