package com.drape.ui.ListClothes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drape.R
import com.drape.ui.theme.*

data class WardrobeItem(
    val id: Int,
    val name: String,
    val brand: String,
    val imageRes: Int,
    val category: String,
    val color: String,
    val season: String
)

@Composable
fun WardrobeListScreen() {
    val filters = listOf("All", "Tops", "Bottoms", "Shoes")
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<WardrobeItem?>(null) }

    val wardrobeItems = listOf(
        WardrobeItem(1, "Silk Blouse", "ZARA", R.drawable.outfitbianco, "Tops", "White", "Spring"),
        WardrobeItem(2, "Vintage Jeans", "LEVI'S", R.drawable.outfitblu, "Bottoms", "Blue", "All Season"),
        WardrobeItem(3, "Royal Pumps", "MANOLO BLAHNIK", R.drawable.outfitviolascuro, "Shoes", "Blue", "Summer"),
        WardrobeItem(4, "Air Force 1", "NIKE", R.drawable.outfitgrigiochiaro, "Shoes", "White", "All Season"),
        WardrobeItem(5, "Classic Trench", "BURBERRY", R.drawable.outfitarancione, "Outerwear", "Beige", "Autumn"),
        WardrobeItem(6, "Leather Satchel", "MADEWELL", R.drawable.outfitarancione, "Accessories", "Brown", "All Season")
    )

    if (selectedItem != null) {
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            confirmButton = {},
            text = {
                WardrobeItemCard(
                    item = selectedItem!!,
                    onClick = { selectedItem = null },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Logic removed as requested */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            FilterSection(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filteredItems = wardrobeItems.filter { item ->
                (selectedFilter == "All" || item.category == selectedFilter) &&
                        (searchQuery.isEmpty() ||
                                item.name.contains(searchQuery, ignoreCase = true) ||
                                item.brand.contains(searchQuery, ignoreCase = true) ||
                                item.color.contains(searchQuery, ignoreCase = true))
            }

            WardrobeGrid(wardrobeItems = filteredItems, onItemClick = { selectedItem = it })
        }
    }
}

@Composable
fun TopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onSearchClosed: () -> Unit
) {
    if (isSearchActive) {
        SearchTopBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onClose = onSearchClosed
        )
    } else {
        DefaultTopBar(onSearchTriggered = onSearchTriggered)
    }
}

@Composable
fun SearchTopBar(
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Close Search",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search clothes...") },
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
                            contentDescription = "Clear Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun DefaultTopBar(onSearchTriggered: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Wardrobe",
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
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Profile Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(percent = 50),
                color = backgroundColor,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null,
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun WardrobeGrid(wardrobeItems: List<WardrobeItem>, onItemClick: (WardrobeItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Add padding for FAB
    ) {
        items(wardrobeItems) { item ->
            WardrobeItemCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun WardrobeItemCard(item: WardrobeItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth().clickable { onClick() }
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
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${item.brand} • ${item.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
             
            Spacer(modifier = Modifier.height(2.dp))
             
            Text(
                text = "${item.color} • ${item.season}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WardrobeListPreview() {
    DrapeTheme {
        WardrobeListScreen()
    }
}
