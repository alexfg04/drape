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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A modern navigation bar with dynamic notch and floating icon.
 *
 * @param items Navigation items to display.
 * @param selectedIndex Index of the currently active item.
 * @param onItemSelected Callback when selection changes.
 */
@Composable
fun CurvedBottomNavigation(
    items: List<DrapeNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest


    val density = LocalDensity.current
    var barWidth by remember { mutableFloatStateOf(0f) }

    // Calculate dimensions and positions
    val itemWidth = if (items.isNotEmpty() && barWidth > 0) barWidth / items.size else 0f
    val targetX = if (itemWidth > 0) (selectedIndex * itemWidth) + (itemWidth / 2f) else 0f

    // Smooth animation for the notch
    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CutoutPosition"
    )

    // Notch parameters (DP -> PX)
    val cutoutWidthPx = with(density) { 80.dp.toPx() }
    val cutoutHeightPx = with(density) { 32.dp.toPx() }

    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = navigationBarsPadding.calculateBottomPadding())
            .height(95.dp)
            .graphicsLayer(clip = false)
    ) {
        // 1. Background with notch (the white bar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(65.dp)
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
                    .offset(x = bubbleOffset, y = 12.dp)
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
                    contentDescription = stringResource(id = items[selectedIndex].titleRes),
                    tint = activeColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 3. Navigation items
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
                                contentDescription = stringResource(id = item.titleRes),
                                tint = inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = item.titleRes),
                                color = inactiveColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            // If selected, icon and label are hidden (shown in floating bubble)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
