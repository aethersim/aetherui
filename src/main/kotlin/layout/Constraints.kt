package com.aetherui.layout

class Constraints {
    data class Directions<T : Units?>(var start: T, var top: T, var end: T, var bottom: T) {
        constructor(x: T, y: T) : this(x, y, x, y)
        constructor(all: T) : this(all, all, all, all)

        fun forNegativeOrientation(orientation: Orientation): T {
            return when(orientation) {
                Orientation.Horizontal -> start
                Orientation.Vertical -> top
            }
        }

        fun forPositiveOrientation(orientation: Orientation): T {
            return when(orientation) {
                Orientation.Horizontal -> end
                Orientation.Vertical -> bottom
            }
        }
    }

    data class Dimensions<T : Units?>(var width: T, var height: T) {
        constructor(all: T) : this(all, all)

        fun forOrientation(orientation: Orientation): T {
            return when(orientation) {
                Orientation.Horizontal -> width
                Orientation.Vertical -> height
            }
        }
    }

    var margins: Directions<Units> = Directions(all = Units.scalar(0))
    var padding: Directions<Units> = Directions(all = Units.scalar(0))

    var sizeMinimum: Dimensions<Units.Fixed> = Dimensions(all = Units.scalar(0))
    var sizeDesired: Dimensions<Units> = Dimensions(all = Units.Auto)
    var sizeMaximum: Dimensions<Units.Fixed?> = Dimensions(all = null)
}
