package com.example.appconnectespble

import android.util.Log
import com.example.appconnectespble.SampleGattAttributes.Companion.attributes
import java.util.HashMap

class SampleGattAttributes {

    companion object {
        val attributes: HashMap<String, String> = HashMap<String, String>()
        var HEART_RATE_MEASUREMENT = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        var CLIENT_CHARACTERISTIC_CONFIG = "00002a05-0000-1000-8000-00805f9b34fb"
        var BENGALARX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
        var BENGALATX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
        var BENGALA_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"

        init{
            // Sample Services.
            attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Heart Rate Service")
            attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Device Information Service")
            attributes.put(BENGALA_UUID, "Bengala Service")
            // Sample Characteristics.
            attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement")
            attributes.put("6E400003-B5A3-F393-E0A9-E50E24DCCA9E", "Manufacturer Name String")
            attributes.put("6e400002-b5a3-f393-e0a9-e50e24dcca9e", "BENGALARX")
            attributes.put(BENGALATX, "BENGALATX")
        }

        fun lookup(uuid: String, defaultName: String): String {


            Log.i("TANIRO2", attributes.size.toString())

            val name = attributes.get(uuid)
            Log.i("TANIRO2", "${uuid} ${name.toString()}")
            return name ?: defaultName
        }
    }




}