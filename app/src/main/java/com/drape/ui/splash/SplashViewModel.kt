package com.drape.ui.splash

import androidx.lifecycle.ViewModel
import com.drape.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    val isUserLoggedIn: Boolean
        get() = authRepository.isUserLoggedIn
}
