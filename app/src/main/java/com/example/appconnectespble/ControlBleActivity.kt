package com.example.appconnectespble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.bluetooth.BluetoothGattCharacteristic
import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Build
import android.widget.ExpandableListView
import android.widget.TextView
import android.view.Menu
import android.view.MenuItem
import android.content.IntentFilter
import kotlinx.android.synthetic.main.activity_control_ble.*
import android.content.Intent
import com.example.appconnectespble.BluetoothLeService.Companion.ACTION_DATA_AVAILABLE
import com.example.appconnectespble.BluetoothLeService.Companion.ACTION_GATT_CONNECTED
import com.example.appconnectespble.BluetoothLeService.Companion.ACTION_GATT_DISCONNECTED
import com.example.appconnectespble.BluetoothLeService.Companion.ACTION_GATT_SERVICES_DISCOVERED
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ControlBleActivity : AppCompatActivity() {

    private val TAG = TestBLE::class.java!!.simpleName

    private val mConnectionState: TextView? = null
    private val mDataField: TextView? = null
    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private val mGattServicesList: ExpandableListView? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    var mGattCharacteristics =  arrayListOf<BluetoothGattCharacteristic>()
    private var connected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"


    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            Log.i("TANIRO", mBluetoothLeService.toString())
            if (!mBluetoothLeService!!.initialize()) {
                Log.i("Erro!", "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("Status", intent.action)
            when (intent.action){
                ACTION_GATT_CONNECTED -> {
                    connected = true
                    //updateConnectionState)
                    (context as? Activity)?.invalidateOptionsMenu()
                }
                ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    //updateConnectionState(R.string.disconnected)
                    (context as? Activity)?.invalidateOptionsMenu()
                    //clearUI()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the
                    // user interface.
                    displayGattServices(mBluetoothLeService!!.supportedGattServices)
                }
                ACTION_DATA_AVAILABLE -> {
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }
            }
        }
    }
    val characteristic = null

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String?
        val unknownServiceString: String = resources.getString(R.string.unknown_service)
        val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()
        mGattCharacteristics = arrayListOf()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid!!, unknownServiceString)
            currentServiceData[LIST_UUID] = uuid!!
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService.characteristics
            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
                Log.i("TANIROUID", uuid!!.toString())
                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid!!, unknownCharaString)
                currentCharaData[LIST_UUID] = uuid!!
                gattCharacteristicGroupData += currentCharaData
            }
            mGattCharacteristics.plusAssign(charas)
            gattCharacteristicData += gattCharacteristicGroupData

            mGattCharacteristics.forEach {
                Log.i("Teste1000", it.toString())
            }
            Log.i("Teste1000", "${mGattCharacteristics}")
            Log.i("Teste1001", "${gattCharacteristicData}")

            gattCharacteristicData.forEach {
                Log.i("Teste1002", it[0]["NAME"].toString())
            }
        }
        preencher()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun preencher() {
        Log.i("Cincuunn", "${mGattCharacteristics[4].descriptors}")
        if (mGattCharacteristics != null) {
            val characteristic = mGattCharacteristics[4]

            val char: Int = characteristic.properties
            Log.i("Hev", "${characteristic.value} + $char")
            if (char or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService!!.setCharacteristicNotification(
                        mNotifyCharacteristic!!, false
                    )
                    mNotifyCharacteristic = null
                }
                mBluetoothLeService!!.readCharacteristic(characteristic)

            }
            if (char or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setCharacteristicNotification(
                    characteristic, true
                )
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_ble)

        val intent:Intent = intent
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)

        Log.i("TANIRO", mDeviceAddress)

        address.text = mDeviceAddress
        //data.text = findViewById<TextView>(R.id.data_value)
        //state.text = findViewById<TextView>(R.id.connection_state)

        //actionBar!!.title = mDeviceName
        //actionBar!!.setDisplayHomeAsUpEnabled(true)
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

    }

    private fun updateConnectionState(resourceId: Int) {
        Log.i("TANIRO33", mConnectionState.toString())
       // runOnUiThread { mConnectionState!!.setText(resourceId) }
    }

    private fun displayData(data: String?) {
        if (data != null) {
            mDataField!!.text = data
        }
    }

    //private fun clearUI() {
       /// mGattServicesList!!.setAdapter(null as SimpleExpandableListAdapter?)
       // mDataField!!.text = R.string.no_data.toString()
    //}

    override fun onResume() {
        Log.i("TANIRO", "OnResume")
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(TAG, "Connect request result=$result")
        }

        Log.i("TANIRO", mBluetoothLeService.toString())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gatt_services, menu)
        if (connected) {
            menu.findItem(R.id.menu_connect).setVisible(false)
            menu.findItem(R.id.menu_disconnect).setVisible(true)
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true)
            menu.findItem(R.id.menu_disconnect).setVisible(false)
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.i("Service","$mBluetoothLeService")
        Log.i("Address","$mDeviceAddress")

        when (item.itemId) {
            R.id.menu_connect -> {
                Log.i(">>>>>>>>>>>>>>", "$mBluetoothLeService")
                mBluetoothLeService!!.connect(mDeviceAddress)
                return true
            }
            R.id.menu_disconnect -> {
                mBluetoothLeService!!.disconnect()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_GATT_CONNECTED)
            intentFilter.addAction(ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }



}
