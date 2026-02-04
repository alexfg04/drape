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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
    onNavigateToClothingDetail: (ClothingItem) -> Unit,
    wardrobeViewModel: WardrobeViewModel = hiltViewModel()
) {
    val wardrobeUiState by wardrobeViewModel.uiState.collectAsState()

    WardrobeScreenContent(
        wardrobeUiState = wardrobeUiState,
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
    onWardrobeItemClick: (ClothingItem) -> Unit,
    onWardrobeDeleteItem: (String) -> Unit,
    onWardrobeRefresh: () -> Unit,
    onClearError: () -> Unit,
    onClearDeleteSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteSuccessMessage = "Capo eliminato con successo"
    val errorMessage = wardrobeUiState.errorMessage

    // Handle deletion success
    LaunchedEffect(wardrobeUiState.deleteSuccess) {
        if (wardrobeUiState.deleteSuccess) {
            snackbarHostState.showSnackbar(
                message = deleteSuccessMessage,
                duration = SnackbarDuration.Short
            )
            onClearDeleteSuccess()
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
            TopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchTriggered = { isSearchActive = true },
                onSearchClosed = {
                    isSearchActive = false
                    searchQuery = ""
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                DrapeSnackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WardrobeListContent(
                uiState = wardrobeUiState,
                searchQuery = searchQuery,
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
    onItemClick: (ClothingItem) -> Unit,
    onRefresh: () -> Unit
) {
    val allFilterText = stringResource(R.string.wardrobe_filter_all)
    val filters = listOf(allFilterText) + ItemCategory.entries.map { it.name }
    var selectedFilter by remember { mutableStateOf(allFilterText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        FilterSection(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it })

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingState()
            }

            uiState.errorMessage != null && uiState.clothingItems.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage, onRetry = onRefresh
                )
            }

            uiState.clothingItems.isEmpty() -> {
                EmptyState()
            }

            else -> {
                val filteredItems = uiState.clothingItems.filter { item ->
                    (selectedFilter == allFilterText || item.category.equals(
                        selectedFilter, ignoreCase = true
                    )) && (searchQuery.isEmpty() || item.name.contains(
                        searchQuery,
                        ignoreCase = true
                    ) || item.brand.contains(
                        searchQuery,
                        ignoreCase = true
                    ) || item.color.contains(searchQuery, ignoreCase = true))
                }

                if (filteredItems.isEmpty()) {
                    NoResultsState(searchQuery = searchQuery, filter = selectedFilter)
                } else {
                    WardrobeGrid(
                        clothingItems = filteredItems, onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

/**
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val backgroundColor =
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val contentColor =
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            val borderColor =
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(percent = 50),
                color = backgroundColor,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(
                    1.dp, borderColor
                ) else null,
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = filter, style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ), color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Grid component displaying clothing items.
 *
 * @param clothingItems List of [ClothingItem] to display in the grid.
 * @param onItemClick Callback triggered when an item is clicked.
 */
@Composable
fun WardrobeGrid(clothingItems: List<ClothingItem>, onItemClick: (ClothingItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(clothingItems, key = { it.id }) { item ->
            WardrobeItemCard(item = item, onClick = { onItemClick(item) })
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
 *
 * @param isSearchActive Boolean flag indicating if search is currently active.
 * @param searchQuery The current search query string.
 * @param onSearchQueryChange Callback triggered when the search query changes.
 * @param onSearchTriggered Callback to activate search mode.
 * @param onSearchClosed Callback to deactivate search mode and clear the query.
 */
@Composable
fun TopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onSearchClosed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
    ) {
        if (isSearchActive) {
            SearchTopBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClose = onSearchClosed
            )
        } else {
            DefaultTopBar(
                onSearchTriggered = onSearchTriggered
            )
        }
    }
}

/**
 * Search view for the [TopBar].
 * Includes a text field to enter search queries and buttons to close search or clear the input.
 *
 * @param query The current search query string.
 * @param onQueryChange Callback triggered when the query changes.
 * @param onClose Callback to close the search view.
 */
@Composable
fun SearchTopBar(
    query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Chiudi ricerca",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.wardrobe_search_placeholder)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Cancella ricerca",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            })
    }
}

/**
 * Default title view for the [TopBar].
 * Displays the screen title, a search icon to trigger search mode, and a profile placeholder.
 *
 * @param onSearchTriggered Callback to activate search mode.
 */
@Composable
fun DefaultTopBar(
    onSearchTriggered: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.home_nav_wardrobe), style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onSearchTriggered) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cerca",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

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
            onWardrobeItemClick = {},
            onWardrobeDeleteItem = {},
            onWardrobeRefresh = {},
            onClearError = {},
            onClearDeleteSuccess = {}
        )
    }
}
