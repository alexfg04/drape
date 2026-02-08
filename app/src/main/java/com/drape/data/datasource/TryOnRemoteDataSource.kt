package com.drape.data.datasource

import android.util.Log
import com.drape.data.model.TryOnRequest
import com.drape.data.model.TryOnResponse
import com.drape.di.TryOnApiToken
import com.drape.network.TryOnApiService
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
     * @param personImageBase64 Base64 encoded image of the person
     * @param productImageBase64 Base64 encoded image of the clothing product
     * @return The try-on response containing the generated image
     * @throws Exception if the API call fails
     */
    suspend fun tryOnClothing(
        personImageBase64: String,
        productImageBase64: String
    ): TryOnResponse {
        val request = TryOnRequest(
            personImageBase64 = personImageBase64,
            productImageBase64 = productImageBase64
        )
        try {
            return tryOnApiService.tryOnClothing(
                token = apiToken,
                request = request
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("TryOnAPI", "HTTP ${e.code()}: $errorBody")
            throw Exception("Try-On API error ${e.code()}: $errorBody", e)
        }
    }
}
