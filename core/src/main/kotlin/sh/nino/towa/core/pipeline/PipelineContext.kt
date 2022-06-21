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

/**
 * Represents a generic context object.
 * @param P The pipeline this context object belongs to.
 */
interface PipelineContext<P: Pipeline> {
    /**
     * The pipeline that was being intercepted.
     */
    val pipeline: P

    /**
     * Represents the extra attributes in this [context object][PipelineContext]
     */
    val attributes: MutableMap<String, Any?>
}

/**
 * Represents a generic context object, which can be used if the pipeline
 * event doesn't have a custom context.
 *
 * @param pipeline The current pipeline that is being intercepted
 * @param towa The [Towa] object, if you ever need it.
 */
class GenericPipelineContext<P: Pipeline>(
    override val pipeline: P,
    override val attributes: MutableMap<String, Any?> = mutableMapOf()
): PipelineContext<P>
