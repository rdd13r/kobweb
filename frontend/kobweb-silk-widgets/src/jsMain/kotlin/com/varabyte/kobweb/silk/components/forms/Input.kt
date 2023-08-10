package com.varabyte.kobweb.silk.components.forms

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.ComponentVariant
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.common.PlaceholderColor
import com.varabyte.kobweb.silk.components.style.disabled
import com.varabyte.kobweb.silk.components.style.focusVisible
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.not
import com.varabyte.kobweb.silk.components.style.placeholder
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorVar
import com.varabyte.kobweb.silk.theme.colors.PlaceholderOpacityVar
import org.jetbrains.compose.web.attributes.AutoComplete
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.autoComplete
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.readOnly
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement

object InputDefaults {
    const val Valid = true
    const val Enabled = true
    const val ReadOnly = false
    const val SpellCheck = false
    val Size = InputSize.MD
    val Variant = OutlinedInputVariant
}

val InputBorderColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputBorderRadiusVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val InputBorderFocusColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputBorderInvalidColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputFilledColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputFilledHoverColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputFilledFocusColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputFontSizeVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val InputHeightVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val InputPaddingVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val InputPlaceholderOpacityVar by StyleVariable(prefix = "silk", defaultFallback = PlaceholderOpacityVar.value())
val InputPlaceholderColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val InputOverlayLeftWidth by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 2.25.cssRem)
val InputOverlayRightWidth by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 2.25.cssRem)

val InputGroupStyle by ComponentStyle.base(prefix = "silk") {
    Modifier
        .outline(0.px, LineStyle.Solid, Colors.Transparent) // Disable, we'll use box shadow instead
        .border(0.px, LineStyle.Solid, Colors.Transparent) // Overridden by variants
        .fontSize(InputFontSizeVar.value())
}

val InputStyle by ComponentStyle(prefix = "silk") {
    base {
        Modifier
            .styleModifier { property("appearance", "none") } // Disable browser styles
            .color(ColorVar.value())
            .height(InputHeightVar.value())
            .fontSize(InputFontSizeVar.value())
            .backgroundColor(Colors.Transparent)
            .outline(0.px, LineStyle.Solid, Colors.Transparent) // Disable, we'll use box shadow instead
            .border(0.px, LineStyle.Solid, Colors.Transparent) // Overridden by variants
            .transition(CSSTransition.group(listOf("border-color", "box-shadow", "background-color"), 200.ms))
    }

    placeholder {
        Modifier
            .opacity(InputPlaceholderOpacityVar.value())
            .color(InputPlaceholderColorVar.value())
    }
}

private fun Modifier.inputPadding(): Modifier {
    val padding = InputPaddingVar.value()
    return this.paddingInline(start = padding, end = padding)
}

val OutlinedInputVariant by InputStyle.addVariant {
    base {
        Modifier
            .inputPadding()
            .borderRadius(InputBorderRadiusVar.value())
            .border(1.px, LineStyle.Solid, InputBorderColorVar.value())
    }
    (focusVisible + not(disabled)) {
        Modifier
            .border(1.px, LineStyle.Solid, InputBorderFocusColorVar.value())
            .boxShadow(spreadRadius = 1.px, color = InputBorderFocusColorVar.value())
    }
}

val FilledInputVariant by InputStyle.addVariant {
    base {
        Modifier
            .inputPadding()
            .backgroundColor(InputFilledColorVar.value())
            .borderRadius(InputBorderRadiusVar.value())
            .border(1.px, LineStyle.Solid, Colors.Transparent)
    }
    (hover + not(disabled)) {
        Modifier.backgroundColor(InputFilledHoverColorVar.value())
    }
    (focusVisible + not(disabled)) {
        Modifier
            .backgroundColor(InputFilledFocusColorVar.value())
            .borderColor(InputBorderFocusColorVar.value())
            .boxShadow(spreadRadius = 1.px, color = InputBorderFocusColorVar.value())
    }
}

val FlushedInputVariant by InputStyle.addVariant {
    base { Modifier.borderBottom(1.px, LineStyle.Solid, InputBorderColorVar.value()) }
    (focusVisible + not(disabled)) {
        Modifier
            .borderColor(InputBorderFocusColorVar.value())
            .boxShadow(offsetY = 1.px, color = InputBorderFocusColorVar.value())

    }
}

val UnstyledInputVariant by InputStyle.addVariant {}

@DslMarker
annotation class InputScopeMarker

internal class InputParams<T : Any>(
    private val type: InputType<T>,
    private val value: T,
    private val modifier: Modifier = Modifier,
    private val variant: ComponentVariant? = InputDefaults.Variant,
    private val placeholder: String? = null,
    private val placeholderColor: PlaceholderColor? = null,
    private val focusBorderColor: CSSColorValue? = null,
    private val invalidBorderColor: CSSColorValue? = null,
    private val enabled: Boolean = InputDefaults.Enabled,
    private val valid: Boolean = InputDefaults.Valid,
    private val readOnly: Boolean = InputDefaults.ReadOnly,
    private val spellCheck: Boolean = InputDefaults.SpellCheck,
    private val autoComplete: AutoComplete? = null,
    private val onValueChanged: (T) -> Unit = {},
    private val onCommit: () -> Unit = {},
    private val ref: ((HTMLInputElement) -> Unit)? = null
) {
    @Composable
    fun renderInput(modifier: Modifier = Modifier) {
        _Input(
            type,
            value,
            this.modifier.then(modifier),
            variant,
            placeholder,
            placeholderColor,
            focusBorderColor,
            invalidBorderColor,
            enabled,
            valid,
            readOnly,
            spellCheck,
            autoComplete,
            onValueChanged,
            onCommit,
            ref,
        )

    }
}

@InputScopeMarker
class InputGroupScope {
    internal var inputParams: InputParams<out Any>? = null
    internal var leftInsert: (@Composable BoxScope.() -> Unit)? = null
    internal var rightInsert: (@Composable BoxScope.() -> Unit)? = null
    internal var leftOverlay: (@Composable BoxScope.() -> Unit)? = null
    internal var leftOverlayWidth: CSSLengthOrPercentageValue? = null
    internal var rightOverlay: (@Composable BoxScope.() -> Unit)? = null
    internal var rightOverlayWidth: CSSLengthOrPercentageValue? = null

    fun <T : Any> Input(
        type: InputType<T>,
        value: T,
        modifier: Modifier = Modifier,
        variant: ComponentVariant? = InputDefaults.Variant,
        placeholder: String? = null,
        placeholderColor: PlaceholderColor? = null,
        focusBorderColor: CSSColorValue? = null,
        invalidBorderColor: CSSColorValue? = null,
        enabled: Boolean = InputDefaults.Enabled,
        valid: Boolean = InputDefaults.Valid,
        readOnly: Boolean = InputDefaults.ReadOnly,
        spellCheck: Boolean = InputDefaults.SpellCheck,
        autoComplete: AutoComplete? = null,
        onValueChanged: (T) -> Unit = {},
        onCommit: () -> Unit = {},
        ref: ((HTMLInputElement) -> Unit)? = null
    ) {
        require(inputParams == null) { "Can only call `Input` once" }

        inputParams = InputParams(
            type,
            value,
            modifier,
            variant,
            placeholder,
            placeholderColor,
            focusBorderColor,
            invalidBorderColor,
            enabled,
            valid,
            readOnly,
            spellCheck,
            autoComplete,
            onValueChanged,
            onCommit,
            ref
        )
    }

    fun LeftInsert(block: @Composable BoxScope.() -> Unit) {
        require(leftInsert == null && leftOverlay == null) { "Can only set one left insert or overlay element" }
        leftInsert = block
    }

    fun RightInsert(block: @Composable BoxScope.() -> Unit) {
        require(rightInsert == null && rightOverlay == null) { "Can only set one right insert or overlay element" }
        rightInsert = block
    }

    fun LeftOverlay(width: CSSLengthOrPercentageValue? = null, block: @Composable BoxScope.() -> Unit) {
        require(leftInsert == null && leftOverlay == null) { "Can only set one left insert or overlay element" }
        leftOverlay = block
        leftOverlayWidth = width
    }

    fun RightOverlay(width: CSSLengthOrPercentageValue? = null, block: @Composable BoxScope.() -> Unit) {
        require(rightInsert == null && rightOverlay == null) { "Can only set one right insert or overlay element" }
        rightOverlay = block
        rightOverlayWidth = width
    }
}

fun InputGroupScope.TextInput(
    text: String,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = InputDefaults.Variant,
    placeholder: String? = null,
    placeholderColor: PlaceholderColor? = null,
    focusBorderColor: CSSColorValue? = null,
    invalidBorderColor: CSSColorValue? = null,
    password: Boolean = false,
    enabled: Boolean = InputDefaults.Enabled,
    valid: Boolean = InputDefaults.Valid,
    readOnly: Boolean = InputDefaults.ReadOnly,
    spellCheck: Boolean = InputDefaults.SpellCheck,
    autoComplete: AutoComplete? = null,
    onTextChanged: (String) -> Unit = {},
    onCommit: () -> Unit = {},
    ref: ((HTMLInputElement) -> Unit)? = null,
) {
    Input(
        if (password) InputType.Password else InputType.Text,
        text,
        modifier,
        variant,
        placeholder,
        placeholderColor,
        focusBorderColor,
        invalidBorderColor,
        enabled,
        valid,
        readOnly,
        spellCheck,
        autoComplete,
        onValueChanged = { onTextChanged(it) },
        onCommit,
        ref,
    )
}


interface InputSize {
    val fontSize: CSSLengthValue
    val height: CSSLengthValue
    val padding: CSSLengthValue
    val borderRadius: CSSLengthValue

    object XS : InputSize {
        override val fontSize = 0.75.cssRem
        override val height = 1.25.cssRem
        override val padding = 0.5.cssRem
        override val borderRadius = 0.05.cssRem
    }

    object SM : InputSize {
        override val fontSize = 0.875.cssRem
        override val height = 1.75.cssRem
        override val padding = 0.75.cssRem
        override val borderRadius = 0.1.cssRem
    }

    object MD : InputSize {
        override val fontSize = 1.cssRem
        override val height = 2.25.cssRem
        override val padding = 1.cssRem
        override val borderRadius = 0.375.cssRem
    }

    object LG : InputSize {
        override val fontSize = 1.125.cssRem
        override val height = 2.5.cssRem
        override val padding = 1.cssRem
        override val borderRadius = 0.375.cssRem
    }
}

private fun InputSize.toModifier() = Modifier
    .setVariable(InputBorderRadiusVar, borderRadius)
    .setVariable(InputFontSizeVar, fontSize)
    .setVariable(InputHeightVar, height)
    .setVariable(InputPaddingVar, padding)

private fun PlaceholderColor.toModifier(): Modifier {
    return Modifier
        .setVariable(InputPlaceholderColorVar, color)
        .setVariable(InputPlaceholderOpacityVar, opacity)
}

@Composable
private fun <T : Any> _Input(
    type: InputType<T>,
    value: T,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    placeholder: String? = null,
    placeholderColor: PlaceholderColor? = null,
    focusBorderColor: CSSColorValue? = null,
    invalidBorderColor: CSSColorValue? = null,
    enabled: Boolean = InputDefaults.Enabled,
    valid: Boolean = InputDefaults.Valid,
    readOnly: Boolean = InputDefaults.ReadOnly,
    spellCheck: Boolean = InputDefaults.SpellCheck,
    autoComplete: AutoComplete? = null,
    onValueChanged: (T) -> Unit = {},
    onCommit: () -> Unit = {},
    ref: ((HTMLInputElement) -> Unit)? = null,
) {
    Input(
        type,
        attrs = InputStyle
            .toModifier(variant)
            .thenIf(placeholderColor != null) { placeholderColor!!.toModifier() }
            .thenIf(focusBorderColor != null) { Modifier.setVariable(InputBorderFocusColorVar, focusBorderColor!!) }
            .thenIf(invalidBorderColor != null) {
                Modifier.setVariable(
                    InputBorderInvalidColorVar,
                    invalidBorderColor!!
                )
            }
            .thenIf(!valid) { Modifier.setVariable(InputBorderColorVar, InputBorderInvalidColorVar.value()) }
            .then(modifier)
            .toAttrs {
                if (ref != null) {
                    this.ref { element ->
                        ref(element)
                        onDispose { }
                    }
                }

                when (value) {
                    is String -> value(value)
                    is Number -> value(value)
                    is Boolean -> checked(value)
                    is Unit -> {}
                    else -> error("Unexpected `Input` value type: ${value::class}")
                }

                placeholder?.let { this.placeholder(it) }
                if (!enabled) disabled()
                if (readOnly) readOnly()
                spellCheck(spellCheck)
                autoComplete?.let { this.autoComplete(it) }

                onInput { evt -> onValueChanged(type.inputValue(evt.nativeEvent)) }
                onKeyUp { evt ->
                    if (evt.code == "Enter") {
                        evt.preventDefault()
                        onCommit()
                    }
                }
            }
    )
}

@Composable
fun TextInput(
    text: String,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = InputDefaults.Variant,
    placeholder: String? = null,
    placeholderColor: PlaceholderColor? = null,
    focusBorderColor: CSSColorValue? = null,
    invalidBorderColor: CSSColorValue? = null,
    size: InputSize = InputDefaults.Size,
    password: Boolean = false,
    enabled: Boolean = InputDefaults.Enabled,
    valid: Boolean = InputDefaults.Valid,
    readOnly: Boolean = InputDefaults.ReadOnly,
    spellCheck: Boolean = InputDefaults.SpellCheck,
    autoComplete: AutoComplete? = null,
    onTextChanged: (String) -> Unit = {},
    onCommit: () -> Unit = {},
    ref: ((HTMLInputElement) -> Unit)? = null,
) {
    Input(
        if (!password) InputType.Text else InputType.Password,
        text,
        modifier,
        variant,
        placeholder,
        size,
        placeholderColor,
        focusBorderColor,
        invalidBorderColor,
        enabled,
        valid,
        readOnly,
        spellCheck,
        autoComplete,
        onValueChanged = { onTextChanged(it) },
        onCommit,
        ref,
    )
}

@Composable
fun <T : Any> Input(
    type: InputType<T>,
    value: T,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = InputDefaults.Variant,
    placeholder: String? = null,
    size: InputSize = InputDefaults.Size,
    placeholderColor: PlaceholderColor? = null,
    focusBorderColor: CSSColorValue? = null,
    invalidBorderColor: CSSColorValue? = null,
    enabled: Boolean = InputDefaults.Enabled,
    valid: Boolean = InputDefaults.Valid,
    readOnly: Boolean = InputDefaults.ReadOnly,
    spellCheck: Boolean = InputDefaults.SpellCheck,
    autoComplete: AutoComplete? = null,
    onValueChanged: (T) -> Unit = {},
    onCommit: () -> Unit = {},
    ref: ((HTMLInputElement) -> Unit)? = null
) {
    _Input(
        type,
        value,
        size.toModifier().then(modifier),
        variant,
        placeholder,
        placeholderColor,
        focusBorderColor,
        invalidBorderColor,
        enabled,
        valid,
        readOnly,
        spellCheck,
        autoComplete,
        onValueChanged,
        onCommit,
        ref,
    )
}

@Composable
fun InputGroup(
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    size: InputSize = InputDefaults.Size,
    block: InputGroupScope.() -> Unit,
) {
    val scope = InputGroupScope().apply(block)
    val inputParams = scope.inputParams ?: error("Must call `Input` within `InputGroup` block.")
    Row(
        InputGroupStyle.toModifier(variant)
            .then(size.toModifier())
            .position(Position.Relative) // So we can place overlay elements
            .alignItems(AlignItems.Stretch)
            .then(modifier)
    ) {
        var finalInputModifier: Modifier = Modifier.width(100.percent)

        if (scope.leftInsert != null) {
            finalInputModifier = finalInputModifier.styleModifier {
                property("border-top-left-radius", 0.px)
                property("border-bottom-left-radius", 0.px)
            }
        } else if (scope.leftOverlay != null) {
            finalInputModifier = finalInputModifier.styleModifier {
                paddingInlineStart(scope.leftOverlayWidth ?: InputOverlayLeftWidth.value())
            }
        }

        if (scope.rightInsert != null) {
            finalInputModifier = finalInputModifier.styleModifier {
                property("border-top-right-radius", 0.px)
                property("border-bottom-right-radius", 0.px)
            }
        } else if (scope.rightOverlay != null) {
            finalInputModifier = finalInputModifier.styleModifier {
                paddingInlineEnd(scope.rightOverlayWidth ?: InputOverlayRightWidth.value())
            }
        }

        // Render inserts (if set) and the main input

        scope.leftInsert?.let { leftInsert ->
            val padding = InputPaddingVar.value()
            Box(Modifier.styleModifier {
                property("border-top-left-radius", InputBorderRadiusVar.value())
                property("border-bottom-left-radius", InputBorderRadiusVar.value())
                property("border-top-right-radius", 0.px)
                property("border-bottom-right-radius", 0.px)
            }
                .border(1.px, LineStyle.Solid, InputBorderColorVar.value())
                .borderRight(0.px) // prevent double border with input
                .paddingInline(start = padding, end = padding)
                .backgroundColor(InputFilledColorVar.value()), contentAlignment = Alignment.Center) {
                leftInsert()
            }
        }

        inputParams.renderInput(finalInputModifier)

        scope.rightInsert?.let { rightInsert ->
            val padding = InputPaddingVar.value()
            Box(Modifier
                .styleModifier {
                    property("border-top-left-radius", 0.px)
                    property("border-bottom-left-radius", 0.px)
                    property("border-top-right-radius", InputBorderRadiusVar.value())
                    property("border-bottom-right-radius", InputBorderRadiusVar.value())
                }
                .border(1.px, LineStyle.Solid, InputBorderColorVar.value())
                .borderLeft(0.px) // prevent double border with input
                .paddingInline(start = padding, end = padding)
                .backgroundColor(InputFilledColorVar.value()), contentAlignment = Alignment.Center) {
                rightInsert()
            }
        }

        // Render overlays (if any)

        scope.leftOverlay?.let { leftOverlay ->
            Box(
                Modifier
                    .position(Position.Absolute).top(0.px).bottom(0.px).left(0.px)
                    .width(scope.leftOverlayWidth ?: InputOverlayRightWidth.value()),
                contentAlignment = Alignment.Center
            ) {
                leftOverlay()
            }
        }

        scope.rightOverlay?.let { rightOverlay ->
            Box(
                Modifier
                    .position(Position.Absolute).top(0.px).bottom(0.px).right(0.px)
                    .width(scope.rightOverlayWidth ?: InputOverlayRightWidth.value()),
                contentAlignment = Alignment.Center
            ) {
                rightOverlay()
            }
        }
    }
}
