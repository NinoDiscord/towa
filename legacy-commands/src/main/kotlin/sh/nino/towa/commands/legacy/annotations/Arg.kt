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

package sh.nino.towa.commands.legacy.annotations

/**
 * Represents the metadata about a command argument.
 */
annotation class Arg(
    /**
     * The name of the argument.
     */
    val name: String,

    /**
     * The description this argument gives, useful for outputting
     * in a help command.
     */
    val description: String = "No description has been specified.",

    /**
     * If the argument should consume the rest of the arguments. This can only happen in the last
     * argument, or it will error out due to how argument parsing works.
     */
    val consumeRest: Boolean = false,

    /**
     * If the argument is required to be used. This will emit the [CommandMissingArgumentEvent][sh.nino.towa.commands.legacy.events.CommandMissingArgumentEvent]
     * in the command pipeline.
     *
     * ## Example Interceptor
     * ```kotlin
     * val towa = Towa()
     * towa.useLegacyCommands {
     *    // Registers the interceptor here. You can also do
     *    // towa.legacyCommands.intercept(...)
     *    intercept(CommandMissingArgumentEvent) { ctx, event -> # ctx: TowaLegacyCommandContext, event: CommandMissingArgumentEvent
     *       ctx.reply("You are missing the argument ${event.arg.name}.")
     *    }
     * }
     * ```
     */
    val isRequired: Boolean = false,

    /**
     * If the argument can have multiple outputs. This can't be used with the [consumeRest] option.
     * This option will allow you to use the multi argument type readers feature, and the return value
     * MUST be a list of that receiving object.
     */
    val canHaveMultiple: Boolean = false
)
