package com.msd.notebook.view.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.msd.notebook.R
import com.msd.notebook.common.FileUtils
import com.msd.notebook.databinding.ActivityDocumentBotAiBinding
import com.msd.notebook.models.ChatMessage
import com.msd.notebook.view.adapter.ChatAdapter
import com.msd.notebook.view.viewmodels.GeminiViewModel
import kotlinx.coroutines.launch
import java.io.File

class DocumentBotAI_Activity : AppCompatActivity() {

    lateinit var binding: ActivityDocumentBotAiBinding
    private var selectedPdfUri: Uri? = null

    private val adapter = ChatAdapter()
    private val geminiViewModel: GeminiViewModel by viewModels()
    private val chatMessages = ArrayList<ChatMessage>()
    private var ogTextFromPDF = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDocumentBotAiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.uploadFileImg.setOnClickListener {
            openPdfPicker()
        }

        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.chatRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.chatRecyclerView.setAdapter(adapter)

        geminiObservers()

        binding.buttonSend.setOnClickListener {
            val plainText = binding.editTextMessage.text.toString()
            val chatMessage = ChatMessage(
                plainText,
                true,
                System.currentTimeMillis()
            )
            chatMessages.add(chatMessage)
            adapter.updateMessages(chatMessages)
            binding.editTextMessage.text.clear()

            generateContentFromHistory(plainText)
//            geminiViewModel.generateContent(binding.editTextMessage.text.toString())
        }
    }

    private fun generateContentFromHistory(plainText: String) {
        var history = ""
        history = history + ogTextFromPDF
        for (message in chatMessages) {
            if (message.sentByMe) {
                history += "User: " + message.message + "\n${message.timestamp}"
            } else {
                history += "Bot: " + message.message + "\n" + "${message.timestamp}"
            }
        }
        history += "User: " + plainText + "\n" + System.currentTimeMillis()
        geminiViewModel.generateContent(history)
    }

    private fun geminiObservers() {
        lifecycleScope.launch {
            geminiViewModel.uiState.collect { state ->
                when (state) {
                    is GeminiViewModel.UiState.Initial -> {
                        binding.uploadFileText.text = "Please upload a PDF file to begin"
                    }

                    is GeminiViewModel.UiState.Loading -> {
                        binding.uploadFileText.visibility = View.VISIBLE
                        binding.uploadFileText.text = "Generating..."
                        binding.buttonSend.isEnabled = false
                    }

                    is GeminiViewModel.UiState.Success -> {
                        binding.uploadFileText.visibility = View.GONE
//                        binding.uploadFileText.text = state.content

                        val chatMessage: ChatMessage = ChatMessage(
                            state.content,
                            false,
                            System.currentTimeMillis()
                        )
                        chatMessages.add(chatMessage)
                        adapter.updateMessages(chatMessages)
                        binding.buttonSend.isEnabled = true
                    }

                    is GeminiViewModel.UiState.Error -> {
                        binding.uploadFileText.text = "Error: ${state.message}"
                    }
                }
            }
        }
    }

    private fun openPdfPicker() {
        pickPdfFile.launch("application/pdf")
    }

    private val pickPdfFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedPdfUri = it
            binding.uploadFileText.text = "Selected PDF: ${uri.lastPathSegment}"
//            btnExtractText.isEnabled = true
            val file = FileUtils.getFileFromUriNew(this, selectedPdfUri!!)
            textExtractor(file)
        }
    }

    private fun textExtractor(file: File?) {
        try {
            var parsedText = ""
            val reader: PdfReader = PdfReader(file?.path)
            val n: Int = reader.numberOfPages
            for (i in 0 until n) {
                parsedText = (parsedText + PdfTextExtractor.getTextFromPage(reader, i + 1)
                    .trim()).toString() + "\n" //Extracting the content from the different pages
            }
            println(parsedText)
//            binding.uploadFileText.text = parsedText
            setClipboard(this, parsedText)
            reader.close()
            val prompt = "\n\nAbove is a text from a study material, can you explain everything in detail"
            ogTextFromPDF = parsedText + prompt
            geminiViewModel.generateContent(ogTextFromPDF)
        } catch (e: Exception) {
            println(e)
        }
    }

    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                context.getSystemService(CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboard.text = text
        } else {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}