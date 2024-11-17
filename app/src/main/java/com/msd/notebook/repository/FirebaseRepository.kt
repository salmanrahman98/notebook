package com.msd.notebook.repository

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.msd.notebook.common.Constants
import com.msd.notebook.common.FileUtils.getfileExtension
import com.msd.notebook.models.Instructor
import com.msd.notebook.models.InstructorFiles
import java.io.File

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val storageRef = storage.getReference()


    fun instructorFiles(userDoc: String, callback: (List<InstructorFiles>?) -> Unit) {
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .get()
            .addOnSuccessListener { task ->
                if (!task.isEmpty) {
                    val files = task.toObjects(InstructorFiles::class.java)
                    callback(files)
                } else {
                    callback(null)
                }
//                if (task.isSuccessful) {
//                    for (document in task.result) {
//                        val file = InstructorFiles(
//                            document.id,
//                            document.getData()[Constants.FILE_NAME].toString(),
//                            document.getData()[Constants.FILE_URL].toString(),
//                            document.getData()[Constants.FILE_EXT].toString()
//                        )
//                        filesList.add(file)
//                    }
//                    adapter!!.updateFiles(filesList)
//                } else {
//                    Log.e("HomeFragment", "Error getting documents.", task.exception)
//                }
            }
    }

    fun yourInstructorListFireStore(userDoc: String, callback: (List<Instructor>) -> Unit) {
        if (userDoc != null) {
            db.collection(Constants.STUDENT)
                .document(userDoc)
                .collection(Constants.YOUR_INSTRUCTORS)
                .get()
                .addOnSuccessListener { task ->
                    val teachers = task.toObjects(Instructor::class.java)
                    callback(teachers)
                }
                .addOnFailureListener { exception ->
                    //fail
                    Log.i("InException", " - caught -$exception")
                    callback(emptyList())
                }
        }
    }

    public fun getInstructorFireStore1(instructorId: String?, callback: (Instructor?) -> Unit) {

        db.collection(Constants.INSTRUCTOR)
            .document(instructorId!!)
            .get()
            .addOnSuccessListener { result ->
                if (result != null) {
//                    val instructor = result.documents[0].toObject(Instructor::class.java)
                    callback(
                        Instructor(
                            result[Constants.INSTRUCTOR_ID].toString(),
                            result[Constants.INSTRUCTOR_NAME].toString()
                        )
                    )
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
                callback(null)
            }
    }

    fun addInstructorToStudentListFireStore(
        userDoc: String,
        instructorDetails: MutableMap<String, Any?>,
        callback: (Instructor?) -> Unit
    ) {
        if (userDoc != null) {
            db.collection(Constants.STUDENT)
                .document(userDoc)
                .collection(Constants.YOUR_INSTRUCTORS)
                .add(instructorDetails)
                .addOnSuccessListener { result ->
                    callback(
                        Instructor(
                            instructorDetails[Constants.INSTRUCTOR_ID].toString(),
                            instructorDetails[Constants.INSTRUCTOR_NAME].toString()
                        )
                    )
                }
                .addOnFailureListener {
                    callback(null)
                }
        }
    }

    fun uploadFileToFirebase(
        instructorDocId: String,
        fileInstructorFile: InstructorFiles,
        uri: Uri,
        context: Context,
        callback: (InstructorFiles?) -> Unit
    ) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference = storageRef.child(
            instructorDocId + "/" +
                    fileInstructorFile.fileName + "." + fileInstructorFile.fileExtenstion
        )
        reference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.getDownloadUrl()
                while (!uriTask.isComplete);
                /*val url = uriTask.result
                if (fileInstructorFile.fileName  != null) {
//                    saveFileInFireStoreUploads(fileName, url, fileExtension)
                }*/
                fileInstructorFile.fileUrl = uriTask.result.toString()
                Toast.makeText(context, "File Uploaded", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                callback(fileInstructorFile)
            }
            .addOnProgressListener { snapshot ->
                val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                progressDialog.setMessage("Uploaded: " + progress.toInt() + "%")
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun saveFileInFireStoreUploads(
        userDoc: String,
        fileInstructorFile: InstructorFiles,
        url: Uri,
        context: Context,
        callback: (InstructorFiles?) -> Unit
    ) {
        val file: MutableMap<String?, Any?> = HashMap()
        file[Constants.FILE_NAME] = fileInstructorFile.fileName
        file[Constants.FILE_URL] = fileInstructorFile.fileUrl
        file[Constants.FILE_EXT] = fileInstructorFile.fileExtenstion
        file[Constants.FILE_DESCRIPTION] = fileInstructorFile.fileDescription
        file[Constants.FILE_DATE] = fileInstructorFile.fileDate
        file[Constants.FILE_SUBJECT] = fileInstructorFile.fileSubject
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .add(file)
            .addOnSuccessListener(object : OnSuccessListener<DocumentReference?> {
                override fun onSuccess(documentReference: DocumentReference?) {
//                    Toast.makeText(context, "File Added", Toast.LENGTH_SHORT).show()
//                    filesList.clear()
//                    instructorFiles()
                    callback(fileInstructorFile)
                }
            }).addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error, Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}