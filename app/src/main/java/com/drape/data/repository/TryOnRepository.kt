package com.drape.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import com.drape.data.datasource.TryOnRemoteDataSource
import com.drape.data.model.TryOnResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale

/**
 * Repository for managing Virtual Try-On operations.
 *
 * Provides a clean API for the UI layer to perform virtual try-ons.
 * Handles image encoding/decoding and error management.
 */
@Singleton
class TryOnRepository @Inject constructor(
    private val tryOnRemoteDataSource: TryOnRemoteDataSource
) {

    /**
     * Performs a virtual try-on by combining a person image with a clothing product.
     *
     * @param personImage Bitmap of the person wearing clothes
     * @param productImage Bitmap of the clothing product to try on
     * @return Result containing the generated try-on image as Bitmap, or an exception on failure
     */
    suspend fun tryOnClothing(
        personImage: Bitmap,
        productImage: Bitmap
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val personBytes = encodePersonBitmapToWebp(personImage)
            val productBytes = encodeProductBitmapToJpeg(productImage)

            // Call the API
            val response = tryOnRemoteDataSource.tryOnClothing(
                personImageBytes = personBytes,
                personContentType = PERSON_CONTENT_TYPE,
                productImageBytes = productBytes,
                productContentType = PRODUCT_CONTENT_TYPE
            )

            // Decode the response image
            val resultBitmap = decodeBase64ToBitmap(response.imageBase64)

            Result.success(resultBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Encodes the person bitmap to WebP bytes, resizing and compressing to reduce payload.
     */
    private fun encodePersonBitmapToWebp(bitmap: Bitmap): ByteArray {
        val resized = resizeBitmap(bitmap, PERSON_MAX_SIZE)
        val outputStream = ByteArrayOutputStream()
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
        resized.compress(format, PERSON_WEBP_QUALITY, outputStream)
        if (resized !== bitmap) {
            resized.recycle()
        }
        return outputStream.toByteArray()
    }

    /**
     * Encodes the product bitmap to JPEG bytes using the existing quality/resizing.
     */
    private fun encodeProductBitmapToJpeg(bitmap: Bitmap): ByteArray {
        val resized = resizeBitmap(bitmap, PRODUCT_MAX_SIZE)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, PRODUCT_JPEG_QUALITY, outputStream)
        if (resized !== bitmap) {
            resized.recycle()
        }
        return outputStream.toByteArray()
    }

    /**
     * Resizes a bitmap so that its longest side is at most [maxSize] pixels.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        return bitmap.scale(newWidth, newHeight)
    }

    /**
     * Decodes a base64 string to Bitmap.
     */
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private companion object {
        private const val PERSON_MAX_SIZE = 1536
        private const val PERSON_WEBP_QUALITY = 80
        private const val PRODUCT_MAX_SIZE = 1024
        private const val PRODUCT_JPEG_QUALITY = 85
        private const val PERSON_CONTENT_TYPE = "image/webp"
        private const val PRODUCT_CONTENT_TYPE = "image/jpeg"
    }
}
