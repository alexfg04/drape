package com.drape.ui.statistics

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import java.util.Locale

/**
 * Detailed Statistics Screen displaying comprehensive wardrobe analytics.
 * 
 * This screen provides in-depth statistics about the user's wardrobe including:
 * - Outfit usage statistics (total, used, unused, percentage)
 * - Clothing items statistics (total, used, unused, percentage)
 * - Monthly usage bar chart
 * - Category distribution chart
 * - Top 5 most used outfits
 * 
 * The screen uses ComposeCharts library for data visualization and features
 * a clean, card-based layout with Material 3 design.
 *
 * @param onNavigateBack Callback invoked when the user presses the back button
 * @param viewModel The [StatisticsViewModel] instance for accessing statistics data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statistiche Dettagliate",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Outfit Statistics Section
                OutfitStatsSection(uiState.outfitStats)

                // Clothing Statistics Section
                ClothingStatsSection(uiState.clothingStats)

                // Monthly Usage Bar Chart
                if (uiState.monthlyStats.isNotEmpty()) {
                    MonthlyUsageChart(uiState.monthlyStats)
                }

                // Category Distribution Chart
                if (uiState.clothingStats.byCategory.isNotEmpty()) {
                    CategoryDistributionChart(uiState.clothingStats.byCategory)
                }

                // Top Used Outfits
                if (uiState.topUsedOutfits.isNotEmpty()) {
                    TopUsedOutfitsSection(uiState.topUsedOutfits)
                }

                // Least Used Outfits
                if (uiState.leastUsedOutfits.isNotEmpty()) {
                    LeastUsedOutfitsSection(uiState.leastUsedOutfits)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Card component displaying outfit usage statistics.
 * 
 * Shows the total number of outfits, how many have been used in the planner,
 * how many are unused, and a progress bar indicating the usage percentage.
 *
 * @param stats The [OutfitStats] data to display
 */
@Composable
private fun OutfitStatsSection(stats: OutfitStats) {
    StatsCard(title = "Outfit") {
        Column {
            // Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = stats.totalOutfits.toString(),
                    label = "Totali",
                    color = MaterialTheme.colorScheme.primary
                )
                StatBox(
                    value = stats.usedOutfits.toString(),
                    label = "Utilizzati",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatBox(
                    value = stats.unusedOutfits.toString(),
                    label = "Non usati",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Usage Bar
            Text(
                text = "Percentuale utilizzo: ${String.format(Locale.getDefault(), "%.1f", stats.usagePercentage)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { stats.usagePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Card component displaying clothing items statistics.
 * 
 * Shows the total number of clothing items, how many have been used in outfits,
 * how many are unused, and a progress bar indicating the usage percentage.
 *
 * @param stats The [ClothingStats] data to display
 */
@Composable
private fun ClothingStatsSection(stats: ClothingStats) {
    StatsCard(title = "Capi di Abbigliamento") {
        Column {
            // Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = stats.totalClothes.toString(),
                    label = "Totali",
                    color = MaterialTheme.colorScheme.primary
                )
                StatBox(
                    value = stats.usedClothes.toString(),
                    label = "Utilizzati",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatBox(
                    value = stats.unusedClothes.toString(),
                    label = "Non usati",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Usage Bar
            Text(
                text = "Percentuale utilizzo: ${String.format(Locale.getDefault(), "%.1f", stats.usagePercentage)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { stats.usagePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Card component displaying a bar chart of monthly outfit usage.
 * 
 * Uses ComposeCharts library to render an animated column chart showing
 * how many outfits were scheduled in each month.
 *
 * @param monthlyStats List of monthly usage statistics to display
 */
@Composable
private fun MonthlyUsageChart(monthlyStats: List<MonthlyUsageStats>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    val barData = remember(monthlyStats, primaryColor, secondaryColor, tertiaryColor) {
        monthlyStats.mapIndexed { index, stat ->
            val monthLabel = extractMonthLabel(stat.month) // Safely extract month from "yyyy-MM"
            val color = when (index % 3) {
                0 -> primaryColor
                1 -> secondaryColor
                else -> tertiaryColor
            }
            
            Bars(
                label = monthLabel,
                values = listOf(
                    Bars.Data(
                        label = "Outfit",
                        value = stat.outfitCount.toDouble(),
                        color = SolidColor(color)
                    )
                )
            )
        }
    }

    StatsCard(title = "Utilizzo Mensile") {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 8.dp)
            ) {
                ColumnChart(
                    modifier = Modifier.fillMaxSize(),
                    data = barData,
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                        spacing = 8.dp,
                        thickness = 24.dp
                    ),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }
}

/**
 * Card component displaying clothing distribution by category.
 * 
 * Shows horizontal progress bars for each category indicating the relative
 * quantity of items in that category compared to the largest category.
 *
 * @param categoryData Map of category names to item counts
 */
@Composable
private fun CategoryDistributionChart(categoryData: Map<String, Int>) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer
    )

    StatsCard(title = "Distribuzione per Categoria") {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simple horizontal bar chart representation
            val maxValue = categoryData.values.maxOrNull()?.toFloat() ?: 1f
            
            categoryData.entries.forEachIndexed { index, entry ->
                val percentage = entry.value / maxValue
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = entry.value.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colors.getOrElse(index) { colors.first() },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Card component displaying the top 5 most frequently used outfits.
 * 
 * Shows a ranked list with the outfit name and usage count,
 * with visual ranking indicators (colored badges for top 3).
 *
 * @param topOutfits List of top used outfits sorted by usage count
 */
@Composable
private fun TopUsedOutfitsSection(topOutfits: List<TopUsedOutfit>) {
    StatsCard(title = "Outfit Più Utilizzati") {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            topOutfits.forEachIndexed { index, outfit ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank badge with different colors for top 3
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = when (index) {
                                        0 -> MaterialTheme.colorScheme.primary
                                        1 -> MaterialTheme.colorScheme.secondary
                                        2 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (index < 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Outfit name
                        Text(
                            text = outfit.outfitName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Usage count badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${outfit.usageCount} volte",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card component displaying the least frequently used outfits.
 *
 * Shows a ranked list with outfit name and usage count,
 * sorted from lowest usage to highest usage.
 *
 * @param leastOutfits List of least used outfits
 */
@Composable
private fun LeastUsedOutfitsSection(leastOutfits: List<LeastUsedOutfit>) {
    StatsCard(title = "Outfit Meno Utilizzati") {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leastOutfits.forEachIndexed { index, outfit ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Text(
                            text = outfit.outfitName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${outfit.usageCount} volte",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * A reusable card component for grouping related statistics.
 * 
 * Provides consistent styling with rounded corners, elevation, and padding
 * for all statistics sections in the screen.
 *
 * @param title The section title displayed at the top of the card
 * @param content The composable content to display inside the card
 */
@Composable
private fun StatsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * A reusable component for displaying a single statistic value with its label.
 * 
 * Displays the value in large bold text with the label below it.
 *
 * @param value The numeric value to display
 * @param label The descriptive label
 * @param color The color for the value text
 * @param modifier Optional modifier for customization
 */
@Composable
private fun StatBox(
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
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Safely extracts the month part from a date string in "yyyy-MM" or "yyyy-MM-dd" format.
 *
 * @param dateString The date string to parse (expected format: "yyyy-MM" or "yyyy-MM-dd")
 * @return The month portion (e.g., "MM" from "yyyy-MM"), or the original string if parsing fails
 */
private fun extractMonthLabel(dateString: String): String {
    return when {
        dateString.length >= 7 -> dateString.substring(5, 7)
        dateString.contains("-") -> dateString.split("-").getOrNull(1) ?: dateString
        else -> dateString
    }
}
