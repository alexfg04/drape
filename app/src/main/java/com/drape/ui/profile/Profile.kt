package com.drape.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drape.ui.upload_clothes.rememberImagePicker
import com.drape.ui.my_outfit.SavedOutfitsViewModel
import com.drape.ui.theme.DrapeTheme
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.drape.receivers.NotificationReceiver

/**
 * Profile screen.
 * Displays user profile information with cover, profile picture, and banners.
 * Uses only Core Material Icons to ensure maximum compatibility.
 */
@Composable
fun ProfileScreen(
    onSavedOutfitsClick: () -> Unit = {},
    onWardrobeClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSeasonClick: (String) -> Unit = {},
    onBackToHome: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    viewModel: SavedOutfitsViewModel = hiltViewModel(),
    wardrobeViewModel: com.drape.ui.wardrobe.WardrobeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val wardrobeUiState by wardrobeViewModel.uiState.collectAsState()
    val user by profileViewModel.userFlow.collectAsState(initial = null)
    val context = LocalContext.current

    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var isTryOnExpanded by remember { mutableStateOf(false) }
    // profileImageUri removed as we use user.photoUrl now

    val coverImageLauncher = rememberImagePicker(
        context = context,
        onImageSelected = { uri -> coverImageUri = uri }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                NotificationReceiver.showNotification(context)
            }
        }
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
                    .height(180.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (user?.coverPhotoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user?.coverPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.8f
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person, // Or generic image icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Back/Home Button
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Or Icons.Default.Home
                    contentDescription = "Back to Home",
                    tint = Color.White
                )
            }

            // Circular Profile Picture
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomCenter),
                shape = CircleShape,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
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
                text = user?.displayName?.takeIf { it.isNotEmpty() } ?: "Utente",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!user?.bio.isNullOrEmpty()) {
                Text(
                    text = user!!.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                 Text(
                    text = "Aggiungi una didascalia...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
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
                modifier = Modifier
                    .weight(1f)
                    .clickable { onWardrobeClick() }
            )
            StatBox(
                label = "Giorni",
                value = profileViewModel.daysInApp.toString(),
                icon = Icons.Default.Person, // Or DateRange if available in Core
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
                backgroundImageRes = R.drawable.autunnobutton,
                onClick = { onSeasonClick("Autunno") }
            )
            CartinaBanner(
                title = "Outfit Inverno",
                backgroundColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                backgroundImageRes = R.drawable.invernobutton,
                onClick = { onSeasonClick("Inverno") }
            )
            CartinaBanner(
                title = "Outfit Estate",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = Color.White,
                backgroundImageRes = R.drawable.estate,
                onClick = { onSeasonClick("Estate") }
            )
            CartinaBanner(
                title = "Outfit Primavera",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = Color.White,
                backgroundImageRes = R.drawable.primavera,
                onClick = { onSeasonClick("Primavera") }
            )

        }

        Spacer(modifier = Modifier.height(32.dp))

        // Body Reference Section (Virtual Try-On)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var isTryOnExpanded by remember { mutableStateOf(false) }

            // Header with Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isTryOnExpanded = !isTryOnExpanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Virtual Try-On",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isTryOnExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isTryOnExpanded) "Collassa" else "Espandi",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // Collapsible Content
                androidx.compose.animation.AnimatedVisibility(visible = isTryOnExpanded) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Advice Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Consigli per una foto perfetta:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val adviceList = listOf(
                                "Usa uno specchio a figura intera",
                                "Assicurati che la stanza sia ben illuminata",
                                "Indossa abiti aderenti per misurazioni precise",
                                "Tieni il telefono all'altezza della vita",
                                "Assicurati che tutto il corpo sia visibile"
                            )
                            adviceList.forEach { advice ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("• ", color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = advice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Placeholder Image Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                             // Placeholder for user body image
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nessuna foto salvata",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                )
                             }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* TODO: Take Photo */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Scatta Foto")
                            }
                            OutlinedButton(
                                onClick = { /* TODO: Upload Photo */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Carica")
                            }
                        }
                        
                        // Edit Button (Visible only if photo exists - simulated as always visible for layout)
                        TextButton(
                            onClick = { /* TODO: Edit Photo */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Modifica Foto")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
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
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "Modifica Profilo",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            TextButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            NotificationReceiver.showNotification(context)
                        } else {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        NotificationReceiver.showNotification(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Test Notifica",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            TextButton(
                onClick = {
                    profileViewModel.signOut()
                    onLogout()
                },
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
    backgroundImageRes: Int? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
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
