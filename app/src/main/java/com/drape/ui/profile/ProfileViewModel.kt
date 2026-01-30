package com.drape.ui.profile

import androidx.lifecycle.ViewModel
import com.drape.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userFlow = authRepository.userFlow

    fun signOut() {
        authRepository.signOut()
    }

    val daysInApp: Long
        get() {
            val user = authRepository.currentUser ?: return 0L
            val createdAt = user.createdAt
            if (createdAt == 0L) return 0L
            
            val diff = System.currentTimeMillis() - createdAt
            return diff / (1000 * 60 * 60 * 24)
        }
}
