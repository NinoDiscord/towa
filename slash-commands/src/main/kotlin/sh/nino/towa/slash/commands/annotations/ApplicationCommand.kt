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

package sh.nino.towa.slash.commands.annotations

/**
 * Represents an application command that can be registered. This is the metadata
 * for a slash command, in simpler terms.
 *
 * @param name The name of the application command, 1-32 characters long. Use `AbstractApplicationCommand.localiseName(Locale, String)`
 * to append [`name_localizations`](https://discord.com/developers/docs/interactions/application-commands#create-global-application-command) with this command.
 *
 * @param description The description of the command, use `AbstractApplicationCommand.localiseDescription(Locale, String)` to
 * append [`descriptions_localizations`](https://discord.com/developers/docs/interactions/application-commands#create-global-application-command) with this command.
 *
 * @param defaultMemberPermissions A list of permissions that this command requires. When we register or update the commands,
 * this will be a long rather than array of longs.
 *
 * @param dmPermission Indicates whether the command is also available in DMs with the app, this is only used
 * for global-scoped commands.
 *
 * @param useInGuilds A list of guild IDs that this command should be registered in, by default, it is globally-scoped
 * if [SlashCommandsConfiguration.devServerId][sh.nino.towa.slash.commands.SlashCommandsConfiguration.devServerId] is populated,
 * it will append the `devServerId` and any other guilds appended by this array.
 */
@Target(AnnotationTarget.CLASS)
annotation class ApplicationCommand(
    val name: String,
    val description: String = "No description has been set.",
    val defaultMemberPermissions: LongArray = [],
    val dmPermission: Boolean = false,
    val useInGuilds: LongArray = []
)
