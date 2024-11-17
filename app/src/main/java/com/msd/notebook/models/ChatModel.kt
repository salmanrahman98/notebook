package com.msd.notebook.models

data class ChatModel (
    val message: String,
    val sentByMe: Boolean,
    val timestamp: String
)