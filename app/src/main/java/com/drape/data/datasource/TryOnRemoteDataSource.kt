package com.drape.data.datasource

import android.util.Log
import com.drape.data.model.TryOnResponse
import com.drape.di.TryOnApiToken
import com.drape.network.TryOnApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for the Virtual Try-On API.
 *
 * Handles communication with the external Try-On service via Retrofit.
 * All methods are suspend functions for coroutine support.
 */
@Singleton
class TryOnRemoteDataSource @Inject constructor(
    private val tryOnApiService: TryOnApiService,
    @TryOnApiToken private val apiToken: String
) {

    /**
     * Performs a virtual try-on by sending a person image and a product image to the API.
     *
     * @param personImageBytes Binary image bytes for the person
     * @param personContentType MIME type for the person image
     * @param productImageBytes Binary image bytes for the product
     * @param productContentType MIME type for the product image
     * @return The try-on response containing the generated image
     * @throws Exception if the API call fails
     */
    suspend fun tryOnClothing(
        personImageBytes: ByteArray,
        personContentType: String,
        productImageBytes: ByteArray,
        productContentType: String
    ): TryOnResponse {
        val personPart = buildPart(
            fieldName = "person_image",
            bytes = personImageBytes,
            contentType = personContentType
        )
        val productPart = buildPart(
            fieldName = "product_image",
            bytes = productImageBytes,
            contentType = productContentType
        )

        try {
            return tryOnApiService.tryOnClothing(
                token = apiToken,
                personImage = personPart,
                productImage = productPart
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("TryOnAPI", "HTTP ${e.code()}: $errorBody")
            throw Exception("Try-On API error ${e.code()}: $errorBody", e)
        }
    }

    private fun buildPart(fieldName: String, bytes: ByteArray, contentType: String): MultipartBody.Part {
        val extension = when {
            contentType.contains("webp", ignoreCase = true) -> "webp"
            contentType.contains("png", ignoreCase = true) -> "png"
            else -> "jpg"
        }
        val filename = "${fieldName}.${extension}"
        val body = bytes.toRequestBody(contentType.toMediaType())
        return MultipartBody.Part.createFormData(fieldName, filename, body)
    }
}
