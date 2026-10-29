package com.mobiletaplay.app.data

data class Tower(
    val id: Int,
    val name: String,
    val title: String,
    val author: String = "",
    val image: String? = null,
    val floors: String? = null,
    val people: String? = null,
    val win: String? = null,
    val rate: String? = null,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val text: String? = null,
    val content: String? = null,
    val link: String? = null,
    val previews: List<String> = emptyList(),
    val commentCount: Int = 0,
    val thumbUp: Int = 0,
    val difficulty: String? = null,
    val uploadTime: String? = null,
    val updateTime: String? = null,
    val recommend: Boolean = false,
    val similarIds: List<Int> = emptyList(),
    // classification
    val section: Int = 0,
    val classify: String? = null,
    val color: Int? = null,
    val competition: Int = 0,
    val difficulties: List<String> = emptyList(),
    val endings: List<String> = emptyList(),
)

data class TowerPage(
    val code: Int,
    val message: String?,
    val total: Int,
    val page: Int,
    val towers: List<Tower>,
)

data class TowerComment(
    val id: Int,
    val authorName: String,
    val authorId: Int = 0,
    val text: String = "",
    val timestamp: String = "",
    val loveNum: Int = 0,
    val replies: List<TowerComment> = emptyList(),
)
