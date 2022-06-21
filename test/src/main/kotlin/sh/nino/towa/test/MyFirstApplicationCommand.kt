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

package sh.nino.towa.test

import sh.nino.towa.slash.commands.annotations.SlashCommand
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.application.ApplicationCommandContext
import sh.nino.towa.slash.commands.application.int
import sh.nino.towa.slash.commands.application.user

@SlashCommand("boop", "Boop command!")
class MyFirstApplicationCommand: AbstractApplicationCommand() {
    private val user by user("user", "Which should should be booped?")
    private val boops by int("boops", "How many boops the end user should receive.")

    override suspend fun execute(context: ApplicationCommandContext) {
        val amountOfBoops = context.option(boops)
        val booper = context.option(user)

        context.reply("${context.author.tag} [${context.author.id}] gave ${booper.tag} [${booper.id}] $amountOfBoops boops!")
    }
}
