package com.aetherui.components

import com.aetherui.layout.Constraints
import com.aetherui.layout.Dimensions
import com.aetherui.layout.Position

/**
 * A generic UI component with no functionality.
 *
 * @property position The position of the component within its container
 * @property size The actual dimensions of the component
 * @property constraints The constraints to apply when building component layouts
 */
abstract class UIComponent : UIParentable, UIRenderable {
    var position = Position(0, 0)
        protected set
    var size = Dimensions(0, 0)
        protected set
    val constraints = Constraints()
    // TODO Bounding box

    override var parent: UIContainer? = null
        // TODO protected set
    override var parentWindow: UIWindow? = null
        // TODO protected set
}
