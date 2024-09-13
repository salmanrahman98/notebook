package com.msd.notebook.activity;

import static com.msd.notebook.common.Constants.SIGN_IN_AS;
import static com.msd.notebook.common.Constants.STUDENT;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.msd.notebook.common.Constants;
import com.msd.notebook.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.signInCardInstructor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                intent.putExtra(SIGN_IN_AS, Constants.INSTRUCTOR);
                startActivity(intent);
            }
        });

        binding.signInCardStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                intent.putExtra(SIGN_IN_AS, STUDENT);
                startActivity(intent);
            }
        });
    }
}