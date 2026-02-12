package com.example.todoapp.logic

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceCreator {
    private const val BASE_URL = "https://v1.hitokoto.cn/"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)
    inline fun <reified T> create(): T = create(T::class.java)
}