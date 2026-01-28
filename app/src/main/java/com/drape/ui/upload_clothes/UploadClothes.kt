package com.drape.ui.upload_clothes

import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.data.model.ItemCategory
import com.drape.ui.theme.DrapeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import android.util.Log
import com.drape.R

/**
 * Stateful version of the screen that handles ViewModel integration.
 */
@Composable
fun UploadItemScreen(
    onBackClick: () -> Unit,
    viewModel: UploadClothesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    UploadItemContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onUploadClothingItem = viewModel::uploadClothingItem,
        onProcessImage = viewModel::processImage,
        onClearError = viewModel::clearError,
        onClearSuccessState = viewModel::clearSuccessState,
        onClearProcessedImage = viewModel::clearProcessedImage
    )
}

/**
 * Stateless version of the screen, ideal for Previews and testing.
 * Refactored to avoid direct ViewModel dependency in UI components.
 */
@Composable
fun UploadItemContent(
    uiState: UploadClothesUiState,
    onBackClick: () -> Unit = {},
    onUploadClothingItem: (Uri, String, String, String, String, String, Boolean) -> Unit,
    onProcessImage: (Uri, Boolean) -> Unit,
    onClearError: () -> Unit,
    onClearSuccessState: () -> Unit,
    onClearProcessedImage: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val uriSaver = Saver<MutableState<Uri?>, String>(
        save = { it.value?.toString() ?: "" },
        restore = { mutableStateOf(if (it.isEmpty()) null else it.toUri()) }
    )

    var selectedImageUri by rememberSaveable(saver = uriSaver) { mutableStateOf(null) }
    
    // Background removal state
    var removeBackgroundEnabled by rememberSaveable { mutableStateOf(true) }
    
    // Form State
    var isFormVisible by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var season by rememberSaveable { mutableStateOf("") }

    val successMessage = stringResource(R.string.upload_clothes_success)
    val noImageErrorMessage = stringResource(R.string.upload_clothes_error_no_image)
    // Handle successful upload
    LaunchedEffect(uiState.isUploadSuccessful) {
        if (uiState.isUploadSuccessful) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            onClearSuccessState()
            // Navigate back to wardrobe
            onBackClick()
        }
    }

    // Handle errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            onClearError()
        }
    }

    // Inizializza il selettore di immagini con validazione delle dimensioni
    val imagePickerLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> 
            selectedImageUri = uri
            onClearProcessedImage()
            // Process image immediately if toggle is enabled
            if (removeBackgroundEnabled) {
                onProcessImage(uri, true)
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            TopBarSection(
                title = if (isFormVisible) stringResource(R.string.upload_clothes_title_details) else stringResource(R.string.upload_clothes_title_upload),
                onClose = {
                    onBackClick()
                }
            ) 
        },
        bottomBar = { 
            if (!isFormVisible) {
                BottomActionButton(
                    onClick = {
                        if (selectedImageUri != null) {
                            isFormVisible = true
                        } else {
                            Toast.makeText(context, noImageErrorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isFormVisible) {
            AddItemForm(
                modifier = Modifier.padding(paddingValues),
                imageUri = uiState.processedImageUri ?: selectedImageUri,
                name = name,
                onNameChange = { name = it },
                brand = brand,
                onBrandChange = { brand = it },
                category = category,
                onCategoryChange = { category = it },
                color = color,
                onColorChange = { color = it },
                season = season,
                onSeasonChange = { season = it },
                uiState = uiState,
                onSave = {
                    selectedImageUri?.let { uri ->
                        onUploadClothingItem(
                            uri,
                            name,
                            brand,
                            category,
                            color,
                            season,
                            removeBackgroundEnabled
                        )
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Anteprima immagine - Cliccando si apre il selettore
                MainImagePreview(
                    imageUri = if (removeBackgroundEnabled) uiState.processedImageUri ?: selectedImageUri else selectedImageUri,
                    isProcessing = uiState.isProcessingBackground,
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Card Rimozione Sfondo
                RemoveBackgroundCard(
                    isChecked = removeBackgroundEnabled,
                    isProcessing = uiState.isProcessingBackground,
                    onCheckedChange = { enabled ->
                        removeBackgroundEnabled = enabled
                        selectedImageUri?.let { uri ->
                            if (enabled) {
                                onProcessImage(uri, true)
                            } else {
                                onClearProcessedImage()
                            }
                        }
                    }
                )

                // Spingiamo i pulsanti di azione verso il basso
                Spacer(modifier = Modifier.weight(1f))

                // 3. Pulsanti di editing (Undo, Crop, Rotate)
                ActionButtonsRow(
                    onUndo = { 
                        selectedImageUri = null
                        onClearProcessedImage()
                    },
                    onCrop = {
                        Toast.makeText(context, "Crop functionality requires an external library.", Toast.LENGTH_SHORT).show()
                    },
                    onRotate = {
                        selectedImageUri?.let { uri ->
                            scope.launch {
                                val newUri = withContext(Dispatchers.IO) {
                                    ImagePickerHandler.rotateImage(context, uri)
                                }
                                if (newUri != null) {
                                    selectedImageUri = newUri
                                    onClearProcessedImage()
                                    // Re-run background removal if enabled
                                    if (removeBackgroundEnabled) {
                                        onProcessImage(newUri, true)
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to rotate image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
                
                // Spazio finale
                Spacer(modifier = Modifier.height(62.dp))
            }
        }
    }
}

/**
 * The top bar section of the Upload screen.
 * Displays the screen title and a progress indicator.
 *
 * @param title The title text to display.
 * @param onClose Callback triggered when the close button is clicked.
 */
@Composable
fun TopBarSection(title: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Close", 
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            IconButton(onClick = { /* TODO: Help */ }) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline, 
                    contentDescription = "Help", 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (title == stringResource(R.string.upload_clothes_title_upload)) {
            Text(
                text = stringResource(R.string.upload_clothes_review_capture),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * A preview card for the selected clothing image.
 * Shows a loading state during background processing or an "add" icon if no image is selected.
 *
 * @param imageUri The URI of the image to display.
 * @param isProcessing Flag indicating if the background removal process is active.
 * @param onClick Callback triggered when the card is clicked.
 */
@Composable
fun MainImagePreview(
    imageUri: Uri?,
    isProcessing: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected garment",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Processing overlay
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.upload_clothes_removing_background),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.upload_clothes_ai_enhanced), 
                                color = MaterialTheme.colorScheme.onTertiary, 
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.upload_clothes_tap_to_add), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * A card allowing the user to toggle background removal for the selected image.
 *
 * @param isChecked Boolean state of the toggle.
 * @param isProcessing Boolean flag showing if processing is active.
 * @param onCheckedChange Callback when the toggle state changes.
 */
@Composable
fun RemoveBackgroundCard(
    isChecked: Boolean,
    isProcessing: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.upload_clothes_remove_background),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isProcessing) stringResource(R.string.upload_clothes_processing) else stringResource(R.string.upload_clothes_process_info),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = !isProcessing,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * A row of action buttons for common image operations (Undo, Crop, Rotate).
 *
 * @param onUndo Callback for the undo action.
 * @param onCrop Callback for the crop action.
 * @param onRotate Callback for the rotate action.
 */
@Composable
fun ActionButtonsRow(
    onUndo: () -> Unit,
    onCrop: () -> Unit,
    onRotate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(icon = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", onClick = onUndo)
        Spacer(modifier = Modifier.width(32.dp))
        ActionButton(icon = Icons.Default.Crop, contentDescription = "Crop", onClick = onCrop)
        Spacer(modifier = Modifier.width(32.dp))
        ActionButton(icon = Icons.Default.Refresh, contentDescription = "Rotate", onClick = onRotate)
    }
}

/**
 * A circular button used for specific image actions.
 *
 * @param icon The [ImageVector] to display.
 * @param contentDescription Accessibility description for the button.
 * @param onClick Callback triggered on click.
 */
@Composable
fun ActionButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.size(56.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon, 
                contentDescription = contentDescription, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * A form for entering the metadata details of a new clothing item.
 *
 * @param modifier Modifier for layout customization.
 * @param imageUri The URI of the garment image.
 * @param name The name of the garment.
 * @param onNameChange Callback for name updates.
 * @param brand The brand of the garment.
 * @param onBrandChange Callback for brand updates.
 * @param category The selected category (e.g., TOP, BOTTOM).
 * @param onCategoryChange Callback for category updates.
 * @param color The selected color.
 * @param onColorChange Callback for color updates.
 * @param season The selected season.
 * @param onSeasonChange Callback for season updates.
 * @param uiState The state of the upload process.
 * @param onSave Callback triggered when the "Save" button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemForm(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    name: String,
    onNameChange: (String) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    season: String,
    onSeasonChange: (String) -> Unit,
    uiState: UploadClothesUiState,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val categories = ItemCategory.entries.toList()
    val seasons = stringArrayResource(R.array.seasons).toList()
    var categoryExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Validation Image
        if (imageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected garment",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Basic Info Section
        Text(
            text = stringResource(R.string.upload_clothes_base_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.upload_clothes_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = brand,
            onValueChange = onBrandChange,
            label = { Text(stringResource(R.string.upload_clothes_brand)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                value = if (category.isNotEmpty()) {
                    try {
                        getDisplayNameForCategory(context, ItemCategory.valueOf(category))
                    } catch (e: Exception) {
                        Log.e("UploadClothes", "Error parsing category: $category", e)
                        category
                    }
                } else "",
                onValueChange = {},
                label = { Text(stringResource(R.string.upload_clothes_category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(getDisplayNameForCategory(context, selectionOption)) },
                        onClick = {
                            onCategoryChange(selectionOption.name)
                            categoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Details Section
        Text(
            text = stringResource(R.string.upload_clothes_details),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Color Palette
        ColorSelectionSection(
            selectedColorName = color,
            onColorSelected = onColorChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = seasonExpanded,
            onExpandedChange = { seasonExpanded = !seasonExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                value = season,
                onValueChange = {},
                label = { Text(stringResource(R.string.upload_clothes_season)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = seasonExpanded,
                onDismissRequest = { seasonExpanded = false }
            ) {
                seasons.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onSeasonChange(selectionOption)
                            seasonExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSave,
            enabled = name.isNotEmpty() && category.isNotEmpty() && !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = stringResource(R.string.upload_clothes_add_to_closet),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Section for selecting a color from a predefined palette.
 *
 * @param selectedColorName The name of the currently selected color.
 * @param onColorSelected Callback when a color is chosen.
 */
@Composable
fun ColorSelectionSection(
    selectedColorName: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        Pair("Black", Color.Black),
        Pair("White", Color.White),
        Pair("Grey", Color.Gray),
        Pair("Red", Color.Red),
        Pair("Blue", Color.Blue),
        Pair("Green", Color.Green),
        Pair("Yellow", Color.Yellow),
        Pair("Orange", Color(0xFFFFA500)),
        Pair("Purple", Color(0xFF800080)),
        Pair("Pink", Color(0xFFFFC0CB)),
        Pair("Brown", Color(0xFFA52A2A)),
        Pair("Beige", Color(0xFFF5F5DC))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Colore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Simple Grid for colors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // First 6 colors
            colors.take(6).forEach { (name, color) ->
                ColorCircle(
                    color = color,
                    isSelected = name == selectedColorName,
                    onClick = { onColorSelected(name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
             // Next 6 colors
            colors.drop(6).take(6).forEach { (name, color) ->
                ColorCircle(
                    color = color,
                    isSelected = name == selectedColorName,
                    onClick = { onColorSelected(name) }
                )
            }
        }
    }
}

/**
 * A small colored circle representing a selection in the color palette.
 *
 * @param color The [Color] value.
 * @param isSelected Boolean flag for selection state.
 * @param onClick Callback triggered on selection.
 */
@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.LightGray),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

/**
 * The primary action button at the bottom of the screen to proceed with the upload.
 *
 * @param onClick Callback triggered on click.
 */
@Composable
fun BottomActionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
    ) {
        Text(
            text = "Add to Closet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Rounded.Checkroom, contentDescription = null)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUploadItem() {
    DrapeTheme {
        UploadItemContent(
            uiState = UploadClothesUiState(),
            onUploadClothingItem = { _, _, _, _, _, _, _ -> },
            onProcessImage = { _, _ -> },
            onClearError = {},
            onClearSuccessState = {},
            onClearProcessedImage = {}
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
