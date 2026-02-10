package com.drape.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A standard static bottom navigation bar.
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
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp + navBarPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp + navBarPadding)
                .background(color = backgroundColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
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
                        val animatedColor by animateColorAsState(
                            targetValue = if (isSelected) activeColor else inactiveColor,
                            animationSpec = tween(durationMillis = 300),
                            label = "ColorAnimation"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "ScaleAnimation"
                        )
                        val translationY by animateDpAsState(
                            targetValue = if (isSelected) (-6).dp else 0.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "TranslationAnimation"
                        )
                        
                        val backgroundAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0.1f else 0f,
                            animationSpec = tween(durationMillis = 300),
                            label = "BackgroundAlpha"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .scale(scale)
                                .offset(y = translationY)
                        ) {
                            // Rounded background bubble
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(activeColor.copy(alpha = backgroundAlpha))
                            )
                            
                            when (val icon = item.icon) {
                                is IconSource.Vector -> {
                                    Icon(
                                        imageVector = icon.imageVector,
                                        contentDescription = stringResource(id = item.titleRes),
                                        tint = animatedColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                is IconSource.Drawable -> {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = icon.id),
                                        contentDescription = stringResource(id = item.titleRes),
                                        tint = animatedColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
