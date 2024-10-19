package com.msd.notebook.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.msd.notebook.databinding.ActivitySpeechNotesBinding
import com.msd.notebook.view.viewmodels.SpeechNotesViewModel

class SpeechNotesActivity : AppCompatActivity() {
    private var binding: ActivitySpeechNotesBinding? = null
    private lateinit var viewModel: SpeechNotesViewModel
    private val RECORD_AUDIO_PERMISSION_CODE = 200


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySpeechNotesBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        viewModel = ViewModelProvider(this)[SpeechNotesViewModel::class.java]

        setupButtons()
        observeViewModel()
        checkPermission()
    }

    private fun setupButtons() {
        binding!!.startRecordingBtn.setOnClickListener {
            viewModel.startRecording(this)
        }

        binding!!.stopRecordingBtn.setOnClickListener {
            viewModel.stopRecording()
        }

        binding!!.pausePlayBtn.setOnClickListener {
            viewModel.playPauseAudio()
        }
    }

    private fun observeViewModel() {
        viewModel.recordingState.observe(this) { isRecording ->
            binding!!.startRecordingBtn.isEnabled = !isRecording
            binding!!.stopRecordingBtn.isEnabled = isRecording
        }

        viewModel.playbackState.observe(this) { isPlaying ->
            binding!!.pausePlayBtn.text = if (isPlaying) "Pause" else "Play"
        }

        viewModel.recordingResult.observe(this) { result ->
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
}