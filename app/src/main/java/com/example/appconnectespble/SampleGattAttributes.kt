package com.example.appconnectespble

import com.example.appconnectespble.SampleGattAttributes.Companion.attributes
import java.util.HashMap

class SampleGattAttributes {

    companion object {
        val attributes: HashMap<String, String> = HashMap<String, String>()
        var HEART_RATE_MEASUREMENT = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        var CLIENT_CHARACTERISTIC_CONFIG = "00002a05-0000-1000-8000-00805f9b34fb"

        fun lookup(uuid: String, defaultName: String): String {
            val name = attributes[uuid]
            return name ?: defaultName
        }
    }

    init{
        // Sample Services.
        attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Heart Rate Service")
        attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Device Information Service")
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement")
        attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Manufacturer Name String")
    }


}