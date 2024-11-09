package com.msd.notebook.repository

import java.io.File

class AudioRepository(private val apiService: AssemblyAIService) {
    fun uploadAudio(file: File, callback: (Result<String>) -> Unit) {
        apiService.uploadAudio(file, callback)
    }

    fun transcribeAudio(audioUrl: String, callback: (Result<String>) -> Unit) {
        apiService.transcribeAudio(audioUrl, callback)
    }
}