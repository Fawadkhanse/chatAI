package com.example.chatai.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * SpeechManager - Handles Speech-to-Text and Text-to-Speech functionality
 */
class SpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    companion object {
        private const val TAG = "SpeechManager"
    }

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    /**
     * Initialize Speech Recognizer
     */
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    _speechState.value = SpeechState.Listening
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                    _speechState.value = SpeechState.Speaking
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Voice level changed - can be used for visual feedback
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                    _speechState.value = SpeechState.Processing
                }

                override fun onError(error: Int) {
                    val errorMessage = getErrorText(error)
                    Log.e(TAG, "Speech recognition error: $errorMessage")
                    _speechState.value = SpeechState.Error(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Recognized: $recognizedText")
                        _recognizedText.value = recognizedText
                        _speechState.value = SpeechState.Success(recognizedText)
                    } else {
                        _speechState.value = SpeechState.Error("No speech recognized")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partialText = matches[0]
                        Log.d(TAG, "Partial: $partialText")
                        _recognizedText.value = partialText
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Reserved for future use
                }
            })
        } else {
            Log.e(TAG, "Speech recognition not available")
            _speechState.value = SpeechState.Error("Speech recognition not available on this device")
        }
    }

    /**
     * Initialize Text to Speech
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isTTSInitialized = true
                    Log.d(TAG, "TTS initialized successfully")
                }

                // Set speech rate and pitch
                textToSpeech?.setPitch(1.0f)
                textToSpeech?.setSpeechRate(1.0f)

                // Set utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        Log.d(TAG, "TTS started")
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        Log.d(TAG, "TTS completed")
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        Log.e(TAG, "TTS error")
                    }
                })
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    /**
     * Start listening for speech
     */
    fun startListening() {
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }

        try {
            _speechState.value = SpeechState.Initializing
            _recognizedText.value = ""
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _speechState.value = SpeechState.Error("Failed to start listening: ${e.message}")
        }
    }

    /**
     * Stop listening for speech
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _speechState.value = SpeechState.Idle
            Log.d(TAG, "Stopped listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    /**
     * Cancel speech recognition
     */
    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _speechState.value = SpeechState.Idle
            _recognizedText.value = ""
            Log.d(TAG, "Cancelled listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling speech recognition", e)
        }
    }

    /**
     * Speak text using Text-to-Speech
     */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isTTSInitialized) {
            Log.e(TAG, "TTS not initialized")
            return
        }

        try {
            // Stop any ongoing speech
            stopSpeaking()

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            Log.d(TAG, "Speaking: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking", e)
            _isSpeaking.value = false
        }
    }

    /**
     * Stop current speech
     */
    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
                _isSpeaking.value = false
                Log.d(TAG, "Stopped speaking")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    /**
     * Check if TTS is currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    /**
     * Set speech rate (0.5 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set speech pitch (0.5 to 2.0)
     */
    fun setSpeechPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Get error message for speech recognition error codes
     */
    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown error"
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            stopListening()
            stopSpeaking()
            speechRecognizer?.destroy()
            textToSpeech?.shutdown()
            speechRecognizer = null
            textToSpeech = null
            isTTSInitialized = false
            Log.d(TAG, "Cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

/**
 * Speech State sealed class
 */
sealed class SpeechState {
    object Idle : SpeechState()
    object Initializing : SpeechState()
    object Listening : SpeechState()
    object Speaking : SpeechState()
    object Processing : SpeechState()
    data class Success(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}