package com.msd.notebook.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.msd.notebook.common.Constants
import com.msd.notebook.common.Constants.INSTRUCTOR
import com.msd.notebook.common.Constants.LOGGED_IN_AS
import com.msd.notebook.common.Constants.STUDENT
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.FragmentHomeBinding
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.view.activity.DocumentBotAI_Activity
import com.msd.notebook.view.activity.InstructorUploadsFileActivity
import com.msd.notebook.view.adapter.FileAdapter
import com.msd.notebook.view.adapter.FileAdapter.FileBtnClick
import com.msd.notebook.view.viewmodels.InstructorFilesViewModel

class InstructorFilesFragment : Fragment() {
    var binding: FragmentHomeBinding? = null
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var filesList: ArrayList<InstructorFiles> = ArrayList()
    var adapter: FileAdapter? = null
    private lateinit var viewModel: InstructorFilesViewModel
    var instructorId: String? = null

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

        viewModel = ViewModelProvider(this)[InstructorFilesViewModel::class.java]

        preferenceClass = PreferenceClass(context)
        binding!!.addFileFloat.setOnClickListener {
            val intent = Intent(context, InstructorUploadsFileActivity::class.java)
            startActivity(intent)
        }
        adapter = FileAdapter(requireContext()!!, true, object : FileBtnClick {
            override fun btnClick(file: InstructorFiles?) {
                //delete code
                removeProductFromFirestore(file)
            }

            override fun cardClcik(file: InstructorFiles?) {

            }
        })
        val linearLayoutManager = LinearLayoutManager(context)
        binding!!.filesRecyclerView.setLayoutManager(linearLayoutManager)
        binding!!.filesRecyclerView.setAdapter(adapter)

        instructorId = if (preferenceClass?.getString(LOGGED_IN_AS).equals(INSTRUCTOR))
            preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID) else
            preferenceClass!!.getString(Constants.INSTRUCTOR_ID)

        binding!!.aiImg.setOnClickListener {
            val intent = Intent(context, DocumentBotAI_Activity::class.java)
            startActivity(intent)
        }

//        yourFiles()
        instructorFiles()

        if (preferenceClass?.getString(LOGGED_IN_AS).equals(STUDENT))
            binding?.addFileFloat?.visibility = View.GONE else
            binding?.addFileFloat?.visibility = View.VISIBLE
    }

    private fun instructorFiles() {
        viewModel.files.observe(viewLifecycleOwner) { files ->
//            adapter?.files = files as ArrayList<InstructorFiles>?
            if (files != null) {
                adapter?.updateFiles(ArrayList(files))
            } else {
                Toast.makeText(context, "No files available", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadFiles(instructorId!!)
    }

    /*fun yourFiles() {
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
            }
    }*/


    /*private fun uploadFileToFirebase(data: Intent?) {
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
    }*/

    private fun removeProductFromFirestore(file: InstructorFiles?) {
        val userDoc = if (preferenceClass?.getString(LOGGED_IN_AS).equals(INSTRUCTOR))
            preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID) else
            preferenceClass!!.getString(Constants.INSTRUCTOR_ID)
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
                    instructorFiles()
                }
            }).addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error, Please try again", Toast.LENGTH_SHORT
                ).show()
            }
    }

    /*private fun saveFileInFireStoreUploads(fileName: String, url: Uri, ext: String?) {
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
                    instructorFiles()
                }
            }).addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error, Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }*/
}