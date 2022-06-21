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
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.rest.builder.interaction.*
import kotlin.properties.ReadOnlyProperty

// [ == options == ]

/**
 * Represents an application command option.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
abstract class CommandOption<V>(
    val name: String,
    val description: String,
    val nameLocalizations: Map<Locale, String>,
    val descriptionLocalizations: Map<Locale, String>
) {
    /**
     * Returns the builder object to bulk create or update the slash commands.
     */
    abstract fun toKordBuilder(): OptionsBuilder

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is CommandOption<*>) return false

        return this.name == other.name
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
abstract class CommandOptionWithChoice<V, E: Any>(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    val choices: List<CommandChoice<E>>,
    val autocompleteActionName: String? = null
): CommandOption<V>(name, description, nameLocalizations, descriptionLocalizations)

/**
 * Represents an option that can be optional.
 */
interface NullableOption

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **String**.
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class StringCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<String>>,
    autocompleteActionName: String? = null
): CommandOptionWithChoice<String, String>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName) {
    override fun toKordBuilder(): OptionsBuilder = StringChoiceBuilder(name, description).apply {
        val self = this@StringCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        // `NullableOption` makes this optional.
        this.required = true
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **String?**
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class NullableStringCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<String>>,
    autocompleteActionName: String? = null
): CommandOptionWithChoice<String?, String>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = StringChoiceBuilder(name, description).apply {
        val self = this@NullableStringCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        // `NullableOption` makes this optional.
        this.required = false
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **Long**.
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class IntCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<Long>>,
    autocompleteActionName: String? = null,
    val min: Long? = null,
    val max: Long? = null
): CommandOptionWithChoice<Long, Long>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName) {
    override fun toKordBuilder(): OptionsBuilder = IntegerOptionBuilder(name, description).apply {
        val self = this@IntCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        if (min != null)
            this.minValue = min

        if (max != null)
            this.maxValue = max

        // `NullableOption` makes this optional.
        this.required = true
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **Long?**.
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class NullableIntCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<Long>>,
    autocompleteActionName: String? = null,
    val min: Long? = null,
    val max: Long? = null
): CommandOptionWithChoice<Long?, Long>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = IntegerOptionBuilder(name, description).apply {
        val self = this@NullableIntCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        if (min != null)
            this.minValue = min

        if (max != null)
            this.maxValue = max

        // `NullableOption` makes this optional.
        this.required = false
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **Double**.
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class NumberCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<Double>>,
    autocompleteActionName: String? = null
): CommandOptionWithChoice<Double, Double>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName) {
    override fun toKordBuilder(): OptionsBuilder = NumberOptionBuilder(name, description).apply {
        val self = this@NumberCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        // `NullableOption` makes this optional.
        this.required = true
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option that can have choices and autocompletion features,
 * but the value type is **Double?**.
 *
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 * @param choices The list of choices for this option.
 * @param autocompleteActionName The action name for autocompletion.
 */
class NullableNumberCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>,
    choices: List<CommandChoice<Double>>,
    autocompleteActionName: String? = null
): CommandOptionWithChoice<Double?, Double>(name, description, nameLocalizations, descriptionLocalizations, choices, autocompleteActionName), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = NumberOptionBuilder(name, description).apply {
        val self = this@NullableNumberCommandOption

        if (autocompleteActionName != null)
            this.autocomplete = true

        this.nameLocalizations = self.nameLocalizations.toMutableMap()
        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()

        // `NullableOption` makes this optional.
        this.required = false
        for (c in self.choices) {
            choice(c.name, c.value) {
                nameLocalizations = self.nameLocalizations.toMutableMap()
            }
        }
    }
}

/**
 * Represents an application command option which its type is **Boolean**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class BooleanCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Boolean>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun toKordBuilder(): OptionsBuilder = BooleanBuilder(name, description).apply {
        val self = this@BooleanCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = true
    }
}

/**
 * Represents an application command option which its type is **Boolean?**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class NullableBooleanCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Boolean?>(name, description, nameLocalizations, descriptionLocalizations), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = BooleanBuilder(name, description).apply {
        val self = this@NullableBooleanCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = false
    }
}

/**
 * Represents an application command option which its type is **[User]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class UserCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<User>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun toKordBuilder(): OptionsBuilder = UserBuilder(name, description).apply {
        val self = this@UserCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = true
    }
}

/**
 * Represents an application command option which its type is **[User?][User]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class NullableUserCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<User?>(name, description, nameLocalizations, descriptionLocalizations), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = UserBuilder(name, description).apply {
        val self = this@NullableUserCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = false
    }
}

/**
 * Represents an application command option which its type is **[Channel]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class ChannelCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Channel>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun toKordBuilder(): OptionsBuilder = ChannelBuilder(name, description).apply {
        val self = this@ChannelCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = true
    }
}

/**
 * Represents an application command option which its type is **[Channel?][Channel]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class NullableChannelCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Channel?>(name, description, nameLocalizations, descriptionLocalizations), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = ChannelBuilder(name, description).apply {
        val self = this@NullableChannelCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = false
    }
}

/**
 * Represents an application command option which its type is **[Role]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class RoleCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Role>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun toKordBuilder(): OptionsBuilder = RoleBuilder(name, description).apply {
        val self = this@RoleCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = true
    }
}

/**
 * Represents an application command option which its type is **[Role?][Role]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class NullableRoleCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Role?>(name, description, nameLocalizations, descriptionLocalizations), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = RoleBuilder(name, description).apply {
        val self = this@NullableRoleCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = false
    }
}

/**
 * Represents an application command option which its type is **[Attachment]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class AttachmentCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Attachment>(name, description, nameLocalizations, descriptionLocalizations) {
    override fun toKordBuilder(): OptionsBuilder = AttachmentBuilder(name, description).apply {
        val self = this@AttachmentCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = true
    }
}

/**
 * Represents an application command option which its type is **[Attachment?][Attachment]**.
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 * @param nameLocalizations A map of localisations for this option based off the user's [Locale].
 * @param descriptionLocalizations A map of localisations for this option based off the user's [Locale].
 */
class NullableAttachmentCommandOption(
    name: String,
    description: String,
    nameLocalizations: Map<Locale, String>,
    descriptionLocalizations: Map<Locale, String>
): CommandOption<Attachment?>(name, description, nameLocalizations, descriptionLocalizations), NullableOption {
    override fun toKordBuilder(): OptionsBuilder = AttachmentBuilder(name, description).apply {
        val self = this@NullableAttachmentCommandOption

        this.descriptionLocalizations = self.descriptionLocalizations.toMutableMap()
        this.nameLocalizations = self.descriptionLocalizations.toMutableMap()
        this.required = false
    }
}

// [ == builders == ]
/**
 * Represents a generic builder for constructing [command options][CommandOption].
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 */
sealed class CommandOptionBuilder<V>(val name: String, val description: String) {
    /**
     * Represents the localisations for the option name based off the user's [Locale].
     */
    internal val nameLocalizations: MutableMap<Locale, String> = mutableMapOf()

    /**
     * Represents the localisations for the option description based off the user's [Locale].
     */
    internal val descriptionLocalizations: MutableMap<Locale, String> = mutableMapOf()

    /**
     * Convenient method to localise this option's name based off the user's [Locale].
     * @param locale The locale the option should translate for.
     * @param name The name of the localised name.
     * @return This builder for chaining methods.
     */
    fun localiseName(locale: Locale, name: String): CommandOptionBuilder<V> {
        nameLocalizations.computeIfAbsent(locale) { name }
        return this
    }

    /**
     * Convenient method to localise this option's description based off the user's [Locale].
     * @param locale The locale the option should translate for.
     * @param description The localised description.
     * @return This builder for chaining methods.
     */
    fun localiseDescription(locale: Locale, description: String): CommandOptionBuilder<V> {
        descriptionLocalizations.computeIfAbsent(locale) { description }
        return this
    }

    /**
     * Method to build a command option.
     */
    abstract fun build(): CommandOption<V>
}

/**
 * Represents a builder for building a option for a specific [application command][AbstractApplicationCommand].
 * @param name The name of the option, 1-32 characters long.
 * @param description The description of the option, 1-32 characters long.
 */
sealed class CommandOptionWithChoiceBuilder<V, E: Any>(name: String, description: String): CommandOptionBuilder<V>(name, description) {
    /**
     * The list of choices available to this option.
     */
    internal val choices = mutableListOf<CommandChoice<E>>()

    /**
     * The action name to link an autocomplete action function.
     */
    var autocompleteActionName: String? = null

    /**
     * Appends a new choice to this builder, the max cannot be over 25 choices.
     * @param name The name of the choice
     * @param value The value of the choice
     * @param nameLocalizations The localisations for mapping the choice name.
     */
    fun choice(name: String, value: E, nameLocalizations: Map<Locale, String> = mapOf()): CommandOptionWithChoiceBuilder<V, E> {
        require(choices.size < 25) { "You went over board with the choices!!!" }

        choices.add(buildChoice(name, value, nameLocalizations))
        return this
    }

    /**
     * Method to create a [CommandChoice] object.
     */
    abstract fun buildChoice(
        name: String,
        value: E,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<E>
}

class StringCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<String, String>(name, description) {
    override fun build(): CommandOption<String> = StringCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName
    )

    override fun buildChoice(
        name: String,
        value: String,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<String> = StringCommandChoice(name, value, nameLocalizations)
}

class NullableStringCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<String?, String>(name, description) {
    override fun build(): CommandOption<String?> = NullableStringCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName
    )

    override fun buildChoice(
        name: String,
        value: String,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<String> = StringCommandChoice(name, value, nameLocalizations)
}

class IntegerCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<Long, Long>(name, description) {
    var max: Long? = null
    var min: Long? = null

    override fun build(): CommandOption<Long> = IntCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName,
        min,
        max
    )

    override fun buildChoice(
        name: String,
        value: Long,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<Long> = IntegerCommandChoice(name, value, nameLocalizations)
}

class NullableIntCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<Long?, Long>(name, description) {
    var max: Long? = null
    var min: Long? = null

    override fun build(): CommandOption<Long?> = NullableIntCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName,
        min,
        max
    )

    override fun buildChoice(
        name: String,
        value: Long,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<Long> = IntegerCommandChoice(name, value, nameLocalizations)
}

class NumberCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<Double, Double>(name, description) {
    override fun build(): CommandOption<Double> = NumberCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName
    )

    override fun buildChoice(
        name: String,
        value: Double,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<Double> = NumberCommandChoice(name, value, nameLocalizations)
}

class NullableNumberCommandOptionBuilder(name: String, description: String): CommandOptionWithChoiceBuilder<Double?, Double>(name, description) {
    override fun build(): CommandOption<Double?> = NullableNumberCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations,
        choices,
        autocompleteActionName
    )

    override fun buildChoice(
        name: String,
        value: Double,
        nameLocalizations: Map<Locale, String>
    ): CommandChoice<Double> = NumberCommandChoice(name, value, nameLocalizations)
}

class BooleanCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Boolean>(name, description) {
    override fun build(): CommandOption<Boolean> = BooleanCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class NullableBooleanCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Boolean?>(name, description) {
    override fun build(): CommandOption<Boolean?> = NullableBooleanCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class UserCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<User>(name, description) {
    override fun build(): CommandOption<User> = UserCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class NullableUserCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<User?>(name, description) {
    override fun build(): CommandOption<User?> = NullableUserCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class RoleCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Role>(name, description) {
    override fun build(): CommandOption<Role> = RoleCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class NullableRoleCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Role?>(name, description) {
    override fun build(): CommandOption<Role?> = NullableRoleCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class ChannelCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Channel>(name, description) {
    override fun build(): CommandOption<Channel> = ChannelCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class NullableChannelCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Channel?>(name, description) {
    override fun build(): CommandOption<Channel?> = NullableChannelCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class AttachmentCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Attachment>(name, description) {
    override fun build(): CommandOption<Attachment> = AttachmentCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

class NullableAttachmentCommandOptionBuilder(name: String, description: String): CommandOptionBuilder<Attachment?>(name, description) {
    override fun build(): CommandOption<Attachment?> = NullableAttachmentCommandOption(
        name,
        description,
        nameLocalizations,
        descriptionLocalizations
    )
}

// [ == readonly delegate props == ]
/**
 * Delegated property to create a [StringCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * ```kotlin
 * class MySlashCommand: AbstractApplicationCommand() {
 *    // This should be `private` since it shouldn't have access to
 *    // these!
 *    val textOption = string("text", "The text to send!") {
 *       localiseName(Locale.EN_GB, "textu")
 *       localiseDescription(Locale.EN_GB, "The textu to send!")
 *    }
 *
 *    override suspend fun execute(context: ApplicationCommandContext) {
 *       val text = context.options[textOption] // => String
 *       context.reply(text)
 *    }
 * }
 * ```
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;String&gt;][StringCommandOption]
 */
fun AbstractApplicationCommand.string(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<String, String>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<String>> = ReadOnlyProperty { _, _ ->
    StringCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableStringCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * ```kotlin
 * class MySlashCommand: AbstractApplicationCommand() {
 *    // This should be `private` since it shouldn't have access to
 *    // these!
 *    val textOption = optionalString("text", "The text to send!") {
 *       localiseName(Locale.EN_GB, "textu")
 *       localiseDescription(Locale.EN_GB, "The textu to send!")
 *    }
 *
 *    override suspend fun execute(context: ApplicationCommandContext) {
 *       val text = context.options[textOption] // => String?
 *       context.reply(text ?: "cannot retrieve text. :(")
 *    }
 * }
 * ```
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;String?&gt;][NullableStringCommandOption]
 */
fun AbstractApplicationCommand.optionalString(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<String?, String>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<String?>> = ReadOnlyProperty { _, _ ->
    NullableStringCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [IntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Long&gt;][IntCommandOption]
 */
fun AbstractApplicationCommand.int(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Long, Long>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Long>> = ReadOnlyProperty { _, _ ->
    IntegerCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableIntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Long?&gt;][NullableIntCommandOption]
 */
fun AbstractApplicationCommand.optionalInt(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Long?, Long>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Long?>> = ReadOnlyProperty { _, _ ->
    NullableIntCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NumberCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Double&gt;][NumberCommandOption]
 */
fun AbstractApplicationCommand.number(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Double, Double>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Double>> = ReadOnlyProperty { _, _ ->
    NumberCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableNumberCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Double?&gt;][NullableNumberCommandOption]
 */
fun AbstractApplicationCommand.optionalNumber(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Double?, Double>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Double?>> = ReadOnlyProperty { _, _ ->
    NullableNumberCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [BooleanCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Boolean&gt;][BooleanCommandOption]
 */
fun AbstractApplicationCommand.boolean(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Boolean>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Boolean>> = ReadOnlyProperty { _, _ ->
    BooleanCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableIntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Boolean?&gt;][NullableBooleanCommandOption]
 */
fun AbstractApplicationCommand.optionalBoolean(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Boolean?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Boolean?>> = ReadOnlyProperty { _, _ ->
    NullableBooleanCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [UserCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;User&gt;][UserCommandOption]
 */
fun AbstractApplicationCommand.user(
    name: String,
    description: String,
    builder: CommandOptionBuilder<User>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<User>> = ReadOnlyProperty { _, _ ->
    UserCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableUserCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;User?&gt;][NullableUserCommandOption]
 */
fun AbstractApplicationCommand.optionalUser(
    name: String,
    description: String,
    builder: CommandOptionBuilder<User?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<User?>> = ReadOnlyProperty { _, _ ->
    NullableUserCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [ChannelCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Channel&gt;][ChannelCommandOption]
 */
fun AbstractApplicationCommand.channel(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Channel>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Channel>> = ReadOnlyProperty { _, _ ->
    ChannelCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableChannelCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Channel?&gt;][NullableChannelCommandOption]
 */
fun AbstractApplicationCommand.optionalChannel(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Channel?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Channel?>> = ReadOnlyProperty { _, _ ->
    NullableChannelCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [RoleCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Role&gt;][RoleCommandOption]
 */
fun AbstractApplicationCommand.role(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Role>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Role>> = ReadOnlyProperty { _, _ ->
    RoleCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableRoleCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Role?&gt;][NullableRoleCommandOption]
 */
fun AbstractApplicationCommand.optionalRole(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Role?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Role?>> = ReadOnlyProperty { _, _ ->
    NullableRoleCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [AttachmentCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Attachment&gt;][AttachmentCommandOption]
 */
fun AbstractApplicationCommand.attachment(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Attachment>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Attachment>> = ReadOnlyProperty { _, _ ->
    AttachmentCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableAttachmentCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Attachment?&gt;][NullableAttachmentCommandOption]
 */
fun AbstractApplicationCommand.optionalAttachment(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Attachment?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationCommand, CommandOption<Attachment?>> = ReadOnlyProperty { _, _ ->
    NullableAttachmentCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [StringCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * ```kotlin
 * class MySlashCommand: AbstractApplicationCommand() {
 *    // This should be `private` since it shouldn't have access to
 *    // these!
 *    val textOption = string("text", "The text to send!") {
 *       localiseName(Locale.EN_GB, "textu")
 *       localiseDescription(Locale.EN_GB, "The textu to send!")
 *    }
 *
 *    override suspend fun execute(context: ApplicationCommandContext) {
 *       val text = context.options[textOption] // => String
 *       context.reply(text)
 *    }
 * }
 * ```
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;String&gt;][StringCommandOption]
 */
fun AbstractApplicationSubcommand.string(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<String, String>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<String>> = ReadOnlyProperty { _, _ ->
    StringCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableStringCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * ```kotlin
 * class MySlashCommand: AbstractApplicationCommand() {
 *    // This should be `private` since it shouldn't have access to
 *    // these!
 *    val textOption = optionalString("text", "The text to send!") {
 *       localiseName(Locale.EN_GB, "textu")
 *       localiseDescription(Locale.EN_GB, "The textu to send!")
 *    }
 *
 *    override suspend fun execute(context: ApplicationCommandContext) {
 *       val text = context.options[textOption] // => String?
 *       context.reply(text ?: "cannot retrieve text. :(")
 *    }
 * }
 * ```
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;String?&gt;][NullableStringCommandOption]
 */
fun AbstractApplicationSubcommand.optionalString(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<String?, String>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<String?>> = ReadOnlyProperty { _, _ ->
    NullableStringCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [IntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Long&gt;][IntCommandOption]
 */
fun AbstractApplicationSubcommand.int(
    name: String,
    description: String,
    builder: IntegerCommandOptionBuilder.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Long>> = ReadOnlyProperty { _, _ ->
    IntegerCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableIntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Long?&gt;][NullableIntCommandOption]
 */
fun AbstractApplicationSubcommand.optionalInt(
    name: String,
    description: String,
    builder: NullableIntCommandOptionBuilder.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Long?>> = ReadOnlyProperty { _, _ ->
    NullableIntCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NumberCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Double&gt;][NumberCommandOption]
 */
fun AbstractApplicationSubcommand.number(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Double, Double>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Double>> = ReadOnlyProperty { _, _ ->
    NumberCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableNumberCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Double?&gt;][NullableNumberCommandOption]
 */
fun AbstractApplicationSubcommand.optionalNumber(
    name: String,
    description: String,
    builder: CommandOptionWithChoiceBuilder<Double?, Double>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Double?>> = ReadOnlyProperty { _, _ ->
    NullableNumberCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [BooleanCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Boolean&gt;][BooleanCommandOption]
 */
fun AbstractApplicationSubcommand.boolean(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Boolean>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Boolean>> = ReadOnlyProperty { _, _ ->
    BooleanCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableIntCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Boolean?&gt;][NullableBooleanCommandOption]
 */
fun AbstractApplicationSubcommand.optionalBoolean(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Boolean?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Boolean?>> = ReadOnlyProperty { _, _ ->
    NullableBooleanCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [UserCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;User&gt;][UserCommandOption]
 */
fun AbstractApplicationSubcommand.user(
    name: String,
    description: String,
    builder: CommandOptionBuilder<User>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<User>> = ReadOnlyProperty { _, _ ->
    UserCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableUserCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;User?&gt;][NullableUserCommandOption]
 */
fun AbstractApplicationSubcommand.optionalUser(
    name: String,
    description: String,
    builder: CommandOptionBuilder<User?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<User?>> = ReadOnlyProperty { _, _ ->
    NullableUserCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [ChannelCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Channel&gt;][ChannelCommandOption]
 */
fun AbstractApplicationSubcommand.channel(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Channel>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Channel>> = ReadOnlyProperty { _, _ ->
    ChannelCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableChannelCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Channel?&gt;][NullableChannelCommandOption]
 */
fun AbstractApplicationSubcommand.optionalChannel(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Channel?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Channel?>> = ReadOnlyProperty { _, _ ->
    NullableChannelCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [RoleCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Role&gt;][RoleCommandOption]
 */
fun AbstractApplicationSubcommand.role(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Role>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Role>> = ReadOnlyProperty { _, _ ->
    RoleCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableRoleCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Role?&gt;][NullableRoleCommandOption]
 */
fun AbstractApplicationSubcommand.optionalRole(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Role?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Role?>> = ReadOnlyProperty { _, _ ->
    NullableRoleCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [AttachmentCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Attachment&gt;][AttachmentCommandOption]
 */
fun AbstractApplicationSubcommand.attachment(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Attachment>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Attachment>> = ReadOnlyProperty { _, _ ->
    AttachmentCommandOptionBuilder(name, description).apply(builder).build()
}

/**
 * Delegated property to create a [NullableAttachmentCommandOption]. You use this property to
 * retrieve arguments from the [ApplicationCommandContext].
 *
 * ## Example
 * Read the example from [optionalString] or [string].
 *
 * @param name The name of the option, 1-32 characters length.
 * @param description The description of the option, 1-32 characters length.
 * @param builder The builder DSL function to construct this [CommandOption].
 * @return The built command option object, as [CommandOption&lt;Attachment?&gt;][NullableAttachmentCommandOption]
 */
fun AbstractApplicationSubcommand.optionalAttachment(
    name: String,
    description: String,
    builder: CommandOptionBuilder<Attachment?>.() -> Unit = {}
): ReadOnlyProperty<AbstractApplicationSubcommand, CommandOption<Attachment?>> = ReadOnlyProperty { _, _ ->
    NullableAttachmentCommandOptionBuilder(name, description).apply(builder).build()
}
