package dev.fishies.routine.util

import dev.fishies.routine.util.geometry.Inches
import dev.fishies.routine.util.geometry.Radians
import dev.fishies.routine.util.geometry.inches
import dev.fishies.routine.util.geometry.radians
import dev.fishies.routine.util.math.symmetricSqrt

class SquIDController(var p: Double = 0.0) {
    fun calculate(setpoint: Double, current: Double) = ((setpoint - current) * p).symmetricSqrt()
    fun calculate(setpoint: Inches, current: Inches) = ((setpoint - current) * p).inches.symmetricSqrt().inches
    fun calculate(setpoint: Radians, current: Radians) = (setpoint.angleTo(current) * p).radians.symmetricSqrt().radians
}