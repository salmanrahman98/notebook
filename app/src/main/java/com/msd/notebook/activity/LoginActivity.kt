package com.msd.notebook.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.msd.notebook.common.Constants
import com.msd.notebook.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    var binding: ActivityLoginBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        binding!!.signInCardInstructor.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignInActivity::class.java)
            intent.putExtra(Constants.SIGN_IN_AS, Constants.INSTRUCTOR)
            startActivity(intent)
        }
        binding!!.signInCardStudent.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignInActivity::class.java)
            intent.putExtra(Constants.SIGN_IN_AS, Constants.STUDENT)
            startActivity(intent)
        }
    }
}