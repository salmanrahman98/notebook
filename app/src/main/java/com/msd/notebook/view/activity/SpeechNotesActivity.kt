package com.msd.notebook.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.msd.notebook.common.FileUtils.getFileFromUriNew
import com.msd.notebook.databinding.ActivitySpeechNotesBinding
import com.msd.notebook.repository.AssemblyAIService
import com.msd.notebook.repository.AudioRepository
import com.msd.notebook.view.viewmodels.AudioUploadViewModel
import com.msd.notebook.view.viewmodels.SpeechNotesViewModel

class SpeechNotesActivity : AppCompatActivity() {
    private var binding: ActivitySpeechNotesBinding? = null
    private lateinit var speechNotesViewModel: SpeechNotesViewModel
    private val RECORD_AUDIO_PERMISSION_CODE = 200

    private lateinit var audioUploadViewModel: AudioUploadViewModel
    private val PICK_AUDIO_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivitySpeechNotesBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        speechNotesViewModel = ViewModelProvider(this)[SpeechNotesViewModel::class.java]
//        audioUploadViewModel = ViewModelProvider(this)[AudioUploadViewModel::class.java]

        // Manual Dependency Injection
        val apiService = AssemblyAIService()
        val repository = AudioRepository(apiService)
        audioUploadViewModel = AudioUploadViewModel(repository)

        setupButtons()
        observeRecordingViewModel()
        observeAudioUploadViewModel()
        observeTranscriptionViewModel()
        checkPermission()
    }

    private fun observeTranscriptionViewModel() {
        audioUploadViewModel.transcriptionResult.observe(this) { result ->
            result.fold(
                onSuccess = { transcription ->
                    binding?.speechNotesTv?.text = transcription

                },
                onFailure = { error ->
                    Toast.makeText(
                        this,
                        "Transcription failed: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun observeAudioUploadViewModel() {
        audioUploadViewModel.uploadResult.observe(this) { result ->
            result.fold(
                onSuccess = { uploadUrl ->
                    Toast.makeText(this, "Upload successful: $uploadUrl", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            )
        }
    }

    private fun setupButtons() {
        binding!!.startRecordingBtn.setOnClickListener {
            speechNotesViewModel.startRecording(this)
        }

        binding!!.stopRecordingBtn.setOnClickListener {
            speechNotesViewModel.stopRecording()
        }

        binding!!.pausePlayBtn.setOnClickListener {
            speechNotesViewModel.playPauseAudio()
        }

        binding!!.uploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, PICK_AUDIO_REQUEST)
        }
    }

    private fun observeRecordingViewModel() {
        speechNotesViewModel.recordingState.observe(this) { isRecording ->
            binding!!.startRecordingBtn.isEnabled = !isRecording
            binding!!.stopRecordingBtn.isEnabled = isRecording
        }

        speechNotesViewModel.playbackState.observe(this) { isPlaying ->
            binding!!.pausePlayBtn.text = if (isPlaying) "Pause" else "Play"
        }

        speechNotesViewModel.recordingResult.observe(this) { result ->
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio Recording Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Audio Recording Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                /*   FileUtils.getFileFromUri(this, uri)?.let { file ->
                       audioUploadViewModel.uploadAudio(file)
                   } ?: Toast.makeText(this, "Failed to get file from URI", Toast.LENGTH_SHORT).show()
             */
                val file = getFileFromUriNew(this, uri)
                audioUploadViewModel.uploadAudio(file)
            }
        }
    }
}