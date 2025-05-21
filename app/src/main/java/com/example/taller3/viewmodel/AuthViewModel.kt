package com.example.taller3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState

    fun registerUser(
        name: String,
        idNumber: String,
        email: String,
        password: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        val user = User(
                            uid = uid,
                            name = name,
                            idNumber = idNumber,
                            email = email,
                            phone = phone
                        )
                        firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "Error al guardar usuario") }
                    } else {
                        onError(task.exception?.message ?: "Error de autenticación")
                    }
                }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _authState.value = auth.currentUser
                        onSuccess()
                    } else {
                        onError(it.exception?.message ?: "Credenciales inválidas")
                    }
                }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = null
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Nueva función para obtener datos del usuario
    fun getUserData(
        uid: String,
        onResult: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) onResult(user)
                else onError("Usuario no encontrado")
            }
            .addOnFailureListener {
                onError(it.message ?: "Error al cargar usuario")
            }
    }

    // Nueva función para actualizar datos del usuario
    fun updateUserData(
        user: User,
        newPassword: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener {
                if (!newPassword.isNullOrBlank()) {
                    FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                        ?.addOnSuccessListener { onSuccess() }
                        ?.addOnFailureListener {
                            onError(it.message ?: "No se pudo cambiar la contraseña")
                        }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Error al actualizar datos")
            }
    }
}