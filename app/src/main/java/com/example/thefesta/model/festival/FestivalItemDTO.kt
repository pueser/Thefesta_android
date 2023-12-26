package com.example.thefesta.model.festival

data class FestivalItemDTO(
    var contentid: String,
    var title: String,
    var eventstartdate: String,
    var eventenddate: String,
    var addr1: String,
    var eventintro: String,
    var eventtext: String,
    var homepage: String,
    var agelimit: String,
    var sponsor1: String,
    var sponsor1tel: String,
    var sponsor2: String,
    var sponsor2tel: String,
    var usetimefestival: String,
    var playtime: String,
    var firstimage: String,
    var firstimage2: String,
    var acode: Int,
    var scode: Int,
    var mapx: Double,
    var mapy: Double,
    var likeStatus: Boolean = false
)
