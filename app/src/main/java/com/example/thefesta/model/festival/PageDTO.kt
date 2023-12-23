package com.example.thefesta.model.festival

data class PageDTO(
    var startPage: Int,
    var endPage: Int,
    var realEnd: Int,
    var prev: Boolean,
    var next: Boolean,
    var total: Int,
    var cri: Criteria
)
