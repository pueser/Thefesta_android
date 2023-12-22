package com.example.thefesta.model.member

data class MemberDTO (

    var id: String,
    var nickname: String? = null,
    var password: String? = null,
    var statecode: String? = null,
    var profileImg: String? = null,
    var agreement: String? = null,
    var joindate: String? = null,
    var finalaccess: String? = null,
    var withdrawdate: String? = null,
    var reportnum: Int? = null,
    var updatedate: Long? = null,
    var changeList: MutableList<MemberChangeDTO?>? = null
) {
    constructor(
        id: String,
        nickname: String,
        password: String,
        statecode: String,
        profileImg: String,
        agreement: String,
        joindate: String,
        finalaccess: String,
        reportnum: Int,
        updatedate: Long,
        changeList: MutableList<MemberChangeDTO?>?
    ) : this(id, nickname, password, joindate, profileImg, agreement, joindate, finalaccess, null, reportnum, updatedate, changeList)
}