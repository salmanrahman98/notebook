package com.msd.notebook.view.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msd.notebook.databinding.ActivityGeminiChatBinding
import com.msd.notebook.view.viewmodels.GeminiViewModel
import kotlinx.coroutines.launch

class GeminiChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityGeminiChatBinding
    private val viewModel: GeminiViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivityGeminiChatBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        binding.generateButton.setOnClickListener {
            val prompt = binding.promptEditText.text.toString()
            viewModel.generateContent(prompt)
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is GeminiViewModel.UiState.Initial -> {
                        binding.resultTextView.text = "Enter a prompt and click generate"
                    }

                    is GeminiViewModel.UiState.Loading -> {
                        binding.resultTextView.text = "Generating..."
                    }

                    is GeminiViewModel.UiState.Success -> {
                        binding.resultTextView.text = state.content
                    }

                    is GeminiViewModel.UiState.Error -> {
                        binding.resultTextView.text = "Error: ${state.message}"
                    }
                }
            }
        }

    }
}