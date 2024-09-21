package com.aetherui.providers.awt

import com.aetherui.components.UIWindow
import com.aetherui.layout.Dimension
import com.aetherui.providers.Provider

object AWTProvider: Provider() {
    private val internalFrames: MutableMap<UIWindow, java.awt.Frame> = mutableMapOf()

    override fun initializeWindow(window: UIWindow) {
        internalFrames[window] = java.awt.Frame(window.title)
    }

    override fun setWindowTitle(window: UIWindow, title: String) {
        internalFrames[window]?.title = title
    }

    override fun setWindowVisibility(window: UIWindow, visibility: Boolean) {
        internalFrames[window]?.isVisible = visibility
    }

    override fun setWindowSize(window: UIWindow, size: Dimension) {
        internalFrames[window]?.size = java.awt.Dimension(size.width.toInt(), size.height.toInt())
    }
}
