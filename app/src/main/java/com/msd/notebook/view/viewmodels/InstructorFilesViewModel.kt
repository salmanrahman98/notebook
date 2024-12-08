package com.msd.notebook.view.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.repository.FirebaseRepository

class InstructorFilesViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _files = MutableLiveData<List<InstructorFiles>?>()
    val files: LiveData<List<InstructorFiles>?> = _files

    private val _uploadingFile = MutableLiveData<InstructorFiles?>()
    val uploadingFile: LiveData<InstructorFiles?> = _uploadingFile

    fun loadFiles(userDoc: String) {
        repository.instructorFiles(userDoc) { filesValue ->
            _files.value = filesValue
        }
    }

    fun uploadFile(
        userDoc: String,
        file: InstructorFiles,
        uri: Uri,
        context: Context
    ) {
        repository.uploadFileToFirebase(userDoc, file, uri, context) { uploadedFile ->
            repository.saveFileInFireStoreUploads(userDoc, uploadedFile!!, uri, context) { savedFile ->
                _uploadingFile.value = savedFile
            }
        }
    }

}