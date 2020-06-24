package com.example.pop3server.data.model

data class MailDetail(
    val from: String,
    val to: String,
    val subject: String,
    val date: String,
    val containAttachment: Boolean,
    val body: Any?
)