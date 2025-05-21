package com.example.taller3.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taller3.model.User
import com.example.taller3.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val uid = Firebase.auth.currentUser?.uid.orEmpty()
    var userData by remember { mutableStateOf<User?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getUserData(
            uid = uid,
            onResult = {
                userData = it
                name = it.name
                idNumber = it.idNumber
                phone = it.phone
            },
            onError = { error = it }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Editar Perfil", style = MaterialTheme.typography.headlineMedium)

        if (userData != null) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
            OutlinedTextField(value = idNumber, onValueChange = { idNumber = it }, label = { Text("ID") })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nueva contraseña (opcional)") })

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    loading = true
                    val updatedUser = userData!!.copy(
                        name = name,
                        idNumber = idNumber,
                        phone = phone
                    )
                    viewModel.updateUserData(
                        user = updatedUser,
                        newPassword = newPassword.ifBlank { null },
                        onSuccess = {
                            loading = false
                            onBack()
                        },
                        onError = {
                            loading = false
                            error = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Text("Guardar cambios")
            }

            TextButton(onClick = onBack) {
                Text("Cancelar")
            }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            Text("Cargando datos...")
        }
    }
}
