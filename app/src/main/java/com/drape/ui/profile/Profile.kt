package com.drape.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drape.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.ui.upload_clothes.rememberImagePicker
import com.drape.ui.myOutfit.SavedOutfitsViewModel
import com.drape.ui.theme.DrapeTheme
import android.net.Uri

/**
 * Profile screen.
 * Displays user profile information with cover, profile picture, and banners.
 * Uses only Core Material Icons to ensure maximum compatibility.
 */
@Composable
fun ProfileScreen(
    onSavedOutfitsClick: () -> Unit = {},
    viewModel: SavedOutfitsViewModel = hiltViewModel(),
    wardrobeViewModel: com.drape.ui.wardrobe.WardrobeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val wardrobeUiState by wardrobeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val coverImageLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> coverImageUri = uri }
    )

    val profileImageLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> profileImageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ... (Header Section remains the same) ...
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Cover Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable {
                        coverImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (coverImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.8f
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.outfitblu),
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                }
            }

            // Circular Profile Picture
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomCenter)
                    .clickable {
                        profileImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = CircleShape,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Info Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mario Rossi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@mario_drape",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Fashion Enthusiast | Drape Style",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics Section (Using Core Icons only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                label = "Outfit",
                value = uiState.outfits.size.toString(),
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFF1976D2),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSavedOutfitsClick() }
            )
            StatBox(
                label = "Capi",
                value = wardrobeUiState.clothingItems.size.toString(),
                icon = Icons.Default.Star,
                iconColor = Color(0xFF7B1FA2),
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Profilo",
                value = "100%",
                icon = Icons.Default.Person,
                iconColor = Color(0xFFC2185B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Banners Section (Cartine)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "I miei Outfit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            CartinaBanner(
                title = "Outfit Autunno",
                backgroundColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                backgroundImageRes = R.drawable.autunnobutton
            )
            CartinaBanner(
                title = "Outfit Inverno",
                backgroundColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                backgroundImageRes = R.drawable.invernobutton
            )
            CartinaBanner(
                title = "Outfit Estate",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = Color.White,
                backgroundImageRes = R.drawable.estate
            )
            CartinaBanner(
                title = "Outfit Eventi",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = Color.White,
                backgroundImageRes = R.drawable.elegante
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom Actions Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* No logic */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "Modifica app",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            TextButton(
                onClick = { /* No logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background decoration
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp),
                tint = iconColor.copy(alpha = 0.05f)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = iconColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CartinaBanner(
    title: String,
    backgroundColor: Color,
    contentColor: Color,
    backgroundImageRes: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundImageRes != null) {
                Image(
                    painter = painterResource(id = backgroundImageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (backgroundImageRes != null) Color.White.copy(alpha = 0.2f) else contentColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.iconamaglietta),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (backgroundImageRes != null) Color.White else contentColor

                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (backgroundImageRes != null) Color.White else contentColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DrapeTheme {
        ProfileScreen()
    }
}
