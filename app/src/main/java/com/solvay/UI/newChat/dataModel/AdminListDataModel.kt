package com.solvay.UI.newChat.dataModel
data class AdminListRequest(
    val offset: Int? = null,
    val limit: Int? = null
)

data class AdminListResponse(
    var `data`: Data? = null,
    var message: String? = null,
    var success: Boolean? = null
)

data class Data(
    var list: ArrayList<Admin>? = null,
    var pagination: Pagination? = null
)

data class Admin(
    var _id: String? = null,
    var createdAt: String? = null,
    var email: String? = null,
    var image: String? = null,
    var isOnline: Boolean? = false,
    var location: List<Double?>? = null,
    var name: String? = null
)

data class Pagination(
    var currentPage: Int? = null,
    var nextPage: Int? = null,
    var prevPage: Int? = null,
    var totalCount: Int? = null
)



data class TranslationRequest(
    val q: List<String>,
    val target: String
)

data class TranslationResponse(
    val data: TranslationData
)

data class TranslationData(
    val translations: List<TranslatedText>
)

data class TranslatedText(
    val translatedText: String
)
