package com.msd.notebook.repository

import com.google.ai.client.generativeai.GenerativeModel

class GeminiRepository(private val apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    suspend fun generateContent(prompt: String): String {
        val response = generativeModel.generateContent(prompt)
        return response.text ?: "No response"
    }
}