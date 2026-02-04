package com.drape.ui.clothing_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.R
import com.drape.data.model.ClothingItem
import com.drape.ui.components.DrapeSnackbar
import com.drape.ui.theme.DrapeTheme

/**
 * Detail screen for displaying comprehensive information about a clothing item.
 * 
 * This screen shows:
 * - Large image of the clothing item
 * - Item name as the screen title
 * - Detailed information (brand, category, color, season)
 * - Delete button in the top app bar
 * 
 * The screen handles loading states, errors, and provides feedback via Snackbars.
 * Users can delete the item from this screen, which will navigate back on success.
 *
 * @param onNavigateBack Callback invoked when the user presses the back button
 *                       or when the item is deleted successfully
 * @param onItemDeleted Callback invoked specifically when an item is deleted,
 *                      typically used to trigger navigation back
 * @param viewModel The [ClothingItemDetailViewModel] instance for managing screen state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingItemDetailScreen(
    onNavigateBack: () -> Unit,
    onItemDeleted: () -> Unit,
    viewModel: ClothingItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Handle errors by showing them in a Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.item?.name ?: "Dettaglio Capo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
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
                actions = {
                    // Show delete button only when an item is loaded
                    if (uiState.item != null) {
                        IconButton(
                            onClick = { viewModel.deleteItem(onItemDeleted) },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina capo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                DrapeSnackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Show loading indicator while fetching data
                uiState.isLoading -> {
                    LoadingState()
                }
                // Show error state if item not found
                uiState.item == null -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Capo non trovato",
                        onRetry = onNavigateBack
                    )
                }
                // Show item details
                else -> {
                    val item = uiState.item!!
                    ClothingItemDetailContent(
                        item = item,
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }
            }
        }
    }
}

/**
 * Content component displaying the clothing item details.
 * 
 * Shows a large image of the item, the item name, and a card containing
 * all the item's details (brand, category, color, season).
 *
 * @param item The clothing item to display
 * @param modifier Optional modifier for customization
 */
@Composable
private fun ClothingItemDetailContent(
    item: ClothingItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large image of the clothing item
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .diskCacheKey(item.id)
                        .memoryCacheKey(item.id)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                // Placeholder when no image is available
                Text(
                    text = "👔",
                    fontSize = 120.sp
                )
            }
        }

        // Item name
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem(
                    label = "Marca",
                    value = item.brand
                )
                DetailItem(
                    label = "Categoria",
                    value = item.category
                )
                DetailItem(
                    label = "Colore",
                    value = item.color
                )
                DetailItem(
                    label = "Stagione",
                    value = item.season
                )
            }
        }
    }
}

/**
 * Component for displaying a single detail item.
 * 
 * Shows a label and value pair, only rendering if the value is not blank.
 *
 * @param label The label describing the detail (e.g., "Brand", "Color")
 * @param value The actual value of the detail
 */
@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    if (value.isNotBlank()) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Loading state component.
 * 
 * Displays a centered circular progress indicator.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state component.
 * 
 * Displays an error message with a retry/back button.
 *
 * @param message The error message to display
 * @param onRetry Callback invoked when the retry button is pressed
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "😕",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Torna indietro")
            }
        }
    }
}

/**
 * Preview composable for the clothing item detail content.
 * 
 * Shows the detail screen with sample data in the IDE preview.
 */
@Preview(showBackground = true)
@Composable
private fun ClothingItemDetailScreenPreview() {
    DrapeTheme {
        val sampleItem = ClothingItem(
            id = "1",
            name = "T-shirt Bianca",
            brand = "Levi's",
            category = "TOP",
            color = "Bianco",
            season = "Estate"
        )
        
        ClothingItemDetailContent(item = sampleItem)
    }
}
