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

package sh.nino.towa.slash.commands.extensions

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.ComponentType
import dev.kord.common.entity.InteractionType

/**
 * Returns the name of this interaction type for pretty logging.
 */
val InteractionType.name: String
    get() = when (this) {
        is InteractionType.AutoComplete -> "Autocomplete"
        is InteractionType.ApplicationCommand -> "Application Command"
        is InteractionType.ModalSubmit -> "Modal Submit"
        is InteractionType.Component -> "Component"
        is InteractionType.Ping -> "Ping"
        is InteractionType.Unknown -> "Unknown [$type]"
    }

/**
 * Returns the name of this application command's type for pretty logging!
 */
val ApplicationCommandType.name: String
    get() = when (this) {
        is ApplicationCommandType.ChatInput -> "Chat Input (/command)"
        is ApplicationCommandType.Message -> "Message"
        is ApplicationCommandType.User -> "User"
        is ApplicationCommandType.Unknown -> "Unknown [$value]"
    }

/**
 * Returns the name of this component's type for pretty logging!
 */
val ComponentType.name: String
    get() = when (this) {
        is ComponentType.ActionRow -> "Action Row"
        is ComponentType.Button -> "Button"
        is ComponentType.SelectMenu -> "Select Menu"
        is ComponentType.TextInput -> "Text Input"
        is ComponentType.Unknown -> "Unknown [$value]"
    }
