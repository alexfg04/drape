package com.drape.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drape.R
import com.drape.ui.theme.DrapeTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ColorSelectionScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onContinueClick: (Color) -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf(Color(0xFF1565C0)) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00458D)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.color_selection_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.color_selection_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            ColorPickerCircular(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutfitPreviewCard(color = selectedColor)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onContinueClick(selectedColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
            ) {
                Text(
                    text = stringResource(id = R.string.continue_button),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedColor == Color.White || selectedColor == Color(0xFFFFEB3B)) Color.Black else Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ColorPickerCircular(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF1565C0), Color(0xFF26A69A), Color(0xFF66BB6A),
        Color(0xFFFFEB3B), Color(0xFFFF7043), Color(0xFFD32F2F),
        Color(0xFF8E24AA), Color(0xFF5E35B1),
        Color.Black, Color.DarkGray, Color.LightGray, Color.White
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val diameter = minOf(maxWidth, maxHeight)
        val centerSize = diameter * 0.35f
        val itemSize = diameter * 0.15f
        val radius = diameter * 0.38f

        // Central Large Color
        Surface(
            modifier = Modifier
                .size(centerSize),
            shape = CircleShape,
            color = selectedColor,
            shadowElevation = 8.dp,
            border = BorderStroke(4.dp, Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (selectedColor == Color.White || selectedColor == Color(0xFFFFEB3B)) Color.Black else Color.White,
                    modifier = Modifier.size(centerSize * 0.4f)
                )
            }
        }

        // Outer colors arranged in a circle
        colors.forEachIndexed { index, color ->
            val angle = (2 * Math.PI * index.toDouble() / colors.size) - (Math.PI / 2)
            val x = (radius.value * cos(angle)).dp
            val y = (radius.value * sin(angle)).dp

            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selectedColor) 2.dp else 1.dp,
                        color = if (color == selectedColor) Color.White else Color.LightGray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
fun OutfitPreviewCard(
    color: Color,
    modifier: Modifier = Modifier
) {
    val outfitImage = when (color) {
        Color(0xFF1565C0) -> R.drawable.outfitblu
        Color(0xFF26A69A) -> R.drawable.outfitverdeacqua
        Color(0xFF66BB6A) -> R.drawable.outfitverde
        Color(0xFFFFEB3B) -> R.drawable.outfitgiallo
        Color(0xFFFF7043) -> R.drawable.outfitarancione
        Color(0xFFD32F2F) -> R.drawable.outfitrosso
        Color(0xFF8E24AA) -> R.drawable.outfitviolachiaro
        Color(0xFF5E35B1) -> R.drawable.outfitviolascuro
        Color.Black -> R.drawable.outfitnero
        Color.DarkGray -> R.drawable.outfitgrigioscuro
        Color.LightGray -> R.drawable.outfitgrigiochiaro
        Color.White -> R.drawable.outfitbianco
        else -> R.drawable.outfitblu
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = outfitImage),
                    contentDescription = "Outfit Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.outfit_preview_label),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (color == Color.White) Color.Black else color.copy(alpha = 0.9f)
                    )
                )
                Text(
                    text = "Personalizza il tuo stile",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorSelectionScreenPreview() {
    DrapeTheme {
        ColorSelectionScreen()
    }
}
