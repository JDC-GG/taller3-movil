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
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.taller3.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon


@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = Firebase.auth.currentUser?.uid.orEmpty()

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var otherUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    val polylinePoints = remember { mutableStateListOf<LatLng>() }
    val otherUserPolylines = remember { mutableStateMapOf<String, MutableList<LatLng>>() }

    val cameraPositionState = rememberCameraPositionState()

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

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            // Escuchar usuarios conectados
            firestore.collection("users")
                .whereEqualTo("isConnected", true)
                .addSnapshotListener { snapshot, _ ->
                    val users = snapshot?.documents
                        ?.mapNotNull { it.toObject(User::class.java) }
                        ?.filter { it.uid != uid }
                        ?.take(100)

                    otherUsers = users ?: emptyList()

                    users?.forEach { user ->
                        val currentLatLng = LatLng(user.latitude, user.longitude)

                        val existingRoute = otherUserPolylines[user.uid] ?: mutableListOf()
                        if (existingRoute.lastOrNull() != currentLatLng) {
                            existingRoute.add(currentLatLng)
                            otherUserPolylines[user.uid] = existingRoute
                        }
                    }

                    val activeUids = users?.map { it.uid } ?: emptyList()
                    otherUserPolylines.keys
                        .filterNot { it in activeUids }
                        .forEach { otherUserPolylines.remove(it) }
                }

            while (isConnected) {
                val location = fusedLocationClient.lastLocation.await()
                userLocation = location

                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    polylinePoints.add(latLng)

                    firestore.collection("users").document(uid).update(
                        mapOf(
                            "latitude" to it.latitude,
                            "longitude" to it.longitude,
                            "isConnected" to true
                        )
                    )
                }

                delay(3000L)
            }
        } else {
            firestore.collection("users").document(uid)
                .update("isConnected", false)
            otherUsers = emptyList()
            polylinePoints.clear()
            otherUserPolylines.clear()
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
            Column {
                Text("Ubicación activa")
                Switch(checked = isConnected, onCheckedChange = { isConnected = it })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Perfil")
                }
                Button(onClick = onLogout) {
                    Text("Cerrar sesión")
                }
            }
        }

        if (userLocation != null) {
            val myLatLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = myLatLng),
                    title = "Tú",
                    snippet = "Mi ubicación"
                )

                otherUsers.forEach { user ->
                    Marker(
                        state = MarkerState(position = LatLng(user.latitude, user.longitude)),
                        title = user.name,
                        snippet = user.email
                    )
                }

                if (polylinePoints.size >= 2) {
                    Polyline(
                        points = polylinePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 8f
                    )
                }

                otherUserPolylines.forEach { (_, points) ->
                    if (points.size >= 2) {
                        Polyline(
                            points = points,
                            color = MaterialTheme.colorScheme.tertiary,
                            width = 6f
                        )
                    }
                }
            }

            LaunchedEffect(myLatLng) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(myLatLng, 16f),
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