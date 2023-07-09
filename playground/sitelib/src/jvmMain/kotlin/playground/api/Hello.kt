package playground.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.stream.ApiStream

@Api
fun hello(ctx: ApiContext) {
    ctx.res.body = "hello world".toByteArray()
}
