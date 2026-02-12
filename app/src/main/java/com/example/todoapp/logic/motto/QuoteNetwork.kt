package com.example.todoapp.logic.motto

import com.example.todoapp.logic.ServiceCreator
import kotlin.random.Random

object QuoteNetwork {

    private val quoteService: QuoteService = ServiceCreator.create<QuoteService>()
    private val types = listOf("d", "i") // d=文学, i=诗词

    suspend fun randomType(): QuoteModel {
        val randomType = types[Random.nextInt(types.size)]
        return quoteService.getQuote(type = randomType)
    }
}