package com.drape.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Una barra di navigazione moderna con incavo dinamico e icona fluttuante.
 *
 * @param items Elementi della navigazione.
 * @param selectedIndex Indice dell'elemento attivo.
 * @param onItemSelected Callback al cambio di selezione.
 */
@Composable
fun CurvedBottomNavigation(
    items: List<DrapeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Tonalità Drape Blue (#00458D)
    val activeColor = Color(0xFF00458D)
    val inactiveColor = Color(0xFF9E9E9E)
    val backgroundColor = Color.White

    val density = LocalDensity.current
    var barWidth by remember { mutableFloatStateOf(0f) }

    // Calcolo dimensioni e posizioni
    val itemWidth = if (items.isNotEmpty() && barWidth > 0) barWidth / items.size else 0f
    val targetX = if (itemWidth > 0) (selectedIndex * itemWidth) + (itemWidth / 2f) else 0f

    // Animazione fluida dell'incavo
    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CutoutPosition"
    )

    // Parametri dell'incavo (DP -> PX)
    val cutoutWidthPx = with(density) { 80.dp.toPx() }
    val cutoutHeightPx = with(density) { 32.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp) // Altezza totale per permettere la sporgenza del bubble
            .graphicsLayer(clip = false)
    ) {
        // 1. Sfondo con Incavo (La Barra Bianca)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(65.dp) // Altezza della barra
                .onGloballyPositioned { barWidth = it.size.width.toFloat() }
                .shadow(
                    elevation = 15.dp,
                    shape = curvedShape(animatedX, cutoutWidthPx, cutoutHeightPx),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = activeColor.copy(alpha = 0.4f)
                )
                .background(
                    color = backgroundColor,
                    shape = curvedShape(animatedX, cutoutWidthPx, cutoutHeightPx)
                )
        )

        // 2. Floating Icon (Bubble)
        if (barWidth > 0 && selectedIndex in items.indices) {
            val bubbleSize = 56.dp
            val bubbleOffset = with(density) { animatedX.toDp() - (bubbleSize / 2f) }

            Box(
                modifier = Modifier
                    .offset(x = bubbleOffset, y = 12.dp) // Posizionamento sopra l'incavo
                    .size(bubbleSize)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = activeColor
                    )
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = items[selectedIndex].icon,
                    contentDescription = items[selectedIndex].title,
                    tint = activeColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 3. Voci di Navigazione
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(65.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!isSelected) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                color = inactiveColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            // Se selezionato, l'icona e la scritta scompaiono dalla barra (sono nel bubble fluttuante)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
