package com.chatcallapp.chatcallfirebase.model

data class PushNotification(
    var data: NotificationData,
    var to: String
)