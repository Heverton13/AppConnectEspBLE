
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.BaseAdapter
import com.example.appconnectespble.MainActivity
import com.example.appconnectespble.R
import com.example.appconnectespble.TestBLE
import com.example.appconnectespble.ViewHolder


class LeDeviceListAdapter(var c: Context) : BaseAdapter() {

    val mLeDevices: ArrayList<BluetoothDevice>
    val mInflator: LayoutInflater? = null

    init {
        mLeDevices = ArrayList()
    }

    fun addDevice(device: BluetoothDevice) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(i: Int): Any {
        return mLeDevices[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val viewHolder: ViewHolder
        // General ListView optimization code.
        if (view == null) {
            view = LayoutInflater.from(c).inflate(R.layout.listitem_device, viewGroup, false)
            viewHolder = ViewHolder(view)
            viewHolder.textViewAdress = view.findViewById(R.id.deviceAddress)
            viewHolder.textViewName = view.findViewById(R.id.deviceName)
            view!!.setTag(viewHolder)
        } else {
            viewHolder = view!!.getTag() as ViewHolder
        }

        val device = mLeDevices[i]
        val deviceName = device.name
        if (deviceName != null && deviceName.length > 0)
            viewHolder.textViewName.setText(deviceName)
        else
            viewHolder.textViewName.setText(R.string.unknown_device)
            viewHolder.textViewAdress.setText(device.address)

        return view
    }


}