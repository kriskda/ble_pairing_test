package com.example.blepairingtest.view

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blepairingtest.ViewState

@Composable
fun ScanResultsView(
    viewState: ViewState.ScanState,
    onStartScan: () -> Unit,
    onConnect: (device: BluetoothDevice?) -> Unit,
) {
    Column {
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = onStartScan
        ) {
            Text(text = "Scan for devices")
        }

        when (viewState) {
            ViewState.ScanState.NoResults -> Text(text = "No scan results")
            ViewState.ScanState.Loading -> Text(text = "Scanning...")
            is ViewState.ScanState.Success ->
                ScanResultsList(
                    scanResults = viewState.scanResults,
                    onConnect = onConnect,
                )
            is ViewState.ScanState.Failure -> Text(text = viewState.errorMessage)
        }
    }
}

@Composable
private fun ScanResultsList(
    scanResults: List<ScanResult>,
    onConnect: (device: BluetoothDevice?) -> Unit,
) {
    LazyColumn {
        items(scanResults) { scanResult ->
            ScanResultsRow(
                scanResult = scanResult,
                onConnect = onConnect,
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun ScanResultsRow(
    scanResult: ScanResult,
    onConnect: (device: BluetoothDevice?) -> Unit,
) {
    val device = scanResult.device
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = device.name ?: "UNKNOWN",
                fontWeight = FontWeight.Bold
            )
            Text(text = device.address ?: "UNKNOWN")
        }
        Button(
            modifier = Modifier.padding(4.dp),
            onClick = { onConnect(device) }) {
            Text(text = "CONNECT")
        }
    }
}
