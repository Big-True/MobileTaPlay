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
    val link: String? = null,
    val previews: List<String> = emptyList(),
)

data class TowerPage(
    val code: Int,
    val message: String?,
    val total: Int,
    val page: Int,
    val towers: List<Tower>,
)
