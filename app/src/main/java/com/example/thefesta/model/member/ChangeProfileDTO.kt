package com.example.thefesta.model.member

data class ChangeProfileDTO(

    var fileName: String,
    var uploadPath: String,
    var uuid: String,
    var image: Boolean
)