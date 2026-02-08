package com.drape.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.repository.AuthRepository
import com.drape.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for body reference image upload.
 */
data class BodyImageUploadState(
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val uploadSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val userFlow = userRepository.userFlow

    private val _bodyImageUploadState = MutableStateFlow(BodyImageUploadState())
    val bodyImageUploadState: StateFlow<BodyImageUploadState> = _bodyImageUploadState

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

    /**
     * Uploads a body reference image for virtual try-on.
     *
     * @param imageUri The URI of the image to upload
     */
    fun uploadBodyReferenceImage(imageUri: Uri) {
        viewModelScope.launch {
            _bodyImageUploadState.value = BodyImageUploadState(isUploading = true, uploadError = null, uploadSuccess = false)
            
            try {
                userRepository.uploadBodyReferenceImage(imageUri)
                _bodyImageUploadState.value = BodyImageUploadState(isUploading = false, uploadError = null, uploadSuccess = true)
            } catch (e: Exception) {
                _bodyImageUploadState.value = BodyImageUploadState(
                    isUploading = false, 
                    uploadError = e.message ?: "Errore durante il caricamento", 
                    uploadSuccess = false
                )
            }
        }
    }

    /**
     * Removes the body reference image.
     */
    fun removeBodyReferenceImage() {
        viewModelScope.launch {
            _bodyImageUploadState.value = BodyImageUploadState(isUploading = true, uploadError = null, uploadSuccess = false)
            
            try {
                userRepository.removeBodyReferenceImage()
                _bodyImageUploadState.value = BodyImageUploadState(isUploading = false, uploadError = null, uploadSuccess = true)
            } catch (e: Exception) {
                _bodyImageUploadState.value = BodyImageUploadState(
                    isUploading = false, 
                    uploadError = e.message ?: "Errore durante la rimozione", 
                    uploadSuccess = false
                )
            }
        }
    }

    /**
     * Clears the upload success state.
     */
    fun clearUploadSuccess() {
        _bodyImageUploadState.value = _bodyImageUploadState.value.copy(uploadSuccess = false)
    }

    /**
     * Clears the upload error state.
     */
    fun clearUploadError() {
        _bodyImageUploadState.value = _bodyImageUploadState.value.copy(uploadError = null)
    }
}
