package com.varabyte.kobweb.api.stream

import com.varabyte.kobweb.api.data.Data
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import com.varabyte.kobweb.api.log.Logger

/**
 * A container for a bunch of relevant utility classes that may be needed when handling a streaming event.
 *
 * The classes can be used to query the current state of the API call as well as respond to it.
 *
 * @property data Readonly data store potentially populated by methods annotated with [InitApi].
 *   See also: [InitApiContext].
 */
class ApiStreamContext(
    val event: StreamEvent,
    val data: Data,
    val logger: Logger,
)
