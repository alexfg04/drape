package com.drape.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.R
import com.drape.data.model.ClothingItem
import com.drape.data.model.ItemCategory
import com.drape.ui.theme.*
import com.drape.ui.components.DrapeSnackbar

/**
 * Main screen for viewing and managing the user's wardrobe.
 */
@Composable
fun WardrobeScreen(
    itemAdded: Boolean = false,
    itemDeleted: Boolean = false,
    onNavigateToClothingDetail: (ClothingItem) -> Unit,
    onNavigateToOutfitCreator: () -> Unit,
    onNavigateToProfile: () -> Unit,
    wardrobeViewModel: WardrobeViewModel = hiltViewModel()
) {
    val wardrobeUiState by wardrobeViewModel.uiState.collectAsState()

    WardrobeScreenContent(
        wardrobeUiState = wardrobeUiState,
        itemAdded = itemAdded,
        itemDeleted = itemDeleted,
        onNavigateToOutfitCreator = onNavigateToOutfitCreator,
        onNavigateToProfile = onNavigateToProfile,
        onWardrobeItemClick = onNavigateToClothingDetail,
        onWardrobeDeleteItem = { wardrobeViewModel.deleteClothingItem(it) },
        onWardrobeRefresh = { wardrobeViewModel.refresh() },
        onClearError = { wardrobeViewModel.clearError() },
        onClearDeleteSuccess = { wardrobeViewModel.clearDeleteSuccess() }
    )
}

/**
 * The content section of the Wardrobe screen.
 * Separated for easier previewing and testing.
 *
 * @param wardrobeUiState The current UI state of the wardrobe.
 * @param onWardrobeItemClick Callback triggered when a clothing item is clicked.
 * @param onWardrobeDeleteItem Callback to delete a specific clothing item.
 * @param onWardrobeRefresh Callback to refresh the wardrobe contents.
 */
@Composable
fun WardrobeScreenContent(
    wardrobeUiState: WardrobeUiState,
    itemAdded: Boolean = false,
    itemDeleted: Boolean = false,
    onNavigateToOutfitCreator: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onWardrobeItemClick: (ClothingItem) -> Unit,
    onWardrobeDeleteItem: (String) -> Unit,
    onWardrobeRefresh: () -> Unit,
    onClearError: () -> Unit,
    onClearDeleteSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteSuccessMessage = stringResource(R.string.wardrobe_delete_success)
    val addSuccessMessage = stringResource(R.string.upload_clothes_success)
    val errorMessage = wardrobeUiState.errorMessage

    // Handle deletion success (from wardrobe delete action)
    LaunchedEffect(wardrobeUiState.deleteSuccess) {
        if (wardrobeUiState.deleteSuccess) {
            snackbarHostState.showSnackbar(
                message = deleteSuccessMessage,
                duration = SnackbarDuration.Short
            )
            onClearDeleteSuccess()
        }
    }

    // Handle navigation results
    LaunchedEffect(itemAdded) {
        if (itemAdded) {
            snackbarHostState.showSnackbar(
                message = addSuccessMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(itemDeleted) {
        if (itemDeleted) {
            snackbarHostState.showSnackbar(
                message = deleteSuccessMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Handle errors (only if not a full screen error)
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && wardrobeUiState.clothingItems.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            onClearError()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // TopBar moved to content
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                DrapeSnackbar(snackbarData = data)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WardrobeListContent(
                uiState = wardrobeUiState,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onNavigateToOutfitCreator = onNavigateToOutfitCreator,
                onNavigateToProfile = onNavigateToProfile,
                onItemClick = onWardrobeItemClick,
                onRefresh = onWardrobeRefresh
            )
        }
    }
}

@Composable
fun WardrobeListContent(
    uiState: WardrobeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToOutfitCreator: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onItemClick: (ClothingItem) -> Unit,
    onRefresh: () -> Unit
) {
    val allFilterText = stringResource(R.string.wardrobe_filter_all)
    val filters = listOf(allFilterText) + ItemCategory.entries.map { it.name }
    var selectedFilter by remember { mutableStateOf(allFilterText) }

    val filteredItems = remember(uiState.clothingItems, searchQuery, selectedFilter) {
        uiState.clothingItems.filter { item ->
            (selectedFilter == allFilterText || item.category.equals(selectedFilter, ignoreCase = true)) &&
            (searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true) ||
             item.brand.contains(searchQuery, ignoreCase = true) ||
             item.color.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- HEADER SECTION (Full Span) ---
        item(span = { GridItemSpan(2) }) {
            Column {
                TopBar(onProfileClick = onNavigateToProfile)

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cerca",
                            tint = Color.Gray
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(50) // Rounded corners
                )
                
                Spacer(modifier = Modifier.height(16.dp)) // Added Spacer

                // Promo Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp) // Increased spacing
                        .clickable { onNavigateToOutfitCreator() },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.banner),
                        contentDescription = stringResource(R.string.promo_banner_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp) // Adjust height as needed
                    )
                }

                // Category Section
                Text(
                    text = stringResource(R.string.category_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp) // Increased spacing
                )

                FilterSection(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )


                // Increased spacing
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // --- CONTENT SECTION (Grid Items or Full Span States) ---
        when {
            uiState.isLoading -> {
                item(span = { GridItemSpan(2) }) {
                   Box(modifier = Modifier.height(400.dp)) {
                       LoadingState()
                   }
                }
            }

            uiState.errorMessage != null && uiState.clothingItems.isEmpty() -> {
                item(span = { GridItemSpan(2) }) {
                     Box(modifier = Modifier.height(400.dp)) {
                        ErrorState(message = uiState.errorMessage, onRetry = onRefresh)
                     }
                }
            }

            uiState.clothingItems.isEmpty() -> {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(400.dp)) {
                        EmptyState()
                    }
                }
            }

            filteredItems.isEmpty() -> {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(400.dp)) {
                        NoResultsState(searchQuery = searchQuery, filter = selectedFilter)
                    }
                }
            }
            
            else -> {
                items(filteredItems, key = { it.id }) { item ->
                    WardrobeItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

/**
<<<<<<< refactor/my-outfit-ui
 * Dialog displaying details of a selected clothing item.
 * Provides an option to delete the item.
 *
 * @param item The [ClothingItem] to display details for.
 * @param isDeleting Boolean flag indicating if a deletion is in progress.
 * @param onDismiss Callback to dismiss the dialog.
 * @param onDelete Callback to trigger the deletion of the item.
 */
@Composable
fun ItemDetailDialog(
    item: ClothingItem, isDeleting: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() }, confirmButton = {
        TextButton(
            onClick = onDelete, enabled = !isDeleting, colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.wardrobe_delete_confirm))
            }
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss, enabled = !isDeleting
        ) {
            Text(stringResource(R.string.wardrobe_delete_cancel))
        }
    }, text = {
        ClothingItemDetailCard(item = item)
    }, title = {
        Text(stringResource(R.string.wardrobe_delete_title))
    }, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(28.dp)
    )
}

/**
 * Card component displaying the detailed information of a clothing item.
 * Used within the [ItemDetailDialog].
 *
 * @param item The [ClothingItem] to display.
 */
@Composable
fun ClothingItemDetailCard(item: ClothingItem) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context).data(item.imageUrl).diskCacheKey(item.id)
                    .memoryCacheKey(item.id).crossfade(true).build(),
                contentDescription = item.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.name, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ), color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(label = stringResource(R.string.wardrobe_item_detail_brand), value = item.brand)
        DetailRow(label = stringResource(R.string.wardrobe_item_detail_category), value = item.category)
        DetailRow(label = stringResource(R.string.wardrobe_item_detail_color), value = item.color)
        DetailRow(label = stringResource(R.string.wardrobe_item_detail_season), value = item.season)
    }
}

/**
 * A single row showing a label and a value for a clothing item's attribute.
 * Only displays if the value is not blank.
 *
 * @param label The attribute name (e.g., "Brand").
 * @param value The attribute value (e.g., "Levi's").
 */
@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
=======
>>>>>>> main
 * Full-screen loading state for the Wardrobe.
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.wardrobe_loading_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Full-screen error state for the Wardrobe.
 *
 * @param message The error message to display.
 * @param onRetry Callback to retry the failed operation.
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "😕", style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.wardrobe_error_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.wardrobe_retry_button))
            }
        }
    }
}

/**
 * Full-screen empty state for the Wardrobe when no items are available.
 */
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "👕", style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.wardrobe_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wardrobe_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Full-screen state displayed when search or filter returns no results.
 *
 * @param searchQuery The current search query.
 * @param filter The currently selected filter category.
 */
@Composable
fun NoResultsState(searchQuery: String, filter: String) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🔍", style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.wardrobe_no_results_title), style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    stringResource(R.string.wardrobe_no_results_search, searchQuery)
                } else {
                    stringResource(R.string.wardrobe_no_results_filter, filter)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


/**
 * Horizontal scrollable section for filtering wardrobe items by category.
 *
 * @param filters List of available filter categories.
 * @param selectedFilter The currently selected filter category.
 * @param onFilterSelected Callback triggered when a new filter is selected.
 */
@Composable
fun FilterSection(
    filters: List<String>, selectedFilter: String, onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            val contentColor = MaterialTheme.colorScheme.primary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onFilterSelected(filter) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                ) {
                   val (iconVector, iconDetail) = when (filter.uppercase()) {
                       "TUTTI", "ALL" -> Icons.Default.AllInclusive to null
                       "TOP", "TOPS" -> null to R.drawable.iconamaglietta
                       "BOTTOM", "BOTTOMS" -> null to R.drawable.pantaloniicone
                       "SHOES" -> null to R.drawable.scarpeicone
                       "ACCESSORIES" -> Icons.Default.Diamond to null
                       else -> Icons.Default.Category to null
                   }

                   if (iconVector != null) {
                       Icon(
                           imageVector = iconVector,
                           contentDescription = filter,
                           tint = contentColor,
                           modifier = Modifier.size(24.dp)
                       )
                   } else if (iconDetail != null) {
                       Icon(
                           painter = androidx.compose.ui.res.painterResource(id = iconDetail),
                           contentDescription = filter,
                           tint = contentColor,
                           modifier = Modifier.size(24.dp)
                       )
                   }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = filter,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



/**
 * Card component representing a single clothing item in the wardrobe grid.
 * Displays an image of the item along with its name, brand, category, color, and season.
 *
 * @param item The [ClothingItem] to display.
 * @param onClick Callback triggered when the card is clicked.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
fun WardrobeItemCard(item: ClothingItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent), // Removed gray background
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context).data(item.imageUrl).diskCacheKey(item.id)
                        .memoryCacheKey(item.id).crossfade(true).build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit, // Changed to Fit to ensure entire item is visible without cropping
                    modifier = Modifier
                        .fillMaxSize()
                        //.padding(8.dp) // Removed padding to maximize item size
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.name, style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ), color = MaterialTheme.colorScheme.onSurface, maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.brand} • ${item.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${item.color} • ${item.season}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}


/**
 * The top bar of the Wardrobe screen.
 * Displays the screen title and a profile button.
 *
 * @param onProfileClick Callback triggered when the profile button is clicked.
 */
@Composable
fun TopBar(
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp), // Increased padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.home_nav_wardrobe),
            style = MaterialTheme.typography.headlineLarge.copy( // Larger Headline
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
            )
        )

        FilledIconButton( // Changed to FilledIconButton for better visibility/style
            onClick = onProfileClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.profile),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Preview for the Wardrobe screen in the IDE.
 * Demonstrates the screen with a set of sample clothing items.
 */
@Preview(showBackground = true)
@Composable
fun WardrobeScreenPreview() {
    val sampleItems = listOf(
        ClothingItem(
            id = "1",
            name = "T-shirt Bianca",
            brand = "Levi's",
            category = "Tops",
            color = "Bianco",
            season = "Estate"
        ), ClothingItem(
            id = "2",
            name = "Jeans Blu",
            brand = "Diesel",
            category = "Bottoms",
            color = "Blu",
            season = "Tutte"
        ), ClothingItem(
            id = "3",
            name = "Giacca di Pelle",
            brand = "Zara",
            category = "Outerwear",
            color = "Nero",
            season = "Autunno"
        ), ClothingItem(
            id = "4",
            name = "Sneakers",
            brand = "Nike",
            category = "Shoes",
            color = "Bianco/Rosso",
            season = "Tutte"
        )
    )

    val uiState = WardrobeUiState(
        isLoading = false, clothingItems = sampleItems
    )

    DrapeTheme {
        WardrobeScreenContent(
            wardrobeUiState = uiState,
            onNavigateToOutfitCreator = {},
            onNavigateToProfile = {},
            onWardrobeItemClick = {},
            onWardrobeDeleteItem = {},
            onWardrobeRefresh = {},
            onClearError = {},
            onClearDeleteSuccess = {}
        )
    }
}
