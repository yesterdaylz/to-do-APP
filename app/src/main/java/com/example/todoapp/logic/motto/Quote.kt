package com.example.todoapp.logic.motto

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class Quote(
    @SerializedName("hitokoto")
    val quote: String,
    val from: String?,
    @SerializedName("from_who")
    val fromWho: String?
)
interface QuoteApi {
    @GET("/")   // baseUrl + "/" => https://v1.hitokoto.cn/?c=i
    suspend fun getQuote(
        @Query("c") categories: List<String> = listOf("i", "d")  // i:诗词,d:文学
        ): Quote
}
