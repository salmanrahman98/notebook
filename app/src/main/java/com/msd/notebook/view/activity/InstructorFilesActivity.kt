package com.msd.notebook.view.activity

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            override fun btnClick(file: InstructorFiles?) {
                if (file != null) {
                    downloadFile(file)
                }
            }

        })

        val linearLayoutManager = LinearLayoutManager(this@InstructorFilesActivity)
        binding!!.filesRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.filesRecyclerView.setAdapter(adapter)
        instructorFiles()
    }

    private fun instructorFiles() {
        viewModel.files.observe(this) { files ->
//            adapter?.files = files as ArrayList<InstructorFiles>?
            adapter?.updateFiles(ArrayList(files))
        }

        viewModel.loadFiles(instructorId!!)
    }

    private fun downloadFile(file: InstructorFiles) {
        ProgressBarClass.instance.showProgress(this@InstructorFilesActivity)
        storageReference = FirebaseStorage.getInstance().getReference()
        ref = storageReference!!.child(
            instructorId + "/" + file.fileName + "."
                    + file.fileExtenstion
        )
        ref!!.getDownloadUrl().addOnSuccessListener {
            downloadFile(
                this@InstructorFilesActivity, file.fileName,
                file.fileExtenstion,
                Environment.DIRECTORY_DOWNLOADS,
                file.fileUrl
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
        request.setDestinationInExternalFilesDir(
            context,
            destinationDirectory,
            fileName + fileExtension
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