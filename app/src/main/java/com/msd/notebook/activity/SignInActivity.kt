package com.msd.notebook.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.msd.notebook.R
import com.msd.notebook.common.Constants
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.common.ProgressBarClass
import com.msd.notebook.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    var binding: ActivitySignInBinding? = null
    var signInAs: String? = null
    var preferenceClass: PreferenceClass? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        preferenceClass = PreferenceClass(this)
        signInAs = intent.getStringExtra(Constants.SIGN_IN_AS)
        binding!!.headerTitle.text = signInAs
        binding!!.signInSignUpCard.setOnClickListener {
            if (validate()) {
                ProgressBarClass.instance.showProgress(this@SignInActivity)
                fetchUserDataFromFirebase(signInAs)
            }
        }
    }

    private fun fetchUserDataFromFirebase(signInAs: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection(signInAs!!)
            .get()
            .addOnCompleteListener { task ->
                var isUserFound = false
                if (task.isSuccessful) {
                    val username = binding!!.etUserName.getText().toString()
                    val password = binding!!.etPassword.getText().toString()
                    for (doc in task.result) {
                        val resultName = doc.getString(Constants.NAME)
                        val resultPassword = doc.getString(Constants.PASSWORD)
                        if (username.equals(resultName, ignoreCase = true) &&
                            password.equals(resultPassword, ignoreCase = true)
                        ) {
                            ProgressBarClass.instance.dismissProgress()
                            isUserFound = true
                            preferenceClass!!.putString(Constants.NAME, username)
                            preferenceClass!!.putString(Constants.PASSWORD, password)
                            preferenceClass!!.putString(Constants.FIRESTORE_DOC_ID, doc.id)
                            Toast.makeText(
                                this@SignInActivity, "User Logged in",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (Constants.INSTRUCTOR.equals(signInAs, ignoreCase = true)) {
                                val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent =
                                    Intent(this@SignInActivity, StudentHomeActivity::class.java)
                                startActivity(intent)
                            }
                            break
                        }
                    }
                    if (!isUserFound) {
                        createUserInFirebase(signInAs)
                    }
                    for (i in 1..5) {
                    }
                } else {
                    ProgressBarClass.instance.dismissProgress()
                    Toast.makeText(
                        this@SignInActivity,
                        "Error, Please try again1",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createUserInFirebase(signInAs: String?) {
        val db = FirebaseFirestore.getInstance()
        val user: MutableMap<String, Any> = HashMap()
        user[Constants.NAME] = binding!!.etUserName.getText().toString()
        user[Constants.PASSWORD] = binding!!.etPassword.getText().toString()
        db.collection(signInAs!!)
            .add(user)
            .addOnSuccessListener { documentReference ->
                ProgressBarClass.instance.dismissProgress()
                preferenceClass!!.putString(
                    Constants.NAME,
                    binding!!.etUserName.getText().toString()
                )
                preferenceClass!!.putString(
                    Constants.PASSWORD,
                    binding!!.etPassword.getText().toString()
                )
                preferenceClass!!.putString(Constants.FIRESTORE_DOC_ID, documentReference.id)
                Toast.makeText(
                    this@SignInActivity,
                    "User Created - " + documentReference.id,
                    Toast.LENGTH_SHORT
                ).show()
                if (Constants.INSTRUCTOR.equals(signInAs, ignoreCase = true)) {
                    val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SignInActivity, StudentHomeActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                ProgressBarClass.instance.dismissProgress()
                Toast.makeText(this@SignInActivity, "Error, Please try again", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun validate(): Boolean {
        var validate = true
        if (binding!!.etUserName.getText()!!.length == 0) {
            binding!!.etUserName.error = getString(R.string.txt_please_enter_this_field)
            validate = false
        }
        if (binding!!.etPassword.getText()!!.length == 0) {
            binding!!.etPassword.error = getString(R.string.txt_please_enter_this_field)
            validate = false
        }
        return validate
    }
}