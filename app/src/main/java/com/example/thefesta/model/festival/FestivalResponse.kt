package com.example.thefesta.model.festival

data class FestivalResponse(
    val list: List<FestivalItemDTO>?,
    val fiList: List<FestivalImgItemDTO>?,
    val fDto: FestivalItemDTO?,
    val areaCode: List<AreacodeDTO>,
    val pageMaker: PageDTO
)
