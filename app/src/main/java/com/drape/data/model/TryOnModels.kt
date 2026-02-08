package com.drape.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request data class for the Virtual Try-On API.
 *
 * @property personImageBase64 Base64 encoded image of the person wearing clothes
 * @property productImageBase64 Base64 encoded image of the clothing product to try on
 */
data class TryOnRequest(
    @SerializedName("person_image_base64")
    val personImageBase64: String,
    
    @SerializedName("product_image_base64")
    val productImageBase64: String
)

/**
 * Response data class for the Virtual Try-On API.
 *
 * @property imageBase64 Base64 encoded resulting image with the product tried on the person
 * @property processingTimeSeconds Time taken to process the request in seconds
 * @property model Model identifier used for the generation
 */
data class TryOnResponse(
    @SerializedName("image_base64")
    val imageBase64: String,
    
    @SerializedName("processing_time_seconds")
    val processingTimeSeconds: Double,
    
    @SerializedName("model")
    val model: String
)