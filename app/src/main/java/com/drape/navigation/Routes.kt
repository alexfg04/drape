package com.drape.navigation

import kotlinx.serialization.Serializable

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
data class Camerino(val outfitId: String? = null)

@Serializable
data class EditOutfit(val outfitId: String)

@Serializable
object Wardrobe


@Serializable
object UploadClothes

@Serializable
object SavedOutfits

@Serializable
object EditProfile

@Serializable
object Profile

@Serializable
object Planner

@Serializable
data class SelectOutfit(val day: Int, val month: Int, val year: Int)

@Serializable
data class ProfileSeasonOutfits(val season: String)

@Serializable
object EmptyPage
