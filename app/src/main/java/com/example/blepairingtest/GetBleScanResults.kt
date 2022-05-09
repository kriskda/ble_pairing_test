package com.example.blepairingtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
class GetBleScanResults(context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    operator fun invoke(): Flow<BleScanResult> =
        callbackFlow {
            val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            val bluetoothScanCallback = object : ScanCallback() {

                override fun onBatchScanResults(results: List<ScanResult>) {
                    trySend(BleScanResult.ScanSuccessful(results))
                }

                override fun onScanFailed(errorCode: Int) {
                    trySend(BleScanResult.ScanFailed(errorCode))
                }
            }

            /*
                Used empty filter so I don't have to hardcode mac address
             */
            val filter = listOf(
                ScanFilter.Builder()
                    .build()
            )
            val scanSettings = ScanSettings.Builder()
                /*
                    Value > 0 causes onBatchScanResults to be executed
                    and it will deliver list of scan results.
                    Currently set to 5s, modify as needed
                 */
                .setReportDelay(5000L)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(filter, scanSettings, bluetoothScanCallback)

            awaitClose {
                bluetoothLeScanner?.apply {
                    stopScan(bluetoothScanCallback)
                    flushPendingScanResults(bluetoothScanCallback)
                }
            }
        }
}

sealed interface BleScanResult {
    data class ScanSuccessful(val results: List<ScanResult>) : BleScanResult
    data class ScanFailed(val errorCode: Int) : BleScanResult
}
