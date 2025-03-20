package com.example.wardrobe_share.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wardrobe_share.model.Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {

    private val model = Model.shared

    private val _user = MutableLiveData<FirebaseUser?>().apply {
        value = FirebaseAuth.getInstance().currentUser
    }
    val user: LiveData<FirebaseUser?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun signIn(email: String, password: String) {
        model.signIn(email, password) { firebaseUser, errorMessage ->
            if (firebaseUser != null) {
                Log.d("AuthViewModel", "Sign in successful for user: ${firebaseUser.uid}")
                _user.postValue(firebaseUser)
            } else {
                Log.e("AuthViewModel", "Sign in failed: ${errorMessage ?: "Unknown error"}")
                _error.postValue(errorMessage ?: "Unknown error during sign in.")
            }
        }
    }

    fun signUp(email: String, password: String, username: String, bitmap: Bitmap?) {
        model.signUp(email, password, username, bitmap) { firebaseUser, errorMessage ->
            if (firebaseUser != null) {
                Log.d("AuthViewModel", "Sign up successful for user: ${firebaseUser.uid}")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AuthViewModel", "User profile updated successfully.")
                        _user.postValue(firebaseUser)
                    } else {
                        val exceptionMessage = task.exception?.message ?: "Profile update failed"
                        Log.e("AuthViewModel", exceptionMessage)
                        _error.postValue("Failed to update user profile.")
                    }
                }
            } else {
                Log.e("AuthViewModel", "Sign up failed: ${errorMessage ?: "Unknown error"}")
                _error.postValue(errorMessage ?: "Unknown error during sign up.")
            }
        }
    }

    fun signOut() {
        model.signOut()
        Log.d("AuthViewModel", "User signed out.")
        _user.postValue(null)
    }
}