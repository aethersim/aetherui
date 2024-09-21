package com.aetherui.components

import com.aetherui.layout.Dimension
import com.aetherui.layout.Position

/**
 * A generic UI component with no functionality.
 *
 * @property position The position of the component within its container
 * @property size The actual dimensions of the component
 * @property minimumSize The minimum allowed dimensions for the component
 * @property desiredSize The desired dimensions for the component, which may or may not be honored depending on the
 *                       underlying container layout
 */
abstract class UIComponent : UIParentable, UIRenderable {
    var position = Position(0, 0)
        protected set
    var size = Dimension(0u, 0u)
        protected set
    var minimumSize = Dimension(0u, 0u)
        set(minimumSize) {
            field = minimumSize
            // TODO Repack parent
        }
    var desiredSize: Dimension? = null
        set(desiredSize) {
            field = minimumSize
            // TODO Repack parent
        }
    // TODO Bounding box

    override var parent: UIContainer? = null
        // TODO protected set
    override var parentWindow: UIWindow? = null
        // TODO protected set
}
