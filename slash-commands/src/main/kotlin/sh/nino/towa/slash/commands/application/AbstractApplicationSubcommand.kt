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
import sh.nino.towa.slash.commands.annotations.DeferEphemeral
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Represents a subcommand within an [AbstractApplicationCommand].
 * @param name The name of the subcommand, 1-32 chars in length.
 * @param description The description of the subcommand, 1-32 chars in length.
 */
abstract class AbstractApplicationSubcommand(val name: String, val description: String): Executable {
    internal val options: MutableList<CommandOption<*>> = mutableListOf()
    private val descriptionLocalisations = mutableMapOf<Locale, String>()
    private val nameLocalisations = mutableMapOf<Locale, String>()

    val deferEphemeral: Boolean = this::class.hasAnnotation<DeferEphemeral>()

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
     * Adds a localisation of this subcommand's name.
     * @param locale The [Locale] object to use for the localisation.
     * @param name The name of the subcommand group in that locale.
     * @return This [subcommand][AbstractApplicationSubcommand] to chain methods.
     */
    fun localiseName(locale: Locale, name: String): AbstractApplicationSubcommand {
        this.nameLocalisations.computeIfAbsent(locale) { name }
        return this
    }

    /**
     * Adds a localisation of this subcommand group's description.
     * @param locale The [Locale] object to use for the localisation.
     * @param description The description of the subcommand group in that locale.
     * @return This [subcommand][AbstractApplicationSubcommand] to chain methods.
     */
    fun localiseDescription(locale: Locale, description: String): AbstractApplicationSubcommand {
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
}
