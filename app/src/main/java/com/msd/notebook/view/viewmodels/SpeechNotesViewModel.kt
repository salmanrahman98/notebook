package com.msd.notebook.view.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.common.AudioRecordingManager

class SpeechNotesViewModel : ViewModel() {

    private val audioRecorder = AudioRecordingManager()

    private val _recordingState = MutableLiveData<Boolean>()
    val recordingState: LiveData<Boolean> = _recordingState

    private val _actionResult = MutableLiveData<String>()
    val recordingResult: LiveData<String> = _actionResult

    private val _playbackState = MutableLiveData<Boolean>()
    val playbackState: LiveData<Boolean> = _playbackState

    fun startRecording(context: Context) {
        val result = audioRecorder.startRecording(context)
        _recordingState.value = true
        _actionResult.value = result
    }

    fun stopRecording() {
        val result = audioRecorder.stopRecording()
        _recordingState.value = false
        _actionResult.value = result
    }

    fun playPauseAudio() {
        val result = audioRecorder.playPauseAudio()
        _playbackState.value = result.startsWith("Playing") || result.startsWith("Audio resumed")
        _actionResult.value = result
    }

    fun stopPlaying() {
        audioRecorder.stopPlaying()
        _playbackState.value = false
        _actionResult.value = "Playback stopped"
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
    }
}
