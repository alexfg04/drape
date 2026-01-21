package com.drape.data.repository

import com.drape.data.datasource.AuthRemoteDataSource
import com.drape.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user authentication management.
 *
 * Provides an abstraction over the remote data source, exposing domain [User] models
 * instead of Firebase types. Handles sign-in, sign-up, sign-out, and account deletion.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    /**
     * The currently authenticated user, or `null` if not logged in.
     */
    val currentUser: User?
        get() = authRemoteDataSource.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                isAnonymous = firebaseUser.isAnonymous
            )
        }

    /**
     * Flow emitting the current user's ID. Emits `null` when no user is authenticated.
     * Useful for observing authentication state changes.
     */
    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    /**
     * `true` if a user is currently authenticated, `false` otherwise.
     */
    val isUserLoggedIn: Boolean
        get() = authRemoteDataSource.currentUser != null

    /**
     * Signs in with email and password.
     *
     * @param email the user's email address
     * @param password the user's password
     * @throws FirebaseAuthException if credentials are invalid
     */
    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    /**
     * Registers a new user with email and password.
     *
     * @param email the email address for the new account
     * @param password the password for the new account
     * @param displayName the user's display name
     * @throws FirebaseAuthException if registration fails (e.g., email already in use)
     */
    suspend fun signUp(email: String, password: String, displayName: String) {
        authRemoteDataSource.signUp(email, password, displayName)
    }

    /**
     * Signs in using Google Sign-In.
     *
     * @param idToken the ID token obtained from Google Sign-In
     * @throws FirebaseAuthException if authentication fails
     */
    suspend fun signInWithGoogle(idToken: String) {
        authRemoteDataSource.signInWithGoogle(idToken)
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        authRemoteDataSource.signOut()
    }

    /**
     * Deletes the current user's account.
     *
     * @throws FirebaseAuthException if deletion fails
     */
    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }
}
