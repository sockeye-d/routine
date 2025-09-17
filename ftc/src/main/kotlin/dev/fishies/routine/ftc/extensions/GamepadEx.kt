package dev.fishies.routine.ftc.extensions

import dev.fishies.routine.util.geometry.Vector2
import dev.fishies.routine.util.geometry.inches
import com.qualcomm.robotcore.hardware.Gamepad
import java.util.concurrent.ThreadLocalRandom.current

/**
 * Get a button input supplier.
 *
 * **NOTE**:
 * Returns a supplier, not a value!
 * If you want a value, use [current]
 * @param input The [Button]'s state to produce
 */
inline operator fun Gamepad.get(input: Button) = { current(input) }

/**
 * Gets the current value of an [Analog]
 *
 * @param input The [Analog]'s state to get
 */
inline operator fun Gamepad.get(input: Analog) = current(input)

/**
 * Gets the current value of an [Vector]
 *
 * @param input The [Vector]'s state to get
 */
inline operator fun Gamepad.get(input: Vector) = current(input)

inline fun Gamepad.current(input: Button) = with(input) { value() }
inline fun Gamepad.current(input: Analog) = with(input) { value() }
inline fun Gamepad.current(input: Vector) = with(input) { value() }

@JvmName("booleanDouble")
fun (() -> Double).bool(predicate: (Double) -> Boolean = { it > 0.5 }) = { predicate(this()) }

@JvmName("booleanVector")
fun (() -> Vector2).bool(predicate: (Vector2) -> Boolean = { it.lengthSquared > 0.5 * 0.5 }) = { predicate(this()) }

abstract class GamepadInput<V>(val value: Gamepad.() -> V)

sealed class Button(value: Gamepad.() -> Boolean) : GamepadInput<Boolean>(value) {
    data object DPAD_UP : Button({ dpad_up })
    data object DPAD_DOWN : Button({ dpad_down })
    data object DPAD_LEFT : Button({ dpad_left })
    data object DPAD_RIGHT : Button({ dpad_right })
    data object A : Button({ a })
    data object B : Button({ b })
    data object X : Button({ x })
    data object Y : Button({ y })
    data object CROSS : Button({ cross })
    data object CIRCLE : Button({ circle })
    data object SQUARE : Button({ square })
    data object TRIANGLE : Button({ triangle })
    data object BUMPER_LEFT : Button({ left_bumper })
    data object BUMPER_RIGHT : Button({ right_bumper })
    data object TOUCHPAD_PRESS : Button({ touchpad })
    data object TOUCHPAD_TOUCH : Button({ touchpad_finger_1 })
    data object TOUCHPAD_TOUCH_FINGER_2 : Button({ touchpad_finger_2 })

    /**
     * "PS4 Support - PS Button" whatever this means
     */
    data object PLAYSTATION_BUTTON : Button({ ps })

    /**
     * "button guide - often the large button in the middle of the controller. The OS may capture this button before
     * it is sent to the app; in which case you'll never receive it" so maybe don't use this one?
     */
    data object GUIDE : Button({ guide })
    data object SHARE : Button({ share })
    data object OPTIONS : Button({ options })
    data object STICK_BUTTON_LEFT : Button({ left_stick_button })
    data object STICK_BUTTON_RIGHT : Button({ right_stick_button })
}

sealed class Analog(value: Gamepad.() -> Double) : GamepadInput<Double>(value) {
    data object TRIGGER_LEFT : Analog({ left_trigger.toDouble() })
    data object TRIGGER_RIGHT : Analog({ right_trigger.toDouble() })
    data object STICK_X_LEFT : Analog({ left_stick_x.toDouble() })
    data object STICK_Y_LEFT : Analog({ left_stick_y.toDouble() })
    data object STICK_X_RIGHT : Analog({ right_stick_x.toDouble() })
    data object STICK_Y_RIGHT : Analog({ right_stick_y.toDouble() })
    data object TOUCHPAD_X : Analog({ touchpad_finger_1_x.toDouble() })
    data object TOUCHPAD_Y : Analog({ touchpad_finger_1_y.toDouble() })
    data object TOUCHPAD_X_FINGER_2 : Analog({ touchpad_finger_2_x.toDouble() })
    data object TOUCHPAD_Y_FINGER_2 : Analog({ touchpad_finger_2_y.toDouble() })
}

sealed class Vector(value: Gamepad.() -> Vector2) : GamepadInput<Vector2>(value) {
    data object STICK_LEFT : Vector({
        Vector2(
            left_stick_x.toDouble().inches, left_stick_y.toDouble().inches
        )
    })

    data object STICK_RIGHT : Vector({
        Vector2(
            right_stick_x.toDouble().inches, right_stick_y.toDouble().inches
        )
    })

    data object TOUCHPAD : Vector({
        Vector2(
            touchpad_finger_1_x.toDouble().inches, touchpad_finger_1_y.toDouble().inches
        )
    })

    data object TOUCHPAD_FINGER_2 : Vector({
        Vector2(
            touchpad_finger_2_x.toDouble().inches, touchpad_finger_2_y.toDouble().inches
        )
    })
}

private data class CompositeButton(
    val a: Button,
    val b: Button,
    val predicate: (Boolean, Boolean) -> Boolean,
) : Button({ predicate(current(a), current(b)) })

infix fun Button.and(other: Button): Button = CompositeButton(this, other) { a, b -> a && b }
infix fun Button.or(other: Button): Button = CompositeButton(this, other) { a, b -> a || b }
infix fun Button.xor(other: Button): Button = CompositeButton(this, other) { a, b -> a xor b }