package com.example.appconnectespble

import android.util.Log
import android.view.View
import android.widget.TextView

class ViewHolder(v: View) {

    var textViewAdress: TextView
    var textViewName: TextView

    init {
        Log.i("HOLDER", "Fazendo buscas por id...")
        textViewAdress = v.findViewById(R.id.deviceAddress)
        textViewName = v.findViewById(R.id.deviceName)
    }
}