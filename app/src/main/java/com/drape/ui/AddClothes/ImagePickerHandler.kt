package com.drape.ui.addclothes

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

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
        var size: Long = 0
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex != -1) {
                if (!it.isNull(sizeIndex)) {
                     size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }

    /**
     * Checks if the file at the given Uri is within the allowed size limit.
     */
    fun isFileSizeValid(context: Context, uri: Uri): Boolean {
        val size = getFileSize(context, uri)
        return size <= MAX_FILE_SIZE_BYTES
    }

    /**
     * Rotates the image at the given Uri by 90 degrees and saves it to a temporary file.
     */
    fun rotateImage(context: Context, uri: Uri): Uri? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            val matrix = android.graphics.Matrix()
            matrix.postRotate(90f)

            val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            // Create a temporary file in the cache directory
            val file = java.io.File(context.cacheDir, "rotated_${System.currentTimeMillis()}.jpg")
            val out = java.io.FileOutputStream(file)
            
            // Compress and save
            rotatedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()

            android.net.Uri.fromFile(file)
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
