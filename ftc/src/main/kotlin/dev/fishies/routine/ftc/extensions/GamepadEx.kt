package dev.fishies.routine.ftc.extensions

import android.R.attr.x
import android.R.attr.y
import dev.fishies.routine.util.geometry.Vector2
import dev.fishies.routine.util.geometry.inches
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.util.RobotLog.a
import java.util.concurrent.ThreadLocalRandom.current

/**
 * Get a button input supplier.
 *
 * **NOTE**:
 * Returns a supplier, not a value!
 * If you want a value, use [current]
 * @param input The [ButtonInput]'s state to produce
 */
inline operator fun Gamepad.get(input: ButtonInput) = { current(input) }

/**
 * Gets the current value of an [AnalogInput]
 *
 * @param input The [AnalogInput]'s state to get
 */
inline operator fun Gamepad.get(input: AnalogInput) = current(input)

/**
 * Gets the current value of an [VectorInput]
 *
 * @param input The [VectorInput]'s state to get
 */
inline operator fun Gamepad.get(input: VectorInput) = current(input)

inline fun Gamepad.current(input: ButtonInput) = with(input) { value() }
inline fun Gamepad.current(input: AnalogInput) = with(input) { value() }
inline fun Gamepad.current(input: VectorInput) = with(input) { value() }

@JvmName("booleanDouble")
fun (() -> Double).bool(predicate: (Double) -> Boolean = { it > 0.5 }) = { predicate(this()) }

@JvmName("booleanVector")
fun (() -> Vector2).bool(predicate: (Vector2) -> Boolean = { it.lengthSquared > 0.5 * 0.5 }) = { predicate(this()) }

abstract class GamepadInput<V>(val value: Gamepad.() -> V)

sealed class ButtonInput(value: Gamepad.() -> Boolean) : GamepadInput<Boolean>(value) {
    data object DPAD_UP : ButtonInput({ dpad_up })
    data object DPAD_DOWN : ButtonInput({ dpad_down })
    data object DPAD_LEFT : ButtonInput({ dpad_left })
    data object DPAD_RIGHT : ButtonInput({ dpad_right })
    data object A : ButtonInput({ a })
    data object B : ButtonInput({ b })
    data object X : ButtonInput({ x })
    data object Y : ButtonInput({ y })
    data object CROSS : ButtonInput({ cross })
    data object CIRCLE : ButtonInput({ circle })
    data object SQUARE : ButtonInput({ square })
    data object TRIANGLE : ButtonInput({ triangle })
    data object BUMPER_LEFT : ButtonInput({ left_bumper })
    data object BUMPER_RIGHT : ButtonInput({ right_bumper })
    data object TOUCHPAD_PRESS : ButtonInput({ touchpad })
    data object TOUCHPAD_TOUCH : ButtonInput({ touchpad_finger_1 })
    data object TOUCHPAD_TOUCH_FINGER_2 : ButtonInput({ touchpad_finger_2 })

    /**
     * "PS4 Support - PS Button" whatever this means
     */
    data object PLAYSTATION_BUTTON : ButtonInput({ ps })

    /**
     * "button guide - often the large button in the middle of the controller. The OS may capture this button before
     * it is sent to the app; in which case you'll never receive it" so maybe don't use this one?
     */
    data object GUIDE : ButtonInput({ guide })
    data object SHARE : ButtonInput({ share })
    data object OPTIONS : ButtonInput({ options })
    data object STICK_BUTTON_LEFT : ButtonInput({ left_stick_button })
    data object STICK_BUTTON_RIGHT : ButtonInput({ right_stick_button })
}

sealed class AnalogInput(value: Gamepad.() -> Double) : GamepadInput<Double>(value) {
    data object TRIGGER_LEFT : AnalogInput({ left_trigger.toDouble() })
    data object TRIGGER_RIGHT : AnalogInput({ right_trigger.toDouble() })
    data object STICK_X_LEFT : AnalogInput({ left_stick_x.toDouble() })
    data object STICK_Y_LEFT : AnalogInput({ left_stick_y.toDouble() })
    data object STICK_X_RIGHT : AnalogInput({ right_stick_x.toDouble() })
    data object STICK_Y_RIGHT : AnalogInput({ right_stick_y.toDouble() })
    data object TOUCHPAD_X : AnalogInput({ touchpad_finger_1_x.toDouble() })
    data object TOUCHPAD_Y : AnalogInput({ touchpad_finger_1_y.toDouble() })
    data object TOUCHPAD_X_FINGER_2 : AnalogInput({ touchpad_finger_2_x.toDouble() })
    data object TOUCHPAD_Y_FINGER_2 : AnalogInput({ touchpad_finger_2_y.toDouble() })
}

sealed class VectorInput(value: Gamepad.() -> Vector2) : GamepadInput<Vector2>(value) {
    data object STICK_LEFT : VectorInput({
        Vector2(
            left_stick_x.toDouble().inches, left_stick_y.toDouble().inches
        )
    })

    data object STICK_RIGHT : VectorInput({
        Vector2(
            right_stick_x.toDouble().inches, right_stick_y.toDouble().inches
        )
    })

    data object TOUCHPAD : VectorInput({
        Vector2(
            touchpad_finger_1_x.toDouble().inches, touchpad_finger_1_y.toDouble().inches
        )
    })

    data object TOUCHPAD_FINGER_2 : VectorInput({
        Vector2(
            touchpad_finger_2_x.toDouble().inches, touchpad_finger_2_y.toDouble().inches
        )
    })
}

data class CompositeButtonInput(
    val a: ButtonInput,
    val b: ButtonInput,
    val predicate: (Boolean, Boolean) -> Boolean,
) : ButtonInput({ predicate(current(a), current(b)) })

infix fun ButtonInput.and(other: ButtonInput) = CompositeButtonInput(this, other) { a, b -> a && b }
infix fun ButtonInput.or(other: ButtonInput) = CompositeButtonInput(this, other) { a, b -> a || b }
infix fun ButtonInput.xor(other: ButtonInput) = CompositeButtonInput(this, other) { a, b -> a xor b }