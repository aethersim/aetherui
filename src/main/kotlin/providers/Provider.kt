package com.aetherui.providers

import com.aetherui.components.UIWindow
import com.aetherui.layout.Dimension
import com.aetherui.providers.awt.AWTProvider

abstract class Provider {
    abstract fun initializeWindow(window: UIWindow)
    abstract fun setWindowTitle(window: UIWindow, title: String)
    abstract fun setWindowVisibility(window: UIWindow, visibility: Boolean)
    abstract fun setWindowSize(window: UIWindow, size: Dimension)

    companion object {
        val current: Provider = AWTProvider
    }
}
