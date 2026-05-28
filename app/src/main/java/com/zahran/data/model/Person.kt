package com.zahran.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: String = "",
    val parentId: String = "root",
    val name: String = "",
    val fullName: String = "",
    val nickName: String = "",
    val gen: Int = 0,
    val gender: Int = 0,
    val isDocumented: Boolean = true
)
