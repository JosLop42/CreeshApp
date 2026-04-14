package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class TranslationResponseData(
    @SerializedName("translatedText") val translatedText: String
)

data class TranslationResponse(
    @SerializedName("responseData") val responseData: TranslationResponseData,
    @SerializedName("responseStatus") val responseStatus: Int
)
