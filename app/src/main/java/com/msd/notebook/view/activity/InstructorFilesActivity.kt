package com.msd.notebook.view.activity

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.common.ProgressBarClass
import com.msd.notebook.databinding.ActivityInstructorFilesBinding
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.view.adapter.FileAdapter
import com.msd.notebook.view.viewmodels.InstructorFilesViewModel
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.util.saveTo
import java.io.File


class InstructorFilesActivity : AppCompatActivity() {
    var binding: ActivityInstructorFilesBinding? = null
    var firebaseStorage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var ref: StorageReference? = null
    var adapter: FileAdapter? = null
    var db = FirebaseFirestore.getInstance()
    var preferenceClass: PreferenceClass? = null
    var filesList: MutableList<InstructorFiles> = ArrayList()
    var instructorId: String? = ""
    var instructorName: String? = ""
    var progressBarClass: ProgressBarClass? = null

    private lateinit var viewModel: InstructorFilesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorFilesBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        viewModel = ViewModelProvider(this)[InstructorFilesViewModel::class.java]
        instructorId = intent.getStringExtra(Constants.INSTRUCTOR_ID)
        instructorName = intent.getStringExtra(Constants.INSTRUCTOR_NAME)
        binding!!.headerTitle.text = "Files of Prof. $instructorName"
        preferenceClass = PreferenceClass(this)

        /*
        *   adapter = new FileAdapter(InstructorFilesActivity.this, false, new FileAdapter.FileBtnClick() {
            @Override
            public void btnClick(InstructorFiles file) {
                //student file download
                downloadFile(file);
            }
        });
*/

        adapter = FileAdapter(this, false, object : FileAdapter.FileBtnClick {
            override fun btnClick(instructorFile: InstructorFiles?) {
                if (instructorFile != null) {
                    downloadFile(instructorFile)
                }
            }

            override fun cardClcik(file: InstructorFiles?) {
//                val intent = Intent(this@InstructorFilesActivity, PdfViewerActivity1::class.java)
////                intent.putExtra(Constants.INSTRUCTOR_ID, instructor?.instructor_id)
//                intent.putExtra("fileName", file?.fileName)
//                startActivity(intent)
                val xyzFolder =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString() + "/sample.pdf"
                    )
                PdfViewerActivity.launchPdfFromPath(
                    context = this@InstructorFilesActivity,
                    path = xyzFolder.path,
                    pdfTitle = "Title",
                    saveTo = saveTo.ASK_EVERYTIME,
                    fromAssets = false
                )
            }

        })

        val linearLayoutManager = LinearLayoutManager(this@InstructorFilesActivity)
        binding!!.filesRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.filesRecyclerView.setAdapter(adapter)
        instructorFiles()

        verifyStoragePermissions(this)
    }

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private fun instructorFiles() {
        viewModel.files.observe(this) { files ->
//            adapter?.files = files as ArrayList<InstructorFiles>?
            if (files != null) {
                adapter?.updateFiles(ArrayList(files))
            } else {
                Toast.makeText(this, "No files available", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadFiles(instructorId!!)
    }

    private fun downloadFile(instructorFile: InstructorFiles) {
        ProgressBarClass.instance.showProgress(this@InstructorFilesActivity)
        storageReference = FirebaseStorage.getInstance().getReference()
        ref = storageReference!!.child(
            instructorId + "/" + instructorFile.fileName + "."
                    + instructorFile.fileExtenstion
        )

        var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/Notebook"

        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }

//        path = path + File.separator + instructorFile.fileName

//        val file = File(path)

        ref!!.getDownloadUrl().addOnSuccessListener {
            downloadFile(
                this@InstructorFilesActivity, instructorFile.fileName,
                instructorFile.fileExtenstion,
                path,
                instructorFile.fileUrl
            )
        }.addOnFailureListener {
            Toast.makeText(
                this@InstructorFilesActivity,
                "On Failure",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun downloadFile(
        context: Context,
        fileName: String,
        fileExtension: String,
        destinationDirectory: String?,
        url: String?
    ) {
        val downloadmanager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS, "Notebook/$fileName.$fileExtension"
        )
        downloadmanager.enqueue(request)
        ProgressBarClass.instance.dismissProgress()
        Toast.makeText(this, "File Downloaded", Toast.LENGTH_SHORT).show()
    }

    /* fun instructorFiles() {
         db.collection(Constants.INSTRUCTOR)
             .document(instructorId!!)
             .collection(Constants.YOUR_UPLOADS)
             .get()
             .addOnCompleteListener { task ->
                 ProgressBarClass.instance.dismissProgress()
                 if (task.isSuccessful) {
                     for (document in task.result) {
                         val file = InstructorFiles(
                             document.id,
                             document.getData()[Constants.FILE_NAME].toString(),
                             document.getData()[Constants.FILE_URL].toString(),
                             document.getData()[Constants.FILE_EXT].toString()
                         )
                         filesList.add(file)
                     }
 //                        adapter!!.setFiles(filesList)
                 } else {
                     Log.e("HomeFragment", "Error getting documents.", task.exception)
                 }
             }
     }*/
}