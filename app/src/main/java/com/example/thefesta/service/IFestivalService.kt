package com.example.thefesta.service

import com.example.thefesta.model.festival.FestivalLikeListResponse
import com.example.thefesta.model.festival.FestivalReplyDTO
import com.example.thefesta.model.festival.FestivalReplyReportDTO
import com.example.thefesta.model.festival.FestivalReplyResponse
import com.example.thefesta.model.festival.FestivalResponse
import com.example.thefesta.model.festival.LikeDTO
import com.example.thefesta.model.member.MemberDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IFestivalService {
    // 회원
    @POST("member/selMember")
    fun selMember(@Body mDto: MemberDTO): Call<MemberDTO>

    // 축제
    @GET("festival/list")
    fun getFestivalList(@Query("pageNum") pageNum: Int, @Query("amount") amount: Int, @Query("keyword") keyword: String? = null): Call<FestivalResponse>

    @GET("festival/detail/{contentid}")
    fun getFesivalDetail(@Path("contentid") contentid: String): Call<FestivalResponse>

    // 댓글
    @GET("festival/reply")
    fun getReplyList(@Query("page") page: Int, @Query("contentid") contentid: String): Call<FestivalReplyResponse>

    @POST("festival/insertReply")
    fun insertReply(@Body festivalReplyDto: FestivalReplyDTO): Call<String>

    @PATCH("festival/reply/modify")
    fun replyModify(@Body festivalReplyDto: FestivalReplyDTO, @Query("_method") method: String = "PATCH"): Call<String>

    @PATCH("festival/reply/delete")
    fun replyDelete(@Query("frno") frno: Int): Call<String>

    @POST("admin/festaReplyReport")
    fun festaReplyReport(@Body festivalReplyReportDTO: FestivalReplyReportDTO): Call<Unit>

    // 좋아요
    @POST("festival/likeInsert")
    fun likeInsert(@Body likeDto: LikeDTO): Call<String>

    @POST("festival/likeDelete")
    fun likeDelete(@Body likeDto: LikeDTO): Call<String>

    @GET("festival/likeSearch")
    fun likeSearch(@Query("contentid") contentid: String, @Query("id") id: String): Call<Int>

    @GET("festival/likeList")
    fun getLikeList(@Query("page") page: Int, @Query("id") id: String): Call<FestivalLikeListResponse>
}