package com.msd.notebook.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.common.Constants
import com.msd.notebook.models.Instructor
import com.msd.notebook.repository.FirebaseRepository

class StudentHomeViewModel : ViewModel() {

    private val repository = FirebaseRepository()
    private val _instructorsList = MutableLiveData<List<Instructor>>()
    val instructorsList: LiveData<List<Instructor>> = _instructorsList

    private val _instructor = MutableLiveData<Instructor>()
    val instructor: LiveData<Instructor> = _instructor

    fun loadInstructorsList(userDoc: String) {
        repository.yourInstructorListFireStore(userDoc) { listOfInstructors ->
            _instructorsList.value = listOfInstructors
        }
    }

    fun fetchInstructor(id: String, userDoc: String) {
        repository.getInstructorFireStore1(id) { instructor: Instructor? ->
            if (instructor != null) {
                addInstructor(userDoc, id, instructor?.instructor_name.toString())
            }else {
                _instructor.value = null
            }
        }
    }

    fun addInstructor(userDoc: String, id: String, name: String) {
        val instructorDetails: MutableMap<String, Any?> = HashMap()
        instructorDetails[Constants.INSTRUCTOR_ID] = id
        instructorDetails[Constants.INSTRUCTOR_NAME] = name
        repository.addInstructorToStudentListFireStore(
            userDoc,
            instructorDetails
        ) { instructor: Instructor? ->
            _instructor.value = instructor
        }
    }
}