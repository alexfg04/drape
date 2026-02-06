package com.drape.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drape.ui.theme.DrapeTheme

/**
 * A simplified statistics card component designed for the Home screen.
 *
 * This component displays key wardrobe statistics in a compact format suitable
 * for the Home screen layout. It shows:
 * - Total number of outfits
 * - Number of unused outfits
 * - Circular progress indicator showing usage percentage
 *
 * The card also provides a navigation button to access detailed statistics.
 *
 * @param onNavigateToStatistics Callback invoked when the user clicks the "Details" button
 * @param viewModel The [StatisticsViewModel] instance for accessing statistics data,
 *                  defaults to a Hilt-provided instance
 */
@Composable
fun HomeStatisticsCard(
    onNavigateToStatistics: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeStatisticsCardContent(
        uiState = uiState,
        onNavigateToStatistics = onNavigateToStatistics
    )
}

/**
 * Stateless content composable for the Home statistics card.
 *
 * This component displays the actual UI content based on the provided [StatisticsUiState].
 * It can be used in Previews or tests without requiring a ViewModel.
 *
 * @param uiState The statistics UI state containing data to display
 * @param onNavigateToStatistics Callback invoked when the user clicks the "Details" button
 */
@Composable
fun HomeStatisticsCardContent(
    uiState: StatisticsUiState,
    onNavigateToStatistics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header row with title and navigation button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Le tue statistiche",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = onNavigateToStatistics,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dettagli",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator while data is being fetched
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Statistics row with three columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Outfits count
                    StatColumn(
                        value = uiState.outfitStats.totalOutfits.toString(),
                        label = "Outfit\ntotali",
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Visual divider
                    HorizontalDivider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Unused Outfits count
                    StatColumn(
                        value = uiState.outfitStats.unusedOutfits.toString(),
                        label = "Non\nutilizzati",
                        color = MaterialTheme.colorScheme.error
                    )

                    // Visual divider
                    HorizontalDivider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Circular Progress Indicator showing usage percentage
                    CircularProgressIndicator(
                        percentage = uiState.outfitStats.usagePercentage,
                        size = 80.dp,
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * A reusable column component for displaying a statistic value and its label.
 *
 * Displays a large numeric value above a smaller, potentially multiline label.
 * The label supports line breaks using \n for better formatting.
 *
 * @param value The numeric value to display in large bold text
 * @param label The descriptive label shown below the value, supports multiline
 * @param color The color applied to the value text for visual emphasis
 * @param modifier Optional modifier for custom layout adjustments
 */
@Composable
private fun StatColumn(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

/**
 * A custom animated circular progress indicator with percentage display.
 * 
 * This component draws a circular progress arc that animates smoothly when
 * the percentage changes. The percentage value is displayed in the center
 * of the circle with a "%" symbol below it.
 *
 * Features:
 * - Smooth animation when percentage changes
 * - Background track for the unfilled portion
 * - Customizable size, stroke width, and color
 * - Centered percentage text display
 *
 * @param percentage The percentage value to display (0-100)
 * @param size The diameter of the circular indicator
 * @param strokeWidth The thickness of the progress arc stroke
 * @param color The color of the progress arc and percentage text
 * @param modifier Optional modifier for custom layout adjustments
 */
@Composable
private fun CircularProgressIndicator(
    percentage: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage / 100f,
        label = "progress_animation"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val diameter = size.toPx() - strokeWidth.toPx()
            val topLeft = Offset(
                x = (size.toPx() - diameter) / 2,
                y = (size.toPx() - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // Background circle (track)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedPercentage * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }

        // Percentage text in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${percentage.toInt()}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = color
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Preview composable for the HomeStatisticsCard component.
 *
 * Displays the statistics card in the IDE preview with sample data.
 * Useful for visualizing the component's appearance during development.
 */
@Preview
@Composable
private fun HomeStatisticsCardPreview() {
    DrapeTheme {
        HomeStatisticsCardContent(
            uiState = StatisticsUiState(
                outfitStats = OutfitStats(
                    totalOutfits = 12,
                    usedOutfits = 8,
                    unusedOutfits = 4,
                    usagePercentage = 66.7f
                ),
                clothingStats = ClothingStats.EMPTY,
                monthlyStats = emptyList(),
                topUsedOutfits = emptyList(),
                isLoading = false
            ),
            onNavigateToStatistics = {}
        )
    }
}
