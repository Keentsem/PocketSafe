package com.example.pocketsafe.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.data.User
import com.example.pocketsafe.data.dao.UserDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Authentication ViewModel that handles user login and registration
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling for UI elements
 */
class AuthViewModel(
    private val userDao: UserDao
) : ViewModel() {
    
    /**
     * Factory for creating AuthViewModel with the UserDao
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                // Create dao here without Hilt
                val database = MainApplication.getDatabase(application)
                return AuthViewModel(database.userDao()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting to login with email: $email")
                // Firebase authentication
                val firebaseAuth = FirebaseAuth.getInstance()
                var firebaseSuccess = false
                
                try {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    firebaseSuccess = true
                    Log.d("AuthViewModel", "Firebase authentication successful")
                } catch (e: Exception) {
                    val errorMsg = when {
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Invalid email or password"
                        e.message?.contains("badly formatted") == true -> "Email address is badly formatted"
                        e.message?.contains("CONFIGURATION") == true -> "Firebase configuration issue detected. Trying local authentication..."
                        else -> "Firebase login failed: ${e.message}. Trying local authentication..."
                    }
                    Log.e("AuthViewModel", "Firebase login failed: ${e.message}", e)
                    
                    // Don't return here - continue with local authentication as fallback
                    val isConfigError = e.message?.contains("CONFIGURATION") == true
                    val isNetworkError = e.message?.contains("network") == true
                    val isServiceUnavailable = e.message?.contains("unavailable") == true
                    
                    if (isConfigError || isNetworkError || isServiceUnavailable) {
                        Log.d("AuthViewModel", "Using local authentication as fallback")
                    } else {
                        _authState.value = AuthState.Error(errorMsg)
                        return@launch
                    }
                }
                // Local authentication - always try this if Firebase failed or as verification
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }
                
                if (user != null) {
                    Log.d("AuthViewModel", "User found in local database, checking password")
                    if (user.password == password) {
                        Log.d("AuthViewModel", "Password matches, login successful")
                        _authState.value = AuthState.Success("Login successful")
                    } else if (!firebaseSuccess) {
                        // Only show password error if Firebase also failed
                        Log.d("AuthViewModel", "Password does not match")
                        _authState.value = AuthState.Error("Invalid password")
                    }
                } else if (!firebaseSuccess) {
                    // If Firebase authentication failed and user not in local DB
                    Log.d("AuthViewModel", "User not found in local database")
                    _authState.value = AuthState.Error("User not found. Please register first.")
                } else {
                    // Firebase succeeded but user not in local DB - create local user
                    Log.d("AuthViewModel", "Creating local user record after Firebase success")
                    val newUser = User(email = email, password = password)
                    withContext(Dispatchers.IO) {
                        userDao.insert(newUser)
                    }
                    _authState.value = AuthState.Success("Login successful")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting to register with email: $email")
                // Firebase registration
                val firebaseAuth = FirebaseAuth.getInstance()
                var firebaseSuccess = false
                
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    firebaseSuccess = true
                    Log.d("AuthViewModel", "Firebase registration successful")
                } catch (e: Exception) {
                    val errorMsg = when {
                        e.message?.contains("email address is already in use") == true -> "Email address is already in use"
                        e.message?.contains("badly formatted") == true -> "Email address is badly formatted"
                        e.message?.contains("CONFIGURATION") == true -> "Firebase configuration issue detected. Using local registration..."
                        e.message?.contains("weak password") == true -> "Password should be at least 6 characters"
                        else -> "Firebase registration failed: ${e.message}. Using local registration..."
                    }
                    Log.e("AuthViewModel", "Firebase registration failed: ${e.message}", e)
                    
                    // Don't return here for configuration errors - continue with local registration
                    val isConfigError = e.message?.contains("CONFIGURATION") == true
                    val isNetworkError = e.message?.contains("network") == true
                    val isServiceUnavailable = e.message?.contains("unavailable") == true
                    
                    if (!(isConfigError || isNetworkError || isServiceUnavailable)) {
                        _authState.value = AuthState.Error(errorMsg)
                        return@launch
                    } else {
                        Log.d("AuthViewModel", "Using local registration as fallback")
                    }
                }
                // Local registration - always do this if Firebase failed or as verification
                val existingUser = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }
                
                if (existingUser != null && !firebaseSuccess) {
                    // Only show this error if Firebase also failed
                    Log.d("AuthViewModel", "Email already registered locally")
                    _authState.value = AuthState.Error("Email already registered")
                    return@launch
                }
                
                // Create or update local user
                val newUser = User(email = email, password = password)
                withContext(Dispatchers.IO) {
                    if (existingUser != null) {
                        // Update existing user if needed
                        userDao.update(newUser.copy(id = existingUser.id))
                    } else {
                        // Insert new user
                        userDao.insert(newUser)
                    }
                }
                
                Log.d("AuthViewModel", "Registration successful")
                _authState.value = AuthState.FirstTimeUser
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
}