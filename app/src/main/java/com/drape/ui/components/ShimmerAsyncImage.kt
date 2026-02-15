package com.drape.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

@Composable
fun ShimmerAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    onLoadingStateChanged: ((Boolean) -> Unit)? = null
) {
    val painter = rememberAsyncImagePainter(model = model)
    val state = painter.state
    LaunchedEffect(state, onLoadingStateChanged) {
        onLoadingStateChanged?.invoke(
            state is AsyncImagePainter.State.Success || state is AsyncImagePainter.State.Error
        )
    }

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            alpha = alpha
        )

        if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Empty) {
            ShimmerImagePlaceholder(modifier = Modifier.fillMaxSize())
        }
    }
}
