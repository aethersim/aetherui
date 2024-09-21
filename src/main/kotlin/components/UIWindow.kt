package com.aetherui.components

import com.aetherui.layout.Dimensions
import com.aetherui.providers.Provider

class UIWindow(title: String): UIRenderable {
    var title: String = title
        set(title) {
            field = title
            Provider.current.setWindowTitle(this, title)
        }
    var visible: Boolean = false
        set(visible) {
            field = visible
            Provider.current.setWindowVisibility(this, visible)
        }
    var size: Dimensions = Dimensions(600, 480)
        set(size) {
            field = size
            Provider.current.setWindowSize(this, size)
        }

    init {
        Provider.current.initializeWindow(this)
        Provider.current.setWindowTitle(this, title)
        Provider.current.setWindowSize(this, size)
        Provider.current.setWindowVisibility(this, visible)
    }

    override fun render() {}

    internal fun handleEvent() {}
}
