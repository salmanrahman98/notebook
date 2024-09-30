package com.msd.notebook.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.models.InstructorFiles
import com.msd.notebook.repository.FirebaseRepository

class InstructorFilesViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _files = MutableLiveData<List<InstructorFiles>>()
    val files: LiveData<List<InstructorFiles>> = _files

    fun loadFiles(userDoc: String) {
        repository.instructorFiles(userDoc) { files ->
            _files.value = files
        }
    }

}