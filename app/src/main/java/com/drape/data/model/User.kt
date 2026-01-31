package com.drape.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val isAnonymous: Boolean = false,
    val createdAt: Long = 0L,
    val bio: String = "",
    val photoUrl: String? = null,
    val coverPhotoUrl: String? = null
)
