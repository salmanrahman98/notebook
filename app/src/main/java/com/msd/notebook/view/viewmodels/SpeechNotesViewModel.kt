package com.msd.notebook.view.viewmodels

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

class SpeechNotesViewModel : ViewModel() {

    private val _speechText = MutableLiveData<String>()
    val speechText: LiveData<String> = _speechText

    private val _startSpeechRecognition = MutableLiveData<Unit>()
    val startSpeechRecognition: LiveData<Unit> = _startSpeechRecognition

    fun onRequestSpeechRecognition() {
        _startSpeechRecognition.value = Unit
    }

    fun onSpeechRecognitionResult(result: String) {
        _speechText.value = result
    }
/*

    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }

    fun startSpeechRecognition(activity: ComponentActivity) {
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                val matches: ArrayList<String>? = data?.
                getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (matches!=null && matches.isNotEmpty()){
                    _speechText.value = matches[0]
                }
            }
        }.launch(recognizerIntent)
    }*/


}