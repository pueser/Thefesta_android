package com.example.thefesta.retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MemberClient {
    private const val BASE_URL = "http://172.30.1.99:9090/"

    val gson = GsonBuilder().setLenient().create()

    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}