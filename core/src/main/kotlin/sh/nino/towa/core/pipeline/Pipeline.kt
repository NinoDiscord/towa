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

package sh.nino.towa.core.pipeline

import kotlin.reflect.KClass

/**
 * Represents a pipeline to intercept events from any execution point.
 */
open class Pipeline {
    private val events = mutableMapOf<KClass<PipelineEvent<*, *>>, MutableList<suspend PipelineContext<*>.() -> Unit>>()

    /**
     * Adds an interceptor point to a specific [event][E] and will be emitted
     * when the event is fired.
     *
     * @param event The [KClass] for the event.
     * @param interceptor The interceptor logic.
     */
    @Suppress("UNCHECKED_CAST")
    fun <P: Pipeline, C: PipelineContext<P>, E: PipelineEvent<P, C>> intercept(
        event: KClass<E>,
        interceptor: suspend C.() -> Unit
    ): Pipeline {
        if (!events.containsKey(event as KClass<*>)) {
            events[event as KClass<PipelineEvent<*, *>>] = mutableListOf(interceptor as suspend PipelineContext<*>.() -> Unit)
            return this
        }

        val key = event as KClass<PipelineEvent<*, *>>
        events[key]!!.add(interceptor as suspend PipelineContext<*>.() -> Unit)

        return this
    }

    /**
     * Adds an interceptor point to a specific reified [event][E] and will be
     * emitted when the event is fired.
     *
     * @param interceptor The interceptor logic.
     */
    inline fun <P: Pipeline, C: PipelineContext<P>, reified E: PipelineEvent<P, C>> intercept(noinline interceptor: suspend C.() -> Unit): Pipeline =
        intercept(E::class, interceptor)

    /**
     * Emits the [event] specified.
     * @param event The event to emit
     * @param context The context object for the event.
     */
    suspend fun <P: Pipeline, C: PipelineContext<P>, E: PipelineEvent<P, C>> emit(event: KClass<E>, context: C) {
        val all = events[event as KClass<*>] ?: emptyList()
        for (ev in all) {
            ev.invoke(context)
        }
    }

    /**
     * Emits the [event][E] specified.
     * @param context The context object for the event.
     */
    suspend inline fun <reified E: PipelineEvent<P, C>, P: Pipeline, C: PipelineContext<P>> emit(context: C) {
        return emit(E::class, context)
    }
}
