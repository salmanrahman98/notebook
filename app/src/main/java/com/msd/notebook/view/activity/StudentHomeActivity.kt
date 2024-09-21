package com.msd.notebook.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.msd.notebook.view.adapter.InstructorAdapter
import com.msd.notebook.view.adapter.InstructorAdapter.InstructorItemClick
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.common.ProgressBarClass
import com.msd.notebook.databinding.ActivityStudentHomeBinding
import com.msd.notebook.models.Instructor

class StudentHomeActivity : AppCompatActivity() {
    var binding: ActivityStudentHomeBinding? = null
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var isExists = false
    var instructorName = ""
    var adapter: InstructorAdapter? = null
    var instructorList: ArrayList<Instructor> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        preferenceClass = PreferenceClass(this@StudentHomeActivity)
        //setting the options for gms barcode
        val options = GmsBarcodeScannerOptions.Builder()
            .enableAutoZoom()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()

        //click listener for opening barcode
        binding!!.addInstructorFloat.setOnClickListener {
            val scanner = GmsBarcodeScanning.getClient(this@StudentHomeActivity, options)
            scanner
                .startScan()
                .addOnSuccessListener { barcode: Barcode -> getInstructor(barcode.rawValue) }
                .addOnCanceledListener {
                    Toast.makeText(
                        this@StudentHomeActivity,
                        "Scan cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e: Exception? ->
                    Toast.makeText(
                        this@StudentHomeActivity,
                        "Error, Please scan again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // instructors
        adapter = InstructorAdapter(this@StudentHomeActivity, object : InstructorItemClick {
            override fun itemClick(instructor: Instructor?) {
                val intent = Intent(this@StudentHomeActivity, InstructorFilesActivity::class.java)
                intent.putExtra(Constants.INSTRUCTOR_ID, instructor?.id)
                intent.putExtra(Constants.INSTRUCTOR_NAME, instructor?.instructorName)
                startActivity(intent)
            }

            override fun itemCardClick(instructor: Instructor?) {
                TODO("Not yet implemented")
            }
        })
        val linearLayoutManager = LinearLayoutManager(this@StudentHomeActivity)
        binding!!.filesRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.filesRecyclerView.setAdapter(adapter)
        yourInstructorList()
    }

    private fun getInstructor(instructorId: String?) {
        isExists = false
        instructorName = ""
        db.collection(Constants.INSTRUCTOR)
            .get()
            .addOnCompleteListener { task ->
                ProgressBarClass.instance.dismissProgress()
                if (task.isSuccessful) {
                    for (document in task.result) {
                        Log.d(MotionEffect.TAG, document.id + " => " + document.getData())
                        if (document.id
                                .equals(instructorId, ignoreCase = true)
                        ) {
                            instructorName = document[Constants.NAME].toString()
                            isExists = true
                        }
                    }
                    if (isExists) {
                        Toast.makeText(
                            this@StudentHomeActivity,
                            "Instructor found",
                            Toast.LENGTH_SHORT
                        ).show()
                        addInstructorToStudentList(instructorId)
                    } else {
                        Toast.makeText(
                            this@StudentHomeActivity,
                            "Instructor not found, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.w(MotionEffect.TAG, "Error getting documents.", task.exception)
                }
            }
    }

    private fun addInstructorToStudentList(instructorId: String?) {
        val file: MutableMap<String, Any?> = HashMap()
        file[Constants.INSTRUCTOR_ID] = instructorId
        file[Constants.INSTRUCTOR_NAME] = instructorName
        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        if (userDoc != null) {
            db.collection(Constants.STUDENT)
                .document(userDoc)
                .collection(Constants.YOUR_INSTRUCTORS)
                .add(file)
                .addOnSuccessListener(object : OnSuccessListener<DocumentReference?> {
                    override fun onSuccess(documentReference: DocumentReference?) {
                        Toast.makeText(this@StudentHomeActivity, "Instructor Added", Toast.LENGTH_SHORT)
                            .show()
                        instructorList.clear()
                        yourInstructorList()
                    }
                }).addOnFailureListener {
                    Toast.makeText(
                        this@StudentHomeActivity,
                        "Error, Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun yourInstructorList(){
            val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
            if (userDoc != null) {
                db.collection(Constants.STUDENT)
                    .document(userDoc)
                    .collection(Constants.YOUR_INSTRUCTORS)
                    .get()
                    .addOnCompleteListener { task ->
                        ProgressBarClass.instance.dismissProgress()
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val instructor = Instructor(
                                    document.getData()[Constants.INSTRUCTOR_ID].toString(),
                                    document.getData()[Constants.INSTRUCTOR_NAME].toString()
                                )
                                instructorList.add(instructor)
                            }
                            adapter!!.updateInstructorList(instructorList)
                        } else {
                            Log.e("HomeFragment", "Error getting documents.", task.exception)
                        }

        //                        if (filesList.isEmpty()) {
        //                            binding.fileText.setText("Your list is empty");
        //                        }
                    }
            }
        }
}