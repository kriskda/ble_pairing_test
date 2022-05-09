package com.example.blepairingtest

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.blepairingtest.view.MainScreen

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainViewModel(applicationContext)
        setContent {
            MainScreen(
                viewModel = viewModel,
                onStartScanClick = { viewModel.scanForDevices() },
                onConnectClick = { device -> viewModel.connect(applicationContext, device) },
                onDisconnectClick = { viewModel.disconnect() },
            )
        }
        setPermissions()
    }

    override fun onStart() {
        super.onStart()
        val bondFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(viewModel.bondStateReceiver, bondFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(viewModel.bondStateReceiver)
        viewModel.stopScan()
        viewModel.disconnect()
    }

    private fun setPermissions() {
        permissionsLauncher.launch(permissions)
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionToResult ->

    }
}

private val permissions: Array<String> =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.S ->
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.READ_PHONE_STATE
            )
        else -> arrayOf(Manifest.permission.READ_PHONE_STATE)
    }
