package com.msd.notebook.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.models.Lecture
import com.msd.notebook.repository.FirebaseRepository

class LecturesViewModel: ViewModel() {

    private val repository = FirebaseRepository()
    private val _LecturesList = MutableLiveData<List<Lecture>>()
    val lecturesList: LiveData<List<Lecture>> = _LecturesList

    private val _lectureAdded = MutableLiveData<Boolean>()
    val lectureAdded: LiveData<Boolean> = _lectureAdded

    fun loadLectureList(userDoc: String) {
        repository.getLectures(userDoc) { listOfLectures ->
            _LecturesList.value = listOfLectures
        }
    }

    fun addLecture(lecture: Lecture, userDoc: String) {
        repository.addLecture(lecture, userDoc) { bool ->
            _lectureAdded.value = bool
        }
    }
}