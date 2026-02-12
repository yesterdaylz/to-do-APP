package com.example.todoapp.logic.motto

import com.example.todoapp.MyApp
import com.example.todoapp.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class Quote(private val repo: Repository = Repository.getInstance(MyApp.context)
) {
    fun quoteLoop(time: Long): Flow<String> = flow {
        while (true) {
            val q = repo.getRandomQuote()
            emit(QuoteFormatter.format(q))
            delay(time)
        }
    }
}