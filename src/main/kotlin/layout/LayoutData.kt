package com.aetherui.layout

import com.aetherui.components.UIComponent

class LayoutData {
    data class ConstrainedSizes(
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

        operator fun plus(other: ConstrainedSizes): ConstrainedSizes {
            return ConstrainedSizes(
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

        fun coalesce(other: ConstrainedSizes, orientation: Orientation): ConstrainedSizes {
            // For minimum and desired values, the on-axis (parallel to orientation) totals are the sum of the two
            // values.  The off-axis (orthogonal to orientation) totals are the maximum of the two values.
            // For maximum values, the on-axis totals are the sum of the two values unless either value is null, in
            // which case the total is null.  The off-axis totals are the minimum of the two values unless either value
            // is null, in which case the total is null.
            return when (orientation) {
                Orientation.Horizontal -> ConstrainedSizes(
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

                Orientation.Vertical -> ConstrainedSizes(
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

    private val constrainedSizes: MutableMap<UIComponent, ConstrainedSizes> = mutableMapOf()

    fun setConstrainedSizes(component: UIComponent, sizes: ConstrainedSizes) {
        constrainedSizes[component] = sizes
    }

    fun getConstrainedSizes(component: UIComponent): ConstrainedSizes {
        return constrainedSizes[component] ?: ConstrainedSizes()
    }
}
