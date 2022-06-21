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

@file:Suppress("UNCHECKED_CAST", "UNUSED")
package sh.nino.towa.core

import dev.floofy.utils.slf4j.logging
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import sh.nino.towa.core.annotations.InjectKord
import sh.nino.towa.core.annotations.InjectTowa
import sh.nino.towa.core.extensions.AbstractExtension
import java.io.Closeable
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Represents the main Towa object to interact with the lifecycle of Towa,
 * or its extensions feature.
 */
class Towa(val kord: Kord): Closeable {
    // Represents the extensions that you have registered.
    internal val extensionsMap: MutableMap<String, AbstractExtension> = mutableMapOf()

    // The logger to log messages, duh.
    private val log by logging<Towa>()

    /**
     * Registers an extension to this Towa object.
     * @param extension The extension to register.
     */
    fun <E: AbstractExtension> register(extension: E): Towa {
        log.debug("Registering extension ${extension.key}...")
        extensionsMap.computeIfAbsent(extension.key) { extension }

        log.debug("Registered!")
        return this
    }

    /**
     * Starts Towa and loads all the extensions.
     */
    suspend fun start() {
        // the vtuber usually says Ohayappi to say hello to her viewers.
        log.debug("おはやっぴー!! (Ohayappi)")

        for ((key, extension) in extensionsMap) {
            log.info("Loading extension $key...")

            val kordProperty = extension::class.declaredMemberProperties.firstOrNull {
                it.hasAnnotation<InjectKord>()
            }

            if (kordProperty != null) {
                kordProperty.isAccessible = true
                kordProperty.javaField?.set(extension, kord)
            }

            val towaProperty = extension::class.declaredMemberProperties.firstOrNull {
                it.hasAnnotation<InjectTowa>()
            }

            if (towaProperty != null) {
                towaProperty.isAccessible = true
                towaProperty.javaField?.set(extension, this)
            }

            try {
                extension.load()
            } catch (e: Exception) {
                log.error("Unable to load extension $key:", e)
                extensionsMap.remove(key)
            }
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * As noted in [AutoCloseable.close], cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * *mark* the `Closeable` as closed, prior to throwing
     * the `IOException`.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    override fun close() {
        // the vtuber usually says Otsuyappi to say goodbye to her viewers.
        log.warn("おつやっぴー... (Otsuyappi) :(")

        for ((_, extension) in extensionsMap) {
            runBlocking {
                extension.unload()
            }
        }

        extensionsMap.clear()
    }
}

/**
 * Returns a list of extensions that were created in this Towa object.
 */
val Towa.extensions: Map<String, AbstractExtension>
    get() = extensionsMap.toMap()

/**
 * Checks if an extension by its [key] exists in this Towa object. If not,
 * it'll return `null`.
 *
 * @param key The extension's key to find.
 * @return The extension casted as [T] or `null`.
 */
fun <T: AbstractExtension> Towa.extensionOrNull(key: String): T? = if (extensionsMap.containsKey(key)) extensionsMap[key] as? T else null

/**
 * Returns the extension if it was found in this Towa object.
 * @param key The extension's key to find it.
 * @throws IllegalStateException If the extension by its key wasn't found.
 * @return The extension casted as [T].
 */
fun <T: AbstractExtension> Towa.extension(key: String): T = extensionOrNull(key) ?: error("Unable to find extension $key.")

/**
 * Convenience method to call [Kord.on] on this [Towa] object.
 */
inline fun <reified T: Event> Towa.on(scope: CoroutineScope = kord, noinline consumer: suspend T.() -> Unit) =
    kord.on(scope, consumer)
