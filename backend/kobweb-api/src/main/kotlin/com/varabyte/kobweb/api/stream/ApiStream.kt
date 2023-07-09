package com.varabyte.kobweb.api.stream

import com.varabyte.kobweb.api.data.Data
import com.varabyte.kobweb.api.log.Logger

/**
 * An annotation which identifies a suspend function as one which will be used to handle a streaming connection.
 *
 * The method should take a [ApiStreamContext] as its only parameter.
 *
 * The method's filename will be used to generate the stream's ID, e.g. "api/text/echo.kt" ->
 * "text/echo". Additionally, the name transformation lowercases the name, e.g. "api/Chat.kt" -> "chat"
 *
 * For technical readers, note that streams are NOT websockets. Instead, if configured, a Kobweb server will open a
 * single web socket that delegates to the appropriate stream handler based on the incoming stream ID (extracted from
 * the message sent to it).
 *
 * We use this approach instead of websockets directly for two reasons:
 * 1. this potentially reduces the number of connections a server needs to manage even if a site wants to register
 *    multiple separate streams.
 * 2. Kobweb supports a live reloading experience, and we cannot easily dynamically create and destroy websocket
 *    handlers in ktor. However, we can create a single streaming endpoint and multiplex the incoming messages to the
 *    appropriate stream handlers.
 *
 * ## routeOverride
 *
 * Note that the route generated for this page is quite customizable by setting the [routeOverride] parameter. In
 * general, you should NOT set it, as this will make it harder for people to navigate your project and find where a
 * api stream is being defined.
 *
 * However, if you do set it, in most cases, it is expected to just be a single, lowercase word, which changes the slug
 * used for this route (instead of the file name).
 *
 * But wait, there's more!
 *
 * If the value starts with a slash, it will be treated as a full path starting at the site's root. If the value ends
 * with a slash, it means the override represents a change in the URL path but the slug will still be derived from
 * the filename.
 *
 * Some examples should clear up the various cases. Let's say the site is `com.example` and this `@Api` is defined in
 * `package api.user` in file `Fetch.kt`:
 *
 * ```
 * ApiStream -> /user/fetch
 * ApiStream("retrieve") -> /user/retrieve
 * ApiStream("current/") -> /user/current/fetch
 * ApiStream("current/retrieve") -> /user/current/retrieve
 * ApiStream("/users/") -> /users/fetch
 * ApiStream("/users/retrieve") -> /users/retrieve
 * ApiStream("/") -> /fetch
 * ```
 *
 * @param routeOverride If specified, override the logic for generating a name for this API stream as documented in this
 *   header doc.
 */
abstract class ApiStream(val routeOverride: String = "") {
    class UserConnectedContext(val stream: Stream, val streamerId: StreamerId, val data: Data, val logger: Logger)
    class MessageReceivedContext(
        val stream: Stream,
        val streamerId: StreamerId,
        val message: String,
        val data: Data,
        val logger: Logger
    )

    class UserDisconnectedContext(val streamerId: StreamerId, val data: Data, val logger: Logger)

    open suspend fun onUserConnected(ctx: UserConnectedContext) = Unit
    abstract suspend fun onMessageReceived(ctx: MessageReceivedContext)
    open suspend fun onUserDisconnected(ctx: UserDisconnectedContext) = Unit
}

fun ApiStream(routeOverride: String = "", block: suspend (ApiStream.MessageReceivedContext) -> Unit) =
    object : ApiStream(routeOverride) {
        override suspend fun onMessageReceived(ctx: MessageReceivedContext) = block(ctx)
    }
