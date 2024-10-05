package com.msd.notebook.view.activity

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.msd.notebook.databinding.ActivitySpeechNotesBinding
import com.msd.notebook.view.viewmodels.SpeechNotesViewModel
import java.util.Locale

class SpeechNotesActivity : AppCompatActivity() {
    private var binding: ActivitySpeechNotesBinding? = null
    private lateinit var viewModel: SpeechNotesViewModel

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private val recognizerIntent = Intent(
        RecognizerIntent
            .ACTION_RECOGNIZE_SPEECH
    ).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Bol Bey")

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySpeechNotesBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        viewModel = ViewModelProvider(this)[SpeechNotesViewModel::class.java]

        /* viewModel.speechText.observe(this) { recognizedText ->
             binding!!.speechNotesTv.text = recognizedText
         }

         binding!!.startRecordingBtn.setOnClickListener {
             viewModel.startSpeechRecognition(this)
         }*/

        viewModel.speechText.observe(this) { recognizedText ->
            binding!!.speechNotesTv.text = recognizedText
        }

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val data: Intent? = result.data
                    val matches: ArrayList<String>? =
                        data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (matches != null && matches.isNotEmpty()) {
                        viewModel.onSpeechRecognitionResult(matches[0])
                    }
                }
            }

        viewModel.startSpeechRecognition.observe(this) {
            startForResult.launch(recognizerIntent)
        }

        binding!!.startRecordingBtn.setOnClickListener {
            viewModel.onRequestSpeechRecognition()
        }


    }
}