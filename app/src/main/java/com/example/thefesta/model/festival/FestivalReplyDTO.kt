package com.example.thefesta.model.festival

import java.sql.Timestamp

data class FestivalReplyDTO(
    var frno: Int,
    var contentid: String,
    var id: String,
    var nickname: String,
    var frcontent: String,
    var profileImg: String,
    var frregist: String? = null,
    var fredit: String? = null
)
