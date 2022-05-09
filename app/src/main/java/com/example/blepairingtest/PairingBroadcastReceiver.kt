package com.example.blepairingtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.UnsupportedEncodingException

class PairingBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(LOG_TAG, "PairingBroadcastReceiver onReceive")
        if (BluetoothDevice.ACTION_PAIRING_REQUEST == intent.action) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            if (device != null) {
                try {
                    /* Set pin value for pairing here */
                    val pinValue = "1111"

                    /* setPin returns: true pin has been set, false for error */
                    val wasPinSet = device.setPin(pinValue.toByteArray(charset("UTF-8")))

                    if (wasPinSet) {
                        Log.i(LOG_TAG, "Pin $pinValue was set")
                    } else {
                        Log.i(LOG_TAG, "Failed to set pin")
                    }
                } catch (e: UnsupportedEncodingException) {
                    Log.i(LOG_TAG, "Failed to set pin: ${e.printStackTrace()} ")
                }
            }
        }
    }
}
