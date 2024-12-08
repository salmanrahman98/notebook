package com.msd.notebook.view.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.msd.notebook.common.Config.getCurrentDateFormatted
import com.msd.notebook.common.Constants.FIRESTORE_DOC_ID
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.ActivityAddAnnouncementBinding
import com.msd.notebook.models.Announcement
import com.msd.notebook.view.viewmodels.AnnouncementsViewModel

class AddAnnouncementActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddAnnouncementBinding
    var preferenceClass: PreferenceClass? = null
    var announcement: Announcement = Announcement()
    private lateinit var viewModel: AnnouncementsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceClass = PreferenceClass(this)
        viewModel = ViewModelProvider(this)[AnnouncementsViewModel::class.java]

        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dateEt.setText(getCurrentDateFormatted())

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.addAnnouncementBtn.setOnClickListener(View.OnClickListener {
            addAnnouncement()
        })

        observeAnnouncementAdded()
    }

    private fun observeAnnouncementAdded() {
        viewModel.announcementAdded.observe(this) { announce ->
            if (announce) {
                Toast.makeText(this, "Announcement Added", Toast.LENGTH_SHORT).show()

                binding.headerEt.setText("")
                binding.descriptionEt.setText("")
                binding.dateEt.setText(getCurrentDateFormatted())
            } else {
                Toast.makeText(this, "Failed to Add", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun addAnnouncement() {
        val header = binding.headerEt.text.toString()
        val description = binding.descriptionEt.text.toString()
        val date = binding.dateEt.text.toString()

        announcement.header = header
        announcement.description = description
        announcement.date = System.currentTimeMillis()

        var userDoc = preferenceClass!!.getString(FIRESTORE_DOC_ID)

        viewModel.addAnnouncement(announcement, userDoc!!)
    }

}