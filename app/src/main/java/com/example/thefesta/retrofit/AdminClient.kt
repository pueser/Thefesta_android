package com.example.thefesta.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AdminClient {
    private const val BASE_URL = "http://192.168.4.16:9090/"

    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}