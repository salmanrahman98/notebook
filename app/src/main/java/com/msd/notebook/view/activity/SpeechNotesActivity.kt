package com.msd.notebook.view.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.msd.notebook.common.SpeechRecognitionService
import com.msd.notebook.databinding.ActivitySpeechNotesBinding
import kotlinx.coroutines.delay


class SpeechNotesActivity : AppCompatActivity() {
    private val TAG = "SpeechNotesActivity"
    private var binding: ActivitySpeechNotesBinding? = null
    private var speechReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity created")
        enableEdgeToEdge()

        binding = ActivitySpeechNotesBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "available", Toast.LENGTH_SHORT
            ).show()
        }else{
            Toast.makeText(
                this,
                "not available", Toast.LENGTH_SHORT
            ).show()
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            val appPackageName = "com.google.android.googlequicksearchbox"
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }

        binding!!.startRecordingBtn.setOnClickListener { v ->
            Log.d(TAG, "Start recording button clicked")
            startSpeechRecognition()
        }
        binding!!.stopRecordingBtn.setOnClickListener { v ->
            Log.d(TAG, "Stop recording button clicked")
            stopSpeechRecognition()
        }

        speechReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                Log.d(TAG, "Broadcast received: ${intent.action}")
                when (intent.action) {
                    "SPEECH_RECOGNIZED" -> {
                        val recognizedText = intent.getStringExtra("text")
                        Log.i(TAG, "Recognized text: $recognizedText")
                        binding!!.speechNotesTv.text = recognizedText
                    }

                    "SPEECH_STATUS" -> {
                        val status = intent.getStringExtra("status")
                        Log.i(TAG, "Speech status: $status")
                        binding!!.statusTv.text = status
                    }
                }
            }
        }

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "available", Toast.LENGTH_SHORT
            ).show()
        }else{
            Toast.makeText(
                this,
                "not available", Toast.LENGTH_SHORT
            ).show()
        }

        checkPermission()
    }

    private fun checkPermission() {
        Log.d(TAG, "Checking RECORD_AUDIO permission")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Requesting RECORD_AUDIO permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            Log.i(TAG, "RECORD_AUDIO permission already granted")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Registering broadcast receiver")
        val filter = IntentFilter()
        filter.addAction("SPEECH_RECOGNIZED")
        filter.addAction("SPEECH_STATUS")
        registerReceiver(speechReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Unregistering broadcast receiver")
        unregisterReceiver(speechReceiver)
    }

    private fun startSpeechRecognition() {
        Log.d(TAG, "Starting speech recognition service")
        val intent = Intent(this, SpeechRecognitionService::class.java)
        intent.action = "START_LISTENING"
        startService(intent)
    }

    private fun stopSpeechRecognition() {
        Log.d(TAG, "Stopping speech recognition service")
        val intent = Intent(this, SpeechRecognitionService::class.java)
        intent.action = "STOP_LISTENING"
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "RECORD_AUDIO permission granted")
            // Permission granted, you can start the service here if needed
        } else {
            Log.w(TAG, "RECORD_AUDIO permission denied")
            // Permission denied, handle this case (e.g., show a message to the user)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity is being destroyed")
        unregisterReceiver(speechReceiver)
        stopService(Intent(this, SpeechRecognitionService::class.java))
    }
}