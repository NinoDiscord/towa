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

package sh.nino.towa.slash.commands.application

import dev.floofy.utils.kotlin.ifNotNull
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.orEmpty
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.FollowupMessageModifyBuilder
import dev.kord.rest.builder.message.modify.WebhookMessageModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.kord.rest.json.request.WebhookEditMessageRequest
import dev.kord.rest.route.Route
import kotlinx.coroutines.runBlocking

class ApplicationCommandContext(
    private val event: ApplicationCommandInteractionCreateEvent,
    private val options: Map<CommandOption<*>, Any?>
) {
    /**
     * Returns the raw interaction object from the [ApplicationCommandInteractionCreateEvent].
     */
    val interaction: ApplicationCommandInteraction = event.interaction

    /**
     * Returns the [Kord] instance from the [ApplicationCommandInteractionCreateEvent].
     */
    val kord: Kord = event.kord

    /**
     * Returns the [User] object that represents the command executor.
     */
    val author: User = event.interaction.data.user.value.ifNotNull {
        User(it, kord)
    }!!

    /**
     * Returns the [Member] object if there was one present.
     */
    val member: Member? = event.interaction.data.member.value.ifNotNull {
        val userData = event.interaction.data.user.value!!
        Member(it, userData, kord)
    }

    /**
     * Returns a [command option][CommandOption] by the option object.
     * @param key The option object to search through
     * @return The result cast to [U].
     * @throws IllegalStateException If the option was required and the result was `null`.
     */
    @Suppress("UNCHECKED_CAST")
    fun <U, T: CommandOption<U>> option(key: T): U {
        if (!options.containsKey(key) && key !is NullableOption)
            throw IllegalStateException("Required option with name '${key.name}' was not provided.")

        if (key is UserCommandOption) {
            return runBlocking {
                kord.getUser(options[key] as Snowflake)
            } as U
        }

        if (key is ChannelCommandOption) {
            return runBlocking {
                kord.getChannel(options[key] as Snowflake)
            } as U
        }

        if (key is RoleCommandOption) {
            return runBlocking {
                val guild = kord.getGuild(event.interaction.data.guildId.value!!)!!
                guild.getRole(options[key] as Snowflake)
            } as U
        }

        return options[key] as U
    }

    /**
     * Returns the [Guild] that this command was executed in. Returns `null`
     * if it was in a DM channel as a globally-scoped command.
     */
    suspend fun getGuild(): Guild? {
        if (event.interaction.data.guildId.value == null)
            return null

        return event.kord.getGuild(event.interaction.data.guildId.value!!)
    }

    /**
     * Returns the channel that this context was built upon, cast as [T].
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T: Channel> getChannel(): T? = event.kord.getChannel(event.interaction.data.channelId) as? T

    /**
     * Sends out a reply to the user who has executed this command.
     * @param ephemeral If the message should be an ephemeral message or not.
     * @param builder The builder to send out.
     */
    suspend fun reply(
        ephemeral: Boolean = false,
        builder: FollowupMessageCreateBuilder.() -> Unit = {}
    ): DiscordMessage = kord.rest.interaction.createFollowupMessage(
        kord.selfId,
        interaction.token,
        FollowupMessageCreateBuilder(ephemeral).apply(builder).toRequest()
    )

    /**
     * Sends out a reply to the user who has executed this command.
     * @param content The content that should be sent out.
     * @param ephemeral If the message should be marked as ephemeral
     * @param embedBuilder The embed builder, if any.
     */
    suspend fun reply(
        content: String,
        ephemeral: Boolean = false,
        embedBuilder: (EmbedBuilder.() -> Unit)? = null
    ): DiscordMessage = reply(ephemeral) {
        if (content.isNotEmpty())
            this.content = content

        if (embedBuilder != null)
            embed(embedBuilder)
    }

    /**
     * Edits a followup message created by [#reply()][reply].
     * @param messageId The message ID that was in the reply.
     * @param builder The builder to modify the message with.
     */
    suspend fun edit(
        messageId: Snowflake,
        builder: FollowupMessageModifyBuilder.() -> Unit = {}
    ): DiscordMessage = kord.rest.interaction.modifyFollowupMessage(
        kord.selfId,
        interaction.token,
        messageId,
        FollowupMessageModifyBuilder().apply(builder).toRequest()
    )

    /**
     * Edits a followup message created by [#reply()][reply].
     * @param messageId The message ID that was in the reply.
     * @param embedBuilder The embed builder, if any.
     */
    suspend fun edit(
        messageId: Snowflake,
        content: String,
        embedBuilder: (EmbedBuilder.() -> Unit)? = null
    ): DiscordMessage = edit(messageId) {
        if (content.isNotEmpty())
            this.content = content

        if (embedBuilder != null)
            this.embed(embedBuilder)
    }

    /**
     * Edits the original message. By default, Towa defers the message update, so
     * your best options are to use [#reply()][reply] to mark the message update
     * complete, but you can use this to edit the original message, which is nothing.
     *
     * @param builder The builder object to use
     */
    @OptIn(KordUnsafe::class, KordExperimental::class)
    suspend fun editOriginalMessage(
        builder: WebhookMessageModifyBuilder.() -> Unit = {}
    ): DiscordMessage = kord.rest.unsafe(Route.OriginalInteractionResponseModify) {
        keys[Route.InteractionToken] = interaction.token
        keys[Route.ApplicationId] = kord.selfId

        val body = WebhookMessageModifyBuilder().apply(builder).toRequest()
        body(WebhookEditMessageRequest.serializer(), body.request)
        body.files.orEmpty().onEach { file(it) }
    }

    /**
     * Deletes a message that was once created by the [#reply()][reply] function.
     * @param messageId The message ID.
     */
    suspend fun delete(messageId: Snowflake) {
        kord.rest.interaction.deleteFollowupMessage(kord.selfId, interaction.token, messageId)
    }
}
