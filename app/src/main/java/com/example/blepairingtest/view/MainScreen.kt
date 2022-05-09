package com.example.blepairingtest.view

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.blepairingtest.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onStartScanClick: () -> Unit,
    onConnectClick: (device: BluetoothDevice?) -> Unit,
    onDisconnectClick: () -> Unit,
) {
    val scanState by viewModel.scanState.collectAsState()
    val connectionStateState by viewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(0.5f)
        ) {
            ScanResultsView(
                viewState = scanState,
                onStartScan = onStartScanClick,
                onConnect = onConnectClick,
            )
        }
        Box {
            ConnectionDetailsView(
                viewState = connectionStateState,
                onDisconnect = onDisconnectClick
            )
        }
    }
}
