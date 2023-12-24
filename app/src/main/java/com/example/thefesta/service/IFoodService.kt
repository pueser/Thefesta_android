package com.example.thefesta.service

import com.example.thefesta.model.food.FoodResponse
import com.example.thefesta.model.food.ItemDTO
import com.example.thefesta.model.food.LikeDTO
import com.example.thefesta.model.food.LikeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IFoodService {

    @GET("food/list")
    fun getFoodList(@Query("contentid") contentId: String, @Query("id") id: String ): Call<FoodResponse>

    @GET("food/detail")
    fun getFoodDetail(@Query("contentid") contentId: String): Call<ItemDTO>

    // 좋아요
    @POST("food/likefood")
    fun postLikeFood(@Body likeDto: LikeDTO): Call<Void>

    // 좋아요 취소
    @POST("food/unlikefood")
    fun postUnlikeFood(@Body likeDto: LikeDTO): Call<Void>

    // 회원이 좋아요 누른 음식점 리스트
    @GET("food/userlikelist")
    fun getUserLikeList(@Query("id") id: String): Call<LikeResponse>
}