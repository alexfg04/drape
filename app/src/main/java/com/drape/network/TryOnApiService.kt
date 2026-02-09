package com.drape.network

import com.drape.data.model.TryOnResponse
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit service interface for the Virtual Try-On API.
 *
 * This service handles communication with the external Virtual Try-On API
 * that generates images of clothing items on a person.
 */
interface TryOnApiService {
    
    /**
     * Performs a virtual try-on by sending a person image and a product image.
     *
     * @param token API authentication token (passed via X-Token header)
     * @param personImage The person image as multipart file
     * @param productImage The product image as multipart file
     * @return The try-on response with the generated image
     */
    @Multipart
    @POST("try-on")
    suspend fun tryOnClothing(
        @Header("X-Token") token: String,
        @Part personImage: MultipartBody.Part,
        @Part productImage: MultipartBody.Part
    ): TryOnResponse
}
