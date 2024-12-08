package com.msd.notebook.common

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordingManager {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: String? = null
    private var isPlaying = false

    fun startRecording(context: Context): String {
        if (mediaRecorder != null) {
            return "Already recording"
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        val fileName = "AudioRecording_$currentDateTime.mp3"

        val notebookLecturesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "NotebookLectures")
        if (!notebookLecturesDir.exists()) {
            notebookLecturesDir.mkdirs()
        }

        outputFile = File(notebookLecturesDir, fileName).absolutePath

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            return "Recording started"
        } catch (e: IOException) {
            return "Failed to start recording: ${e.message}"
        }
    }

    fun stopRecording(): String {
        if (mediaRecorder == null) {
            return "Not recording"
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            return "Recording saved: $outputFile"
        } catch (e: Exception) {
            return "Failed to stop recording: ${e.message}"
        }
    }

    fun playPauseAudio(): String {
        outputFile?.let { file ->
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file)
                    prepare()
                    start()
                }
                isPlaying = true
                return "Playing audio"
            } else {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                    return "Audio paused"
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    return "Audio resumed"
                }
            }
        }
        return "No audio file to play"
    }

    fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        stopPlaying()
    }

}