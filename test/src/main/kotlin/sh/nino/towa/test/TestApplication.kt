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

@file:JvmName("TestApplicationKt")
package sh.nino.towa.test

import dev.kord.common.entity.PresenceStatus
import kotlinx.coroutines.runBlocking
import sh.nino.towa.core.Towa
import sh.nino.towa.slash.commands.useSlashCommands
import sh.nino.towa.slash.commands.locator.ListBasedLoader

object TestApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        val token = args.first()
        val towa = Towa {
            kord(token) {
                enableShutdownHook = true
            }
        }

        towa.useSlashCommands {
            locate(ListBasedLoader(listOf(MyFirstCommand)))
            devServerId = 743698927039283201
        }

        runBlocking {
            towa.kord.login {
                presence {
                    playing("with noobs.")
                    status = PresenceStatus.Idle
                }
            }
        }
    }
}
