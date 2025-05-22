package com.example.taller3.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.taller3.viewmodel.ProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageRef = remember { Firebase.storage.reference }

    val user by viewModel.userData.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    // Esperar a que el usuario esté autenticado
    val currentUser = Firebase.auth.currentUser

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            Log.d("EDIT_PROFILE", "UID activo: ${currentUser.uid}")
            viewModel.loadUserProfile()
        } else {
            Log.w("EDIT_PROFILE", "FirebaseAuth.currentUser aún no está listo.")
        }
    }

    // Inicializar campos cuando llegan datos
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            idNumber = it.idNumber
            phone = it.phone
            photoUrl = it.photoUrl
        }
    }

    // Selector de imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val fileName = "profile_images/${UUID.randomUUID()}.jpg"
                    val imageRef = storageRef.child(fileName)
                    imageRef.putFile(it).await()
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    photoUrl = downloadUrl
                    Log.d("EDIT_PROFILE", "Imagen subida correctamente: $downloadUrl")
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.setError("Error al subir imagen")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editar Perfil", style = MaterialTheme.typography.headlineMedium)

            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cambiar Foto")
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("Número de Identificación") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva Contraseña (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    viewModel.updateProfile(
                        name = name.trim(),
                        idNumber = idNumber.trim(),
                        phone = phone.trim(),
                        password = password.trim().ifBlank { null },
                        photoUrl = photoUrl,
                        onSuccess = onProfileUpdated,
                        onError = { msg -> viewModel.setError(msg) }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("Guardar cambios")
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
