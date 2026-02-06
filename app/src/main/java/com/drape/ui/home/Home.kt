package com.drape.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.drape.R
import com.drape.data.model.ClothingItem
import com.drape.data.model.Outfit
import com.drape.ui.statistics.HomeStatisticsCard
import com.drape.ui.theme.DrapeTheme

/**
 * Main app screen after login.
 *
 * Displays a greeting to the user, suggested outfits, and recent items.
 * Provides access to the side menu and profile.
 *
 * Note: Bottom navigation is handled at app level (MainActivity).
 *
 * @param onNavigateToProfile Callback invoked to navigate to profile screen.
 * @param onNavigateToStatistics Callback invoked to navigate to statistics screen.
 * @param onNavigateToPlanner Callback invoked to navigate to planner screen.
 * @param onOutfitClick Callback invoked when an outfit is clicked.
 * @param onClothingItemClick Callback invoked when a clothing item is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onOutfitClick: (Outfit) -> Unit = {},
    onClothingItemClick: (ClothingItem) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToPlanner = onNavigateToPlanner,
        onOutfitClick = onOutfitClick,
        onClothingItemClick = onClothingItemClick
    )
}

/**
 * Stateless version of HomeScreen for better testability and Preview support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onOutfitClick: (Outfit) -> Unit = {},
    onClothingItemClick: (ClothingItem) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Logo icon - replace with your actual logo resource
                    Image(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    // App name text
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            navigationIcon = {},
            actions = {
                IconButton(onClick = onNavigateToProfile) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = stringResource(R.string.home_profile_description),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = stringResource(R.string.home_greeting_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Statistics Card
            item {
                HomeStatisticsCard(
                    onNavigateToStatistics = onNavigateToStatistics
                )
            }

            // Recent Outfits Section
            if (uiState.outfits.isNotEmpty()) {
                item {
                    HomeSectionTitle(
                        title = stringResource(R.string.home_section_outfits),
                        showSeeAll = uiState.outfits.size >= 4,
                        onSeeAllClick = { /* Navigate to outfits list */ }
                    )
                }

                // Outfit cards in rows of 2
                items(uiState.outfits.chunked(2)) { outfitPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        outfitPair.forEach { outfit ->
                            OutfitCard(
                                outfit = outfit,
                                onClick = { onOutfitClick(outfit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add empty spacer if odd number of outfits
                        if (outfitPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Planner Banner
            item {
                PlannerBanner(
                    onNavigateToPlanner = onNavigateToPlanner
                )
            }

            // Recent Clothing Items Section
            if (uiState.recentClothes.isNotEmpty()) {
                item {
                    HomeSectionTitle(
                        title = stringResource(R.string.home_section_recent_items),
                        showSeeAll = uiState.recentClothes.size >= 5,
                        onSeeAllClick = { /* Navigate to wardrobe */ }
                    )
                }

                items(uiState.recentClothes) { item ->
                    ClothingItemCard(
                        item = item,
                        onClick = { onClothingItemClick(item) }
                    )
                }
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun HomeSectionTitle(
    title: String,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        if (showSeeAll) {
            TextButton(onClick = onSeeAllClick) {
                Text(stringResource(R.string.home_see_all), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PlannerBanner(
    onNavigateToPlanner: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4A49A1), // Softer Deep Blue
                        Color(0xFF6FC8E3)  // Softer Cyan
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)), // Glassy look
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    // Small alert badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFFF6D6D), CircleShape) // Reddish dot for contrast
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.home_planner_banner_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_planner_banner_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToPlanner,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4A49A1)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.home_planner_banner_button),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitCard(
    outfit: Outfit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Outfit thumbnail
            if (outfit.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = outfit.thumbnailUrl,
                    contentDescription = outfit.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
            } else {
                // Placeholder when no thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👕",
                        fontSize = 48.sp
                    )
                }
            }

            // Outfit name at bottom
            Text(
                text = outfit.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (outfit.thumbnailUrl.isNotEmpty()) {
                    androidx.compose.ui.graphics.Color.White
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ClothingItemCard(
    item: ClothingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clothing item image
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👔",
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.brand.isNotEmpty()) {
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Legacy placeholder composable - keeping for reference
@Composable
fun OutfitPlaceholderCard(modifier: Modifier = Modifier, title: String) {
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    DrapeTheme {
        HomeContent(
            uiState = HomeUiState(
                outfits = listOf(
                    Outfit(name = "Summer Look", thumbnailUrl = ""),
                    Outfit(name = "Business Casual", thumbnailUrl = "")
                ),
                recentClothes = listOf(
                    ClothingItem(name = "White T-Shirt", brand = "Uniqlo", category = "TOP"),
                    ClothingItem(name = "Blue Jeans", brand = "Levi's", category = "BOTTOM")
                ),
                isLoading = false
            ),
            onNavigateToProfile = {},
            onNavigateToStatistics = {},
            onOutfitClick = {},
            onClothingItemClick = {},
        )
    }
}
