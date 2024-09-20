package com.aetherui.rendering.colors

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The superclass for all displayable colors.
 *
 * Colors can be defined in either the Red-Green-Blue (RGB) space using [Color.RGB] or the Hue-Saturation-Lightness
 * (HSL) space using [Color.HSL].  Colors can be converted between color spaces after they are created, but this may
 * result in a loss of precision (e.g., converting from RGB to HSL and back again may result in a slightly different
 * color).
 *
 * @property alpha The alpha component for the color, from `0f` to `1f`
 */
abstract class Color protected constructor(alpha: Float) {
    val alpha: Float = Math.clamp(alpha, 0f, 1f)

    /**
     * Converts the color to the Red-Green-Blue (RGB) space.
     *
     * If the color is currently defined in a different space, this conversion may result in a loss of precision (e.g.,
     * converting from HSL to RGB and back again may result in a slightly different color).
     *
     * @return the closest representation of the current color in the RGB space
     */
    abstract fun toRGB(): RGB

    /**
     * Converts the color to the Hue-Saturation-Lightness (HSL) space.
     *
     * If the color is currently defined in a different space, this conversion may result in a loss of precision (e.g.,
     * converting from RGB to HSL and back again may result in a slightly different color).
     *
     * @return the closest representation of the current color in the HSL space
     */
    abstract fun toHSL(): HSL

    abstract override fun toString(): String

    /**
     * A color in the Red-Green-Blue (RGB) space.
     *
     * Each color component is 8 bits, so their values can range from `0u` to `255u`.
     *
     * @property red The red component for the color, from `0u` to `255u`
     * @property green The green component for the color, from `0u` to `255u`
     * @property blue The blue component for the color, from `0u` to `255u`
     */
    class RGB(val red: UByte, val green: UByte, val blue: UByte, alpha: Float = 1f) : Color(alpha) {
        override fun toRGB(): RGB = this

        override fun toHSL(): HSL {
            val normalizedComponents = getNormalizedComponents();
            val componentExtrema = getComponentExtrema(normalizedComponents)
            val (minimumComponent, maximumComponent) = componentExtrema
            val (chroma, hue) = getHueAndChroma(normalizedComponents, componentExtrema)
            val lightness = (maximumComponent + minimumComponent) / 2;
            val saturation = if (lightness == 0f || lightness == 1f) 0f else chroma / (1 - abs(2 * lightness - 1))

            return HSL(hue, saturation, lightness, alpha)
        }

        private fun getNormalizedComponents(): Triple<Float, Float, Float> {
            return Triple(
                red.toFloat() / 255,
                green.toFloat() / 255,
                blue.toFloat() / 255,
            )
        }

        private fun getComponentExtrema(normalizedComponents: Triple<Float, Float, Float>): Pair<Float, Float> {
            val (normalizedRed, normalizedGreen, normalizedBlue) = normalizedComponents
            return Pair(
                minOf(normalizedRed, normalizedGreen, normalizedBlue),
                maxOf(normalizedRed, normalizedGreen, normalizedBlue)
            )
        }

        private fun getHueAndChroma(normalizedComponents: Triple<Float, Float, Float>, componentExtrema: Pair<Float, Float>): Pair<Float, Float> {
            val (normalizedRed, normalizedGreen, normalizedBlue) = normalizedComponents
            val (minimumComponent, maximumComponent) = componentExtrema
            val chroma = maximumComponent - minimumComponent
            return Pair(
                chroma,
                60 * when {
                    chroma == 0f -> 0f
                    maximumComponent == normalizedRed -> ((normalizedGreen - normalizedBlue) / chroma) % 6
                    maximumComponent == normalizedGreen -> ((normalizedBlue - normalizedRed) / chroma) + 2
                    else -> ((normalizedRed - normalizedGreen) / chroma) + 4
                }
            )
        }

        override fun toString(): String {
            return if (alpha == 1f) {
                String.format("rgb(%d, %d, %d)", red.toInt(), green.toInt(), blue.toInt())
            } else {
                String.format("rgba(%d, %d, %d, %.1f%%)", red.toInt(), green.toInt(), blue.toInt(), alpha * 100);
            }
        }
    }

    /**
     * A color in the Hue-Saturation-Lightness (HSL) space.
     *
     * The hue component is defined in degrees, so its value can range from `0f` to `360f`.  The saturation and
     * lightness components are defined as percentages, so their values can range from `0f` to `1f`.  Any values outside
     * of those ranges will be clamped to them.
     *
     * @property hue The hue component for the color, from `0f` to `360f`
     * @property saturation The saturation component for the color, from `0f` to `1f`
     * @property lightness The lightness component for the color, from `0f` to `1f`
     */
    class HSL(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f): Color(alpha) {
        val hue: Float = Math.clamp(hue, 0f, 360f)
        val saturation: Float = Math.clamp(saturation, 0f, 1f)
        val lightness: Float = Math.clamp(lightness, 0f, 1f)

        override fun toRGB(): RGB {
            val chroma = (1 - abs(2 * lightness - 1)) * saturation
            val huePrime = hue / 60
            val component = chroma * (1 - abs(huePrime % 2 - 1))
            val componentPrimes: Triple<Float, Float, Float> = when {
                huePrime >= 0f && huePrime < 1f -> Triple(chroma, component, 0f)
                huePrime >= 1f && huePrime < 2f -> Triple(component, chroma, 0f)
                huePrime >= 2f && huePrime < 3f -> Triple(0f, chroma, component)
                huePrime >= 3f && huePrime < 4f -> Triple(0f, component, chroma)
                huePrime >= 4f && huePrime < 5f -> Triple(component, 0f, chroma)
                else -> Triple(chroma, 0f, component)
            }
            val (redPrime, greenPrime, bluePrime) = componentPrimes
            val m = lightness - (chroma / 2)

            return RGB(((redPrime + m) * 255).roundToInt().toUByte(), ((greenPrime + m) * 255).roundToInt().toUByte(), ((bluePrime + m) * 255).roundToInt().toUByte())
        }

        override fun toHSL(): HSL = this

        override fun toString(): String {
            return if (alpha == 1f) {
                String.format("hsl(%.1f°, %.1f%%, %.1f%%)", hue, saturation * 100, lightness * 100);
            } else {
                String.format("hsla(%.1f°, %.1f%%, %.1f%%, %.1f%%)", hue, saturation * 100, lightness * 100, alpha * 100);
            }
        }
    }
}
