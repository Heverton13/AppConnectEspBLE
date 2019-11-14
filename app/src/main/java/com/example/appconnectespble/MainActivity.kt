package com.example.appconnectespble

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.os.postDelayed
import android.bluetooth.le.BluetoothLeScanner
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import java.lang.Compiler.enable
import android.bluetooth.BluetoothDevice
import android.view.InputDevice.getDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import android.R.attr.name
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import java.lang.Compiler.enable
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothGattCallback
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*


@TargetApi(21)
class MainActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1
    private var mHandler: Handler? = null
    private val SCAN_PERIOD: Long = 10000
    private var mLEScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    private var filters: List<ScanFilter>? = null
    private var mGatt: BluetoothGatt? = null



    //Função Principal
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHandler = Handler()
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish()
        }

        //val  bluetoothManager:BluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE)
                //mBluetoothAdapter = bluetoothManager.getAdapter();

        btnConnect.setOnClickListener {
            checkLocationPermission()
        }

    }

    fun startScan(){

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            var enableBtIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner()
                settings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
                filters = ArrayList<ScanFilter>()
            }
            scanLeDevice(true)
        }
    }

    fun scanLeDevice(enable : Boolean){
        if (enable) {
            mHandler!!.postDelayed(Runnable {
                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter?.stopLeScan(mLeScanCallback)
                } else {
                    mLEScanner!!.stopScan(mScanCallback)

                }
            }, SCAN_PERIOD)
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter!!.startLeScan(mLeScanCallback)
            } else {
                mLEScanner!!.startScan(filters, settings, mScanCallback)
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            } else {
                mLEScanner!!.stopScan(mScanCallback)
            }
        }

    }

    private val mScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("callbackType", callbackType.toString())
            Log.i("result", result.toString())
            val btDevice = result.getDevice()
            confConetion.text = result.toString()
            connectToDevice(btDevice)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i("ScanResult - Results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    private val mLeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
                Log.i("onLeScan", device.toString())
                connectToDevice(device)
            }

        }

    fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback)
            scanLeDevice(false)// will stop after first device detection
        }
    }


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("onConnectionStateChange", "Status: $status")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("gattCallback", "STATE_CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> Log.e("gattCallback", "STATE_DISCONNECTED")
                else -> Log.e("gattCallback", "STATE_OTHER")
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val services = gatt.services
            Log.i("onServicesDiscovered", services.toString())
            gatt.readCharacteristic(services[1].characteristics[0])
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.i("onCharacteristicRead", characteristic.toString())
            gatt.disconnect()
        }
    }

    fun checkLocationPermission() {
        if(isReadStorageAllowed()){
            //If permission is already having then showing the toast
            Toast.makeText(this,"You already have the permission",Toast.LENGTH_LONG).show()
            //Existing the method with return
            startScan()
            return
        }
        //If the app has not the permission then asking for the permission
    }

    fun isReadStorageAllowed():Boolean {
        //Getting the permission status
        var result:Int = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED) {
            Log.i("INFO", "Permitiu")
            return true;
        }
        //If permission is not granted returning false
        return false;
    }

    fun connectToBle(v: View) {
        checkLocationPermission()
    }

}
