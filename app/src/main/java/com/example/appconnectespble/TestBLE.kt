package com.example.appconnectespble

import LeDeviceListAdapter
import android.Manifest
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
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.core.content.ContextCompat

@TargetApi(21)
class TestBLE : ListActivity() {

    var REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD: Long = 10000
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null
    private var settings: ScanSettings? = null
    private var filters: List<ScanFilter>? = null
    var REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 200

    val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_ble)
        mHandler = Handler()


        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        checkLocationPermission()

        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            fuckMarshMallow()
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

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val device: BluetoothDevice = mLeDeviceListAdapter!!.getDevice(position)
        if (device == null) return
        var intent = Intent(this, ControlBleActivity::class.java)
        intent.putExtra(ControlBleActivity.EXTRAS_DEVICE_NAME, device.name)
        intent.putExtra(ControlBleActivity.EXTRAS_DEVICE_ADDRESS, device.address)
        if (mScanning) {
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            mScanning = false
        }
        startActivity(intent)
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
        if (!mBluetoothAdapter!!.isEnabled()!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = LeDeviceListAdapter(this)
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

    fun checkLocationPermission() {
        Log.i("INFO", "Entro em check")
        if (isReadStorageAllowed()) {
            //If permission is already having then showing the toast
            Toast.makeText(this, "You already have the permission", Toast.LENGTH_LONG).show()
            //Existing the method with return
            startScan()
            return
        }
        //If the app has not the permission then asking for the permission
    }

    fun isReadStorageAllowed(): Boolean {
        //Getting the permission status
        Log.i("INFO", "Entro Read")
        var result: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        var result2: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED) {
            Log.i("INFO", "Permitiu")
            return true
        }
        //If permission is not granted returning false
        return false
    }

    fun startScan() {

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter!!.bluetoothLeScanner
            settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            filters = ArrayList<ScanFilter>()
        }
        Log.i("BLE", "$filters")
        scanLeDevice(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                val perms = HashMap<String, Int>()
                // Initial
                perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED

                // Fill with results
                for (i in permissions.indices)
                    perms[permissions[i]] = grantResults[i]

                // Check for ACCESS_FINE_LOCATION
                if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    // Permission Denied
                    Toast.makeText(
                        this@TestBLE,
                        "All Permission GRANTED !! Thank You :)",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    // Permission Denied
                    Toast.makeText(
                        this@TestBLE,
                        "One or More Permissions are DENIED Exiting App :(",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    finish()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun fuckMarshMallow() {
        val permissionsNeeded = ArrayList<String>()

        val permissionsList = ArrayList<String>()
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location")

        if (permissionsList.size > 0) {
            if (permissionsNeeded.size > 0) {

                // Need Rationale
                var message = "App need access to " + permissionsNeeded[0]

                for (i in 1 until permissionsNeeded.size)
                    message = message + ", " + permissionsNeeded[i]

                showMessageOKCancel(message,
                    DialogInterface.OnClickListener { dialog, which ->
                        requestPermissions(
                            permissionsList.toTypedArray(),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                        )
                    })
                return
            }
            requestPermissions(
                permissionsList.toTypedArray(),
                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
            return
        }

        Toast.makeText(
            this@TestBLE,
            "No new Permission Required- Launching App .You are Awesome!!",
            Toast.LENGTH_SHORT
        )
            .show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false
        }
        return true
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@TestBLE)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


}
