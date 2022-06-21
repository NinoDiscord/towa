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

package sh.nino.towa.slash.commands.events

import dev.floofy.utils.kotlin.ifNotNull
import sh.nino.towa.core.pipeline.PipelineContext
import sh.nino.towa.slash.commands.SlashCommandPipeline
import sh.nino.towa.slash.commands.events.message.MessageCommandPipelineContext
import sh.nino.towa.slash.commands.events.user.UserContextPipelineContext
import sh.nino.towa.slash.commands.message.MessageCommand
import sh.nino.towa.slash.commands.user.UserContextCommand

/**
 * Returns the exception that occurred in a [UserContextException][sh.nino.towa.slash.commands.events.user.UserContextException],
 * [MessageCommandException][sh.nino.towa.slash.commands.events.message.MessageCommandException], or
 * [ApplicationCommandException][sh.nino.towa.slash.commands.events.application.ApplicationCommandContext] interceptor,
 * return `null` if the above events haven't been emitted.
 */
val PipelineContext<SlashCommandPipeline>.exception: Throwable?
    get() = attributes["exception"]?.ifNotNull { ex -> ex as Throwable }

/**
 * The command that this current pipeline context belongs to.
 */
val UserContextPipelineContext.command: UserContextCommand?
    get() = attributes["command"]?.ifNotNull { cmd -> cmd as UserContextCommand }

/**
 * The command that this current pipeline context belongs to.
 */
val MessageCommandPipelineContext.command: MessageCommand?
    get() = attributes["command"]?.ifNotNull { cmd -> cmd as MessageCommand }
