package com.varabyte.kobweb.compose.ui.modifiers

import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.events.SyntheticAnimationEvent

fun Modifier.animation(vararg animations: CSSAnimation) = styleModifier {
    animation(*animations)
}

fun Modifier.onAnimationEnd(onAnimationEnd: (SyntheticAnimationEvent) -> Unit): Modifier = attrsModifier {
    onAnimationEnd(onAnimationEnd)
}

fun Modifier.onAnimationIteration(onAnimationIteration: (SyntheticAnimationEvent) -> Unit): Modifier = attrsModifier {
    onAnimationIteration(onAnimationIteration)
}

fun Modifier.onAnimationStart(onAnimationStart: (SyntheticAnimationEvent) -> Unit): Modifier = attrsModifier {
    onAnimationStart(onAnimationStart)
}
