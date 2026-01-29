package com.drape.ui.myOutfit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.drape.data.model.Outfit
import com.drape.ui.theme.DrapeTheme

/**
 * Screen for viewing and managing saved outfits.
 */
@Composable
fun SavedOutfitsScreen(
    viewModel: SavedOutfitsViewModel = hiltViewModel(),
    onEditOutfit: (Outfit) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SavedOutfitsScreenContent(
        uiState = uiState,
        onOutfitImageClick = { viewModel.selectOutfit(it) },
        onDismissDetail = { viewModel.selectOutfit(null) },
        onDeleteOutfit = { viewModel.deleteOutfit(it) },
        onEditOutfit = onEditOutfit,
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
fun SavedOutfitsScreenContent(
    uiState: SavedOutfitsUiState,
    onOutfitImageClick: (Outfit) -> Unit,
    onDismissDetail: () -> Unit,
    onDeleteOutfit: (String) -> Unit,
    onEditOutfit: (Outfit) -> Unit,
    onToggleFavorite: (Outfit) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var outfitToDelete by remember { mutableStateOf<Outfit?>(null) }

    // Delete Confirmation Dialog
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
                        onDismissDetail() // Close detail if open
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

    // Zoomed/Centered Detail Dialog
    uiState.selectedOutfit?.let { outfit ->
        OutfitDetailDialog(
            outfit = outfit,
            isFavorite = uiState.favoriteOutfitIds.contains(outfit.id),
            onDismiss = onDismissDetail,
            onToggleFavorite = { onToggleFavorite(outfit) },
            onDelete = { outfitToDelete = outfit }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SavedOutfitsTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchTriggered = { isSearchActive = true },
                onSearchClosed = {
                    isSearchActive = false
                    searchQuery = ""
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SavedOutfitsListContent(
                uiState = uiState,
                searchQuery = searchQuery,
                onOutfitImageClick = onOutfitImageClick,
                onDeleteOutfit = { outfitToDelete = it },
                onEditOutfit = onEditOutfit,
                onToggleFavorite = onToggleFavorite,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
fun SavedOutfitsListContent(
    uiState: SavedOutfitsUiState,
    searchQuery: String,
    onOutfitImageClick: (Outfit) -> Unit,
    onDeleteOutfit: (Outfit) -> Unit,
    onEditOutfit: (Outfit) -> Unit,
    onToggleFavorite: (Outfit) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                SavedOutfitsLoadingState()
            }

            uiState.errorMessage != null && uiState.outfits.isEmpty() -> {
                SavedOutfitsErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRefresh
                )
            }

            uiState.outfits.isEmpty() -> {
                SavedOutfitsEmptyState()
            }

            else -> {
                val filteredOutfits = uiState.outfits.filter { outfit ->
                    searchQuery.isEmpty() || outfit.name.contains(searchQuery, ignoreCase = true)
                }

                if (filteredOutfits.isEmpty()) {
                    SavedOutfitsNoResultsState(searchQuery = searchQuery)
                } else {
                    SavedOutfitsGrid(
                        outfits = filteredOutfits,
                        favoriteOutfitIds = uiState.favoriteOutfitIds,
                        onOutfitImageClick = onOutfitImageClick,
                        onDeleteOutfit = onDeleteOutfit,
                        onEditOutfit = onEditOutfit,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

/**
 * Dialog displaying a "zoomed" version of the outfit.
 */
@Composable
fun OutfitDetailDialog(
    outfit: Outfit,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Preferiti",
                                tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Chiudi")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(0.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (outfit.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(outfit.thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = outfit.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "No Image",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = outfit.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SavedOutfitsGrid(
    outfits: List<Outfit>,
    favoriteOutfitIds: Set<String>,
    onOutfitImageClick: (Outfit) -> Unit,
    onDeleteOutfit: (Outfit) -> Unit,
    onEditOutfit: (Outfit) -> Unit,
    onToggleFavorite: (Outfit) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(outfits, key = { it.id }) { outfit ->
            SavedOutfitItemCard(
                outfit = outfit,
                isFavorite = favoriteOutfitIds.contains(outfit.id),
                onImageClick = { onOutfitImageClick(outfit) },
                onDelete = { onDeleteOutfit(outfit) }, // Pass the whole outfit for confirmation
                onEdit = { onEditOutfit(outfit) },
                onToggleFavorite = { onToggleFavorite(outfit) }
            )
        }
    }
}

@Composable
fun SavedOutfitItemCard(
    outfit: Outfit,
    isFavorite: Boolean,
    onImageClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Image Section (Clickable for Zoom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(0.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (outfit.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(outfit.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = outfit.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Star Button Overlay
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Preferiti",
                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = outfit.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            // Action Row (Edit/Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${outfit.items.size} capi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifica",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedOutfitsTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onSearchClosed: () -> Unit
) {
    if (isSearchActive) {
        SavedOutfitsSearchTopBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onClose = onSearchClosed
        )
    } else {
        SavedOutfitsDefaultTopBar(onSearchTriggered = onSearchTriggered)
    }
}

@Composable
fun SavedOutfitsSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
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
                            contentDescription = stringResource(R.string.wardrobe_search_clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SavedOutfitsDefaultTopBar(onSearchTriggered: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "I miei outfit",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
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

@Composable
fun SavedOutfitsLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Caricamento outfit...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SavedOutfitsErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(text = "😕", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ops! Qualcosa è andato storto",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
            Button(onClick = onRetry) { Text("Riprova") }
        }
    }
}

@Composable
fun SavedOutfitsEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(text = "👗", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nessun outfit salvato",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea il tuo primo outfit nel planner!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SavedOutfitsNoResultsState(searchQuery: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(text = "🔍", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nessun risultato",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nessun outfit trovato per \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedOutfitsScreenPreview() {
    val sampleOutfits = listOf(
        Outfit(id = "1", name = "Summer Vibes", thumbnailUrl = ""),
        Outfit(id = "2", name = "Office Casual", thumbnailUrl = ""),
        Outfit(id = "3", name = "Date Night", thumbnailUrl = "")
    )
    val uiState = SavedOutfitsUiState(isLoading = false, outfits = sampleOutfits)
    DrapeTheme {
        SavedOutfitsScreenContent(
            uiState = uiState,
            onOutfitImageClick = {},
            onDismissDetail = {},
            onDeleteOutfit = {},
            onEditOutfit = {},
            onToggleFavorite = {},
            onRefresh = {}
        )
    }
}
