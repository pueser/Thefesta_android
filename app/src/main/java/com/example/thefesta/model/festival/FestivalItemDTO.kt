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
//{
//    fun copy(likeStatus: Boolean = this.likeStatus): FestivalItemDTO {
//        return FestivalItemDTO(
//            contentid = this.contentid,
//            title = this.title,
//            eventstartdate = this.eventstartdate,
//            eventenddate = this.eventenddate,
//            addr1 = this.addr1,
//            eventintro = this.eventintro,
//            eventtext = this.eventtext,
//            homepage = this.homepage,
//            agelimit = this.agelimit,
//            sponsor1 = this.sponsor1,
//            sponsor1tel = this.sponsor1tel,
//            sponsor2 = this.sponsor2,
//            sponsor2tel = this.sponsor2tel,
//            usetimefestival = this.usetimefestival,
//            playtime = this.playtime,
//            firstimage = this.firstimage,
//            firstimage2 = this.firstimage2,
//            acode = this.acode,
//            scode = this.scode,
//            mapx = this.mapx,
//            mapy = this.mapy,
//            likeStatus = likeStatus
//        )
//    }
//}
