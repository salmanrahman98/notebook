package com.msd.notebook.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.msd.notebook.common.Constants
import com.msd.notebook.models.Instructor
import com.msd.notebook.models.InstructorFiles

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun instructorFiles(userDoc: String, callback: (List<InstructorFiles>?) -> Unit) {
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .collection(Constants.YOUR_UPLOADS)
            .get()
            .addOnSuccessListener { task ->
                if(!task.isEmpty) {
                    val files = task.toObjects(InstructorFiles::class.java)
                    callback(files)
                }else {
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
            .whereEqualTo("id", instructorId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val instructor = result.documents[0].toObject(Instructor::class.java)
                    callback(instructor)
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

                }
        }
    }


}