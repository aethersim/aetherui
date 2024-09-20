package com.aetherui

import java.awt.Dimension
import java.awt.Frame

fun main() {
    val frame = Frame("Test")
    frame.size = Dimension(800, 600)
    frame.isVisible = true
}
