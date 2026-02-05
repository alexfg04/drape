package com.drape.ui.profile.season

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drape.ui.my_outfit.SavedOutfitsGrid
import com.drape.ui.my_outfit.SavedOutfitsLoadingState
import com.drape.data.model.Outfit
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

@Composable
fun ProfileSeasonOutfitsScreen(
    season: String,
    onBackClick: () -> Unit,
    onNavigateToOutfit: (String) -> Unit,
    onCreateOutfit: () -> Unit,
    viewModel: ProfileSeasonOutfitsViewModel = hiltViewModel()
) {
    LaunchedEffect(season) {
        viewModel.loadMarkedSeason(season)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Outfit $season") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    SavedOutfitsLoadingState()
                }
                uiState.outfits.isEmpty() -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = "Nessun outfit trovato con almeno 2 capi per $season",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // Reuse SavedOutfitsGrid or similar. 
                    // Note: SavedOutfitsGrid requires many callbacks. 
                    // For now, let's just reuse it and pass empty or simple callbacks for actions we don't fully support yet (like delete/fav from this view)
                    // Or ideally we should extract the Grid Item.
                    // Since I cannot easily extract the Grid Item without modifying existing files, I will use SavedOutfitsGrid and provide dummy callbacks for now, 
                    // or implement 'onNavigateToOutfit' as the edit action.

                    SavedOutfitsGrid(
                        outfits = uiState.outfits,
                        favoriteOutfitIds = emptySet(), // Not checking favs here for now
                        onOutfitImageClick = { outfit -> onNavigateToOutfit(outfit.id) },
                        onDeleteOutfit = { /* Prevent delete from here for now */ },
                        onEditOutfit = { outfit -> onNavigateToOutfit(outfit.id) },
                        onCreateOutfit = onCreateOutfit,
                        onToggleFavorite = { /* No favorite toggle here for now */ }
                    )
                }
            }
        }
    }
}
