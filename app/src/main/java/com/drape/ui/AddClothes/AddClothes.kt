package com.drape.ui.addclothes

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.ui.theme.DrapeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Schermata per il caricamento e la revisione di un nuovo capo d'abbigliamento.
 */
@Composable
fun UploadItemScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val uriSaver = Saver<MutableState<Uri?>, String>(
        save = { it.value?.toString() ?: "" },
        restore = { mutableStateOf(if (it.isEmpty()) null else Uri.parse(it)) }
    )

    var selectedImageUri by rememberSaveable(saver = uriSaver) { mutableStateOf<Uri?>(null) }
    
    // Form State
    var isFormVisible by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var season by rememberSaveable { mutableStateOf("") }

    // Inizializza il selettore di immagini con validazione delle dimensioni
    val imagePickerLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            TopBarSection(
                title = if (isFormVisible) "Item Details" else "Upload Item",
                onClose = { 
                    if (isFormVisible) isFormVisible = false 
                    else { /* TODO: Close App/Screen */ } 
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
                            Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isFormVisible) {
            AddItemForm(
                modifier = Modifier.padding(paddingValues),
                imageUri = selectedImageUri,
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
                onSave = {
                    // TODO: Implement save logic
                    Toast.makeText(context, "Saved: $name, $brand, $category, $color, $season", Toast.LENGTH_LONG).show()
                    // Reset or navigation
                    isFormVisible = false
                    selectedImageUri = null
                    name = ""
                    category = ""
                    color = ""
                    season = ""
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
                    imageUri = selectedImageUri,
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Card Rimozione Sfondo
                RemoveBackgroundCard()

                // Spingiamo i pulsanti di azione verso il basso
                Spacer(modifier = Modifier.weight(1f))

                // 3. Pulsanti di editing (Undo, Crop, Rotate)
                ActionButtonsRow(
                    onUndo = { selectedImageUri = null },
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
        if (title == "Upload Item") {
            Text(
                text = "Review your capture",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MainImagePreview(
    imageUri: Uri?,
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
                            "AI Enhanced", 
                            color = MaterialTheme.colorScheme.onTertiary, 
                            style = MaterialTheme.typography.labelSmall
                        )
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
                            "Tap to add a photo", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemoveBackgroundCard() {
    var isChecked by rememberSaveable { mutableStateOf(true) }

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
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Remove Background",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Use magic wand to isolate item.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

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
    onSave: () -> Unit
) {
    val categories = listOf("Tops", "Bottoms", "Shoes", "Outerwear", "Accessories")
    val seasons = listOf("Spring", "Summer", "Autumn", "Winter", "All Season")
    var categoryExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
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
            text = "Basic Info",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = brand,
            onValueChange = onBrandChange,
            label = { Text("Brand") },
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
                    .menuAnchor(),
                readOnly = true,
                value = category,
                onValueChange = {},
                label = { Text("Category") },
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
                        text = { Text(selectionOption) },
                        onClick = {
                            onCategoryChange(selectionOption)
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
            text = "Details",
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
                    .menuAnchor(),
                readOnly = true,
                value = season,
                onValueChange = {},
                label = { Text("Season") },
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
            enabled = name.isNotEmpty() && category.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Save to Closet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

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
            text = "Color",
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
        UploadItemScreen()
    }
}
