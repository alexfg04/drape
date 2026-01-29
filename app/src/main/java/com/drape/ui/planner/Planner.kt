package com.drape.ui.planner


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.drape.ui.theme.DrapeTheme

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    onNavigateToSelectOutfit: () -> Unit = {},
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedView = uiState.viewMode

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
                                    imageVector = Icons.Default.WbSunny, // Placeholder for weather icon
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
            // Placeholder FAB if needed, though mostly handled inline
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface) // Ensure consistent background
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                if (selectedView == PlannerViewMode.MONTHLY) {
                    CalendarGrid(
                        days = uiState.calendarDays,
                        onDayClick = onNavigateToSelectOutfit
                    )
                } else {
                    WeeklyView(
                        daysList = uiState.calendarDays,
                        onDayClick = onNavigateToSelectOutfit
                    )
                }
            }

            item {
                UpcomingHighlightsSection()
            }

            // Removed bottom spacer to avoid whitespace gap with navbar
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
    onDayClick: () -> Unit
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
                        DayCell(day = day, onClick = onDayClick)
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
    onDayClick: () -> Unit
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
                        DayCell(day = dayNum, onClick = onDayClick)
                    }
                }
            }
        }
    }
}


@Composable
fun DayCell(day: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (day == 5 || day == 23) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                color = if (day in 21..27) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content (Outfit, Add Button, or Empty)
            // Hardcoded logic for demo based on screenshot
            when (day) {
                1, 2, 6, 7, 10, 11, 12 -> {
                    // Add Icon
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                3, 4, 5, 8, 9, 13, 14, 18, 23 -> {
                    // Outfit Dot Indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                else -> {
                    // Empty state
                }
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
