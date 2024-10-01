package com.aetherui.layout

import com.aetherui.components.UIComponent
import com.aetherui.components.UIContainer
import kotlin.test.Test
import kotlin.test.assertEquals

class TestLayoutEngine {
    class TestComponent: UIComponent() {
        override fun render() { throw NotImplementedError() }
    }
    class TestContainer: UIContainer(orientation = Orientation.Horizontal) {
        override val components: MutableList<UIComponent> = mutableListOf()

        override fun pack(orientation: Orientation) { throw NotImplementedError() }

        override fun render() { throw NotImplementedError() }
    }
    @Test
    fun `Container packing results in accurate results`() {
        val component1 = TestComponent()
        component1.constraints.sizeMinimum = Constraints.Dimensions(Units.scalar(100), Units.scalar(200))
        component1.constraints.sizeDesired = Constraints.Dimensions(Units.scalar(200), Units.scalar(400))
        component1.constraints.sizeMaximum = Constraints.Dimensions(Units.scalar(300), Units.scalar(600))
        component1.constraints.margins = Constraints.Directions(Units.scalar(10), Units.scalar(15), Units.scalar(25), Units.scalar(40))
        component1.constraints.padding = Constraints.Directions(Units.scalar(20), Units.scalar(25), Units.scalar(35), Units.scalar(50))
        val component2 = TestComponent()
        component2.constraints.sizeMinimum = Constraints.Dimensions(Units.scalar(150), Units.scalar(250))
        component2.constraints.sizeDesired = Constraints.Dimensions(Units.scalar(250), Units.scalar(450))
        component2.constraints.sizeMaximum = Constraints.Dimensions(Units.scalar(350), Units.scalar(650))
        component2.constraints.margins = Constraints.Directions(Units.scalar(15), Units.scalar(20), Units.scalar(30), Units.scalar(45))
        component2.constraints.padding = Constraints.Directions(Units.scalar(25), Units.scalar(30), Units.scalar(40), Units.scalar(55))
        val container = TestContainer()
        container.components.add(component1)
        container.components.add(component2)

        val constrainedSize = LayoutEngine().pack(container)
        assertEquals(Units.scalar(330), constrainedSize.minimumHorizontal)
        assertEquals(Units.scalar(315), constrainedSize.minimumVertical)
        assertEquals(Units.scalar(530), constrainedSize.desiredHorizontal)
        assertEquals(Units.scalar(515), constrainedSize.desiredVertical)
        assertEquals(Units.scalar(730), constrainedSize.maximumHorizontal)
        assertEquals(Units.scalar(655), constrainedSize.maximumVertical)
    }
}
