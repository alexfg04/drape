package com.drape.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserData(userId: String, data: Map<String, Any?>) {
        firestore.collection("users").document(userId)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun getUserData(userId: String): Map<String, Any?> {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.data ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
