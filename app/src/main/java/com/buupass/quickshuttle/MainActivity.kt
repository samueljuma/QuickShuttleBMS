package com.buupass.quickshuttle

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buupass.quickshuttle.navigation.AppNavigation
import com.buupass.quickshuttle.ui.theme.QuickShuttleBMSTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter // could be null if device does not support bluetooth
    }
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false // Set status bar color to white

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /**TODO when you want to check if bluetooth is enabled*/ }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->

            val allPermissionsGranted = perms.all { it.value }

            if (allPermissionsGranted) {
                val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    perms[Manifest.permission.BLUETOOTH_CONNECT] == true
                } else {
                    true
                }

                if (canEnableBluetooth && !isBluetoothEnabled) {
                    enableBluetoothLauncher.launch(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    )
                }
            } else {
                // TODO
            }
        }

        setContent {
            QuickShuttleBMSTheme {
                val mainViewModel: MainViewModel =  koinViewModel ()

                val snackbarHostState = remember { SnackbarHostState() }
                val isConnected by mainViewModel.isOnline.collectAsStateWithLifecycle()

                LaunchedEffect(isConnected) {
                    if (!isConnected) {
                        snackbarHostState.showSnackbar(
                            message = "No internet connection",
                            duration = SnackbarDuration.Indefinite,
                            withDismissAction = true
                        )
                    } else {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data
                            )
                        }
                    },
                    content = { innerPadding ->
                        AppNavigation(
                            modifier = Modifier.padding(innerPadding),
                            mainViewModel = mainViewModel
                        )
//                        PrinterTestScreen(modifier = Modifier.padding(innerPadding))
                    }
                )


            }
        }
    }
}