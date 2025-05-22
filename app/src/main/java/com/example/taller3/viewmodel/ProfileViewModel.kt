package com.example.taller3.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setError(message: String) {
        _error.value = message
    }

    /**
     * Carga los datos del usuario actual desde Firestore.
     */
    fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "Usuario no autenticado"
            Log.w("ProfileViewModel", "UID nulo al cargar perfil")
            return
        }

        Log.d("ProfileViewModel", "Cargando perfil para UID: $uid")

        _loading.value = true
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _loading.value = false
                if (document.exists()) {
                    _userData.value = document.toObject(User::class.java)
                    Log.d("ProfileViewModel", "Datos del usuario cargados correctamente")
                } else {
                    _error.value = "Usuario no encontrado"
                    Log.w("ProfileViewModel", "Documento no existe para UID: $uid")
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                _error.value = e.message ?: "Error al cargar datos"
                Log.e("ProfileViewModel", "Error al obtener documento: ${e.message}", e)
            }
    }

    /**
     * Actualiza los datos del usuario en Firestore y la contraseña en Auth si se provee.
     */
    fun updateProfile(
        name: String,
        idNumber: String,
        phone: String,
        password: String?,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Usuario no autenticado")
            Log.w("ProfileViewModel", "UID nulo al actualizar perfil")
            return
        }

        val uid = user.uid
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "idNumber" to idNumber,
            "phone" to phone
        )

        photoUrl?.let { updates["photoUrl"] = it }

        _loading.value = true

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                if (!password.isNullOrBlank()) {
                    user.updatePassword(password)
                        .addOnSuccessListener {
                            _loading.value = false
                            onSuccess()
                            Log.d("ProfileViewModel", "Perfil y contraseña actualizados")
                        }
                        .addOnFailureListener { e ->
                            _loading.value = false
                            onError(e.message ?: "Error al actualizar contraseña")
                            Log.e("ProfileViewModel", "Error contraseña: ${e.message}", e)
                        }
                } else {
                    _loading.value = false
                    onSuccess()
                    Log.d("ProfileViewModel", "Perfil actualizado (sin contraseña)")
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onError(e.message ?: "Error al actualizar perfil")
                Log.e("ProfileViewModel", "Error Firestore: ${e.message}", e)
            }
    }
}
