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
 * Represents the metadata of a [command][sh.nino.towa.commands.legacy.AbstractCommand].
 */
annotation class Command(
    /**
     * Represents the command's name.
     */
    val name: String,

    /**
     * Represents the command's description, if none was specified,
     * it'll just return "No description has been specified."
     */
    val description: String = "No description has been specified.",

    /**
     * External triggers this command has.
     */
    val aliases: Array<String> = [],

    /**
     * How long in milliseconds should the command should ratelimit for.
     */
    val cooldown: Long = 5000L,

    /**
     * List of inhibitors in this command to be injected automatically,
     * you can also use `Command.registerInhibitor()` dynamically.
     */
    val inhibitors: Array<String> = [],

    /**
     * List of examples that is sent through the default help command,
     * or your custom one!
     *
     * - `{{prefix}}` contains the executed prefix
     * - `{{arg:<name>}}` represents the argument's type name (`{{arg:owo}}` -> `owo: string`)
     */
    val examples: Array<String> = [],

    /**
     * List of permissions that the command executor needs before
     * executing this command.
     */
    val userPermissions: LongArray = [],

    /**
     * List of permissions that the bot needs before executing
     * this command.
     */
    val botPermissions: LongArray = []
)
