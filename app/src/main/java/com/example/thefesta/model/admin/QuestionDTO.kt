package com.example.thefesta.model.admin

data class QuestionDTO (
    //문의번호
    var questionid :Int= 0,

    //문의내용
    var questioncontent: String? = null,

    //문의일자
    var questiondate: String? = null,

    //문의 갯수
    var questioncount :Int = 0,

    //문의한 회원
    var id: String? = null,

    //문의된 축제id
    var contentid: String? = null,

    //축제이름
    var title: String? = null,

    //축제시작일자
    var eventstartdate: String? = null,

    //축제종료일자
    var eventenddate: String? = null,

    //축제주소
    var addr1: String? = null
)