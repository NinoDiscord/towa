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

@file:Suppress("UNUSED")
package sh.nino.towa.core

import dev.kord.core.Kord
import dev.kord.core.builder.kord.KordBuilder
import kotlinx.coroutines.runBlocking
import sh.nino.towa.core.extensions.AbstractExtension
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates

@OptIn(ExperimentalContracts::class)
private fun <T: Any> T.applySuspend(block: suspend T.() -> Unit): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    runBlocking { block() }
    return this
}

/**
 * Creates a new [Towa] object using the [TowaBuilder] to register
 * extensions.
 *
 * @param kord The [Kord] instance, if you already have created a Kord instance.
 */
fun Towa(builder: TowaBuilder.() -> Unit = {}): Towa =
    TowaBuilder().apply(builder).build()

/**
 * The builder to construct the [Towa] object.
 */
@TowaDsl
class TowaBuilder {
    val extensions = mutableMapOf<String, AbstractExtension>()
    private var kord: Kord by Delegates.notNull()

    /**
     * Registers an extension to this builder.
     * @param extension The extension to register.
     */
    fun <E: AbstractExtension> register(extension: E) {
        extensions.computeIfAbsent(extension.key) { extension }
    }

    /**
     * Creates a new Kord instance that Towa can use to create events
     * and so-on.
     *
     * @param token The bot's token
     * @param builder The action to construct a [Kord] instance.
     */
    fun kord(token: String, builder: KordBuilder.() -> Unit = {}) {
        this.kord = runBlocking { KordBuilder(token).apply(builder).build() }
    }

    /**
     * Uses an existing [Kord] instance.
     */
    fun useKord(kord: Kord) {
        this.kord = kord
    }

    /**
     * Creates a new [Towa] object.
     */
    fun build(): Towa {
        val towa = Towa(kord)
        for (extension in extensions.values) towa.register(extension)

        return towa
    }
}
