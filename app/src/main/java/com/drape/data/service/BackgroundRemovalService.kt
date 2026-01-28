package com.drape.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import androidx.core.graphics.createBitmap

/**
 * Service for removing backgrounds from clothing images using ML Kit Subject Segmentation.
 * 
 * This service uses Google's ML Kit Subject Segmentation API to detect and isolate
 * the main subject (clothing item) from an image, creating a transparent PNG.
 */
@Singleton
class BackgroundRemovalService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Configure the segmenter to return the foreground bitmap
    private val segmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
    )

    /**
     * Removes the background from an image, isolating the main subject.
     * 
     * @param imageUri The URI of the image to process
     * @return A new URI pointing to the processed image with transparent background,
     *         or null if processing fails
     */
    suspend fun removeBackground(imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            // Load the original bitmap
            val originalBitmap = loadBitmapFromUri(imageUri) ?: return@withContext null
            
            // Create InputImage for ML Kit
            val inputImage = InputImage.fromBitmap(originalBitmap, 0)
            
            // Process with ML Kit Subject Segmentation
            val foregroundBitmap = processWithSegmenter(inputImage)
            
            // Recycle original bitmap if different from result
            if (foregroundBitmap !== originalBitmap) {
                originalBitmap.recycle()
            }
            
            if (foregroundBitmap == null) {
                return@withContext null
            }
            
            // Save the result as PNG with transparency
            val resultUri = saveBitmapAsPng(foregroundBitmap)
            
            // Recycle the foreground bitmap
            foregroundBitmap.recycle()
            
            resultUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a Bitmap from a URI using the content resolver.
     *
     * @param uri The URI of the image to load.
     * @return The decoded [Bitmap], or null if loading fails.
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Processes the image with ML Kit Subject Segmentation.
     * Uses [suspendCancellableCoroutine] to convert the Task-based API to coroutines.
     *
     * @param inputImage The [InputImage] to segment.
     * @return The foreground [Bitmap] isolated by ML Kit, or null if segmentation fails.
     */
    private suspend fun processWithSegmenter(inputImage: InputImage): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            val task = segmenter.process(inputImage)
            
            val successListener = OnSuccessListener<SubjectSegmentationResult> { result ->
                if (!continuation.isActive) return@OnSuccessListener
                val foreground = result.foregroundBitmap
                if (foreground != null) {
                    // Create a new bitmap with transparent background
                    val transparentBitmap = createTransparentBitmap(foreground)
                    continuation.resume(transparentBitmap)
                    foreground.recycle()
                } else {
                    continuation.resume(null)
                }
            }
            
            val failureListener = OnFailureListener { exception ->
                if (!continuation.isActive) return@OnFailureListener
                continuation.resumeWithException(exception)
            }
            
            task.addOnSuccessListener(successListener)
            task.addOnFailureListener(failureListener)
            
            // Clean up listeners when coroutine is cancelled
            continuation.invokeOnCancellation {
                // Note: Firebase Tasks don't have removeOnSuccessListener/removeOnFailureListener
                // The isActive check in the listeners handles cancellation
            }
        }
    }

    /**
     * Creates a bitmap with transparent background from the foreground bitmap.
     * The foreground bitmap from ML Kit may have a black background, so we
     * ensure proper transparency by drawing onto a transparent canvas.
     *
     * @param foreground The source [Bitmap] (foreground subject).
     * @return A new [Bitmap] with the subject on a transparent background.
     */
    private fun createTransparentBitmap(foreground: Bitmap): Bitmap {

        val result = createBitmap(foreground.width, foreground.height)
        
        val canvas = Canvas(result)
        // Clear with transparent color
        canvas.drawColor(Color.TRANSPARENT)
        // Draw the foreground
        canvas.drawBitmap(foreground, 0f, 0f, null)
        
        return result
    }

    /**
     * Saves a bitmap as a PNG file with transparency to the application's cache directory.
     *
     * @param bitmap The [Bitmap] to save.
     * @return The [Uri] of the saved file, or null if saving fails.
     */
    private fun saveBitmapAsPng(bitmap: Bitmap): Uri? {
        return try {
            val file = File(context.cacheDir, "bg_removed_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Cleans up old processed images from the cache directory.
     * Call this periodically to prevent cache bloat.
     */
    suspend fun cleanupOldProcessedImages() = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            val now = System.currentTimeMillis()
            val maxAge = 24 * 60 * 60 * 1000 // 24 hours
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("bg_removed_") && 
                    now - file.lastModified() > maxAge) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
