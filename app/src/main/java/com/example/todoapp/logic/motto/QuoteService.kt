package com.example.todoapp.logic.motto

import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("/")
    suspend fun getQuote(
        @Query("c") type: String
    ): QuoteModel
}