package com.msd.notebook.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyAQYxm4yCfwDWOibbJBQAhOpTfv5ja_tUk"
    )

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun generateContent(prompt: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = generativeModel.generateContent(prompt)
                _uiState.value = UiState.Success(response.text ?: "No response")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val content: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}