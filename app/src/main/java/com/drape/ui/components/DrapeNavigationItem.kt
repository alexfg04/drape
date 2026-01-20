package com.drape.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rappresenta un elemento della barra di navigazione.
 * 
 * @param title Il titolo visualizzato sotto l'icona (quando non selezionato).
 * @param icon L'icona da mostrare.
 * @param route La rotta di navigazione (Any per supportare Type-Safe navigation).
 */
data class DrapeNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
)
