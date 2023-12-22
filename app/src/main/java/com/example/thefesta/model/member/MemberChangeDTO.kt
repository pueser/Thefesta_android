package com.example.thefesta.model.member

data class MemberChangeDTO(

    var uuid: String?=null,
    var uploadPath : String?=null,
    var fileName : String?=null,
    var filetype : Boolean?=null,
    var id : String?=null
) {
    constructor(uuid: String, uploadPath: String, fileName: String, filetype: Boolean, id: String)
            : this(uuid, uploadPath, fileName, filetype, null)
}