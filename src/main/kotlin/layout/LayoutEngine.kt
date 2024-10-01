package com.aetherui.layout

import com.aetherui.components.UIComponent
import com.aetherui.components.UIContainer
import com.aetherui.components.UIWindow

class LayoutEngine {
    /*
     * PACK
     * (test me first)
     */

    data class CalculatedSizes(
        val minimumHorizontal: Units.Fixed,
        val minimumVertical: Units.Fixed,
        val desiredHorizontal: Units.Fixed,
        val desiredVertical: Units.Fixed,
        val maximumHorizontal: Units.Fixed?,
        val maximumVertical: Units.Fixed?
    ) {
        constructor() : this(
            Units.scalar(0),
            Units.scalar(0),
            Units.scalar(0),
            Units.scalar(0),
            Units.scalar(0),
            Units.scalar(0)
        )

        operator fun plus(other: CalculatedSizes): CalculatedSizes {
            return CalculatedSizes(
                // For minimum and desired sizes, the totals are the sum of the two values
                minimumHorizontal = minimumHorizontal + other.minimumHorizontal,
                minimumVertical = minimumVertical + other.minimumVertical,
                desiredHorizontal = desiredHorizontal + other.desiredHorizontal,
                desiredVertical = desiredVertical + other.desiredVertical,
                // For maximum values, a null value (no maximum) overrides any other maximum values
                maximumHorizontal = if (maximumHorizontal != null && other.maximumHorizontal != null) maximumHorizontal + other.maximumHorizontal else null,
                maximumVertical = if (maximumVertical != null && other.maximumVertical != null) maximumVertical + other.maximumVertical else null
            )
        }

        fun coalesce(other: CalculatedSizes, orientation: Orientation): CalculatedSizes {
            // For minimum and desired values, the on-axis (parallel to orientation) totals are the sum of the two
            // values.  The off-axis (orthogonal to orientation) totals are the maximum of the two values.
            // For maximum values, the on-axis totals are the sum of the two values unless either value is null, in
            // which case the total is null.  The off-axis totals are the minimum of the two values unless either value
            // is null, in which case the total is null.
            return when (orientation) {
                Orientation.Horizontal -> CalculatedSizes(
                    minimumHorizontal = minimumHorizontal + other.minimumHorizontal,
                    minimumVertical = Units.max(minimumVertical, other.minimumVertical),
                    desiredHorizontal = desiredHorizontal + other.desiredHorizontal,
                    desiredVertical = Units.max(desiredVertical, other.desiredVertical),
                    maximumHorizontal = if (maximumHorizontal != null && other.maximumHorizontal != null) maximumHorizontal + other.maximumHorizontal else null,
                    maximumVertical = if (maximumVertical != null && other.maximumVertical != null) Units.min(
                        maximumVertical,
                        other.maximumVertical
                    ) else null
                )

                Orientation.Vertical -> CalculatedSizes(
                    minimumHorizontal = Units.max(minimumHorizontal, other.minimumHorizontal),
                    minimumVertical = minimumVertical + other.minimumVertical,
                    desiredHorizontal = Units.max(desiredHorizontal, other.desiredHorizontal),
                    desiredVertical = desiredVertical + other.desiredVertical,
                    maximumHorizontal = if (maximumHorizontal != null && other.maximumHorizontal != null) Units.min(
                        maximumHorizontal,
                        other.maximumHorizontal
                    ) else null,
                    maximumVertical = if (maximumVertical != null && other.maximumVertical != null) maximumVertical + other.maximumVertical else null
                )
            }
        }

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

    fun pack(component: UIComponent): CalculatedSizes {
        val calculatedSizes = if (component is UIContainer) {
            calculateContainerSize(component)
        } else {
            calculateComponentSize(component)
        }
        componentSizeCache[component] = calculatedSizes
        return calculatedSizes
    }

    private fun calculateContainerSize(container: UIContainer): CalculatedSizes {
        // Calculate the sizes for the container itself, ignoring the sizes of the contents
        val containerSizes = calculateComponentSize(container)
        // Calculate the total sizes for the contents of the container, ignoring the sizes of the container.  The
        // container's padding is added to the content sizes to form the full content sizes.
        val contentSizes = container.components.asSequence()
            .map { component -> pack(component) + calculateMargins(component) }
            .reduce { acc, current -> acc.coalesce(current, container.orientation) } + calculatePadding(container)

        // For the minimum and desired sizes, the final size is the minimum of the container's configured size and the
        // content size (including container padding).  For the maximum sizes. the final size is the maximum of the
        // container's configured size and the content size (including container padding) unless either value is null,
        // in which case the non-null value takes precedence.  This prevents the layout engine from forcing the
        // container or contents to grow beyond their configured maximums, even if the other isn't constrained.
        return CalculatedSizes(
            minimumHorizontal = Units.max(containerSizes.minimumHorizontal, contentSizes.minimumHorizontal),
            minimumVertical = Units.max(containerSizes.minimumVertical, contentSizes.minimumVertical),
            desiredHorizontal = Units.max(containerSizes.desiredHorizontal, contentSizes.desiredHorizontal),
            desiredVertical = Units.max(containerSizes.desiredVertical, contentSizes.desiredVertical),
            maximumHorizontal = if (containerSizes.maximumHorizontal != null && contentSizes.maximumHorizontal != null) Units.min(
                containerSizes.maximumHorizontal,
                contentSizes.maximumHorizontal
            ) else containerSizes.maximumHorizontal ?: contentSizes.maximumHorizontal,
            maximumVertical = if (containerSizes.maximumVertical != null && contentSizes.maximumVertical != null) Units.min(
                containerSizes.maximumVertical,
                contentSizes.maximumVertical
            ) else containerSizes.maximumVertical ?: contentSizes.maximumVertical
        )
    }

    private fun calculateComponentSize(component: UIComponent): CalculatedSizes {
        // The minimum, desired, and maximum sizes of a component are assigned to their configured values.  If no
        // fixed desired sizes are configured, the minimum size is used instead (though more space may be assigned later
        // in the layout process).  Any padding for the component is included in this overall size.  Margins are handled
        // by the parent container.
        val minimumHorizontal = component.constraints.sizeMinimum.width
        val minimumVertical = component.constraints.sizeMinimum.height
        val desiredHorizontal = component.constraints.sizeDesired.width
        val desiredVertical = component.constraints.sizeDesired.height
        val maximumHorizontal = component.constraints.sizeMaximum.width
        val maximumVertical = component.constraints.sizeMaximum.height
        return CalculatedSizes(
            minimumHorizontal = minimumHorizontal,
            minimumVertical = minimumVertical,
            desiredHorizontal = if (desiredHorizontal is Units.Fixed) desiredHorizontal else minimumHorizontal,
            desiredVertical = if (desiredVertical is Units.Fixed) desiredVertical else minimumVertical,
            maximumHorizontal = maximumHorizontal,
            maximumVertical = maximumVertical
        )
    }

    private fun calculateMargins(component: UIComponent): CalculatedSizes {
        return calculateDirectionalConstraints(component.constraints.margins)
    }

    private fun calculatePadding(component: UIComponent): CalculatedSizes {
        return calculateDirectionalConstraints(component.constraints.padding)
    }

    private fun calculateDirectionalConstraints(constraints: Constraints.Directions<Units>): CalculatedSizes {
        val marginsHorizontal = combineUnits(constraints.start, constraints.end)
        val marginsVertical = combineUnits(constraints.top, constraints.bottom)
        return CalculatedSizes(
            minimumHorizontal = marginsHorizontal,
            minimumVertical = marginsVertical,
            desiredHorizontal = marginsHorizontal,
            desiredVertical = marginsVertical,
            maximumHorizontal = marginsHorizontal,
            maximumVertical = marginsVertical
        )
    }

    private fun combineUnits(vararg units: Units): Units.Fixed {
        // Add any fixed units together and ignore non-fixed units, since these are handled later in the layout process
        var sum = Units.scalar(0)
        for (unit in units) {
            if (unit is Units.Fixed) {
                sum += unit
            }
        }
        return sum
    }

    /*
     * SETTLE ON-AXIS
     */

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
                val componentSizes = component.components.asSequence().associateWith { child ->
                    val childSize = componentSizeCache.getOrDefault(child, CalculatedSizes()).minimum(orientation)
                    (childSize.value + (childSize.ratio * dimension)).toInt()
                }.toMutableMap()
                var remainingSize = dimension - componentSizes.values.sum()
                if (remainingSize < 0) {
                    // Scrollbar required
                } else {
                    // Distribute remaining by determining the smallest amount required to get any component to its
                    // desired size.  Then, determine if that amount can be added to all components not yet at their
                    // desired sizes.  When components have reached their desired sizes, check if there are any "fill"
                    // components and grow them proportionally to fill the remaining space.
                }
            }
        } else {
            // TODO How to get value from component itself?
        }
        return 0
    }

    /*
     * SETTLE OFF-AXIS
     */
}
