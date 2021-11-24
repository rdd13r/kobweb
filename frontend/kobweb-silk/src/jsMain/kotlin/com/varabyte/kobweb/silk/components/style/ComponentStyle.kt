package com.varabyte.kobweb.silk.components.style

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asStyleBuilder
import com.varabyte.kobweb.compose.ui.modifiers.classNames
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.SilkConfigInstance
import com.varabyte.kobweb.silk.theme.SilkTheme
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import org.jetbrains.compose.web.css.StyleBuilder
import org.jetbrains.compose.web.css.StylePropertyValue
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.media
import org.jetbrains.compose.web.css.mediaMinWidth

// We need our own implementation of StyleBuilder, so we can both test equality and pull values out of it later
private class SimpleStyleBuilder : StyleBuilder {
    val properties = LinkedHashMap<String, String>() // Preserve insertion order
    val variables = LinkedHashMap<String, String>() // Preserve insertion order

    override fun property(propertyName: String, value: StylePropertyValue) {
        properties[propertyName] = value.toString()
    }

    override fun variable(variableName: String, value: StylePropertyValue) {
        variables[variableName] = value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return (other is SimpleStyleBuilder) && properties == other.properties && variables == other.variables
    }

    override fun hashCode(): Int {
        return properties.hashCode() + variables.hashCode()
    }
}

/**
 * Class used as the receiver to a callback, allowing the user to define various state-dependent styles (defined via
 * [Modifier]s).
 */
class ComponentModifiers {
    /** Base styles for this component, will always be applied first. */
    var base: Modifier? = null

    /**
     * Styles to apply to components that represent navigation links which have not yet been visited.
     *
     * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/:link
     */
    var link: Modifier? = null

    /**
     * Styles to apply to components that represent navigation links which have previously been visited.
     *
     * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/:visited
     */
    var visited: Modifier? = null

    /**
     * Styles to apply to components when a cursor is pointing at them.
     *
     * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/:hover
     */
    var hover: Modifier? = null

    /**
     * Styles to apply to components when a cursor is interacting with them.
     *
     * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/:active
     */
    var active: Modifier? = null

    /**
     * Register layout styles which are dependent on the current window width
     *
     * Breakpoints will be applied in order from smallest to largest.
     */
    val breakpoints = mutableMapOf<Breakpoint, Modifier>()
}

/**
 * A class which allows a user to define styles that get added to the page's stylesheet, instead of just using
 * inline styles.
 *
 * This is important because some functionality is only available when defined in the stylesheet, e.g. link colors,
 * media queries, and psuedo classes.
 */
class ComponentStyle internal constructor(private val name: String) {
    companion object {
        operator fun invoke(name: String, init: ComponentModifiers.(ColorMode) -> Unit) =
            ComponentStyleBuilder(name, init)
    }

    @Composable
    fun toModifier(): Modifier {
        return Modifier.classNames(name, "$name-${getColorMode().name.lowercase()}")
    }
}

private sealed interface StyleGroup {
    class Light(val styles: SimpleStyleBuilder) : StyleGroup
    class Dark(val styles: SimpleStyleBuilder) : StyleGroup
    class ColorAgnostic(val styles: SimpleStyleBuilder) : StyleGroup
    class ColorAware(val lightStyles: SimpleStyleBuilder, val darkStyles: SimpleStyleBuilder) : StyleGroup

    companion object {
        @Suppress("NAME_SHADOWING") // Shadowing used to turn nullable into non-null
        fun from(lightModifiers: Modifier?, darkModifiers: Modifier?): StyleGroup? {
            val lightStyles = lightModifiers?.let { lightModifiers ->
                SimpleStyleBuilder().apply { lightModifiers.asStyleBuilder().invoke(this) }
            }
            val darkStyles = darkModifiers?.let { darkModifiers ->
                SimpleStyleBuilder().apply { darkModifiers.asStyleBuilder().invoke(this) }
            }

            if (lightStyles == null && darkStyles == null) return null
            if (lightStyles != null && darkStyles == null) return Light(lightStyles)
            if (lightStyles == null && darkStyles != null) return Dark(darkStyles)
            check(lightStyles != null && darkStyles != null)
            return if (lightStyles == darkStyles) {
                ColorAgnostic(lightStyles)
            } else {
                ColorAware(lightStyles, darkStyles)
            }
        }
    }
}

class ComponentStyleBuilder internal constructor(
    val name: String,
    private val init: ComponentModifiers.(ColorMode) -> Unit,
) {
    internal val variants = mutableListOf<ComponentVariant>()

    fun addVariant(name: String, init: ComponentModifiers.(ColorMode) -> Unit): ComponentVariant {
        return ComponentVariant(ComponentStyleBuilder("${this.name}-$name", init), baseStyle = this).also {
            variants.add(it)
        }
    }

    private fun StyleSheet.addStyles(selectorName: String, pseudoClass: String?, styles: SimpleStyleBuilder) {
        val classSelector = if (pseudoClass != null) "$selectorName:$pseudoClass" else selectorName
        this.apply {
            classSelector style {
                styles.properties.forEach { entry -> property(entry.key, entry.value) }
                styles.variables.forEach { entry -> variable(entry.key, entry.value) }
            }
        }
    }

    /**
     * Shared logic for using an initial selector name and triggering a callback with the final selector name and
     * CSS styles to be associated with it.
     */
    private fun withFinalSelectorName(selectorBaseName: String, group: StyleGroup, handler: (String, SimpleStyleBuilder) -> Unit) {
        when (group) {
            is StyleGroup.Light -> handler("$selectorBaseName-light", group.styles)
            is StyleGroup.Dark -> handler("$selectorBaseName-dark", group.styles)
            is StyleGroup.ColorAgnostic -> handler(selectorBaseName, group.styles)
            is StyleGroup.ColorAware -> {
                handler("$selectorBaseName-light", group.lightStyles)
                handler("$selectorBaseName-dark", group.darkStyles)
            }
        }
    }

    private fun StyleSheet.addStyles(selectorName: String, pseudoClass: String?, group: StyleGroup) {
        withFinalSelectorName(selectorName, group) { name, styles ->
            addStyles(name, pseudoClass, styles)
        }
    }

    private fun StyleSheet.addStyles(selectorName: String, breakpoint: Breakpoint, styles: SimpleStyleBuilder) {
        media(mediaMinWidth(SilkConfigInstance.breakpoints.getValue(breakpoint))) {
            selectorName style {
                styles.properties.forEach { entry -> property(entry.key, entry.value) }
                styles.variables.forEach { entry -> variable(entry.key, entry.value) }
            }
        }
    }

    private fun StyleSheet.addStyles(selectorName: String, breakpoint: Breakpoint, group: StyleGroup) {
        withFinalSelectorName(selectorName, group) { name, styles ->
            addStyles(name, breakpoint, styles)
        }
    }

    internal fun addStyles(styleSheet: StyleSheet, selectorName: String) {
        val lightModifiers = ComponentModifiers().apply { init(ColorMode.LIGHT) }
        val darkModifiers = ComponentModifiers().apply { init(ColorMode.DARK) }

        StyleGroup.from(lightModifiers.base, darkModifiers.base)?.let { group ->
            styleSheet.addStyles(selectorName, null, group)
        }
        StyleGroup.from(lightModifiers.link, darkModifiers.link)?.let { group ->
            styleSheet.addStyles(selectorName, "link", group)
        }
        StyleGroup.from(lightModifiers.visited, darkModifiers.visited)?.let { group ->
            styleSheet.addStyles(selectorName, "visited", group)
        }
        StyleGroup.from(lightModifiers.hover, darkModifiers.hover)?.let { group ->
            styleSheet.addStyles(selectorName, "hover", group)
        }
        StyleGroup.from(lightModifiers.active, darkModifiers.active)?.let { group ->
            styleSheet.addStyles(selectorName, "active", group)
        }

        for (breakpoint in Breakpoint.values()) {
            StyleGroup.from(lightModifiers.breakpoints[breakpoint], darkModifiers.breakpoints[breakpoint])?.let { group ->
                styleSheet.addStyles(selectorName, breakpoint, group)
            }
        }
    }

    internal fun addStyles(styleSheet: StyleSheet) {
        addStyles(styleSheet, ".$name")
    }
}

class ComponentVariant(internal val style: ComponentStyleBuilder, private val baseStyle: ComponentStyleBuilder) {
    fun addStyles(styleSheet: StyleSheet) {
        // If you are using a variant, require it be associated with a tag already associated with the base style
        // e.g. if you have a link variant ("silk-link-undecorated") it should only be applied if the tag is also
        // a link (so this would be registered as ".silk-link.silk-link-undecorated").
        // To put it another way, if you use a link variant with a surface widget, it won't be applied.
        style.addStyles(styleSheet, ".${baseStyle.name}.${style.name}")
    }
}


@Composable
fun ComponentStyleBuilder.toModifier(variant: ComponentVariant? = null): Modifier {
    return SilkTheme.componentStyles.getValue(name).toModifier().then(
        variant?.style?.toModifier() ?: Modifier
    )
}