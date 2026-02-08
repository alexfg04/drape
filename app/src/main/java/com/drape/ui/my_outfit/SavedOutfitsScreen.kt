package com.drape.ui.my_outfit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape // explicit import
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite // For empty state
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.R
import com.drape.data.model.Outfit
import com.drape.ui.theme.DrapeTheme
import com.drape.ui.components.DrapeSnackbar
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AutoAwesome


/**
 * Screen for viewing and managing saved outfits.
 */
@Composable
fun SavedOutfitsScreen(
    viewModel: SavedOutfitsViewModel = hiltViewModel(),
    onEditOutfit: (Outfit) -> Unit = {},
    onCreateOutfit: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()


    SavedOutfitsScreenContent(
        uiState = uiState,
        onOutfitImageClick = { viewModel.selectOutfit(it) },
        onDismissDetail = { viewModel.selectOutfit(null) },
        onDeleteOutfit = { viewModel.deleteOutfit(it) },
        onEditOutfit = onEditOutfit,
        onCreateOutfit = onCreateOutfit,
        onRefresh = { viewModel.refresh() },
        onClearError = { viewModel.clearError() },
        onClearDeleteSuccess = { viewModel.clearDeleteSuccess() }
    )
}

@Composable
fun SavedOutfitsScreenContent(
    uiState: SavedOutfitsUiState,
    onOutfitImageClick: (Outfit) -> Unit,
    onDismissDetail: () -> Unit,
    onDeleteOutfit: (String) -> Unit,
    onEditOutfit: (Outfit) -> Unit,
    onCreateOutfit: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onClearDeleteSuccess: () -> Unit,
    onClearFavoriteToggled: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var outfitToDelete by remember { mutableStateOf<Outfit?>(null) }
    
    // Handle deletion success
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            snackbarHostState.showSnackbar(
                message = "Outfit eliminato con successo",
                duration = SnackbarDuration.Short
            )
            onClearDeleteSuccess()
        }
    }

    // Handle errors (only if not a full screen error) by showing transient message
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && uiState.outfits.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage ?: "Errore sconosciuto",
                duration = SnackbarDuration.Long
            )
            onClearError()
        }
    }

    // Delete Confirmation Dialog
    outfitToDelete?.let { outfit ->
        AlertDialog(
            onDismissRequest = { outfitToDelete = null },
            title = { Text(stringResource(R.string.saved_outfits_delete_title)) },
            text = { Text(stringResource(R.string.saved_outfits_delete_message, outfit.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteOutfit(outfit.id)
                        outfitToDelete = null
                        onDismissDetail() // Close detail if open
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.saved_outfits_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { outfitToDelete = null }) {
                    Text(stringResource(R.string.saved_outfits_delete_cancel))
                }
            }
        )
    }

    // Zoomed/Centered Detail Dialog
    uiState.selectedOutfit?.let { outfit ->
        OutfitDetailDialog(
            outfit = outfit,
            onDismiss = onDismissDetail,
            onDelete = { outfitToDelete = outfit },
            onEdit = { onEditOutfit(outfit) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
             SnackbarHost(hostState = snackbarHostState) { data ->
                 DrapeSnackbar(snackbarData = data)
             }
        },
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
                onCreateOutfit = onCreateOutfit,
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
    onCreateOutfit: () -> Unit,
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
                        onOutfitImageClick = onOutfitImageClick,
                        onDeleteOutfit = onDeleteOutfit,
                        onEditOutfit = onEditOutfit,
                        onCreateOutfit = onCreateOutfit
                    )
                }
            }
        }
    }
}

/**
 * Dialog displaying a "zoomed" version of the outfit in full screen with actions.
 */
@Composable
fun OutfitDetailDialog(
    outfit: Outfit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit // Added callback for edit
) {
   Dialog(
       onDismissRequest = onDismiss,
       properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
   ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.wardrobe_search_close),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    

                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Main Image Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.8f)
                            .shadow(16.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
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
                                contentScale = ContentScale.Fit, // Contain within box
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.saved_outfits_no_image),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = outfit.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = stringResource(R.string.saved_outfits_items_count_detail, outfit.items.size),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Action Buttons Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Edit Button (Primary)
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.saved_outfits_edit_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Delete Button (Error)
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.saved_outfits_delete_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun SavedOutfitsGrid(
    outfits: List<Outfit>,
    onOutfitImageClick: (Outfit) -> Unit,
    onDeleteOutfit: (Outfit) -> Unit,
    onEditOutfit: (Outfit) -> Unit,
    onCreateOutfit: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            PremiumBanner(onCreateOutfit = onCreateOutfit)
        }

        items(outfits, key = { it.id }) { outfit ->
            SavedOutfitItemCard(
                outfit = outfit,
                onImageClick = { onOutfitImageClick(outfit) },
                onDelete = { onDeleteOutfit(outfit) }, // Pass the whole outfit for confirmation
                onEdit = { onEditOutfit(outfit) }
            )
        }
    }
}

@Composable
fun SavedOutfitItemCard(
    outfit: Outfit,
    onImageClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            .clickable { onImageClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Taller aspect ratio for premium look
        ) {
            // Hero Image
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👕",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 300f
                        )
                    )
            )

            // Content Overlay (Name & Items)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = outfit.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.saved_outfits_items_count, outfit.items.size),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
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
    AnimatedContent(
        targetState = isSearchActive,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "TopBarTransition"
    ) { active ->
        if (active) {
            SavedOutfitsSearchTopBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClose = onSearchClosed
            )
        } else {
            SavedOutfitsDefaultTopBar(onSearchTriggered = onSearchTriggered)
        }
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
            .statusBarsPadding()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.wardrobe_search_close),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp)), // Pill shape for search
            placeholder = {
                Text(
                    stringResource(R.string.wardrobe_search_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
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
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 24.dp, start = 20.dp, end = 20.dp), // More horizontal padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.saved_outfits_topbar_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            // Optional: Date or Subtitle if available in resources, skipping for now to keep it safe
        }

        IconButton(
            onClick = onSearchTriggered,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SavedOutfitsLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.saved_outfits_loading_message),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SavedOutfitsErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // Error Illustration Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "!", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.saved_outfits_error_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            ) { 
                Text(stringResource(R.string.saved_outfits_retry_button), fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun SavedOutfitsEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // Premium Empty State Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite, // Heart icon for saved outfits
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.saved_outfits_empty_message),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.saved_outfits_empty_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

@Composable
fun SavedOutfitsNoResultsState(searchQuery: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
             Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.saved_outfits_search_no_results_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.saved_outfits_search_no_results_message, searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumBanner(
    onCreateOutfit: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp), // Compact height
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Section
                Box(
                    contentAlignment = Alignment.TopStart,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.apparel_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(top = 8.dp) // Push down slightly
                    )
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )
                }

                // Text Section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.premium_banner_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            lineHeight = 28.sp // Better line height for caps
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.premium_banner_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCreateOutfit,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(36.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = stringResource(R.string.premium_banner_button),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
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
            onCreateOutfit = {},
            onRefresh = {},
            onClearError = {},
            onClearDeleteSuccess = {},
            onClearFavoriteToggled = {}
        )
    }
}
