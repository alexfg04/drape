package com.drape.ui.addclothes

import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
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

    // Inizializza il selettore di immagini con validazione delle dimensioni
    val imagePickerLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBarSection() },
        bottomBar = { BottomActionButton() }
    ) { paddingValues ->
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

@Composable
fun TopBarSection() {
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
            IconButton(onClick = { /* TODO: Close */ }) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Close", 
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Upload Item",
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
        Text(
            text = "Review your capture",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
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

@Composable
fun BottomActionButton() {
    Button(
        onClick = { /* TODO: Add to closet */ },
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
