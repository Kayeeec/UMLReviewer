package org.vpreportcorrector.utils

import javafx.event.EventTarget
import javafx.scene.text.TextFlow
import org.vpreportcorrector.app.Styles
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.style
import tornadofx.text

/**
 * Typography helper functions. Must not be nested in TextFlow to preserve text wrapping.
 */

fun EventTarget.h1(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h1)
    addClass(Styles.typographyText)
    text(text)
}

fun EventTarget.h2(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h2)
    addClass(Styles.typographyText)
    text(text)
}

fun EventTarget.h3(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h3)
    addClass(Styles.typographyText)
    text(text)
}


fun EventTarget.p(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.p)
    addClass(Styles.typographyText)
    text(text){
        addClass("text")
    }
}
