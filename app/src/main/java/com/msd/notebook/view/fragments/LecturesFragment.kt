package com.msd.notebook.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
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
import com.msd.notebook.databinding.FragmentLecturesBinding
import com.msd.notebook.view.activity.SpeechNotesActivity
import com.msd.notebook.view.adapter.LectureAdapter
import com.msd.notebook.view.viewmodels.LecturesViewModel

class LecturesFragment : Fragment() {

    lateinit var binding: FragmentLecturesBinding
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    var adapter: LectureAdapter? = null
    lateinit var viewModel: LecturesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLecturesBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceClass = PreferenceClass(context)
        viewModel = ViewModelProvider(this).get(LecturesViewModel::class.java)

        adapter = LectureAdapter(requireContext(), object : LectureAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val lecture = adapter!!.lectures!![position]
                showPopup(lecture.header, lecture.notes)
            }
        })

        binding.lecturesRecyclerView.adapter = adapter

        binding.addLectureFloat.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, SpeechNotesActivity::class.java)
            startActivity(intent)
        })

        yourLecturesList()

        if (preferenceClass?.getString(LOGGED_IN_AS).equals(STUDENT))
            binding?.addLectureFloat?.visibility = View.GONE else
            binding?.addLectureFloat?.visibility = View.VISIBLE
    }

    private fun yourLecturesList() {
        viewModel?.lecturesList?.observe(viewLifecycleOwner) { announcements ->
            adapter?.updateLectures(ArrayList(announcements))
        }

        val userDoc = if (preferenceClass?.getString(LOGGED_IN_AS).equals(INSTRUCTOR))
            preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID) else
            preferenceClass!!.getString(Constants.INSTRUCTOR_ID)
        viewModel?.loadLectureList(userDoc!!)
    }

    private fun showPopup(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(styleHeadings(message))
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun styleHeadings(text: String): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()
        val lines = text.lines()

        for (line in lines) {
            val spannableString = SpannableString(line)

            // Check for headings (### or ##)
            if (line.startsWith("### ")) {
                styleHeading(spannableString, 3, 1.2f) // Size increase for ###
            } else if (line.startsWith("## ")) {
                styleHeading(spannableString, 2, 1.3f) // Size increase for ##
            } else if (line.startsWith("# ")) { // Added support for single # heading
                styleHeading(spannableString, 1, 1.4f) // Size increase for #
            }
            // Check for bold text (**)
            val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
            val boldMatches = boldRegex.findAll(line)
            for (match in boldMatches) {
                val startIndex = match.range.first
                val endIndex = match.range.last + 1
                spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, 0)
            }


            spannableStringBuilder.append(spannableString)
            spannableStringBuilder.append("\n")
        }

        return spannableStringBuilder
    }

    private fun styleHeading(
        spannableString: SpannableString,
        hashCount: Int,
        sizeMultiplier: Float
    ) {
        val headingText = spannableString.substring(hashCount + 1) // Remove hashes and space
        spannableString.setSpan(StyleSpan(Typeface.BOLD), hashCount + 1, spannableString.length, 0)
        spannableString.setSpan(
            RelativeSizeSpan(sizeMultiplier),
            hashCount + 1,
            spannableString.length,
            0
        )
    }
}