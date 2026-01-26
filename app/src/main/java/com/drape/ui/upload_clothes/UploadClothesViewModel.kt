package com.drape.ui.upload_clothes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.repository.ClothesRepository
import com.drape.data.service.BackgroundRemovalService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the upload clothes screen.
 */
data class UploadClothesUiState(
    val isLoading: Boolean = false,
    val isProcessingBackground: Boolean = false,
    val errorMessage: String? = null,
    val isUploadSuccessful: Boolean = false,
    val processedImageUri: Uri? = null
)

/**
 * ViewModel for managing clothing item upload operations.
 * Handles background removal using ML Kit Subject Segmentation.
 */
@HiltViewModel
class UploadClothesViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository,
    private val backgroundRemovalService: BackgroundRemovalService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadClothesUiState())
    val uiState: StateFlow<UploadClothesUiState> = _uiState.asStateFlow()

    private var currentProcessingJob: Job? = null

    /**
     * Processes the image to remove background if enabled.
     * 
     * @param imageUri The URI of the original image
     * @param removeBackground Whether to remove the background
     */
    fun processImage(imageUri: Uri, removeBackground: Boolean) {
        if (!removeBackground) {
            currentProcessingJob?.cancel()
            currentProcessingJob = null
            _uiState.value = _uiState.value.copy(
                processedImageUri = null,
                isProcessingBackground = false
            )
            return
        }

        currentProcessingJob?.cancel()
        currentProcessingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessingBackground = true,
                errorMessage = null
            )

            try {
                val processedUri = backgroundRemovalService.removeBackground(imageUri)
                if (isActive) {
                    _uiState.value = _uiState.value.copy(
                        isProcessingBackground = false,
                        processedImageUri = processedUri,
                        errorMessage = if (processedUri == null) 
                            "Impossibile rimuovere lo sfondo. Prova con un'altra immagine." 
                            else null
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = _uiState.value.copy(
                        isProcessingBackground = false,
                        errorMessage = "Errore durante l'elaborazione: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun uploadClothingItem(
        originalImageUri: Uri,
        name: String,
        brand: String,
        category: String,
        color: String,
        season: String,
        removeBackground: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isUploadSuccessful = false
            )

            try {
                // Use processed image if available and background removal is enabled
                val imageToUpload = if (removeBackground && _uiState.value.processedImageUri != null) {
                    _uiState.value.processedImageUri!!
                } else if (removeBackground) {
                    // Process image before upload if not already processed
                    _uiState.value = _uiState.value.copy(isProcessingBackground = true)
                    val processedUri = backgroundRemovalService.removeBackground(originalImageUri)
                    _uiState.value = _uiState.value.copy(isProcessingBackground = false)
                    if (processedUri == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Background removal failed"
                        )
                        return@launch
                    }
                    processedUri
                } else {
                    originalImageUri
                }

                // Create clothing item data
                val clothingData = ClothingItem(
                    name = name,
                    brand = brand,
                    category = category,
                    color = color,
                    season = season
                )

                // Upload to repository
                clothesRepository.uploadClothingItem(imageToUpload, clothingData)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isUploadSuccessful = true,
                    processedImageUri = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Errore durante il caricamento del capo"
                )
            }
        }
    }

    /**
     * Clears any error message in the UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Resets the success state after navigation.
     */
    fun clearSuccessState() {
        _uiState.value = _uiState.value.copy(isUploadSuccessful = false)
    }

    /**
     * Clears the processed image when the original image changes.
     */
    fun clearProcessedImage() {
        _uiState.value = _uiState.value.copy(processedImageUri = null)
    }
}