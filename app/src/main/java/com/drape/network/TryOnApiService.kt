package com.drape.network

import com.drape.data.model.TryOnRequest
import com.drape.data.model.TryOnResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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
     * @param request The try-on request containing base64 encoded images
     * @return The try-on response with the generated image
     */
    @POST("try-on")
    suspend fun tryOnClothing(
        @Header("X-Token") token: String,
        @Body request: TryOnRequest
    ): TryOnResponse
}