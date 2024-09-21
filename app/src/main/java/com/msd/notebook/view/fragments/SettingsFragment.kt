package com.msd.notebook.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.msd.notebook.R
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    var binding: FragmentSettingsBinding? = null
    var preferenceClass: PreferenceClass? = null
    var db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceClass = PreferenceClass(context)
        binding!!.updateCard.setOnClickListener {
            if (binding!!.etPassword.getText().toString() == "") {
                binding!!.etPassword.error = "Please enter new password"
            } else {
                updatePassword()
            }
        }
    }

    private fun updatePassword() {
        val userDoc = preferenceClass!!.getString(Constants.FIRESTORE_DOC_ID)
        db.collection(Constants.INSTRUCTOR)
            .document(userDoc!!)
            .update(Constants.PASSWORD, binding!!.etPassword.getText().toString())
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    context!!.getString(R.string.updated_password_successfully), Toast.LENGTH_SHORT
                ).show()
                binding!!.etPassword.setText("")
            }
    }
}