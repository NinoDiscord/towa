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

package sh.nino.towa.slash.commands.locator.reflections

import io.github.classgraph.ClassGraph
import sh.nino.towa.core.TowaDsl
import sh.nino.towa.slash.commands.annotations.ApplicationCommand
import sh.nino.towa.slash.commands.application.AbstractApplicationCommand
import sh.nino.towa.slash.commands.locator.ILocator
import kotlin.properties.Delegates

/**
 * Represents the builder to configure the [ReflectionLoaderConfiguration] object.
 */
@TowaDsl
class ReflectionLoaderConfigurationBuilder {
    private var configureClassGraph: (ClassGraph.() -> Unit)? = null

    /**
     * The package to find all the application commands.
     */
    var applicationCommandsPackage: String by Delegates.notNull()

    /**
     * Configures the [ClassGraph] that is used to find all the
     * message, user, and application commands.
     *
     * @param action The action builder DSL to execute
     * @throws IllegalStateException If the to configure block was already registered.
     */
    fun configure(action: ClassGraph.() -> Unit) {
        checkNotNull(configureClassGraph) { "The configure block was already registered!" }

        configureClassGraph = action
    }

    /**
     * Returns a [ReflectionLoaderConfiguration] based off the options used.
     */
    fun build(): ReflectionLoaderConfiguration = ReflectionLoaderConfiguration(
        configureClassGraph?.let { ClassGraph().apply(it) } ?: ClassGraph().enableClassInfo(),
        applicationCommandsPackage
    )
}

/**
 * Represents the configuration for the [ReflectionLocator].
 * @param classGraph The [ClassGraph] instance to lookup.
 * @param applicationCommandsPackage The package to look up all the application commands
 */
data class ReflectionLoaderConfiguration(
    val classGraph: ClassGraph,
    val applicationCommandsPackage: String
)

/**
 * Creates a new [ReflectionLocator] based off a builder DSL block.
 */
@TowaDsl
fun ReflectionLocator(configure: ReflectionLoaderConfigurationBuilder.() -> Unit = {}): ReflectionLocator =
    ReflectionLocator(ReflectionLoaderConfigurationBuilder().apply(configure).build())

class ReflectionLocator(private val config: ReflectionLoaderConfiguration): ILocator {
    override fun findCommands(): List<AbstractApplicationCommand> {
        val result = config.classGraph.scan()
        val instances = result.getClassesWithAnnotation(ApplicationCommand::class.java)

        for (cls in instances.names) {
            try {
                Class.forName(cls)
            } catch (e: ClassNotFoundException) {
                throw IllegalStateException("Unable to find class $cls", e)
            }
        }

        val all = mutableListOf<Any>()
        for (instance in instances.toArray().toList()) {
            // THIS IS BASED OFF ASSUMPTION!!!!
            val cls = (instance as Class<*>).getDeclaredConstructor().newInstance()
            all.add(cls)
        }

        return all.toList().map { it as AbstractApplicationCommand }
    }
}
