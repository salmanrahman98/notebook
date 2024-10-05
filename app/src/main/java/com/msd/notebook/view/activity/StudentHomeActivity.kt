package com.msd.notebook.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.ActivityStudentHomeBinding
import com.msd.notebook.models.Instructor
import com.msd.notebook.view.adapter.InstructorAdapter
import com.msd.notebook.view.adapter.InstructorAdapter.InstructorItemClick
import com.msd.notebook.view.viewmodels.StudentHomeViewModel

class StudentHomeActivity : AppCompatActivity() {
    var binding: ActivityStudentHomeBinding? = null
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var isExists = false
    var instructorName = ""
    var adapter: InstructorAdapter? = null
    var instructorList: ArrayList<Instructor> = ArrayList()

    private lateinit var viewModel: StudentHomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)

        viewModel = ViewModelProvider(this).get(StudentHomeViewModel::class.java)

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
                .addOnSuccessListener { barcode: Barcode ->
                    barcode.rawValue?.let { it1 ->
                        addInstructor(
                            it1
                        )
                    }
                }
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

        binding!!.speechNotesFloat.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@StudentHomeActivity, SpeechNotesActivity::class.java)
            startActivity(intent)
        })

        // instructors
        adapter = InstructorAdapter(this@StudentHomeActivity, object : InstructorItemClick {
            override fun itemClick(instructor: Instructor?) {
                val intent = Intent(this@StudentHomeActivity, InstructorFilesActivity::class.java)
                intent.putExtra(Constants.INSTRUCTOR_ID, instructor?.instructor_id)
                intent.putExtra(Constants.INSTRUCTOR_NAME, instructor?.instructor_name)
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


    private fun addInstructor(instructorId: String) {
        viewModel.instructor.observe(this) { instructor ->
            //addInstructor
            if (instructor != null) {
                instructorList.clear()
                yourInstructorList()
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.fetchInstructor(
            instructorId,
            preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)!!
        )
    }

    private fun yourInstructorList() {
        viewModel.instructorsList.observe(this) { teachers ->
            adapter?.updateInstructorList(ArrayList(teachers))
        }

        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        viewModel.loadInstructorsList(userDoc!!)
    }


}