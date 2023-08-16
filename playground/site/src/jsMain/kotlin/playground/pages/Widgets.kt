package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.disclosure.Tabs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.FilledInputVariant
import com.varabyte.kobweb.silk.components.forms.FlushedInputVariant
import com.varabyte.kobweb.silk.components.forms.Input
import com.varabyte.kobweb.silk.components.forms.InputGroup
import com.varabyte.kobweb.silk.components.forms.InputSize
import com.varabyte.kobweb.silk.components.forms.OutlinedInputVariant
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.silk.components.forms.SwitchShape
import com.varabyte.kobweb.silk.components.forms.SwitchSize
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.forms.UnstyledInputVariant
import com.varabyte.kobweb.silk.components.icons.fa.FaCheck
import com.varabyte.kobweb.silk.components.icons.fa.FaDollarSign
import com.varabyte.kobweb.silk.components.icons.fa.FaUser
import com.varabyte.kobweb.silk.components.icons.fa.IconStyle
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.ColorSchemes
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.attributes.AutoComplete
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import playground.components.layouts.PageLayout
import playground.components.widgets.GoHomeLink

val WidgetSectionStyle by ComponentStyle.base {
    Modifier
        .fillMaxWidth()
        .border(1.px, LineStyle.Solid, colorMode.toSilkPalette().border)
        .position(Position.Relative)
}

val WidgetPaddingStyle by ComponentStyle.base {
    Modifier
        .fillMaxSize()
        .padding(1.cssRem)
}

val WidgetLabelStyle by ComponentStyle.base {
    Modifier
        .position(Position.Relative)
        .fontSize(0.8.cssRem)
        .left(0.3.cssRem)
        .top((-.7).cssRem)
        .padding(0.2.cssRem)
        .backgroundColor(colorMode.toSilkPalette().background)
}

@Composable
fun WidgetSection(title: String, content: @Composable BoxScope.() -> Unit) {
    Box(WidgetSectionStyle.toModifier()) {
        Box(WidgetLabelStyle.toModifier()) {
            SpanText(title)
        }
        Box(WidgetPaddingStyle.toModifier()) {
            content()
        }
    }
}

@Page
@Composable
fun WidgetsPage() {
    PageLayout("WIDGETS") {
        Column(
            Modifier.gap(2.cssRem).fillMaxWidth().padding(2.cssRem).maxWidth(800.px),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WidgetSection("Button") {
                Button(onClick = {}) { Text("Click me!") }
            }

            WidgetSection("Input") {
                var text by remember { mutableStateOf("") }
                Column(Modifier.gap(0.5.cssRem).fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.gap(0.5.cssRem)) {
                            TextInput(
                                text,
                                placeholder = "extra small size",
                                size = InputSize.XS,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "small size",
                                size = InputSize.SM,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "medium size",
                                size = InputSize.MD,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "large size",
                                size = InputSize.LG,
                                onTextChanged = { text = it })
                        }

                        Spacer()

                        Column(Modifier.gap(0.5.cssRem)) {
                            TextInput(
                                text,
                                placeholder = "outlined",
                                variant = OutlinedInputVariant,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "filled",
                                variant = FilledInputVariant,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "flushed",
                                variant = FlushedInputVariant,
                                onTextChanged = { text = it })
                            TextInput(
                                text,
                                placeholder = "unstyled",
                                variant = UnstyledInputVariant,
                                onTextChanged = { text = it })
                        }
                    }

                    Hr(Modifier.fillMaxWidth().toAttrs())

                    Row(Modifier.gap(0.5.cssRem).fillMaxWidth().flexWrap(FlexWrap.Wrap)) {
                        Column(Modifier.gap(0.5.cssRem)) {
                            var telNum by remember { mutableStateOf("") }
                            InputGroup {
                                LeftInsert { Text("+1") }
                                Input(
                                    InputType.Tel,
                                    telNum,
                                    placeholder = "phone number",
                                    autoComplete = AutoComplete.telNational,
                                    onValueChanged = { telNum = it })
                            }

                            var url by remember { mutableStateOf("") }
                            InputGroup(size = InputSize.SM) {
                                LeftInsert { Text("https://") }
                                TextInput(url, placeholder = "url", onTextChanged = { url = it })
                                RightInsert { Text(".com") }
                            }

                            var dateTime by remember { mutableStateOf("") }
                            Input(InputType.DateTimeLocal, dateTime, onValueChanged = { dateTime = it })
                        }

                        Spacer()

                        Column(Modifier.gap(0.5.cssRem)) {
                            var username by remember { mutableStateOf("") }
                            InputGroup {
                                LeftOverlay { FaUser(style = IconStyle.FILLED) }
                                TextInput(
                                    username,
                                    placeholder = "username",
                                    onTextChanged = { username = it })
                            }

                            val dollarRegex = Regex("""^(\d{1,3}(,\d{3})*|(\d+))(\.\d{2})?$""")
                            var amount by remember { mutableStateOf("") }
                            InputGroup(size = InputSize.SM) {
                                LeftOverlay { FaDollarSign() }
                                TextInput(
                                    amount,
                                    placeholder = "amount",
                                    onTextChanged = { amount = it })
                                RightOverlay {
                                    if (dollarRegex.matches(amount)) {
                                        FaCheck(Modifier.color(ColorSchemes.Green._500))
                                    }
                                }
                            }

                            var showPassword by remember { mutableStateOf(false) }
                            var password by remember { mutableStateOf("") }
                            InputGroup(Modifier.width(230.px)) {
                                TextInput(
                                    password,
                                    password = !showPassword,
                                    onTextChanged = { password = it })
                                RightOverlay(width = 4.5.cssRem) {
                                    Button(
                                        onClick = { showPassword = !showPassword },
                                        // TODO: Replace with with ButtonSize.SM once we support it
                                        Modifier.height(1.5.cssRem).width(3.5.cssRem).padding(0.px).fontSize(0.8.cssRem)
                                    ) {
                                        Text(if (showPassword) "Hide" else "Show")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            WidgetSection("Switch") {
                Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                    SwitchShape.values().forEach { shape ->
                        Row(Modifier.gap(1.cssRem), verticalAlignment = Alignment.CenterVertically) {
                            listOf(SwitchSize.SM, SwitchSize.MD, SwitchSize.LG).forEach { size ->
                                var checked by remember { mutableStateOf(false) }
                                Switch(
                                    checked,
                                    onCheckedChange = { checked = it },
                                    size = size,
                                    shape = shape
                                )
                            }
                        }
                    }
                }
            }

            WidgetSection("Tabs") {
                Tabs {
                    TabPanel {
                        Tab { Text("Tab 1") }; Panel { Text("Panel 1") }
                    }
                    TabPanel {
                        Tab { Text("Tab 2") }; Panel { Text("Panel 2") }
                    }
                    TabPanel {
                        Tab { Text("Tab 3") }; Panel { Text("Panel 3") }
                    }
                }
            }

            GoHomeLink()
        }
    }
}
