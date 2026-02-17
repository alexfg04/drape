package com.drape.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserModelTest {

    @Test
    fun defaultConstructor_setsExpectedDefaults() {
        val user = User()

        assertEquals("", user.id)
        assertEquals("", user.email)
        assertEquals("", user.displayName)
        assertEquals(false, user.isAnonymous)
        assertEquals(0L, user.createdAt)
        assertEquals("", user.bio)
        assertNull(user.photoUrl)
        assertNull(user.coverPhotoUrl)
        assertNull(user.bodyReferenceImage)
    }

    @Test
    fun copy_updatesProfileFieldsWithoutLosingIdentityData() {
        val original = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Mario",
            isAnonymous = false,
            createdAt = 12345L
        )

        val updated = original.copy(
            displayName = "Mario Rossi",
            bio = "Casual style",
            photoUrl = "https://example.com/profile.jpg",
            coverPhotoUrl = "https://example.com/cover.jpg",
            bodyReferenceImage = "https://example.com/body.png"
        )

        assertEquals("user-1", updated.id)
        assertEquals("test@example.com", updated.email)
        assertEquals("Mario Rossi", updated.displayName)
        assertEquals("Casual style", updated.bio)
        assertEquals("https://example.com/profile.jpg", updated.photoUrl)
        assertEquals("https://example.com/cover.jpg", updated.coverPhotoUrl)
        assertEquals("https://example.com/body.png", updated.bodyReferenceImage)
    }
}
