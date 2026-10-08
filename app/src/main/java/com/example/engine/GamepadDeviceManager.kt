package com.example.engine

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectedController(
    val id: Int,
    val name: String,
    val descriptor: String,
    val vendorId: Int,
    val productId: Int,
    val isGamepad: Boolean,
    val isJoystick: Boolean
)

class GamepadDeviceManager(context: Context) : InputManager.InputDeviceListener {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager

    private val _controllers = MutableStateFlow<List<ConnectedController>>(emptyList())
    val controllers: StateFlow<List<ConnectedController>> = _controllers.asStateFlow()

    private val _selectedControllerId = MutableStateFlow<Int?>(null)
    val selectedControllerId: StateFlow<Int?> = _selectedControllerId.asStateFlow()

    init {
        inputManager?.registerInputDeviceListener(this, null)
        refreshControllers()
    }

    fun refreshControllers() {
        val deviceIds = InputDevice.getDeviceIds()
        val list = mutableListOf<ConnectedController>()

        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
            val isJoystick = (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK

            if (isGamepad || isJoystick) {
                list.add(
                    ConnectedController(
                        id = device.id,
                        name = device.name ?: "Unknown Controller #$id",
                        descriptor = device.descriptor ?: "",
                        vendorId = device.vendorId,
                        productId = device.productId,
                        isGamepad = isGamepad,
                        isJoystick = isJoystick
                    )
                )
            }
        }

        _controllers.value = list
        if (_selectedControllerId.value == null && list.isNotEmpty()) {
            _selectedControllerId.value = list.first().id
        }
    }

    fun selectController(id: Int?) {
        _selectedControllerId.value = id
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        refreshControllers()
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        refreshControllers()
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        refreshControllers()
    }

    fun cleanup() {
        inputManager?.unregisterInputDeviceListener(this)
    }
}
