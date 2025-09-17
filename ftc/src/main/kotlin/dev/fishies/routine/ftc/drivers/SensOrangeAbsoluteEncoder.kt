package dev.fishies.routine.ftc.drivers

import com.qualcomm.robotcore.hardware.AnalogInputController
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareDevice.Manufacturer
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties
import dev.fishies.routine.util.geometry.Radians
import dev.fishies.routine.util.geometry.degrees
import dev.fishies.routine.util.geometry.radians
import dev.fishies.routine.util.geometry.times

@AnalogSensorType
@DeviceProperties(
    name = "sensOrange® Absolute Encoder",
    xmlTag = "SensOrangeAbsoluteEncoder",
    description = "sensOrange® Absolute Encoder absolute encoder"
)
class SensOrangeAbsoluteEncoder(
    private val controller: AnalogInputController?, private val channel: Int,
) : HardwareDevice {
    override fun getManufacturer(): Manufacturer? {
        return this.controller?.manufacturer
    }

    val voltage get() = controller?.getAnalogInputVoltage(this.channel) ?: -1.0

    var offset = Radians.ZERO
    var maxAngle = 360.degrees
    var maxReportedVoltage = 3.3
    var inverted = false

    private var lastAngle = Radians.ZERO

    val angle
        get() = ((if (inverted) -1.0 else 1.0) * voltage.radians * maxAngle / maxReportedVoltage).rotated(offset)

    override fun getDeviceName() = "sensOrange® Absolute Encoder"

    override fun getConnectionInfo() = "${this.controller!!.connectionInfo}; analog port ${this.channel}"

    override fun getVersion() = 1

    override fun resetDeviceConfigurationForOpMode() = Unit

    override fun close() = Unit
}