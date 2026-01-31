package com.drape.ui.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.userFlow.first()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        displayName = user.displayName,
                        bio = user.bio,
                        currentPhotoUrl = user.photoUrl,
                        currentCoverUrl = user.coverPhotoUrl
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(displayName = newName) }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(bio = newBio) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedPhotoUri = uri) }
    }

    fun onCoverPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedCoverUri = uri) }
    }

     fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authRepository.updateProfile(
                    displayName = uiState.value.displayName,
                    bio = uiState.value.bio,
                    photoUri = uiState.value.selectedPhotoUri,
                    coverPhotoUri = uiState.value.selectedCoverUri
                )
                onSuccess()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class EditProfileUiState(
    val displayName: String = "",
    val bio: String = "",
    val currentPhotoUrl: String? = null,
    val selectedPhotoUri: Uri? = null,
    val currentCoverUrl: String? = null,
    val selectedCoverUri: Uri? = null,
    val isLoading: Boolean = false
)
