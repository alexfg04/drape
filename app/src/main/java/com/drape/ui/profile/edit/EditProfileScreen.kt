package com.drape.ui.profile.edit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.request.ImageRequest
import com.drape.R
import com.drape.ui.components.ShimmerAsyncImage
import com.drape.ui.components.DrapeSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    fun hasGalleryPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        galleryPermission
    ) == PackageManager.PERMISSION_GRANTED
    var pendingPickerAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val saveSuccessMessage = stringResource(R.string.profile_saved)
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = saveSuccessMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onPhotoSelected(uri)
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onCoverPhotoSelected(uri)
    }
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPickerAction?.invoke()
        } else {
            viewModel.clearError()
        }
        pendingPickerAction = null
    }

    val pickImageRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    fun launchWithGalleryPermission(action: () -> Unit) {
        if (hasGalleryPermission()) {
            action()
        } else {
            pendingPickerAction = action
            galleryPermissionLauncher.launch(galleryPermission)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasGalleryPermission()) {
            galleryPermissionLauncher.launch(galleryPermission)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                DrapeSnackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Modifica Profilo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile(onSuccess = onBackClick) },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Salva")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cover Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val coverModel = uiState.selectedCoverUri ?: uiState.currentCoverUrl
                
                Surface(
                    modifier = Modifier.fillMaxSize().clickable {
                        launchWithGalleryPermission {
                            coverPickerLauncher.launch(pickImageRequest)
                        }
                    },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                     if (coverModel != null) {
                        ShimmerAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(coverModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Cover Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                         Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                 SmallFloatingActionButton(
                    onClick = {
                        launchWithGalleryPermission {
                            coverPickerLauncher.launch(pickImageRequest)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Change Cover", tint = MaterialTheme.colorScheme.onSecondary)
                }
            }
            
            // Profile Image (centered overlapping cover)
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            launchWithGalleryPermission {
                                imagePickerLauncher.launch(pickImageRequest)
                            }
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    val model = uiState.selectedPhotoUri ?: uiState.currentPhotoUrl
                    if (model != null) {
                        ShimmerAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
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
                
                SmallFloatingActionButton(
                    onClick = {
                        launchWithGalleryPermission {
                            imagePickerLauncher.launch(pickImageRequest)
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Change Image", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Fields
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome Utente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Didascalia (Bio)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}
