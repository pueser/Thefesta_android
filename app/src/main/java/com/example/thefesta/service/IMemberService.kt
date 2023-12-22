package com.example.thefesta.service

import com.example.thefesta.model.member.MemberDTO
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IMemberService {

    @POST("member/loginPost")
    fun loginPost(
        @Body mDto: MemberDTO,
    ): Call<MemberDTO>

    @GET("member/logout")
    fun logout(
        @Query("id") id: String
    ): Call<String>

    @POST("member/nicknameCheck")
    fun nicknameCheck(
        @Body mDto: MemberDTO,
    ): Call<String>

    @POST("member/idCheck")
    fun idCheck(
        @Field("id") id: String,
    ): Call<String>

    @POST("member/selMember")
    fun selMember(
        @Body mDto: MemberDTO,
    ): Call<MemberDTO>

    @POST("member/mailSend")
    fun mailSend(
        @Body mDto: MemberDTO,
    ): Call<String>

    @POST("member/joinPost")
    fun joinPost(
        @Body mDto: MemberDTO,
    ): Call<Void>

    @POST("member/pwReset")
    fun pwReset(
        @Body mDto: MemberDTO,
    ): Call<Void>

    @POST("member/memInfoReset")
    fun memInfoReset(
        @Body mDto: MemberDTO,
    ): Call<String>

    @Multipart
    @POST("member/changeAjaxAction")
    fun changeAjaxAction(
        @Part file: MultipartBody.Part
    ): Call<String>

    @POST("member/updateState")
    fun updateState(
        @Body mDto: MemberDTO,
    ): Call<Void>

}