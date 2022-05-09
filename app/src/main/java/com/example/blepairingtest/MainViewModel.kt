package com.example.blepairingtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val LOG_TAG = "BLE_TEST"

class MainViewModel(applicationContext: Context) : ViewModel() {

    private val getBleScanResults = GetBleScanResults(applicationContext)
    private var scanJob: Job? = null

    private val _scanState = MutableStateFlow<ViewState.ScanState>(ViewState.ScanState.NoResults)
    val scanState: StateFlow<ViewState.ScanState> = _scanState

    private val _connectionState = MutableStateFlow<ViewState.ConnectionState>(ViewState.ConnectionState.NoConnections)
    val connectionState: StateFlow<ViewState.ConnectionState> = _connectionState

    @SuppressLint("MissingPermission")
    fun scanForDevices() {
        scanJob = viewModelScope.launch {
            Log.i(LOG_TAG, "Scan started")
            _scanState.value = ViewState.ScanState.Loading

            runCatching {
                getBleScanResults().first()
            }.onSuccess { bleScanResult ->
                when (bleScanResult) {
                    is BleScanResult.ScanSuccessful -> {
                        val scanResults = bleScanResult.results.sortedBy { it.device?.address }
                        Log.i(LOG_TAG, "Scan results: $scanResults")
                        _scanState.value = if (scanResults.isEmpty()) {
                            ViewState.ScanState.NoResults
                        } else {
                            ViewState.ScanState.Success(scanResults)
                        }
                    }
                    is BleScanResult.ScanFailed -> {
                        val errorMessage = "Scan failure with code: ${bleScanResult.errorCode}"
                        Log.i(LOG_TAG, errorMessage)
                        _scanState.value = ViewState.ScanState.Failure(errorMessage)
                    }
                }
            }.onFailure { throwable ->
                val errorMessage = "Scan failure: $throwable"
                Log.i(LOG_TAG, errorMessage)
                _scanState.value = ViewState.ScanState.Failure(errorMessage)
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
    }

    @SuppressLint("MissingPermission")
    fun connect(context: Context, device: BluetoothDevice?) {
        _connectionState.value = ViewState.ConnectionState.Connecting
        /*
        In theory we don't have to call createBond as it should be
        handled by the BLE api
         */
        device?.createBond()
        bluetoothGatt = device?.connectGatt(
            context,
            false,
            bleCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    private val bleCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.i(LOG_TAG, "GATT device connected")

                            when (gatt?.device?.bondState) {
                                BluetoothDevice.BOND_NONE -> {
                                    Log.i(LOG_TAG, "GATT device not bonded")
                                }
                                BluetoothDevice.BOND_BONDED -> {
                                    Log.i(LOG_TAG, "GATT device bonded")
                                }
                                BluetoothDevice.BOND_BONDING -> {
                                    Log.i(LOG_TAG, "GATT device bonding")
                                }
                            }
                            gatt?.discoverServices()
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            onGattError("GATT device disconnected")
                            bluetoothGatt?.close()
                        }
                    }
                }
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                    onGattError("GATT insufficient authentication")
                }
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                    onGattError("GATT insufficient encryption")
                }
                else -> {
                    onGattError("GATT error during connection: $status")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    gatt?.logAvailableServices()
                    gatt?.let {
                        _connectionState.value = ViewState.ConnectionState.Success(it.services)
                    }
                }
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                    onGattError("GATT insufficient authentication")
                }
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                    onGattError("GATT insufficient encryption")
                }
                else -> {
                    onGattError("GATT device disconnected")
                }
            }
        }

        private fun onGattError(errorMessage: String) {
            _connectionState.value = ViewState.ConnectionState.Failure(errorMessage)
            Log.i(LOG_TAG, errorMessage)
        }
    }

    /*
    More on bonding: https://medium.com/@martijn.van.welie/making-android-ble-work-part-4-72a0b85cb442
     */
    val bondStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i(LOG_TAG, "BondStateReceiver onReceive")
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            if (bluetoothGatt == null || !device?.address.equals(bluetoothGatt?.device?.address))
                return

            if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val bondState = intent.getIntExtra(EXTRA_BOND_STATE, ERROR)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                when (bondState) {
                    BluetoothDevice.BOND_BONDING -> {
                        Log.i(LOG_TAG, "BondStateReceiver: bonding")
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        Log.i(LOG_TAG, "BondStateReceiver: bonded")
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Log.i(LOG_TAG, "BondStateReceiver: none")
                    }
                    else -> {
                        Log.i(LOG_TAG, "BondStateReceiver: bond unknown state")
                    }
                }
            }
        }
    }
}

sealed interface ViewState {

    sealed interface ScanState {
        object NoResults : ScanState
        object Loading : ScanState
        data class Success(val scanResults: List<ScanResult>) : ScanState
        data class Failure(val errorMessage: String) : ScanState
    }

    sealed interface ConnectionState {
        object NoConnections : ConnectionState
        object Connecting : ConnectionState
        data class Success(val gattServices: List<BluetoothGattService>) : ConnectionState
        data class Failure(val errorMessage: String) : ConnectionState
    }
}

private fun BluetoothGatt.logAvailableServices() {
    services.forEach { service ->
        val characteristicsTable = service?.characteristics?.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { it?.uuid.toString() }
        Log.i(LOG_TAG, "\nGATT Service ${service.uuid}\nCharacteristics:\n$characteristicsTable")
    }
}
