package com.drape.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.drape.R
import com.drape.navigation.BottomNavItems
import com.drape.ui.components.CurvedBottomNavigation
import com.drape.ui.theme.DrapeTheme

private val DrapeBlue = Color(0xFF00458D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DrapeBlue
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Apri menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.home_menu_description))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(com.drape.navigation.Profile) }) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.home_profile_description))
                    }
                }
            )
        },
        bottomBar = {
            CurvedBottomNavigation(
                items = BottomNavItems,
                selectedIndex = 0, // Index for "Home"
                onItemSelected = { index ->
                    if (index != 0) {
                        navController.navigate(BottomNavItems[index].route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.home_greeting_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            item {
                HomeSectionTitle(title = stringResource(R.string.home_section_outfits))
            }

            // Placeholder per gli outfit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutfitPlaceholderCard(modifier = Modifier.weight(1f), title = stringResource(R.string.home_outfit_casual))
                    OutfitPlaceholderCard(modifier = Modifier.weight(1f), title = stringResource(R.string.home_outfit_elegant))
                }
            }

            item {
                HomeSectionTitle(title = stringResource(R.string.home_section_recent_items))
            }

            // Lista fittizia di capi
            val items = listOf("Giacca di jeans", "T-shirt bianca", "Pantaloni neri", "Sneakers")
            items(items) { garment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(4.dp)
                        ) {
                            // Placeholder per immagine capo
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = DrapeBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {}
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = garment, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun HomeSectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(onClick = { /* Vedi tutto */ }) {
            Text(stringResource(R.string.home_see_all), color = DrapeBlue)
        }
    }
}

@Composable
fun OutfitPlaceholderCard(modifier: Modifier = Modifier, title: String) {
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DrapeBlue.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
