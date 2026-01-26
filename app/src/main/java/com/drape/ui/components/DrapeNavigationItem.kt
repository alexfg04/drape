package com.drape.ui.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rappresenta un elemento della barra di navigazione.
 * 
 * @param titleRes Id della risorsa stringa per il titolo.
 * @param icon L'icona da mostrare.
 * @param route La rotta di navigazione (Any per supportare Type-Safe navigation).
 */
import androidx.annotation.DrawableRes

/**
 * Represents the source of an icon, either a Vector or a Drawable resource.
 */
sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Drawable(@DrawableRes val id: Int) : IconSource
}

/**
 * Rappresenta un elemento della barra di navigazione.
 *
 * @param titleRes Id della risorsa stringa per il titolo.
 * @param icon L'icona da mostrare (Vector o Drawable).
 * @param route La rotta di navigazione (Any per supportare Type-Safe navigation).
 */
data class DrapeNavigationItem(
    @StringRes val titleRes: Int,
    val icon: IconSource,
    val route: Any
)
