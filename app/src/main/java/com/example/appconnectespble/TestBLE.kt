package com.example.appconnectespble

import LeDeviceListAdapter
import android.annotation.TargetApi
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import android.view.InputDevice.getDevice
import android.bluetooth.BluetoothDevice
import android.text.method.TextKeyListener.clear
import android.app.Activity
import android.view.Menu
import android.view.MenuItem



@TargetApi(21)
class TestBLE : ListActivity()  {

    var REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD: Long = 10000
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private val mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_ble)
        getActionBar()?.setTitle(R.string.title_devices)
        mHandler = Handler()

        android.R.id.list

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }


        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }


    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed(Runnable {
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                invalidateOptionsMenu()
            }, SCAN_PERIOD)
            mScanning = true
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        }
        invalidateOptionsMenu()
    }

    private val mLeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
                mLeDeviceListAdapter!!.addDevice(device)
                mLeDeviceListAdapter!!.notifyDataSetChanged()
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false)
            menu.findItem(R.id.menu_scan).setVisible(true)
            menu.findItem(R.id.menu_refresh).setActionView(null)
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true)
            menu.findItem(R.id.menu_scan).setVisible(false)
            menu.findItem(R.id.menu_refresh).setActionView(
                R.layout.actionbar_indeterminate_progress
            )
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_scan -> {
                mLeDeviceListAdapter!!.clear()
                scanLeDevice(true)
            }
            R.id.menu_stop -> scanLeDevice(false)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter?.isEnabled()!!) {
            if (!mBluetoothAdapter?.isEnabled()!!) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = LeDeviceListAdapter()
        listAdapter = mLeDeviceListAdapter
        scanLeDevice(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        mLeDeviceListAdapter!!.clear()
    }

}
