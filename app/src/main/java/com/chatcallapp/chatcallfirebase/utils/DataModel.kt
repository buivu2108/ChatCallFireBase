package com.chatcallapp.chatcallfirebase.utils

data class DataModel(
    val target: String,
    val sender: String,
    val data: String?,
    val type: DataModelType
)