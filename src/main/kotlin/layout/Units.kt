package com.aetherui.layout

import kotlin.math.roundToInt

abstract class Units {
    data class Fixed(val value: Int = 0, val ratio: Float = 0f): Units() {
        operator fun plus(other: Fixed): Fixed {
            return Fixed(value + other.value, ratio + other.ratio)
        }

        fun rectify(): Fixed {
            return Fixed(value = (value / (1 - ratio)).roundToInt())
        }
    }

    object Auto : Units()
    object Fill : Units()

    companion object {
        fun scalar(value: Int): Fixed {
            return Fixed(value = value)
        }

        fun ratio(ratio: Float): Fixed {
            return Fixed(ratio = ratio)
        }

        fun min(first: Fixed, second: Fixed): Fixed {
            return if (first.rectify().value < second.rectify().value) first else second
        }

        fun max(first: Fixed, second: Fixed): Fixed {
            return if (first.rectify().value > second.rectify().value) first else second
        }
    }
//    abstract fun inPixels(spaceAvailable: Int): Int
//
//    abstract class DefiniteUnits : Units() {
//
//    }
//
//    object None: Units() {
//        override fun inPixels(spaceAvailable: Int) = 0
//    }
//
//    object Auto: Units() {
//        override fun inPixels(spaceAvailable: Int): Int = spaceAvailable
//    }
//
//    data class Scalar(val value: Int): DefiniteUnits() {
//        override fun inPixels(spaceAvailable: Int): Int = value
//    }
//
//    data class Ratio(val ratio: Float): DefiniteUnits() {
//        init {
//            require(ratio in 0.0..1.0) { "Ratio must be between 0 and 1" }
//        }
//
//        override fun inPixels(spaceAvailable: Int): Int {
//            return (spaceAvailable * ratio).toInt()
//        }
//    }
}
