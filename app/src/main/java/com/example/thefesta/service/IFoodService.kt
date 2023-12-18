package com.example.thefesta.service

import com.example.thefesta.model.food.FoodResponse
import com.example.thefesta.model.food.ItemDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IFoodService {

    @GET("food/list")
    fun getFoodList(@Query("contentid") contentId: String): Call<FoodResponse>

    @GET("food/detail")
    fun getFoodDetail(@Query("contentid") contentId: String): Call<ItemDTO>
}