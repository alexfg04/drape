package com.drape.ui.wardrobe

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.R
import com.drape.data.model.ClothingItem
import com.drape.data.model.ItemCategory

import com.drape.ui.myOutfit.OutfitDetailDialog
import com.drape.ui.myOutfit.SavedOutfitsListContent
import com.drape.ui.myOutfit.SavedOutfitsScreen
import com.drape.ui.myOutfit.SavedOutfitsViewModel
import com.drape.ui.theme.*

/**
 * Main screen for viewing and managing the user's wardrobe and saved outfits.
 * Displays a sliding toggle to switch between clothes and outfits.
 */
@Composable
fun WardrobeScreen(
    wardrobeViewModel: WardrobeViewModel = hiltViewModel(),
    savedOutfitsViewModel: SavedOutfitsViewModel = hiltViewModel(),
    onNavigateToOutfits: () -> Unit = {},
    onEditOutfit: (com.drape.data.model.Outfit) -> Unit = {}
) {
    val wardrobeUiState by wardrobeViewModel.uiState.collectAsState()
    val savedOutfitsUiState by savedOutfitsViewModel.uiState.collectAsState()
    
    // Tab State: 0 for Wardrobe, 1 for Saved Outfits
    var selectedTab by remember { mutableStateOf(0) }

    WardrobeScreenContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        wardrobeUiState = wardrobeUiState,
        savedOutfitsUiState = savedOutfitsUiState,
        onWardrobeItemClick = { wardrobeViewModel.selectItem(it) },
        onWardrobeClearSelection = { wardrobeViewModel.clearSelection() },
        onWardrobeDeleteItem = { wardrobeViewModel.deleteClothingItem(it) },
        onWardrobeRefresh = { wardrobeViewModel.refresh() },
        onOutfitImageClick = { savedOutfitsViewModel.selectOutfit(it) },
        onDismissOutfitDetail = { savedOutfitsViewModel.selectOutfit(null) },
        onDeleteOutfit = { savedOutfitsViewModel.deleteOutfit(it) },
        onEditOutfit = onEditOutfit,
        onToggleFavoriteOutfit = { savedOutfitsViewModel.toggleFavorite(it) },
        onOutfitRefresh = { savedOutfitsViewModel.refresh() }
    )
}

/**
 * The content section of the Wardrobe screen.
 * Separated for easier previewing and testing.
 *
 * @param uiState The current UI state of the wardrobe.
 * @param onItemClick Callback triggered when a clothing item is clicked.
 * @param onClearSelection Callback to clear the currently selected item.
 * @param onDeleteItem Callback to delete a specific clothing item.
 * @param onRefresh Callback to refresh the wardrobe contents.
 */
@Composable
fun WardrobeScreenContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    wardrobeUiState: WardrobeUiState,
    savedOutfitsUiState: com.drape.ui.myOutfit.SavedOutfitsUiState,
    onWardrobeItemClick: (ClothingItem) -> Unit,
    onWardrobeClearSelection: () -> Unit,
    onWardrobeDeleteItem: (String) -> Unit,
    onWardrobeRefresh: () -> Unit,
    onOutfitImageClick: (com.drape.data.model.Outfit) -> Unit,
    onDismissOutfitDetail: () -> Unit,
    onDeleteOutfit: (String) -> Unit,
    onEditOutfit: (com.drape.data.model.Outfit) -> Unit,
    onToggleFavoriteOutfit: (com.drape.data.model.Outfit) -> Unit,
    onOutfitRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // State for outfits deletion
    var outfitToDelete by remember { mutableStateOf<com.drape.data.model.Outfit?>(null) }
    
     // Delete Confirmation Outfits Dialog
    outfitToDelete?.let { outfit ->
        AlertDialog(
            onDismissRequest = { outfitToDelete = null },
            title = { Text("Elimina Outfit") },
            text = { Text("Sei sicuro di voler eliminare l'outfit \"${outfit.name}\"? Questa azione non può essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteOutfit(outfit.id)
                        outfitToDelete = null
                        onDismissOutfitDetail() // Close detail if open
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { outfitToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }


    // Handle selected item dialog (wardrobe)
    wardrobeUiState.selectedItem?.let { selectedItem ->
        ItemDetailDialog(
            item = selectedItem,
            isDeleting = wardrobeUiState.isDeleting,
            onDismiss = onWardrobeClearSelection,
            onDelete = { onWardrobeDeleteItem(selectedItem.id) }
        )
    }
    
     // Zoomed/Centered Detail Dialog (outfits)
    savedOutfitsUiState.selectedOutfit?.let { outfit ->
        OutfitDetailDialog(
            outfit = outfit,
            isFavorite = savedOutfitsUiState.favoriteOutfitIds.contains(outfit.id),
            onDismiss = onDismissOutfitDetail,
            onToggleFavorite = { onToggleFavoriteOutfit(outfit) },
            onDelete = { outfitToDelete = outfit }
        )
    }

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
                },
                selectedTab = selectedTab,
                onTabSelected = { 
                    onTabSelected(it)
                    // Clear search when switching tabs? Optional.
                    // searchQuery = "" 
                }
            )
        },

        ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                WardrobeListContent(
                    uiState = wardrobeUiState,
                    searchQuery = searchQuery,
                    onItemClick = onWardrobeItemClick,
                    onRefresh = onWardrobeRefresh
                )
            } else {
                 SavedOutfitsListContent(
                    uiState = savedOutfitsUiState,
                    searchQuery = searchQuery,
                    onOutfitImageClick = onOutfitImageClick,
                    onDeleteOutfit = { outfitToDelete = it },
                    onEditOutfit = onEditOutfit,
                    onToggleFavorite = onToggleFavoriteOutfit,
                    onRefresh = onOutfitRefresh
                )
            }
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
                Text("Elimina")
            }
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss, enabled = !isDeleting
        ) {
            Text("Chiudi")
        }
    }, text = {
        ClothingItemDetailCard(item = item)
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
                .clip(RoundedCornerShape(16.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.name, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ), color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(label = "Brand", value = item.brand)
        DetailRow(label = "Categoria", value = item.category)
        DetailRow(label = "Colore", value = item.color)
        DetailRow(label = "Stagione", value = item.season)
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
                text = "Caricamento vestiti...",
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
                text = "Ops! Qualcosa è andato storto",
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
                Text("Riprova")
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
                text = "Il tuo guardaroba è vuoto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inizia aggiungendo il tuo primo capo!",
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
                text = "Nessun risultato", style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Nessun capo trovato per \"$searchQuery\""
                } else {
                    "Nessun capo nella categoria \"$filter\""
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
 * @param selectedFilter The currently active filter.
 * @param onFilterSelected Callback when a filter is chosen.
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    val allFilterText = stringResource(R.string.wardrobe_filter_all)
                    val context = LocalContext.current
                    Text(
                        text = if (filter == allFilterText) filter else getDisplayNameForCategory(
                            context, ItemCategory.valueOf(filter)
                        ), style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ), color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * A grid layout that displays a list of clothing item cards.
 *
 * @param clothingItems The list of [ClothingItem] objects to show.
 * @param onItemClick Callback triggered when an item is selected.
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
 * A card component that represents a single item in the wardrobe grid.
 *
 * @param item The [ClothingItem] data.
 * @param onClick Callback when the card is clicked.
 * @param modifier Modifier for visual layout adjustments.
 */
@Composable
fun WardrobeItemCard(item: ClothingItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
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
 * The top bar of the Wardrobe screen, which toggles between default and search modes.
 *
 * @param isSearchActive Flag indicating if the search bar should be visible.
 * @param searchQuery The current text in the search bar.
 * @param onSearchQueryChange Callback when search text is updated.
 * @param onSearchTriggered Callback to initiate search mode.
 * @param onSearchClosed Callback to close search mode.
 */
@Composable
fun TopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onSearchClosed: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
    ) {
        if (isSearchActive) {
            SearchTopBar(
                query = searchQuery, 
                onQueryChange = onSearchQueryChange, 
                onClose = onSearchClosed,
                placeholderText = if (selectedTab == 0) "Cerca vestiti" else "Cerca outfit"
            )
        } else {
            DefaultTopBar(
                onSearchTriggered = onSearchTriggered
            )
        }
        
        // Sliding Toggle centered at the top, always visible
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            SlidingToggle(
                options = listOf("Vestiti", "Outfit"),
                selectedIndex = selectedTab,
                onOptionSelected = onTabSelected
            )
        }
    }
}

/**
 * Top bar with a functional search text field.
 *
 * @param query The search text.
 * @param onQueryChange Callback for text updates.
 * @param onClose Callback to exit search mode.
 */
@Composable
fun SearchTopBar(
    query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit, placeholderText: String
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
                contentDescription = stringResource(R.string.wardrobe_search_close),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholderText) },
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
                            contentDescription = stringResource(R.string.wardrobe_search_clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            })
    }
}

/**
 * The default top bar showing the screen title and a search icon.
 *
 * @param onSearchTriggered Callback to start a search.
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
            text = "Guardaroba",
            style = MaterialTheme.typography.headlineMedium.copy(
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
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

        }
    }
}

/**
 * A custom sliding toggle component.
 */
@Composable
fun SlidingToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    val cornerRadius = 24.dp
    
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(4.dp)
    ) {
        Row(
           verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, text ->
                 val isSelected = index == selectedIndex
                 val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                 val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                 
                 Box(
                     modifier = Modifier
                         .clip(RoundedCornerShape(cornerRadius))
                         .background(backgroundColor)
                         .clickable(
                             interactionSource = remember { MutableInteractionSource() },
                             indication = null
                         ) { onOptionSelected(index) }
                         .padding(vertical = 8.dp, horizontal = 24.dp),
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = text,
                         style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                         color = textColor
                     )
                 }
            }
        }
    }
}

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
            category = "Tops",
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
            selectedTab = 0,
            onTabSelected = {},
            wardrobeUiState = uiState,
            savedOutfitsUiState = com.drape.ui.myOutfit.SavedOutfitsUiState(),
            onWardrobeItemClick = {},
            onWardrobeClearSelection = {},
            onWardrobeDeleteItem = {},
            onWardrobeRefresh = {},
            onOutfitImageClick = {},
            onDismissOutfitDetail = {},
            onDeleteOutfit = {},
            onEditOutfit = {},
            onToggleFavoriteOutfit = {},
            onOutfitRefresh = {}
        )
    }
}

/**
 * Helper function to get the localized display name for a clothing category.
 *
 * @param context The current context.
 * @param category The [ItemCategory] enum value.
 * @return The Italian display string for the category.
 */
fun getDisplayNameForCategory(context: android.content.Context, category: ItemCategory): String {
    return when (category) {
        ItemCategory.TOP -> context.getString(R.string.outfit_creator_category_top)
        ItemCategory.BOTTOM -> context.getString(R.string.outfit_creator_category_bottom)
        ItemCategory.SHOES -> context.getString(R.string.outfit_creator_category_shoes)
        ItemCategory.ACCESSORIES -> context.getString(R.string.outfit_creator_category_accessories)
    }
}
