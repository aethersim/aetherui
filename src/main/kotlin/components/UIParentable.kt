package com.aetherui.components

/**
 * A UI element that can be placed within a container or a window.
 *
 * @property parent The parent [UIContainer] for this component, if applicable
 * @property parentWindow The parent [UIWindow] for this component, if applicable
 */
interface UIParentable {
    val parent: UIContainer?
    val parentWindow: UIWindow?
}
