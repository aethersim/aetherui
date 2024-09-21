package com.aetherui.layout

import com.aetherui.components.UIComponent
import com.aetherui.components.UIContainer
import com.aetherui.components.UIWindow

class LayoutEngine {

    fun pack(container: UIWindow): Dimensions {
        // TODO
        return Dimensions(0, 0)
    }

    private data class CalculatedSizes(
        val minimumHorizontal: Units.Fixed,
        val minimumVertical: Units.Fixed,
        val desiredHorizontal: Units.Fixed,
        val desiredVertical: Units.Fixed,
        val maximumHorizontal: Units.Fixed?,
        val maximumVertical: Units.Fixed?
    ) {
        fun minimum(orientation: Orientation): Units.Fixed {
            return when (orientation) {
                Orientation.Horizontal -> minimumHorizontal
                Orientation.Vertical -> minimumVertical
            }
        }

        fun desired(orientation: Orientation): Units.Fixed {
            return when (orientation) {
                Orientation.Horizontal -> desiredHorizontal
                Orientation.Vertical -> desiredVertical
            }
        }

        fun maximum(orientation: Orientation): Units.Fixed? {
            return when (orientation) {
                Orientation.Horizontal -> maximumHorizontal
                Orientation.Vertical -> maximumVertical
            }
        }
    }

    private val componentSizeCache: MutableMap<UIComponent, CalculatedSizes> = mutableMapOf()

    private fun calculateSize(component: UIComponent): CalculatedSizes {
        val calculatedSizes = if (component is UIContainer) {
            calculateContainerSize(component)
        } else {
            calculateComponentSize(component)
        }
        componentSizeCache[component] = calculatedSizes
        return calculatedSizes
    }

    private fun calculateContainerSize(container: UIContainer): CalculatedSizes {
        var descendantMinimumHorizontal: Units.Fixed = Units.scalar(0)
        var descendantMinimumVertical: Units.Fixed = Units.scalar(0)
        var descendantDesiredHorizontal: Units.Fixed = Units.scalar(0)
        var descendantDesiredVertical: Units.Fixed = Units.scalar(0)
        var descendantMaximumHorizontal: Units.Fixed? = Units.scalar(0)
        var descendantMaximumVertical: Units.Fixed? = Units.scalar(0)
        for (component in container.components) {
            val calculatedSizes = calculateSize(component)
            val (marginsHorizontal, marginsVertical) = calculateComponentMargins(component)
            when(container.orientation) {
                Orientation.Horizontal -> {
                    descendantMinimumHorizontal += calculatedSizes.minimumHorizontal + marginsHorizontal
                    descendantMinimumVertical = Units.max(descendantMinimumVertical, calculatedSizes.minimumVertical + marginsVertical)
                    descendantDesiredHorizontal += calculatedSizes.desiredHorizontal + marginsHorizontal
                    descendantDesiredVertical = Units.max(descendantDesiredVertical, calculatedSizes.desiredVertical + marginsVertical)
                    if (descendantMaximumHorizontal != null && calculatedSizes.maximumHorizontal != null) {
                        descendantMaximumHorizontal += calculatedSizes.maximumHorizontal + marginsHorizontal
                    } else if (descendantMaximumHorizontal != null) {
                        descendantMaximumHorizontal = null
                    }
                    if (descendantMaximumVertical != null && calculatedSizes.maximumVertical != null) {
                        descendantMaximumVertical = Units.min(descendantMaximumVertical, calculatedSizes.maximumVertical + marginsVertical)
                    }
                }
                Orientation.Vertical -> {
                    descendantMinimumHorizontal = Units.max(descendantMinimumHorizontal, calculatedSizes.minimumHorizontal + marginsHorizontal)
                    descendantMinimumVertical += calculatedSizes.minimumVertical + marginsVertical
                    descendantDesiredHorizontal = Units.max(descendantDesiredHorizontal, calculatedSizes.desiredHorizontal + marginsHorizontal)
                    descendantDesiredVertical += calculatedSizes.desiredVertical + marginsVertical
                    if (descendantMaximumHorizontal != null && calculatedSizes.maximumHorizontal != null) {
                        descendantMaximumHorizontal = Units.min(descendantMaximumHorizontal, calculatedSizes.maximumHorizontal + marginsHorizontal)
                    }
                    if (descendantMaximumVertical != null && calculatedSizes.maximumVertical != null) {
                        descendantMaximumVertical += calculatedSizes.maximumVertical + marginsVertical
                    } else if (descendantMaximumVertical != null) {
                        descendantMaximumVertical = null
                    }
                }
            }
        }
        val calculatedSizes = calculateComponentSize(container)
        val (paddingHorizontal, paddingVertical) = calculateComponentPadding(container)
        val findMaximum: (orientation: Orientation) -> Units.Fixed? = { orientation ->
            val maximum = calculatedSizes.maximum(orientation)
            val descendantMaximum = if (orientation == Orientation.Horizontal) descendantMaximumHorizontal else descendantMaximumVertical
            val padding = if (orientation == Orientation.Horizontal) paddingHorizontal else paddingVertical
            when {
                maximum == null && descendantMaximum == null -> null
                maximum == null && descendantMaximum != null -> descendantMaximum + padding
                maximum != null && descendantMaximum == null -> calculatedSizes.maximumHorizontal
                maximum != null && descendantMaximum != null -> Units.min(maximum, descendantMaximum + padding)
                else -> null
            }
        }
        return CalculatedSizes(
            minimumHorizontal = Units.max(calculatedSizes.minimumHorizontal, descendantMinimumHorizontal + paddingHorizontal),
            minimumVertical = Units.max(calculatedSizes.minimumVertical, descendantMinimumVertical + paddingVertical),
            desiredHorizontal = Units.max(calculatedSizes.desiredHorizontal, descendantDesiredHorizontal + paddingHorizontal),
            desiredVertical = Units.max(calculatedSizes.desiredVertical, descendantDesiredVertical + paddingVertical),
            maximumHorizontal = findMaximum(Orientation.Horizontal),
            maximumVertical = findMaximum(Orientation.Vertical)
        )
    }

    private fun calculateComponentSize(component: UIComponent): CalculatedSizes {
        val minimumHorizontal = component.constraints.sizeMinimum.forOrientation(Orientation.Horizontal)
        val minimumVertical = component.constraints.sizeMinimum.forOrientation(Orientation.Vertical)
        val desiredHorizontal = component.constraints.sizeDesired.forOrientation(Orientation.Horizontal)
        val desiredVertical = component.constraints.sizeDesired.forOrientation(Orientation.Vertical)
        val maximumHorizontal = component.constraints.sizeMaximum.forOrientation(Orientation.Horizontal)
        val maximumVertical = component.constraints.sizeMaximum.forOrientation(Orientation.Vertical)
        val (paddingHorizontal, paddingVertical) = calculateComponentPadding(component)
        return CalculatedSizes(
            minimumHorizontal = minimumHorizontal + paddingHorizontal,
            minimumVertical = minimumVertical + paddingVertical,
            desiredHorizontal = (if (desiredHorizontal is Units.Fixed) desiredHorizontal else minimumHorizontal) + paddingHorizontal,
            desiredVertical = (if (desiredVertical is Units.Fixed) desiredVertical else minimumVertical) + paddingVertical,
            maximumHorizontal = if (maximumHorizontal != null) maximumHorizontal + paddingHorizontal else null,
            maximumVertical = if (maximumVertical != null) maximumVertical + paddingVertical else null
        )
    }

    private fun calculateComponentMargins(component: UIComponent): Pair<Units.Fixed, Units.Fixed> {
        return Pair(
            combineUnits(
                component.constraints.margins.start,
                component.constraints.margins.end
            ),
            combineUnits(
                component.constraints.margins.top,
                component.constraints.margins.bottom
            )
        )
    }

    private fun calculateComponentPadding(component: UIComponent): Pair<Units.Fixed, Units.Fixed> {
        return Pair(
            combineUnits(
                component.constraints.padding.start,
                component.constraints.padding.end
            ),
            combineUnits(
                component.constraints.padding.top,
                component.constraints.padding.bottom
            )
        )
    }

    private fun combineUnits(vararg units: Units): Units.Fixed {
        var sum = Units.scalar(0)
        for (unit in units) {
            if (unit is Units.Fixed) {
                sum += unit
            }
        }
        return sum
    }

    private val finalComponentHorizontal: MutableMap<UIComponent, Int> = mutableMapOf()
    private val finalComponentVertical: MutableMap<UIComponent, Int> = mutableMapOf()

    private fun calculateFinalSize(container: UIContainer, dimensions: Dimensions) {
        if (container.orientation == Orientation.Horizontal) {
            calculateFinalComponentSize(container, Orientation.Horizontal, dimensions.height)
        } else {
            calculateFinalComponentSize(container, Orientation.Vertical, dimensions.width)
        }
    }

    private fun calculateFinalComponentSize(component: UIComponent, orientation: Orientation, dimension: Int): Int {
        // This is the "pack" part of the "pack" and "settle"
        if (orientation == Orientation.Horizontal) {
            finalComponentVertical[component] = dimension
        } else {
            finalComponentHorizontal[component] = dimension
        }

        if (component is UIContainer) {
            if (component.orientation == orientation) {
                var totalSize = 0
                for (child in component.components) {
                    totalSize += calculateFinalComponentSize(child, orientation, dimension)
                }
            } else {
                // TODO Distribute dimension among children and recurse
            }
        } else {
            // TODO How to get value from component itself?
        }
        return 0
    }
}
