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

package sh.nino.towa.slash.commands.events.interactables.autocomplete

import dev.floofy.utils.kotlin.ifNotNull
import sh.nino.towa.core.pipeline.PipelineContext
import sh.nino.towa.slash.commands.SlashCommandPipeline

class AutocompletePipelineContext(
    override val pipeline: SlashCommandPipeline,
    override val attributes: MutableMap<String, Any?> = mutableMapOf()
): PipelineContext<SlashCommandPipeline>

/**
 * Returns the option name for what autocomplete function this context object
 * is running in.
 */
val AutocompletePipelineContext.optionName: String?
    get() = attributes["option:name"]?.ifNotNull { name -> name as String }

/**
 * Returns the option's value for what autocomplete function this context object
 * is running in.
 */
val AutocompletePipelineContext.optionValue: String?
    get() = attributes["option:value"]?.ifNotNull { value -> value as String }
