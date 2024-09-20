package com.aetherui.rendering.colors

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream
import kotlin.math.abs

class TestColor {
    @ParameterizedTest
    @MethodSource("rgbColors")
    fun `RGB-RGB conversions are idempotent`(input: Color.RGB) {
        assertEquals(input.red, input.toRGB().red)
        assertEquals(input.green, input.toRGB().green)
        assertEquals(input.blue, input.toRGB().blue)
        assertEquals(input.alpha, input.toRGB().alpha)
    }

    @ParameterizedTest
    @MethodSource("rgbConversions")
    fun `RGB-HSL conversions are within tolerance`(input: Color.RGB, expected: Color.HSL) {
        assertEquals(expected.hue, input.toHSL().hue, TOLERANCE_DEGREES)
        assertEquals(expected.saturation, input.toHSL().saturation, TOLERANCE_PERCENTAGE)
        assertEquals(expected.lightness, input.toHSL().lightness, TOLERANCE_PERCENTAGE)
        assertEquals(expected.alpha, input.toHSL().alpha)
    }

    @ParameterizedTest
    @MethodSource("hslColors")
    fun `HSL-HSL conversions are idempotent`(input: Color.HSL) {
        assertEquals(input.hue, input.toHSL().hue)
        assertEquals(input.saturation, input.toHSL().saturation)
        assertEquals(input.lightness, input.toHSL().lightness)
        assertEquals(input.alpha, input.toHSL().alpha)
    }

    @ParameterizedTest
    @MethodSource("hslConversions")
    fun `HSL-RGB conversions are within tolerance`(input: Color.HSL, expected: Color.RGB) {
        assertTrue(abs(expected.red.toInt() - input.toRGB().red.toInt()) <= TOLERANCE_COMPONENT)
        assertTrue(abs(expected.green.toInt() - input.toRGB().green.toInt()) <= TOLERANCE_COMPONENT)
        assertTrue(abs(expected.blue.toInt() - input.toRGB().blue.toInt()) <= TOLERANCE_COMPONENT)
        assertEquals(expected.alpha, input.toRGB().alpha)
    }

    companion object {
        private const val TOLERANCE_DEGREES = 0.25f
        private const val TOLERANCE_PERCENTAGE = 0.005f
        private const val TOLERANCE_COMPONENT = 1
        private val COLORS = listOf(
            mapOf(
                "rgb" to Color.RGB(176u, 176u, 176u),
                "hsl" to Color.HSL(0f, 0f, 0.69f)
            ),
            mapOf(
                "rgb" to Color.RGB(46u, 30u, 12u),
                "hsl" to Color.HSL(32.0f, 0.59f, 0.11f)
            ),
            mapOf(
                "rgb" to Color.RGB(86u, 42u, 120u),
                "hsl" to Color.HSL(274.0f, 0.48f, 0.32f)
            ),
            mapOf(
                "rgb" to Color.RGB(147u, 181u, 80u),
                "hsl" to Color.HSL(80.0f, 0.41f, 0.51f)
            )
        )

        private fun getColors(format: String): Stream<Color> {
            return COLORS.stream().map { colorMap -> colorMap[format] }
        }

        @JvmStatic
        fun rgbColors(): Stream<Color> {
            return getColors("rgb")
        }

        @JvmStatic
        fun hslColors(): Stream<Color> {
            return getColors("hsl")
        }

        private fun getColorConversion(initialFormat: String, finalFormat: String): Stream<Arguments> {
            return COLORS.stream().map { colorMap -> Arguments.of(colorMap[initialFormat], colorMap[finalFormat]) }
        }

        @JvmStatic
        fun rgbConversions(): Stream<Arguments> {
            return getColorConversion("rgb", "hsl")
        }

        @JvmStatic
        fun hslConversions(): Stream<Arguments> {
            return getColorConversion("hsl", "rgb")
        }
    }
}
