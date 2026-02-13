package com.drape.ui.upload_clothes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.drape.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

/**
 * Handle logic for picking an image and validating its size.
 */
object ImagePickerHandler {
    // Maximum allowed file size: 5MB
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024
    private const val MAX_DIMENSION = 2048
    private const val MIN_QUALITY = 40

    /**
     * Retrieves the file size of a given Uri in bytes.
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = -1
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex != -1 && !it.isNull(sizeIndex)) {
                size = it.getLong(sizeIndex)
            }
        }
        if (size <= 0L) {
            try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    if (afd.length > 0L) size = afd.length
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return size
    }

    /**
     * Validates if the file size of a given Uri is within the 5MB limit.
     * Starts by querying metadata and falls back to streaming if the size is unknown.
     *
     * @param context The current context.
     * @param uri The URI of the file to check.
     * @return true if the size is valid, false otherwise.
     */
    fun isFileSizeValid(context: Context, uri: Uri): Boolean {
        val size = getFileSize(context, uri)
        if (size > 0) {
            val isValid = size <= MAX_FILE_SIZE_BYTES
            if (!isValid) {
                Log.e("ImagePickerHandler", "File size too large: $size bytes (Max: $MAX_FILE_SIZE_BYTES)")
            }
            return isValid
        } else {
            // Unknown size, stream and check
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e("ImagePickerHandler", "Failed to open input stream for URI: $uri")
                    return false
                }
                inputStream.use { stream ->
                    val buffer = ByteArray(8192)
                    var totalBytes = 0L
                    var bytesRead: Int
                    while (stream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytes += bytesRead
                        if (totalBytes > MAX_FILE_SIZE_BYTES) {
                            Log.e("ImagePickerHandler", "Streamed file size exceeded limit ($totalBytes > $MAX_FILE_SIZE_BYTES)")
                            return@use false
                        }
                    }
                    true
                }
            } catch (e: Exception) {
                Log.e("ImagePickerHandler", "Error checking file size for URI: $uri", e)
                false
            }
        }
    }

    /**
     * Compresses the image only when it exceeds the 5MB limit.
     *
     * @return the original URI if already valid, a new compressed URI when possible, or null on failure.
     */
    fun compressImageIfNeeded(context: Context, uri: Uri): Uri? {
        if (isFileSizeValid(context, uri)) {
            return uri
        }

        val decodedBitmap = decodeBitmapForCompression(context, uri) ?: return null
        var currentBitmap = resizeIfNeeded(decodedBitmap, MAX_DIMENSION)
        if (currentBitmap !== decodedBitmap) {
            decodedBitmap.recycle()
        }

        try {
            repeat(4) {
                var quality = 90
                while (quality >= MIN_QUALITY) {
                    val jpegBitmap = toJpegCompatibleBitmap(currentBitmap)
                    val bytes = ByteArrayOutputStream().use { output ->
                        jpegBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                        output.toByteArray()
                    }
                    if (jpegBitmap !== currentBitmap) {
                        jpegBitmap.recycle()
                    }

                    if (bytes.size <= MAX_FILE_SIZE_BYTES) {
                        return writeCompressedToTempFile(context, bytes)
                    }
                    quality -= 10
                }

                val nextWidth = (currentBitmap.width * 0.8f).toInt()
                val nextHeight = (currentBitmap.height * 0.8f).toInt()
                if (nextWidth < 320 || nextHeight < 320) {
                    return null
                }

                val scaledBitmap = currentBitmap.scale(nextWidth, nextHeight)
                if (scaledBitmap !== currentBitmap) {
                    currentBitmap.recycle()
                    currentBitmap = scaledBitmap
                }
            }
        } catch (e: Exception) {
            Log.e("ImagePickerHandler", "Compression failed for URI: $uri", e)
        } finally {
            if (!currentBitmap.isRecycled) {
                currentBitmap.recycle()
            }
        }

        return null
    }

    private fun decodeBitmapForCompression(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return null

        val sampleSize = calculateInSampleSize(
            srcWidth = bounds.outWidth,
            srcHeight = bounds.outHeight,
            reqWidth = MAX_DIMENSION,
            reqHeight = MAX_DIMENSION
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }
    }

    private fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            var halfHeight = srcHeight / 2
            var halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / maxSide.toFloat()
        val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return bitmap.scale(targetWidth, targetHeight)
    }

    private fun toJpegCompatibleBitmap(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) return bitmap
        return createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565).also { rgbBitmap ->
            Canvas(rgbBitmap).apply {
                drawColor(Color.WHITE)
                drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    private fun writeCompressedToTempFile(context: Context, bytes: ByteArray): Uri {
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            out.write(bytes)
            out.flush()
        }
        return Uri.fromFile(file)
    }

    /**
     * Rotates the image at the given Uri by 90 degrees and saves it to a temporary file.
     */
    fun rotateImage(context: Context, uri: Uri): Uri? {
        return try {
            val contentResolver = context.contentResolver
            val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return null

            val matrix = Matrix()
            matrix.postRotate(90f)

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            // Recycle original if rotatedBitmap is a new instance
            if (rotatedBitmap !== bitmap) {
                bitmap.recycle()
            }

            // Create a temporary file in the cache directory
            val file = File(context.cacheDir, "rotated_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            rotatedBitmap.recycle()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Creates and remembers a launcher for image picking with built-in size validation.
 *
 * @param context The current context.
 * @param onImageSelected Callback triggered when a valid image is chosen.
 * @param onSizeExceeded Optional callback triggered when the image exceeds the 5MB limit.
 */
@Composable
fun rememberImagePicker(
    context: Context,
    onImageSelected: (Uri) -> Unit,
    onSizeExceeded: (String) -> Unit = { msg ->
        Log.e("ImagePicker", "Image size exceeded: $msg")
    }
): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
    val errorMessage = stringResource(R.string.error_image_too_large)
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val finalUri = ImagePickerHandler.compressImageIfNeeded(context, it)
            if (finalUri != null) {
                onImageSelected(finalUri)
            } else {
                onSizeExceeded(errorMessage)
            }
        }
    }
}
