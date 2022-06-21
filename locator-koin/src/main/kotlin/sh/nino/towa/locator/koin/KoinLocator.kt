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

package sh.nino.towa.locator.koin

import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext
import sh.nino.towa.core.locator.Locator
import kotlin.reflect.KClass

/**
 * Represents a locator that finds objects based off a Koin context (which is customizable
 * to your taste, defaults to [GlobalContext]).
 */
class KoinLocator(private val context: KoinContext = GlobalContext): Locator {
    /**
     * Finds objects based off the given [KClass][type] and returns a list
     * that the locator can find. The commands' library (legacy/slash commands)
     * will have extension functions to locate a specific part of what it needs.
     *
     * @param type The [KClass] that it needs to use to find references.
     * @return A list of objects, returns empty if nothing was found.
     */
    @OptIn(KoinInternalApi::class)
    override fun <T : Any> findObjects(type: KClass<T>): List<T> {
        val koin = context.get()
        return koin.scopeRegistry.rootScope.getAll(type)
    }
}
