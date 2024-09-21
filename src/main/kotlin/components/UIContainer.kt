package com.aetherui.components

import com.aetherui.layout.Orientation

abstract class UIContainer(val orientation: Orientation): UIComponent() {
    abstract val components: List<UIComponent>

    override var parentWindow: UIWindow? = null
        set(parentWindow) {
            field = parentWindow
            // Set all child components' parent window after setting the container's
            for (component in components) {
                component.parentWindow = parentWindow
            }
        }

    fun pack() {
        for (orientation in arrayOf(Orientation.Horizontal, Orientation.Vertical)) {
            pack(orientation)
            parent?.pack(orientation)
            if (parent == null) {
                settle(orientation)
            }
        }
    }

    protected abstract fun pack(orientation: Orientation)

    protected fun settle(orientation: Orientation) {

    }

    protected fun adopt(component: UIComponent) {
        component.parent = this
    }

    protected fun orphan(component: UIComponent) {
        if (component.parent == this) {
            component.parent = null
        }
    }
}
