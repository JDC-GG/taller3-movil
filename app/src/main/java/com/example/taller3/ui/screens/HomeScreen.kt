package com.example.taller3.ui.screens


import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taller3.viewmodel.AuthViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = Firebase.auth.currentUser?.uid.orEmpty()

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()

    // 📌 Permiso de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                scope.launch {
                    val location = fusedLocationClient.lastLocation.await()
                    userLocation = location
                }
            }
        }
    )

    // 🧠 Solicitar permiso al cargar pantalla
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // 🔁 Actualizar ubicación y estado en Firestore
    LaunchedEffect(isConnected) {
        if (isConnected) {
            val location = fusedLocationClient.lastLocation.await()
            userLocation = location

            location?.let {
                firestore.collection("users").document(uid)
                    .update(
                        mapOf(
                            "latitude" to it.latitude,
                            "longitude" to it.longitude,
                            "isConnected" to true
                        )
                    )
            }
        } else {
            firestore.collection("users").document(uid)
                .update("isConnected", false)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ubicación activa")
            Switch(checked = isConnected, onCheckedChange = { isConnected = it })
            Button(onClick = { onLogout() }) {
                Text("Cerrar sesión")
            }
        }

        if (userLocation != null) {
            val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Mi ubicación",
                    snippet = "Estás aquí"
                )
            }

            LaunchedEffect(latLng) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                    1000
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Esperando ubicación...")
            }
        }
    }
}
