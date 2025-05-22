package com.example.taller3.viewmodel

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

    /**
     * Establece un mensaje de error en el flujo.
     */
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
            return
        }

        _loading.value = true
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _loading.value = false
                if (document.exists()) {
                    _userData.value = document.toObject(User::class.java)
                } else {
                    _error.value = "Usuario no encontrado"
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                _error.value = e.message ?: "Error al cargar datos"
            }
    }

    /**
     * Actualiza los datos del usuario en Firestore y contraseña en Auth si corresponde.
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
        val user = auth.currentUser ?: return onError("Usuario no autenticado")
        val uid = user.uid

        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "idNumber" to idNumber,
            "phone" to phone
        )

        photoUrl?.let {
            updates["photoUrl"] = it
        }

        _loading.value = true

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                if (!password.isNullOrBlank()) {
                    user.updatePassword(password)
                        .addOnSuccessListener {
                            _loading.value = false
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            _loading.value = false
                            onError(e.message ?: "Error al actualizar contraseña")
                        }
                } else {
                    _loading.value = false
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onError(e.message ?: "Error al actualizar perfil")
            }
    }
}
