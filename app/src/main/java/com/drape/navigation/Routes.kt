package com.drape.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * Organized by feature/graph.
 */

// ============================================
// Root Routes
// ============================================

@Serializable
object Splash

// ============================================
// Auth Graph Routes
// ============================================

@Serializable
object AuthGraph  // Nested graph identifier

@Serializable
object Welcome

@Serializable
object SceltaLog

@Serializable
object SignUpEmail

@Serializable
object SignIn

// ============================================
// Home Graph Routes
// ============================================

@Serializable
object HomeGraph  // Nested graph identifier

@Serializable
object Home

@Serializable
object Camerino

@Serializable
object Add

@Serializable
object UploadClothes

@Serializable
object Profile
