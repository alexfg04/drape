package com.drape.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A stylized Snackbar component for the Drape application.
 * Highlights different message types (Success, Error, Info) using colors and icons.
 *
 * @param snackbarData The data provided by the SnackbarHost.
 * @param modifier The modifier to be applied to the Snackbar.
 */
@Composable
fun DrapeSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    val message = snackbarData.visuals.message
    
    // Heuristic to determine the type of message based on keywords
    val isError = message.contains("Errore", ignoreCase = true) || 
                  message.contains("Fallito", ignoreCase = true) ||
                  message.contains("Impossibile", ignoreCase = true) ||
                  message.contains("failed", ignoreCase = true) ||
                  message.contains("requires", ignoreCase = true)
    
    val isSuccess = message.contains("Successo", ignoreCase = true) || 
                    message.contains("Salvato", ignoreCase = true) ||
                    message.contains("Caricato", ignoreCase = true) ||
                    message.contains("successful", ignoreCase = true)

    val (containerColor, contentColor, icon) = when {
        isError -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Error
        )
        isSuccess -> Triple(
            Color(0xFFE8F5E9), // Premium Light Green
            Color(0xFF2E7D32), // Deep Green
            Icons.Default.CheckCircle
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
