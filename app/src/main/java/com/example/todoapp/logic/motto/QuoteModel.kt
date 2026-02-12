package com.example.todoapp.logic.motto

import com.google.gson.annotations.SerializedName

data class QuoteModel(
    @SerializedName("hitokoto")
    val quote: String?,
    val from: String?,
    @SerializedName("from_who")
    val fromWho: String?
)