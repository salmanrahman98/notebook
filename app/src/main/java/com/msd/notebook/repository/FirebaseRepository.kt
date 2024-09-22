package com.msd.notebook.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.msd.notebook.common.Constants
import com.msd.notebook.models.Instructor

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
//        val instructorDetails: MutableMap<String, Any?> = HashMap()
//        instructorDetails[Constants.INSTRUCTOR_ID] = instructorId
//        instructorDetails[Constants.INSTRUCTOR_NAME] = instructorName
//        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
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
                /*.addOnSuccessListener(object : OnSuccessListener<DocumentReference?> {
                    override fun onSuccess(documentReference: DocumentReference?) {
//
                        if(documentReference.doc)
//                        instructorList.clear()
//                        yourInstructorList()
                    }
                })*/.addOnFailureListener {
//                    Toast.makeText(
//                        this@StudentHomeActivity,
//                        "Error, Please try again",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
        }
    }
}