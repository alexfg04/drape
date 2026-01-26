package com.drape.ui.upload_clothes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.io.File
import java.io.FileOutputStream

/**
 * Handle logic for picking an image and validating its size.
 */
object ImagePickerHandler {
    // Maximum allowed file size: 5MB
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 

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

    fun isFileSizeValid(context: Context, uri: Uri): Boolean {
        val size = getFileSize(context, uri)
        if (size > 0) {
            return size <= MAX_FILE_SIZE_BYTES
        } else {
            // Unknown size, stream and check
            return try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(8192)
                    var totalBytes = 0L
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytes += bytesRead
                        if (totalBytes > MAX_FILE_SIZE_BYTES) {
                            return@use false
                        }
                    }
                    true
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
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
    onSizeExceeded: () -> Unit = {
        Toast.makeText(context, "Image is too large. Maximum size is 5MB.", Toast.LENGTH_SHORT).show()
    }
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    uri?.let {
        if (ImagePickerHandler.isFileSizeValid(context, it)) {
            onImageSelected(it)
        } else {
            onSizeExceeded()
        }
    }
}
