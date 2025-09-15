package org.firstinspires.ftc.teamcode.drivers.sensorange

import com.qualcomm.robotcore.hardware.AnalogInputController
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareDevice.Manufacturer
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties

@AnalogSensorType
@DeviceProperties(
    name = "sensOrange Absolute Encoder",
    xmlTag = "SensOrangeAbsoluteEncoder",
    description = "sensOrange Absolute Encoder absolute encoder"
)
class SensOrangeAbsoluteEncoder(
    private val controller: AnalogInputController?, private val channel: Int,
) : HardwareDevice {

    override fun getManufacturer(): Manufacturer? {
        return this.controller?.manufacturer
    }

    val voltage get() = controller?.getAnalogInputVoltage(this.channel) ?: -1.0

    override fun getDeviceName() = "sensOrange Absolute Encoder"

    override fun getConnectionInfo() = "${this.controller!!.connectionInfo}; analog port ${this.channel}"

    override fun getVersion() = 1

    override fun resetDeviceConfigurationForOpMode() = Unit

    override fun close() = Unit
}