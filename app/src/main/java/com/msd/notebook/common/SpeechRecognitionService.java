package com.msd.notebook.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.msd.notebook.R;

import java.util.ArrayList;

public class SpeechRecognitionService extends Service implements RecognitionListener {
    private static final String TAG = "SpeechRecognitionSvc";
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isListening = false;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service created");

        handler = new Handler(Looper.getMainLooper());
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand: Received action: " + action);
        if (action != null) {
            switch (action) {
                case "START_LISTENING":
                    startListening();
                    break;
                case "STOP_LISTENING":
                    stopListening();
                    break;
            }
        }
        return START_STICKY;
    }

    private void startListening() {
        if (!isListening) {
            Log.d(TAG, "startListening: Starting speech recognition");
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
            updateNotification("Listening...");
        }
    }

    private void stopListening() {
        if (isListening) {
            Log.d(TAG, "stopListening: Stopping speech recognition");
            speechRecognizer.stopListening();
            isListening = false;
            updateNotification("Stopped listening");
        }
    }

    private void updateNotification(String status) {
        Log.d(TAG, "updateNotification: Updating notification with status: " + status);
        String CHANNEL_ID = "SpeechRecognitionChannel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.add_svgrepo_com)
                .setContentTitle("Speech Recognition")
                .setContentText(status)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Speech Recognition Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        startForeground(1, builder.build());
    }

    private void restartListening() {
        if (isListening) {
            stopListening();
            handler.postDelayed(this::startListening, 100);
        }
    }

    // ... [keep the existing updateNotification, onBind, sendStatusUpdate, sendRecognizedText, and getErrorText methods] ...

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service is being destroyed");
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech: Ready for speech");
        sendStatusUpdate("Ready for speech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech: Speech started");
        sendStatusUpdate("Speech started");
    }

    @Override
    public void onRmsChanged(float v) {
        // Log.v(TAG, "onRmsChanged: " + v);
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d(TAG, "onBufferReceived: Received " + bytes.length + " bytes");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech: Speech ended");
        sendStatusUpdate("Processing speech...");
    }

    @Override
    public void onError(int error) {
        String errorMessage = "Error occurred: " + getErrorText(error);
        Log.e(TAG, "onError: " + errorMessage);
        sendStatusUpdate(errorMessage);

        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            restartListening();
        } else {
            handler.postDelayed(this::restartListening, 1000);
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        Log.d(TAG, "onResults: Received results");
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String recognizedText = matches.get(0);
            Log.i(TAG, "onResults: Recognized text: " + recognizedText);
            sendRecognizedText(recognizedText);
        }
        restartListening();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults: Received partial results");
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String partialText = matches.get(0);
            Log.i(TAG, "onPartialResults: Partial text: " + partialText);
            sendRecognizedText(partialText);
        }
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG, "onEvent: Event type: " + i);
    }

    private void sendStatusUpdate(String status) {
        Log.d(TAG, "sendStatusUpdate: Sending status update: " + status);
        Intent intent = new Intent("SPEECH_STATUS");
        intent.putExtra("status", status);
        sendBroadcast(intent);
        updateNotification(status);
    }

    private void sendRecognizedText(String text) {
        Log.d(TAG, "sendRecognizedText: Sending recognized text: " + text);
        Intent intent = new Intent("SPEECH_RECOGNIZED");
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}