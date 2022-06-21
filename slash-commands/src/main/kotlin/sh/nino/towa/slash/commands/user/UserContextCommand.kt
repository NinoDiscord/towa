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

package sh.nino.towa.slash.commands.user

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import sh.nino.towa.slash.commands.annotations.DeferEphemeral
import sh.nino.towa.slash.commands.annotations.OnlyInGuilds
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Represents the base command interface, to construct user context commands.
 */
abstract class UserContextCommand(val name: String) {
    /**
     * If this command should be deferred ephemerally. This is `true` if [DeferEphemeral] was
     * implemented in this class.
     */
    val deferEphemeral: Boolean = this::class.hasAnnotation<DeferEphemeral>()

    /**
     * Returns the list of guilds that this [UserContextCommand] is only available in.
     */
    val onlyInGuilds: List<String> = this::class.findAnnotation<OnlyInGuilds>()?.guilds?.toList() ?: emptyList()

    /**
     * Executes the command.
     * @param ctx The user context object.
     */
    abstract suspend fun execute(ctx: UserContext)

    /**
     * Returns this [command][UserContextCommand] as a Kord request object.
     */
    fun toRequest(): ApplicationCommandCreateRequest = ApplicationCommandCreateRequest(
        name,
        type = ApplicationCommandType.User
    )
}
