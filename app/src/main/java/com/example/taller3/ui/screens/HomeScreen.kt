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
import com.example.taller3.model.User
import com.example.taller3.viewmodel.AuthViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

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
    val cameraPositionState = rememberCameraPositionState()

    // Lista de usuarios conectados (hasta 100)
    var connectedUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    // Lista de puntos de ruta del usuario actual para polyline
    val userPathPoints = remember { mutableStateListOf<LatLng>() }

    // Lanzador para permiso de ubicación
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

    // Solicitar permiso ubicación al iniciar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Listener en Firestore para usuarios conectados
    DisposableEffect(Unit) {
        val listenerRegistration: ListenerRegistration = firestore.collection("users")
            .whereEqualTo("isConnected", true)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    connectedUsers = users
                }
            }
        onDispose {
            listenerRegistration.remove()
        }
    }

    // Actualizar estado conexión y ubicación en Firestore cuando cambia isConnected
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
                userPathPoints.clear()
                userPathPoints.add(LatLng(it.latitude, it.longitude))
            }
        } else {
            firestore.collection("users").document(uid)
                .update("isConnected", false)
            userPathPoints.clear()
        }
    }

    // Cuando cambia ubicación local y está conectado, actualizar ruta y Firestore
    LaunchedEffect(userLocation, isConnected) {
        if (isConnected && userLocation != null) {
            val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
            if (userPathPoints.isEmpty() || userPathPoints.last() != latLng) {
                userPathPoints.add(latLng)
                firestore.collection("users").document(uid)
                    .update(
                        mapOf(
                            "latitude" to latLng.latitude,
                            "longitude" to latLng.longitude
                        )
                    )
            }
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
            Button(onClick = { onEditProfile() }) {
                Text("Editar perfil")
            }
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
                // Marcador usuario actual
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Mi ubicación",
                    snippet = "Estás aquí"
                )

                // Marcadores de otros usuarios conectados
                connectedUsers.filter { it.uid != uid }.forEach { user ->
                    Marker(
                        state = MarkerState(position = LatLng(user.latitude, user.longitude)),
                        title = user.name,
                        snippet = "Usuario conectado"
                    )
                }

                // Polyline ruta usuario actual
                if (userPathPoints.size > 1) {
                    Polyline(
                        points = userPathPoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 5f
                    )
                }
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
