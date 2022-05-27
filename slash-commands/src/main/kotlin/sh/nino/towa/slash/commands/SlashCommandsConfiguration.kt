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

package sh.nino.towa.slash.commands

import sh.nino.towa.core.TowaDsl
import sh.nino.towa.slash.commands.locator.EmptyLocator
import sh.nino.towa.slash.commands.locator.ILocator

/**
 * The configuration for the [SlashCommandExtension].
 */
class SlashCommandsConfiguration(val locator: ILocator)

/**
 * The builder DSL for constructing a [SlashCommandsConfiguration] object.
 */
@TowaDsl
class SlashCommandConfigBuilder {
    private var locator: ILocator? = null

    /**
     * Registers a locator object.
     * @param locator The locator to register.
     */
    fun <L: ILocator> locate(locator: L) {
        if (this.locator == null)
            throw IllegalStateException("Cannot re-register a locator!")

        this.locator = locator
    }

    /**
     * Builds a new [SlashCommandsConfiguration] object.
     */
    fun build(): SlashCommandsConfiguration = SlashCommandsConfiguration(
        if (locator == null) EmptyLocator else locator!!
    )
}
