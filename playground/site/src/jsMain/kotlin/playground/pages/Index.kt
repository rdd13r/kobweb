package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.streams.KobwebStream
import com.varabyte.kobweb.streams.StreamEvent
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.components.layouts.PageLayout

@Page
@Composable
fun HomePage() {
PageLayout("Stream Test") {
    val stream = remember { KobwebStream("echo") }
    LaunchedEffect(Unit) {
        stream.connect { evt ->
            when (evt) {
                is StreamEvent.Opened -> send("Hello from the client!")
                is StreamEvent.Text -> println("Got text from server: \"${evt.text}\"")
                is StreamEvent.Closed -> println("Stream closed")
            }
        }
    }

    var text by remember { mutableStateOf("") }
    Input(
        InputType.Text,
        attrs = { onInput { e -> text = e.value } }
    )
    Button(onClick = { stream.send(text) }) {
        Text("Send")
    }
}
}
