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

package sh.nino.towa.core.locator

import kotlin.reflect.KClass

/**
 * Represents a "locator."
 *
 * Locators are a Towa concept to locate objects based off what it gives off. Base locators
 * use the [#findObjects()][Locator.findObjects] utility to find objects based off the given
 * type of it.
 */
interface Locator {
    /**
     * Finds objects based off the given [KClass][type] and returns a list
     * that the locator can find. The commands' library (legacy/slash commands)
     * will have extension functions to locate a specific part of what it needs.
     *
     * @param type The [KClass] that it needs to use to find references.
     * @return A list of objects, returns empty if nothing was found.
     */
    fun <T: Any> findObjects(type: KClass<T>): List<T>
}

/**
 * Finds objects based off the given [type][T] and returns a list
 * that the locator can find. The commands' library (legacy/slash commands)
 * will have extension functions to locate a specific part of what it needs.
 */
inline fun <reified T: Any> Locator.findObjects(): List<T> = findObjects(T::class)
