package com.drape.data.repository

import android.net.Uri
import android.util.Log
import com.drape.data.datasource.AuthRemoteDataSource
import com.drape.data.datasource.StorageRemoteDataSource
import com.drape.data.datasource.UserDataRemoteDataSource
import com.drape.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

/**
 * Repository for user profile management.
 *
 * Handles user profile data (display name, bio, photos, body reference image)
 * by coordinating Auth state, Firestore user data, and Firebase Storage.
 */
@Singleton
class UserRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userDataRemoteDataSource: UserDataRemoteDataSource,
    private val storageRemoteDataSource: StorageRemoteDataSource
) {
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Flow emitting the full user profile (Auth + Firestore).
     * Re-emits when auth state changes or when profile data is updated.
     * Shared as a hot [StateFlow] so data is cached across navigations.
     */
    val userFlow: StateFlow<User?> = flow {
        merge(
            authRemoteDataSource.currentUserIdFlow,
            refreshTrigger
        ).collect {
            val userId = authRemoteDataSource.currentUser?.uid
            if (userId != null) {
                val firebaseUser = authRemoteDataSource.currentUser
                val userData = userDataRemoteDataSource.getUserData(userId)

                emit(
                    User(
                        id = userId,
                        email = firebaseUser?.email ?: "",
                        displayName = firebaseUser?.displayName ?: "",
                        isAnonymous = firebaseUser?.isAnonymous ?: false,
                        createdAt = firebaseUser?.metadata?.creationTimestamp ?: 0L,
                        bio = userData["bio"] as? String ?: "",
                        photoUrl = firebaseUser?.photoUrl?.toString(),
                        coverPhotoUrl = userData["coverPhotoUrl"] as? String,
                        bodyReferenceImage = userData["bodyReferenceImage"] as? String
                    )
                )
            } else {
                emit(null)
            }
        }
    }.stateIn(scope, SharingStarted.Lazily, null)

    /**
     * Updates the user's profile information.
     */
    suspend fun updateProfile(
        displayName: String,
        bio: String,
        photoUri: Uri?,
        coverPhotoUri: Uri?
    ) {
        val user = authRemoteDataSource.currentUser ?: return
        val userId = user.uid

        val firestoreUpdates = mutableMapOf<String, Any?>("bio" to bio)

        if (photoUri != null) {
            val photoUrl = storageRemoteDataSource.uploadImage(
                imageUri = photoUri,
                userId = userId,
                id = "profile_image",
                folder = "profile"
            )
            user.updateProfile(
                com.google.firebase.auth.userProfileChangeRequest {
                    this.photoUri = photoUrl.toUri()
                }
            ).await()
        }

        if (coverPhotoUri != null) {
            val coverUrl = storageRemoteDataSource.uploadImage(
                imageUri = coverPhotoUri,
                userId = userId,
                id = "cover_image",
                folder = "profile"
            )
            firestoreUpdates["coverPhotoUrl"] = coverUrl
        }

        if (displayName != user.displayName) {
            user.updateProfile(
                com.google.firebase.auth.userProfileChangeRequest {
                    this.displayName = displayName
                }
            ).await()
        }

        userDataRemoteDataSource.saveUserData(userId, firestoreUpdates)
        refreshTrigger.tryEmit(Unit)
    }

    /**
     * Uploads a body reference image for virtual try-on.
     *
     * @param imageUri The URI of the image to upload
     */
    suspend fun uploadBodyReferenceImage(imageUri: Uri): String {
        val user = authRemoteDataSource.currentUser
            ?: throw Exception("User not authenticated")
        val userId = user.uid

        val imageUrl = storageRemoteDataSource.uploadImage(
            imageUri = imageUri,
            userId = userId,
            id = "body_reference",
            folder = "profile"
        )

        userDataRemoteDataSource.saveUserData(userId, mapOf("bodyReferenceImage" to imageUrl))
        refreshTrigger.tryEmit(Unit)
        return imageUrl
    }

    /**
     * Removes the body reference image.
     */
    suspend fun removeBodyReferenceImage() {
        val user = authRemoteDataSource.currentUser ?: return
        val userId = user.uid

        val userData = userDataRemoteDataSource.getUserData(userId)
        val currentUrl = userData["bodyReferenceImage"] as? String
        userDataRemoteDataSource.saveUserData(userId, mapOf("bodyReferenceImage" to null))
        refreshTrigger.tryEmit(Unit)

        if (currentUrl != null) {
            val path = storageRemoteDataSource.extractPathFromUrl(currentUrl)
            try {
                storageRemoteDataSource.deleteImage(path)
            } catch (e: Exception) {
                Log.w("UserRepository", "Failed to delete body reference image: $path", e)
            }
        }
    }
}
