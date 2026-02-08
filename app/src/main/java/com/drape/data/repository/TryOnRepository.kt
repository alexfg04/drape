package com.drape.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
            // Convert bitmaps to base64
            val personBase64 = encodeBitmapToBase64(personImage)
            val productBase64 = encodeBitmapToBase64(productImage)

            // Call the API
            val response = tryOnRemoteDataSource.tryOnClothing(
                personImageBase64 = personBase64,
                productImageBase64 = productBase64
            )

            // Decode the response image
            val resultBitmap = decodeBase64ToBitmap(response.imageBase64)

            Result.success(resultBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Performs a virtual try-on using base64 encoded images directly.
     *
     * @param personImageBase64 Base64 encoded image of the person
     * @param productImageBase64 Base64 encoded image of the clothing product
     * @return Result containing the try-on response, or an exception on failure
     */
    suspend fun tryOnClothingWithBase64(
        personImageBase64: String,
        productImageBase64: String
    ): Result<TryOnResponse> = withContext(Dispatchers.IO) {
        try {
            val response = tryOnRemoteDataSource.tryOnClothing(
                personImageBase64 = personImageBase64,
                productImageBase64 = productImageBase64
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Encodes a Bitmap to base64 string, resizing and compressing to stay under size limits.
     */
    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val resized = resizeBitmap(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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
}
