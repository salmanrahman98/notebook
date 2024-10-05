package com.msd.notebook.view.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.common.ProgressBarClass
import com.msd.notebook.databinding.FragmentHomeBinding
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.view.adapter.FileAdapter
import com.msd.notebook.view.adapter.FileAdapter.FileBtnClick
import java.io.File

class InstructorHomeFragment : Fragment() {
    var binding: FragmentHomeBinding? = null
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var filesList: ArrayList<InstructorFiles> = ArrayList()
    var adapter: FileAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceClass = PreferenceClass(context)
        binding!!.addFileFloat.setOnClickListener { openingDocumentintent() }
        adapter = FileAdapter(requireContext()!!, true, object : FileBtnClick {
            override fun btnClick(file: InstructorFiles?) {
                //delete code
                removeProductFromFirestore(file)
            }

            override fun cardClcik(file: InstructorFiles?) {
                TODO("Not yet implemented")
            }
        })
        val linearLayoutManager = LinearLayoutManager(context)
        binding!!.filesRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.filesRecyclerView.setAdapter(adapter)
        yourFiles()
    }

    fun yourFiles() {
        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .get()
            .addOnCompleteListener { task ->
                ProgressBarClass.Companion.instance.dismissProgress()
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
                    adapter!!.updateFiles(filesList)
                } else {
                    Log.e("HomeFragment", "Error getting documents.", task.exception)
                }

//                        if (filesList.isEmpty()) {
//                            binding.fileText.setText("Your list is empty");
//                        }
            }
    }

    private fun openingDocumentintent() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.setType("*/*")
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, Constants.DOCUMENT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK) {
            uploadFileToFirebase(data)
        }
    }

    fun getFileName(context: Context?, uri: Uri): String? {
        var fileName: String? = ""
        if (uri.scheme == "content") {
            val cursor = context?.contentResolver?.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(nameIndex)
                }
            }
        }
        if (fileName == null) {
            // Fallback: extract from the file path if it's not a content Uri
            fileName = uri.path?.substring(uri.path!!.lastIndexOf('/') + 1)
        }
        return fileName
    }

    private fun uploadFileToFirebase(data: Intent?) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val uri = data!!.data
        val file = File(uri!!.path)
        val instructorDocId = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        val fileName = getFileName(context, uri)
        val reference = storageRef.child(
            instructorDocId + "/" +
                    fileName + "." + getfileExtension(uri)
        )
        reference.putFile(data.data!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.getDownloadUrl()
                while (!uriTask.isComplete);
                val url = uriTask.result
                if (fileName != null) {
                    saveFileInFireStoreUploads(fileName, url, getfileExtension(uri))
                }
                Toast.makeText(context, "File Uploaded", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
            .addOnProgressListener { snapshot ->
                val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                progressDialog.setMessage("Uploaded: " + progress.toInt() + "%")
            }
    }

    private fun removeProductFromFirestore(file: InstructorFiles?) {
        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .document(file!!.id)
            .delete()
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(unused: Void?) {
                    Toast.makeText(
                        context,
                        "File Removed ", Toast.LENGTH_SHORT
                    ).show()
                    filesList.clear()
                    yourFiles()
                }
            }).addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error, Please try again", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveFileInFireStoreUploads(fileName: String, url: Uri, ext: String?) {
        val file: MutableMap<String?, Any?> = HashMap()
        file[Constants.FILE_NAME] = fileName
        file[Constants.FILE_URL] = url
        file[Constants.FILE_EXT] = ext
        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .add(file)
            .addOnSuccessListener(object : OnSuccessListener<DocumentReference?> {
                override fun onSuccess(documentReference: DocumentReference?) {
                    Toast.makeText(context, "File Added", Toast.LENGTH_SHORT).show()
                    filesList.clear()
                    yourFiles()
                }
            }).addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error, Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    //https://stackoverflow.com/questions/37951869/how-to-get-the-file-extension-in-android
    private fun getfileExtension(uri: Uri?): String? {
        val extension: String
        val contentResolver = requireContext()!!.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!)).toString()
        return extension
    }
}