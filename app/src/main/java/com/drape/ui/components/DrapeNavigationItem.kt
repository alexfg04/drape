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
data class DrapeNavigationItem(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val route: Any
)
