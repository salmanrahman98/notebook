package com.msd.notebook.view.activity

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msd.notebook.databinding.ActivityPdfViewerBinding
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.util.saveTo
import java.io.File


class PdfViewerActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        loadPdf()
    }

    private fun loadPdf() {
//        val pdffile: File = File("")
        val xyzFolder =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/sample.pdf"
            )

        if (xyzFolder.exists()) {

        } else {
            Toast.makeText(this, "File Not found ", Toast.LENGTH_SHORT).show()
        }
    }
}