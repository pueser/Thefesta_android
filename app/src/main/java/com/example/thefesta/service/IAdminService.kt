package com.example.thefesta.service

import com.example.thefesta.model.admin.QuestionDTO
import com.example.thefesta.model.member.MemberDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IAdminService {

    /*축제 리스트 받아오기*/
    @GET("admin/festaList")
    fun getfestaList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int,
    ): Call<Map<String, Object>>

    /*축제 갯수 가져오기*/
    @GET("admin/festaListCnt")
    fun getfestaListAmount(

    ):Call<Int>

    /*축제건의 갯수 가져오기*/
    @GET("admin/questionListCnt")
    fun getquestionListAmount(
        @Query("contentid") contentid: String
    ):Call<Int>

    /*축제 건의내용 list*/
    @GET("admin/questionList")
    fun getquestionList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int,
        @Query("contentid") contentid: String,
    ): Call<Map<String, Any>>

    //축제 건의 삭제
    @POST("admin/questionDelete")
    fun postQuestionDelete(
        @Query("questionid") questionid: String
    ): Call<String>

    //축제 삭제
    @POST("admin/festaDelete")
    fun postFestaDelete(
        @Query("contentid") contentid: String
    ): Call<Int>




    //게시판 list
    @GET("admin/boardlist")
    fun getBoardList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int
    ): Call<Map<String, Any>>

    //게시판 List 갯수
    @GET("admin/boardListCnt")
    fun getBoardListCnt(

    ): Call<Int>

    //게시판 삭제
    @POST("board/remove")
    fun postBoardDelete(
        @Query("bid") bid: Int
    ): Call<ResponseBody>




    //신고 List
    @GET("admin/reportList")
    fun adminReportList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int
    ): Call<Map<String, Any>>

    //신고 List 갯수
    @GET("admin/reportListCnt")
    fun getReportListCnt(

    ): Call<Int>

    //신고 승인버튼 클릭
    @POST("admin/reportstateChange")
    fun postReportstateChange(
        @Query("reportid") reportid: Int
    ): Call<Int>

    //신고 반려버튼 클릭
    @POST("admin/memberReportDelete")
    fun postMemberReportDelete(
        @Query("reportid") reportid: Int
    ): Call<Int>






    //신고 List
    @GET("admin/memberList")
    fun adminMemberList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int
    ): Call<Map<String, Any>>

    //회원 총원
    @GET("admin/memberListCnt")
    fun getMemberListCnt(

    ): Call<Int>

    //회원 Detail List 갯수
    @GET("admin/memberDetailCnt")
    fun getMemberDetailCnt(
        @Query("id") id: String,
    ): Call<Int>

    //회원Detail List
    @GET("admin/memberDetail")
    fun getMemberDetailList(
        @Query("id") id: String,
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int
    ): Call<Map<String, Any>>


    //회원Detail List 승인버튼
    @GET("admin/memberReportnumRead")
    fun postMemberReportnumRead(
        @Query("reportid") reportid: Int,
        @Query("id") id: String
    ): Call<Int>


    //승인 버튼 클릭시(신고누적갯수 count)
    @POST("admin/memberReportnumCnt")
    fun postMemberReportnumCnt(
        @Query("reportid") reportid: Int,
        @Query("id") id: String
    ): Call<Int>

    //회원Detail 저장버튼 클릭
    @POST("member/updateState")
    fun postUpdateState(
        @Body mDto: MemberDTO
    ): Call<Void>







    //문의사항 list갯수
    @GET("admin/adminQuestionListCnt")
    fun getAdminQuestionListCnt(

    ): Call<Int>

    //문의사항 list
    @GET("admin/adminQuestionList")
    fun getAdminQuestionList(
        @Query("pageNum") pageNum: Int,
        @Query("amount") amount: Int
    ): Call<Map<String, Any>>

    //문의사항 답변
//    @POST("replies/new")
//    fun postReplies(
//        @Body replyDto: ReplyDTO
//    ): Call<ResponseBody>

    //문의사항 완료
    @POST("admin/adminQuestionbstatecodeChange")
    fun postAdminQuestionbstatecodeChange(
        @Query("bid") bid: Int
    ): Call<Void>

    //회원 닉네임 취득
    @GET("admin/memberNickName")
    fun getMemberNickName(
        @Query("id") id: String
    ): Call<MemberDTO>





    //축제건의사항 전송
    @POST("admin/questionRegister")
    fun postQuestionRegister(
        @Body questionDto: QuestionDTO
    ): Call<Void>
}

