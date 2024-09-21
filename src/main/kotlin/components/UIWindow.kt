package com.aetherui.components

import com.aetherui.layout.Dimension
import com.aetherui.providers.Provider

class UIWindow(title: String): UIRenderable {
    var title: String = title
        set(title) {
            if (field != title) {
                Provider.current.setWindowTitle(this, title)
            }
            field = title
        }
    var visible: Boolean = false
        set(visible) {
            if (field != visible) {
                Provider.current.setWindowVisibility(this, visible)
            }
            field = visible
        }
    var size: Dimension = Dimension(600u, 480u)
        set(size) {
            if (field != size) {
                Provider.current.setWindowSize(this, size)
            }
            field = size
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
