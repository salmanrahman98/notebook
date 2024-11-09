package com.msd.notebook.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msd.notebook.repository.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AudioUploadViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    private val _transcriptionResult = MutableLiveData<Result<String>>()
    val transcriptionResult: LiveData<Result<String>> = _transcriptionResult


    fun uploadAudio(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.uploadAudio(file) { result ->
                _uploadResult.postValue(result)
                result.onSuccess { audioUrl ->
                    transcribeAudio(audioUrl)
                }
            }
        }
    }

    private fun transcribeAudio(audioUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.transcribeAudio(audioUrl) { result ->
                _transcriptionResult.postValue(result)
            }
        }
    }
}