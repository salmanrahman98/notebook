package com.msd.notebook.view.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.msd.notebook.R
import com.msd.notebook.common.FileUtils
import com.msd.notebook.databinding.ActivityDocumentBotAiBinding
import java.io.File

class DocumentBotAI_Activity : AppCompatActivity() {

    lateinit var binding: ActivityDocumentBotAiBinding
    private var selectedPdfUri: Uri? = null


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
            val file = FileUtils.getFileFromUri(this, selectedPdfUri!!)
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
            binding.uploadFileText.text = parsedText
            setClipboard(this, parsedText)
            reader.close()
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