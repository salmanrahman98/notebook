package com.msd.notebook.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.msd.notebook.common.Constants
import com.msd.notebook.common.Constants.INSTRUCTOR
import com.msd.notebook.common.Constants.LOGGED_IN_AS
import com.msd.notebook.common.Constants.STUDENT
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.FragmentAnnouncementsBinding
import com.msd.notebook.view.activity.AddAnnouncementActivity
import com.msd.notebook.view.adapter.AnnouncementAdapter
import com.msd.notebook.view.viewmodels.AnnouncementsViewModel

class AnnouncementsFragment : Fragment() {

    lateinit var binding: FragmentAnnouncementsBinding
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var viewModel: AnnouncementsViewModel? = null
    var adapter: AnnouncementAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceClass = PreferenceClass(context)
        viewModel = ViewModelProvider(this).get(AnnouncementsViewModel::class.java)

        adapter = AnnouncementAdapter(object : AnnouncementAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val announcement = adapter!!.announcements!![position]
                showPopup(announcement.header, announcement.description)
            }
        })
        binding.announcementsRecyclerView.adapter = adapter

        binding.addAnnouncementFloat.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, AddAnnouncementActivity::class.java)
            startActivity(intent)
        })

        yourAnnouncementList()
        if (preferenceClass?.getString(LOGGED_IN_AS).equals(STUDENT))
            binding?.addAnnouncementFloat?.visibility = View.GONE else
            binding?.addAnnouncementFloat?.visibility = View.VISIBLE
    }

    private fun yourAnnouncementList() {
        viewModel?.announcementsList?.observe(viewLifecycleOwner) { announcements ->
            adapter?.updateAnnouncements(ArrayList(announcements))
        }

        val userDoc = if (preferenceClass?.getString(LOGGED_IN_AS).equals(INSTRUCTOR))
            preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID) else
            preferenceClass!!.getString(Constants.INSTRUCTOR_ID)
        viewModel?.loadAnnouncementsList(userDoc!!)
    }

    private fun showPopup(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}