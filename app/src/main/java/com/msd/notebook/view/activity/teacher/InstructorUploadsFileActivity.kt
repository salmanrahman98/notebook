package com.msd.notebook.view.activity.teacher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.FirebaseStorage
import com.msd.notebook.R
import com.msd.notebook.common.Constants
import com.msd.notebook.common.FileUtils.getFileName
import com.msd.notebook.common.FileUtils.getfileExtension
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.ActivityTeacherUploadsFileBinding
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.view.viewmodels.InstructorFilesViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InstructorUploadsFileActivity : AppCompatActivity() {

    lateinit var binding: ActivityTeacherUploadsFileBinding
    var preferenceClass: PreferenceClass? = null

    private lateinit var viewModel: InstructorFilesViewModel
    private var fileModel: InstructorFiles = InstructorFiles()
    var uri : Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_uploads_file)

        preferenceClass = PreferenceClass(this)
        viewModel = ViewModelProvider(this)[InstructorFilesViewModel::class.java]


        binding = ActivityTeacherUploadsFileBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        binding.dateEt.setText(getCurrentDateFormatted())

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.selectFileBtn.setOnClickListener {
            openingDocumentintent()
        }

        binding.uploadCard.setOnClickListener {
            fileModel.fileName = binding.fileNameEt.text.toString()
            fileModel.fileDescription = binding.descriptionEt.text.toString()
            fileModel.fileSubject = binding.subjectEt.text.toString()
            fileModel.fileDate = binding.dateEt.text.toString()

            fileUpload()
        }
    }

    fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun fileUpload() {
        viewModel.uploadingFile.observe(this) { files ->
//            adapter?.files = files as ArrayList<InstructorFiles>?
            if (files != null) {
//                adapter?.updateFiles(ArrayList(files))
                Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to Upload", Toast.LENGTH_SHORT).show()
            }
        }

        val instructorId = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        viewModel.uploadFile(instructorId!!, fileModel, uri!!, this)
    }

    private fun openingDocumentintent() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.setType("*/*")
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, Constants.DOCUMENT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.DOCUMENT_REQUEST && resultCode == RESULT_OK) {
//            uploadFileToFirebase(data)
            setInfoFromData(data)
        }
    }

    private fun setInfoFromData(data: Intent?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        uri = data!!.data
//        val file = File(uri!!.path)
        val instructorDocId = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        val fileName = getFileName(this, uri!!)
        val reference = storageRef.child(
            instructorDocId + "/" +
                    fileName + "." + getfileExtension(
                uri,
                this
            )
        )
        fileModel.fileExtenstion = getfileExtension(uri, this).toString()
        binding.fileNameEt.setText(fileName)
    }
}