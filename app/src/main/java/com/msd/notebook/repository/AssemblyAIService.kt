package com.msd.notebook.repository

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class AssemblyAIService {
    //    private val client = OkHttpClient()
    private val baseUrl = "https://api.assemblyai.com/v2/"
    private val apiKey = "e7f7f0937acd4a8eadda9eacc6b1aced"

    fun getClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        return client
    }

    fun uploadAudio(file: File, callback: (Result<String>) -> Unit) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseUrl}upload")
            .addHeader("Authorization", apiKey)
            .post(requestBody)
            .build()

        getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val json = JSONObject(responseBody)
                    val uploadUrl = json.getString("upload_url")
                    callback(Result.success(uploadUrl))
                } ?: callback(Result.failure(IOException("Empty response body")))
            }
        })
    }

    fun transcribeAudio(audioUrl: String, callback: (Result<String>) -> Unit) {
        val jsonBody = JSONObject().apply {
            put("audio_url", audioUrl)
        }.toString()

        val request = Request.Builder()
            .url("${baseUrl}transcript")
            .addHeader("Authorization", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Transcription Request Failed", e.toString())
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    Log.d("", "Transcription Response: $responseBody")
                    try {
                        val json = JSONObject(responseBody)
                        val transcriptionId = json.getString("id")
                        pollTranscriptionResult(transcriptionId, callback)
                    } catch (e: Exception) {
                        Log.e("JSON Parsing Error", e.toString())
                        callback(Result.failure(e))
                    }
                } ?: run {
                    Log.e("Empty Response Body", "")
                    callback(Result.failure(IOException("Empty response body")))
                }
            }
        })
    }

    private fun pollTranscriptionResult(
        transcriptionId: String,
        callback: (Result<String>) -> Unit
    ) {
        val request = Request.Builder()
            .url("${baseUrl}transcript/$transcriptionId")
            .addHeader("Authorization", apiKey)
            .get()
            .build()

        getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Polling Failed", e.toString())
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    Log.d("", "Polling Response: $responseBody")
                    try {
                        val json = JSONObject(responseBody)
                        val status = json.getString("status")
                        when (status) {
                            "completed" -> {
                                val text = json.getString("text")
                                callback(Result.success(text))
                            }

                            "error" -> {
                                callback(Result.failure(IOException("Transcription failed")))
                            }

                            else -> {
                                // If still processing, poll again after a delay
                                Thread.sleep(5000) // Wait for 5 seconds
                                pollTranscriptionResult(transcriptionId, callback)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("JSON Parsing Error", e.toString())
                        callback(Result.failure(e))
                    }
                } ?: run {
                    Log.e("Empty Response Body", "")
                    callback(Result.failure(IOException("Empty response body")))
                }
            }
        })
    }
}