package com.drape.data.repository

import com.drape.data.datasource.AuthRemoteDataSource
import com.drape.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    val currentUser: User?
        get() = authRemoteDataSource.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                isAnonymous = firebaseUser.isAnonymous
            )
        }

    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    val isUserLoggedIn: Boolean
        get() = authRemoteDataSource.currentUser != null

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String, displayName: String) {
        authRemoteDataSource.signUp(email, password, displayName)
    }

    suspend fun signInWithGoogle(idToken: String) {
        authRemoteDataSource.signInWithGoogle(idToken)
    }

    fun signOut() {
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }
}
