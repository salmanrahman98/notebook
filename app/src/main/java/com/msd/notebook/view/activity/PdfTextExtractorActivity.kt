package com.msd.notebook.view.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.msd.notebook.R
import com.msd.notebook.common.FileUtils
import com.msd.notebook.databinding.ActivityPdfTextExtractorBinding
import java.io.File


class PdfTextExtractorActivity : AppCompatActivity() {
    lateinit var binding: ActivityPdfTextExtractorBinding
    private var selectedPdfUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_text_extractor)

        binding = ActivityPdfTextExtractorBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        binding.btnSelectPdf.setOnClickListener(View.OnClickListener {
//            checkPermissionAndPickPdf()
            openPdfPicker()
        })

        binding.btnExtractText.setOnClickListener(View.OnClickListener {
            val file = FileUtils.getFileFromUriNew(this, selectedPdfUri!!)
            textExtractor(file)
        })
    }

    private fun checkPermissionAndPickPdf() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openPdfPicker()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openPdfPicker()
        } else {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
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
            binding.tvResult.text = "Selected PDF: ${uri.lastPathSegment}"
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
            binding.tvResult.text = parsedText
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