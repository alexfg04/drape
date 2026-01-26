package com.drape.ui.outfit_creator

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drape.R
import com.drape.ui.theme.DrapeTheme
import kotlin.math.roundToInt

/**
 * Data class to manage the transformations (scale, rotation, offset) of each clothing item.
 */
data class ItemTransform(
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offset: Offset = Offset.Zero
)

/**
 * Main screen for the Wardrobe section where users can customize their look.
 */
@Composable
fun WardrobeScreen() {
    // Selected item resource IDs for each category
    var selectedTopResId by remember { mutableStateOf<Int?>(null) }
    var selectedBottomResId by remember { mutableStateOf<Int?>(null) }
    var selectedShoeResId by remember { mutableStateOf<Int?>(null) }
    var selectedAccResId by remember { mutableStateOf<Int?>(null) }

        val density = LocalDensity.current
        val topInitialOffset = remember(density) { Offset(0f, with(density) { (-150).dp.toPx() }) }
        val bottomInitialOffset = remember(density) { Offset(0f, with(density) { 150.dp.toPx() }) }
        val shoeInitialOffset = remember(density) { Offset(0f, with(density) { 450.dp.toPx() }) }
        val accInitialOffset = remember(density) { Offset(with(density) { 150.dp.toPx() }, with(density) { (-150).dp.toPx() }) }

        var topTransform by remember { mutableStateOf(ItemTransform(offset = topInitialOffset)) }
        var bottomTransform by remember { mutableStateOf(ItemTransform(offset = bottomInitialOffset)) }
        var shoeTransform by remember { mutableStateOf(ItemTransform(offset = shoeInitialOffset)) }
        var accTransform by remember { mutableStateOf(ItemTransform(offset = accInitialOffset)) }

    // Menu visibility state
    var isMenuExpanded by remember { mutableStateOf(true) }

    // Lists of items for each category
    val tops = listOf(null, R.drawable.image_removebg_preview, R.drawable.outfitnero, R.drawable.outfitrosso)
    val bottoms = listOf(null, R.drawable.image_removebg_preview_2, R.drawable.outfitbianco, R.drawable.outfitgiallo)
    val shoes = listOf(null, R.drawable.image_removebg_preview_3, R.drawable.outfitverdeacqua, R.drawable.outfitviolascuro)
    val accessories = listOf(null, R.drawable.outfitblu, R.drawable.outfitverde, R.drawable.outfitarancione)

    // Current category index and labels
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("Sopra", "Sotto", "Scarpe", "Accessori")
    
    // Items to display in the current gallery
    val currentItems = when (selectedCategoryIndex) {
        0 -> tops
        1 -> bottoms
        2 -> shoes
        else -> accessories
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // PREVIEW AREA (Interactive Canvas) - Occupies remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            // Render items in depth order (Shoes -> Bottom -> Top -> Accessories)
            selectedShoeResId?.let { resId ->
                ClothItem(
                    resId = resId,
                    transform = shoeTransform,
                    isActive = selectedCategoryIndex == 2,
                    onSelect = { selectedCategoryIndex = 2 },
                    onTransformUpdate = { shoeTransform = it }
                )
            }
            selectedBottomResId?.let { resId ->
                ClothItem(
                    resId = resId,
                    transform = bottomTransform,
                    isActive = selectedCategoryIndex == 1,
                    onSelect = { selectedCategoryIndex = 1 },
                    onTransformUpdate = { bottomTransform = it }
                )
            }
            selectedTopResId?.let { resId ->
                ClothItem(
                    resId = resId,
                    transform = topTransform,
                    isActive = selectedCategoryIndex == 0,
                    onSelect = { selectedCategoryIndex = 0 },
                    onTransformUpdate = { topTransform = it }
                )
            }
            selectedAccResId?.let { resId ->
                ClothItem(
                    resId = resId,
                    transform = accTransform,
                    isActive = selectedCategoryIndex == 3,
                    onSelect = { selectedCategoryIndex = 3 },
                    onTransformUpdate = { accTransform = it }
                )
            }

            // BACK ARROW BUTTON
            IconButton(
                onClick = { /* TODO: Implement Home navigation logic */ },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
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

            // ACTION BAR (Save Outfit + Side Buttons)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar Button inside a circle
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, CircleShape),
                    tonalElevation = 4.dp
                ) {
                    IconButton(onClick = { /* TODO: Open Calendar logic */ }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Schedule outfit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Main Save Button
                Button(
                    onClick = { /* TODO: Implement save logic */ },
                    modifier = Modifier
                        .height(48.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Salva Outfit", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }

                Spacer(Modifier.width(16.dp))

                // Style Suggestions Button inside a circle (Using Star as a reliable substitute for Lightbulb)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, CircleShape),
                    tonalElevation = 4.dp
                ) {
                    IconButton(onClick = { /* TODO: Implement style suggestions logic */ }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Get suggestions",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM MENU - Dynamic height adapted to content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // Menu Header (Expand/Collapse)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMenuExpanded = !isMenuExpanded }
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .align(Alignment.TopCenter)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isMenuExpanded) "Personalizza Look" else "Scegli i tuoi capi",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // Category Selector Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEachIndexed { index, title ->
                                val isSelected = selectedCategoryIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { selectedCategoryIndex = index },
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
                            itemsIndexed(currentItems) { _, resId ->
                                val isSelected = when (selectedCategoryIndex) {
                                    0 -> selectedTopResId == resId
                                    1 -> selectedBottomResId == resId
                                    2 -> selectedShoeResId == resId
                                    else -> selectedAccResId == resId
                                }

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
                                        .clickable {
                                            when (selectedCategoryIndex) {
                                                0 -> selectedTopResId = resId
                                                1 -> selectedBottomResId = resId
                                                2 -> selectedShoeResId = resId
                                                else -> selectedAccResId = resId
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (resId != null) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                            Text("Nessuno", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }
                                    
                                    if (isSelected && resId != null) {
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
                        }
                        
                        // Reset Position Button
                        TextButton(
                            onClick = {
                                when (selectedCategoryIndex) {
                                    0 -> topTransform = ItemTransform(offset = topInitialOffset)
                                    1 -> bottomTransform = ItemTransform(offset = bottomInitialOffset)
                                    2 -> shoeTransform = ItemTransform(offset = shoeInitialOffset)
                                    3 -> accTransform = ItemTransform(offset = accInitialOffset)
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                        ) {
                            Text("Reset Posizione", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable representing a single clothing item on the canvas with interaction handles.
 *
 * @param resId The drawable resource ID of the item.
 * @param transform Current transformation state (scale, rotation, offset).
 * @param isActive Boolean indicating if this item is currently selected for editing.
 * @param onSelect Callback when the item is tapped.
 * @param onTransformUpdate Callback when transformations are updated via gestures.
 */
@Composable
fun ClothItem(
    resId: Int,
    transform: ItemTransform,
    isActive: Boolean,
    onSelect: () -> Unit,
    onTransformUpdate: (ItemTransform) -> Unit
) {
    val currentTransform by rememberUpdatedState(transform)
    val currentOnUpdate by rememberUpdatedState(onTransformUpdate)

    Box(
        modifier = Modifier
            .offset { IntOffset(currentTransform.offset.x.roundToInt(), currentTransform.offset.y.roundToInt()) }
            .size(300.dp)
            .graphicsLayer {
                scaleX = currentTransform.scale
                scaleY = currentTransform.scale
                rotationZ = currentTransform.rotation
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnUpdate(currentTransform.copy(offset = currentTransform.offset + dragAmount))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isActive) {
            // Dashed blue outline
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF2196F3),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // RESIZE HANDLE (Bottom-Right) - Vertical drag for scaling
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
                            val newScale = (currentTransform.scale + scaleDelta).coerceIn(0.5f, 5f)
                            currentOnUpdate(currentTransform.copy(scale = newScale))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
            }

            // ROTATION HANDLE (Top-Right) - Horizontal drag for rotating
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
                            val newRotation = currentTransform.rotation + rotationDelta
                            currentOnUpdate(currentTransform.copy(rotation = newRotation))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WardrobeScreenPreview() {
    DrapeTheme {
        WardrobeScreen()
    }
}
