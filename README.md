# Routine

An experimental Kotlin implementation of [WPILib's commands v3](https://github.com/wpilibsuite/allwpilib/pull/6518),
leveraging Kotlin's coroutines.
This means it doesn't require a new JVM version, and should run on control hubs just fine.

## Basic usage

A routine (command in other frameworks) is constructed with the `routine` function:
```kotlin
routine {
    drivetrain.lock()
    ready()
    while (true) {
        drivetrain.drive(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x)
        if (yield()) break
    }
}.run()
```