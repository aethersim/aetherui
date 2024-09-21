package com.aetherui.layout

/**
 * A dimension in 2D space.
 *
 * Dimensions are defined as a rectangular bounding box with a width and a height.
 *
 * @property width The horizontal dimension
 * @property height The vertical dimension
 */
data class Dimensions(val width: Int, val height: Int) {
    init {
        require(width >= 0) { "Width must be positive" }
        require(height >= 0) { "Height must be positive" }
    }
}
