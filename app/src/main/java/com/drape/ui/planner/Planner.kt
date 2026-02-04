package com.drape.ui.planner


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.drape.ui.theme.DrapeTheme

import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    onNavigateToSelectOutfit: (day: Int, month: Int, year: Int) -> Unit = { _, _, _ -> },
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedView = uiState.viewMode
    
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Get planned outfits for selected day from ViewModel
    val selectedDayOutfits = remember(selectedDay, uiState.plannedDays, uiState.outfits) {
        viewModel.getPlannedItemsForDay(selectedDay)
    }

    if (showBottomSheet) {
        PlannedOutfitsBottomSheet(
            sheetState = sheetState,
            day = selectedDay,
            plannedOutfits = selectedDayOutfits,
            onDismiss = { showBottomSheet = false },
            onRemoveOutfit = { outfitId -> 
                viewModel.removeOutfitFromDay(selectedDay, outfitId)
            },
            onAddOutfit = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                    onNavigateToSelectOutfit(selectedDay, uiState.currentMonth, uiState.currentYear)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "October",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "22°C Sunny",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {

                    }
                )

                // Toggle view
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "Monthly",
                        isSelected = selectedView == PlannerViewMode.MONTHLY,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setViewMode(PlannerViewMode.MONTHLY) }
                    )
                    TabButton(
                        text = "Weekly",
                        isSelected = selectedView == PlannerViewMode.WEEKLY,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setViewMode(PlannerViewMode.WEEKLY) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Placeholder FAB if needed
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                if (selectedView == PlannerViewMode.MONTHLY) {
                    CalendarGrid(
                        days = uiState.calendarDays,
                        isDayOccupied = { day -> viewModel.isDayOccupied(day) },
                        isDayPast = { day -> viewModel.isDayPast(day) },
                        onDayClick = { day ->
                            if (viewModel.isDayOccupied(day)) {
                                selectedDay = day
                                showBottomSheet = true
                            } else {
                                onNavigateToSelectOutfit(day, uiState.currentMonth, uiState.currentYear)
                            }
                        }
                    )
                } else {
                    WeeklyView(
                        daysList = uiState.calendarDays,
                        isDayOccupied = { day -> viewModel.isDayOccupied(day) },
                        isDayPast = { day -> viewModel.isDayPast(day) },
                        onDayClick = { day ->
                            if (viewModel.isDayOccupied(day)) {
                                selectedDay = day
                                showBottomSheet = true
                            } else {
                                onNavigateToSelectOutfit(day, uiState.currentMonth, uiState.currentYear)
                            }
                        }
                    )
                }
            }

            item {
                UpcomingHighlightsSection()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedOutfitsBottomSheet(
    sheetState: SheetState,
    day: Int,
    plannedOutfits: List<PlannedOutfitDisplay>,
    onDismiss: () -> Unit,
    onRemoveOutfit: (String) -> Unit,
    onAddOutfit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Outfit per il giorno $day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(plannedOutfits) { outfit ->
                    PlannedOutfitCard(
                        outfit = outfit,
                        onRemove = { onRemoveOutfit(outfit.outfitId) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onAddOutfit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aggiungi Outfit")
            }
        }
    }
}

@Composable
fun PlannedOutfitCard(
    outfit: PlannedOutfitDisplay,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Outfit image
            if (!outfit.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = outfit.imageUrl,
                    contentDescription = outfit.outfitTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .padding(2.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rimuovi",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 60f
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = outfit.outfitTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = outfit.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarGrid(
    days: List<Int>,
    isDayOccupied: (Int) -> Boolean,
    isDayPast: (Int) -> Boolean,
    onDayClick: (Int) -> Unit
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")

    Column {
        // Weekday Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid
        // Calculating chunks for rows (7 days per row)
        val rows = days.chunked(7)

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(85.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { day ->
                    // Handle empty slots for first row if needed, but here our list is simple
                    Box(modifier = Modifier.weight(1f).padding(2.dp)) {
                        DayCell(
                            day = day,
                            isOccupied = isDayOccupied(day),
                            isPast = isDayPast(day),
                            onClick = { onDayClick(day) }
                        )
                    }
                }
                // Fill remaining space if last row incomplete
                if (row.size < 7) {
                    repeat(7 - row.size) {
                        Spacer(modifier = Modifier.weight(1f).padding(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyView(
    daysList: List<Int>,
    isDayOccupied: (Int) -> Boolean,
    isDayPast: (Int) -> Boolean,
    onDayClick: (Int) -> Unit
) {
    // Current week view (e.g., 22-28)
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.zip(daysList).forEach { (dayName, dayNum) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.6f)) {
                        DayCell(
                            day = dayNum,
                            isOccupied = isDayOccupied(dayNum),
                            isPast = isDayPast(dayNum),
                            onClick = { onDayClick(dayNum) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DayCell(day: Int, isOccupied: Boolean, isPast: Boolean, onClick: () -> Unit) {
    Card(
        onClick = { if (!isPast) onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPast -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
                isOccupied -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPast) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day Number
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPast) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) 
                else 
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content indicator
            if (isPast) {
                // No indicator for past days
            } else if (isOccupied) {
                // Outfit Dot Indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                // Add Icon
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun UpcomingHighlightsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Upcoming Highlights",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val highlights = listOf(
            HighlightItem(
                title = "Date Night",
                subtitle = "Dressy Casual",
                date = "OCT 23"
            ),
            HighlightItem(
                title = "Presentation",
                subtitle = "Formal Business",
                date = "OCT 25"
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth() // Should be LazyRow in real app if many items
        ) {
            highlights.forEach { item ->
                HighlightCard(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

data class HighlightItem(val title: String, val subtitle: String, val date: String)

@Composable
fun HighlightCard(item: HighlightItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outfit Thumb
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.width(50.dp).fillMaxHeight()
            ) {
                // Image
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlannerPreview() {
    DrapeTheme {
        PlannerScreen()
    }
}
