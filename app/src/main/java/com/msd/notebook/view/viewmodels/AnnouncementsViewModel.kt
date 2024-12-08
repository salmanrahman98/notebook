package com.msd.notebook.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msd.notebook.models.Announcement
import com.msd.notebook.repository.FirebaseRepository

class AnnouncementsViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val _announcementsList = MutableLiveData<List<Announcement>>()
    val announcementsList: LiveData<List<Announcement>> = _announcementsList

    private val _announcementAdded = MutableLiveData<Boolean>()
    val announcementAdded: LiveData<Boolean> = _announcementAdded

    fun loadAnnouncementsList(userDoc: String) {
        repository.getAnnouncements(userDoc) { listOfAnnouncements ->
            _announcementsList.value = listOfAnnouncements
        }
    }

    fun addAnnouncement(announcement: Announcement, userDoc: String) {
        repository.addAnnouncement(announcement, userDoc) { bool ->
            _announcementAdded.value = bool
        }
    }
}