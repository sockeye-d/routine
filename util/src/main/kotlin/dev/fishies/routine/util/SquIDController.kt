package dev.fishies.routine.util

import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

class SquIDController(var p: Double) {
    fun calculate(setpoint: Double, current: Double): Double {
        return sqrt(abs((setpoint - current) * p)) * sign(setpoint - current)
    }
}