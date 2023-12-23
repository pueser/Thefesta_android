package com.example.thefesta.model.festival

data class FestivalList(
    var list: List<FestivalItemDTO>,
    var pageMaker: PageDTO,
    var areaCode: List<AreacodeDTO>
)
