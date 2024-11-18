package com.msd.notebook.models

data class ChatMessage (
    val message: String,
    val sentByMe: Boolean,
    val timestamp: Long
)