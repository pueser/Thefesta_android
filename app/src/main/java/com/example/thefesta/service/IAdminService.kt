package com.example.thefesta.service

import com.example.thefesta.model.admin.Criteria
import com.example.thefesta.model.admin.QuestionDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.POST
interface IAdminService {
   /* @GET("admin/festaList")
    fun getfestaList(
        @Body cri: Criteria
    ): Call<QuestionDTO>*/
    @FormUrlEncoded
    @GET("admin/festaList")
    fun getfestaList(
        @Field("pageNum") pageNum: Int,
        @Field("amount") amount: Int,
    ): Call<QuestionDTO>


    @POST("admin/questionDelete")
    fun postquestionDelete(
        @Field("questionid") questionid: String,
    ): Call<String>
//    @Headers("Content-Type: application/json")
//    @HTTP(method = "GET", path="/admin/festaList",hasBody = true)
//    fun getfestaList(@Body cri: Criteria): Call<QuestionDTO>
}

