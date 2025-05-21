package com.example.taller3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.taller3.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    onProfileUpdated: () -> Unit
) {
    val user = viewModel.getCurrentUser()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            viewModel.getUserData(uid) { fetchedUser, err ->
                if (fetchedUser != null) {
                    name = fetchedUser.name
                    idNumber = fetchedUser.idNumber
                    phone = fetchedUser.phone
                } else {
                    error = err ?: "Error al cargar datos"
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
                label = { Text("Nueva Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    loading = true
                    viewModel.updateUserProfile(
                        name.trim(),
                        idNumber.trim(),
                        phone.trim(),
                        password.trim(),
                        onSuccess = {
                            loading = false
                            onProfileUpdated()
                        },
                        onError = {
                            loading = false
                            error = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
