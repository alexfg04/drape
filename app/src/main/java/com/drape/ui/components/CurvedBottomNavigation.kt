package com.drape.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        val tintColor = if (isSelected) activeColor else inactiveColor
                        when (val icon = item.icon) {
                            is IconSource.Vector -> {
                                Icon(
                                    imageVector = icon.imageVector,
                                    contentDescription = stringResource(id = item.titleRes),
                                    tint = tintColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            is IconSource.Drawable -> {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = icon.id),
                                    contentDescription = stringResource(id = item.titleRes),
                                    tint = tintColor,
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
