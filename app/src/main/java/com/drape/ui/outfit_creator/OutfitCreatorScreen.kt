package com.drape.ui.outfit_creator

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.drape.R
import com.drape.data.model.ItemCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Main screen for creating and customizing outfits.
 */
@Composable
fun OutfitCreatorScreen(
    outfitId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: OutfitCreatorViewModel = hiltViewModel()
) {
    LaunchedEffect(outfitId) {
        if (outfitId != null) {
            viewModel.loadOutfit(outfitId)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    
    // Menu visibility state
    var isMenuExpanded by remember { mutableStateOf(true) }

    // Category labels and mapping
    val categories = listOf(
        stringResource(R.string.outfit_creator_category_top),
        stringResource(R.string.outfit_creator_category_bottom),
        stringResource(R.string.outfit_creator_category_shoes),
        stringResource(R.string.outfit_creator_category_accessories)
    )
    val categoryMapping = listOf(
        ItemCategory.TOP,
        ItemCategory.BOTTOM,
        ItemCategory.SHOES,
        ItemCategory.ACCESSORIES
    )
    
    val selectedCategory = uiState.selectedCategory
    val selectedCategoryIndex = categoryMapping.indexOf(selectedCategory)

    // Filter available clothes by current selected category
    val currentItems = uiState.availableClothes.filter {
        it.category.equals(selectedCategory.name, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // PREVIEW AREA (Interactive Canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.updateCanvasOffset(dragAmount)
                    }
                }
                .pointerInput(Unit) {
                    // Tap on background to hide selection
                    detectTapGestures(onTap = { viewModel.toggleSelectionVisibility(false) })
                },
            contentAlignment = Alignment.Center
        ) {
            // SCROLLABLE CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            uiState.canvasOffset.x.roundToInt(),
                            uiState.canvasOffset.y.roundToInt()
                        )
                    }
                    .drawWithContent {
                        // Start recording into graphicsLayer (Excludes UI controls outside this Box)
                        graphicsLayer.record(
                            size = IntSize(size.width.roundToInt(), size.height.roundToInt())
                        ) {
                            this@drawWithContent.drawContent()
                        }
                        // Draw the recorded layer to the screen
                        drawLayer(graphicsLayer)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Render items in depth order (Shoes -> Bottom -> Top -> Accessories)
                categoryMapping.sortedBy { cat: ItemCategory -> getRenderPriority(cat) }.forEach { category ->
                    val itemState = uiState.placedItems[category]
                    if (itemState != null) {
                        ClothItem(
                            imageUrl = itemState.clothingItem.imageUrl,
                            scale = itemState.scale,
                            rotation = itemState.rotation,
                            offset = itemState.offset,
                            isActive = uiState.isSelectionVisible && (selectedCategory == category),
                            onSelect = { viewModel.selectCategory(category) },
                            onTransformUpdate = { s, r, o ->
                                viewModel.updateTransform(category, s, r, o)
                            }
                        )
                    }
                }
            }

            // BACK ARROW BUTTON
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.outfit_creator_back_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // CATEGORY INDICATOR
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                shape = CircleShape
            ) {
                Text(
                    text = categories[selectedCategoryIndex],
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ACTION BAR
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp).shadow(8.dp, CircleShape),
                    tonalElevation = 4.dp
                ) {
                    IconButton(onClick = { /* TODO: Open Calendar logic */ }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.outfit_creator_schedule_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Save Button
                Button(
                    onClick = {
                        scope.launch {
                            // Hide selection before capture to ensure clean thumbnail
                            viewModel.toggleSelectionVisibility(false)
                            // A small delay might be needed for state to propagate, but usually record { } records the next draw
                            val thumbnailUri = captureThumbnail(graphicsLayer, context)
                            viewModel.saveOutfit(thumbnailUri)
                        }
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.height(48.dp).shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.outfit_creator_save_button),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Suggestions Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp).shadow(8.dp, CircleShape),
                    tonalElevation = 4.dp
                ) {
                    IconButton(onClick = { /* TODO: Suggestions */ }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.outfit_creator_suggestions_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        
            // Success Feedback
            if (uiState.saveSuccess) {
                AlertDialog(
                    onDismissRequest = { 
                        viewModel.clearSaveSuccess()
                        onBackClick()
                    },
                    title = { Text(stringResource(R.string.outfit_creator_success_title)) },
                    text = { Text(stringResource(R.string.outfit_creator_success_message)) },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.clearSaveSuccess()
                            onBackClick() 
                        }) { Text(stringResource(R.string.ok)) }
                    }
                )
            }

            // Error Feedback
            LaunchedEffect(uiState.errorResId) {
                uiState.errorResId?.let { resId ->
                    Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .animateContentSize()
            ) {

                // Menu Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMenuExpanded = !isMenuExpanded }
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .align(Alignment.TopCenter)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isMenuExpanded) {
                                stringResource(R.string.outfit_creator_menu_title_expanded)
                            } else {
                                stringResource(R.string.outfit_creator_menu_title_collapsed)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isMenuExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        // Category Selector Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEachIndexed { index, title ->
                                val category = categoryMapping[index]
                                val isSelected = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { viewModel.selectCategory(category) },
                                    contentAlignment = Alignment.Center
                               ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Gallery of Items
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 12.dp, end = 20.dp)
                        ) {
                            // "None" item for clearing selection
                            item {
                                GalleryItem(
                                    imageUrl = null,
                                    isSelected = uiState.placedItems[selectedCategory] == null,
                                    onClick = { viewModel.selectItem(selectedCategory, null) }
                                )
                            }
                            
                            itemsIndexed(currentItems) { _, item ->
                                val isSelected = uiState.placedItems[selectedCategory]?.clothingItem?.id == item.id
                                GalleryItem(
                                    imageUrl = item.imageUrl,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectItem(selectedCategory, item) }
                                )
                            }
                        }
                        
                        // Reset Position Button
                        TextButton(
                            onClick = { 
                                viewModel.resetTransform(selectedCategory) 
                            },
                            modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.outfit_creator_reset_position),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Captures the current content of the graphics layer as a bitmap and saves it to a temporary file.
 *
 * @param graphicsLayer The layer containing the UI to capture.
 * @param context The current context.
 * @return The [Uri] of the saved thumbnail file, or null if an error occurs.
 */
private suspend fun captureThumbnail(
    graphicsLayer: GraphicsLayer,
    context: Context
): Uri? = withContext(Dispatchers.IO) {
    try {
        val imageBitmap = graphicsLayer.toImageBitmap()
        val bitmap = imageBitmap.asAndroidBitmap()
        val file = File(context.cacheDir, "outfit_thumb_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * An item card in the selection gallery at the bottom.
 *
 * @param imageUrl The URL of the clothing image. If null, a "None" option is shown.
 * @param isSelected Flag indicating if this item is the currently selected one in its category.
 * @param onClick Callback triggered when the item is clicked.
 */
@Composable
fun GalleryItem(
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(12.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                Text(
                    text = stringResource(R.string.outfit_creator_none),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        if (isSelected && imageUrl != null) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

/**
 * Determines rendering order based on item category.
 * Used to ensure logical layering (e.g., shoes appear behind pants).
 *
 * @param category The [ItemCategory] to evaluate.
 * @return An integer priority value (lower values are rendered first).
 */
private fun getRenderPriority(category: ItemCategory): Int {
    return when (category) {
        ItemCategory.SHOES -> 0
        ItemCategory.BOTTOM -> 1
        ItemCategory.TOP -> 2
        ItemCategory.ACCESSORIES -> 3
    }
}

/**
 * Individual clothing item on the canvas with interactive transformation controls.
 * Handles dragging, scaling, and rotation.
 *
 * @param imageUrl The URL of the image to display.
 * @param scale The current scale factor.
 * @param rotation The current rotation in degrees.
 * @param offset The (x, y) offset on the canvas.
 * @param isActive Flag indicating if the item is currently active for editing.
 * @param onSelect Callback to select this item as active.
 * @param onTransformUpdate Callback for updates to scale, rotation, or offset.
 */
@Composable
fun ClothItem(
    imageUrl: String,
    scale: Float,
    rotation: Float,
    offset: Offset,
    isActive: Boolean,
    onSelect: () -> Unit,
    onTransformUpdate: (Float?, Float?, Offset?) -> Unit
) {
    // We use rememberUpdatedState to ensure pointerInput captures the latest values without resetting
    val currentOffset by rememberUpdatedState(offset)
    val currentScale by rememberUpdatedState(scale)
    val currentRotation by rememberUpdatedState(rotation)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnTransformUpdate by rememberUpdatedState(onTransformUpdate)

    Box(
        modifier = Modifier
            .offset { IntOffset(currentOffset.x.roundToInt(), currentOffset.y.roundToInt()) }
            .size(250.dp)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
            }
            .pointerInput(Unit) {
                // Unified tap/drag management to avoid conflicts
                detectTapGestures(onTap = { currentOnSelect() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { currentOnSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Rotate the drag delta to match the screen coordinate system
                        val rotatedDrag = rotateVector(dragAmount, currentRotation)
                        // Add rotated drag delta to current offset from state
                        currentOnTransformUpdate(null, null, currentOffset + rotatedDrag)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isActive) {
            // Dashed blue outline when item is active
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF2196F3),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // RESIZE HANDLE
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 15.dp, y = 15.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF2196F3), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val scaleDelta = dragAmount.y * 0.005f
                            currentOnTransformUpdate(currentScale + scaleDelta, null, null)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
            }

            // ROTATION HANDLE
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 15.dp, y = (-15).dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF2196F3), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val rotationDelta = dragAmount.x * 0.5f
                            currentOnTransformUpdate(null, currentRotation + rotationDelta, null)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
            }
        }
    }
}


/**
 * Rotates a vector by a given angle in degrees.
 */
private fun rotateVector(vector: Offset, angleDegrees: Float): Offset {
    val angleRadians = Math.toRadians(angleDegrees.toDouble())
    val cos = kotlin.math.cos(angleRadians)
    val sin = kotlin.math.sin(angleRadians)
    return Offset(
        x = (vector.x * cos - vector.y * sin).toFloat(),
        y = (vector.x * sin + vector.y * cos).toFloat()
    )
}
