package com.example.blepairingtest.view

import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blepairingtest.ViewState

@Composable
fun ConnectionDetailsView(
    viewState: ViewState.ConnectionState,
    onDisconnect: () -> Unit
) {
    Box(modifier = Modifier.padding(4.dp)) {
        when (viewState) {
            ViewState.ConnectionState.NoConnections -> {
                Text(
                    color = Color.Black,
                    text = "No connections"
                )
            }
            ViewState.ConnectionState.Connecting -> {
                Text(
                    color = Color.Black,
                    text = "Connecting..."
                )
            }
            is ViewState.ConnectionState.Success -> {
                ConnectionDetails(
                    gattServices = viewState.gattServices,
                    onDisconnect = onDisconnect
                )
            }
            is ViewState.ConnectionState.Failure -> {
                Text(
                    color = Color.Black,
                    text = "Connection error: ${viewState.errorMessage}"
                )
            }
        }
    }
}

@Composable
private fun ConnectionDetails(
    gattServices: List<BluetoothGattService>,
    onDisconnect: () -> Unit
) {
    Column {
        Button(
            modifier = Modifier.padding(2.dp),
            onClick = { onDisconnect() }
        ) {
            Text(text = "DISCONNECT")
        }
        LazyColumn {
            itemsIndexed(gattServices) { index, gattService ->
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        color = Color.Black,
                        text = "${index + 1}) Gatt service",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        color = Color.Black,
                        text = "${gattService.uuid}"
                    )
                    Text(
                        color = Color.Black,
                        text = "Characteristics",
                        fontWeight = FontWeight.Bold
                    )
                    gattService.characteristics.forEach { characteristic ->
                        Text(
                            color = Color.Black,
                            text = "${characteristic?.uuid}"
                        )
                    }
                }
            }
        }
    }
}
