package com.msd.notebook.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.msd.notebook.common.Config.getCurrentDateFormatted
import com.msd.notebook.common.Config.getFileFromUriNew
import com.msd.notebook.common.Constants.FIRESTORE_DOC_ID
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.common.ProgressBarClass
import com.msd.notebook.databinding.ActivitySpeechNotesBinding
import com.msd.notebook.models.Lecture
import com.msd.notebook.repository.AssemblyAIService
import com.msd.notebook.repository.AudioRepository
import com.msd.notebook.view.viewmodels.AudioUploadViewModel
import com.msd.notebook.view.viewmodels.GeminiViewModel
import com.msd.notebook.view.viewmodels.LecturesViewModel
import com.msd.notebook.view.viewmodels.SpeechNotesViewModel
import kotlinx.coroutines.launch

class SpeechNotesActivity : AppCompatActivity() {
    private var binding: ActivitySpeechNotesBinding? = null
    private lateinit var speechNotesViewModel: SpeechNotesViewModel
    private val RECORD_AUDIO_PERMISSION_CODE = 200

    private lateinit var audioUploadViewModel: AudioUploadViewModel
    private lateinit var lecturesViewModel: LecturesViewModel
    private val PICK_AUDIO_REQUEST = 1

    private val viewModel: GeminiViewModel by viewModels()
    private val progressDialog = ProgressBarClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivitySpeechNotesBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        binding?.toolbar?.setNavigationOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        speechNotesViewModel = ViewModelProvider(this)[SpeechNotesViewModel::class.java]

        lecturesViewModel = ViewModelProvider(this)[LecturesViewModel::class.java]

        // Manual Dependency Injection
        val apiService = AssemblyAIService()
        val repository = AudioRepository(apiService)
        audioUploadViewModel = AudioUploadViewModel(repository)

        binding?.dateEt?.setText(getCurrentDateFormatted())

        binding!!.addLectureBtn.setOnClickListener(View.OnClickListener {
            addLectureToFirebase()
        })

        setupButtons()
        observeRecordingViewModel()
        observeAudioUploadViewModel()
        observeTranscriptionViewModel()
        observeAnnouncementAdded()
        checkPermission()

        geminiTranscriptor()
    }

    private fun observeAnnouncementAdded() {
        lecturesViewModel.lectureAdded.observe(this) { announce ->
            if (announce) {
                Toast.makeText(this, "Lecture Added", Toast.LENGTH_SHORT).show()

                binding?.headerEt?.setText("")
                binding?.descriptionEt?.setText("")
                binding?.dateEt?.setText(getCurrentDateFormatted())
                binding?.speechNotesTv?.setText("")
            } else {
                Toast.makeText(this, "Failed to Add", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun addLectureToFirebase() {
        val header = binding?.headerEt?.text.toString()
        val description = binding?.descriptionEt?.text.toString()
//        val date = binding?.dateEt?.text.toString()
        val notes = binding?.speechNotesTv?.text.toString()

        var lecture = Lecture()
        lecture.header = header
        lecture.description = description
        lecture.date = System.currentTimeMillis()
        lecture.notes = notes

        var userDoc = PreferenceClass.getInstance(this)!!.getString(FIRESTORE_DOC_ID)

        lecturesViewModel.addLecture(lecture, userDoc!!)
    }

    private fun geminiTranscriptor() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is GeminiViewModel.UiState.Initial -> {
//                        binding.resultTextView.text = "Enter a prompt and click generate"
                    }

                    is GeminiViewModel.UiState.Loading -> {
                        binding!!.speechNotesTv.text = "Generating..."
                    }

                    is GeminiViewModel.UiState.Success -> {

                        binding!!.speechNotesTv.text = styleHeadings(state.content)
                        progressDialog.dismissProgress()
                    }

                    is GeminiViewModel.UiState.Error -> {
                        binding!!.speechNotesTv.text = "Error: ${state.message}"
                        progressDialog.dismissProgress()
                    }
                }
            }
        }
    }

    private fun observeTranscriptionViewModel() {
        audioUploadViewModel.transcriptionResult.observe(this) { result ->
            result.fold(
                onSuccess = { transcription ->
                    binding?.speechNotesTv?.text = transcription
                    //copying the text to clipboard
                    val clipboard =
                        getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Copied Text", transcription)
                    clipboard.setPrimaryClip(clip)

                    progressDialog.showProgress(this, "Using AI to generate explanation...")
                    viewModel.generateContent(
                        transcription + "\n\nAbove is audio text from a lecture, " +
                                "Please give me complete explanation of above text."
                    )

                },
                onFailure = { error ->
                    Toast.makeText(
                        this,
                        "Transcription failed: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.dismissProgress()
                }
            )
        }
    }

    private fun observeAudioUploadViewModel() {
        audioUploadViewModel.uploadResult.observe(this) { result ->
            result.fold(
                onSuccess = { uploadUrl ->
                    Toast.makeText(this, "Upload successful: $uploadUrl", Toast.LENGTH_SHORT).show()
                    progressDialog.showProgress(this, "Generating transcription...")
                },
                onFailure = { error ->
                    Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                    progressDialog.dismissProgress()
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

                progressDialog.showProgress(this, "Uploading audio...")
            }
        }
    }

    private fun styleHeadings(text: String): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()
        val lines = text.lines()

        for (line in lines) {
            val spannableString = SpannableString(line)

            // Check for headings (### or ##)
            if (line.startsWith("### ")) {
                styleHeading(spannableString, 3, 1.2f) // Size increase for ###
            } else if (line.startsWith("## ")) {
                styleHeading(spannableString, 2, 1.3f) // Size increase for ##
            } else if (line.startsWith("# ")) { // Added support for single # heading
                styleHeading(spannableString, 1, 1.4f) // Size increase for #
            }
            // Check for bold text (**)
            val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
            val boldMatches = boldRegex.findAll(line)
            for (match in boldMatches) {
                val startIndex = match.range.first
                val endIndex = match.range.last + 1
                spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, 0)
            }


            spannableStringBuilder.append(spannableString)
            spannableStringBuilder.append("\n")
        }

        return spannableStringBuilder
    }

    private fun styleHeading(
        spannableString: SpannableString,
        hashCount: Int,
        sizeMultiplier: Float
    ) {
        val headingText = spannableString.substring(hashCount + 1) // Remove hashes and space
        spannableString.setSpan(StyleSpan(Typeface.BOLD), hashCount + 1, spannableString.length, 0)
        spannableString.setSpan(
            RelativeSizeSpan(sizeMultiplier),
            hashCount + 1,
            spannableString.length,
            0
        )
    }
}