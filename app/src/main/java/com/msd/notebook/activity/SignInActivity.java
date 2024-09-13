package com.msd.notebook.activity;

import static com.msd.notebook.common.Constants.FIRESTORE_DOC_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR;
import static com.msd.notebook.common.Constants.NAME;
import static com.msd.notebook.common.Constants.PASSWORD;
import static com.msd.notebook.common.Constants.SIGN_IN_AS;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.msd.notebook.R;
import com.msd.notebook.common.PreferenceClass;
import com.msd.notebook.common.ProgressBarClass;
import com.msd.notebook.databinding.ActivitySignInBinding;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    String signInAs;

    PreferenceClass preferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        preferenceClass = new PreferenceClass(this);

        signInAs = getIntent().getStringExtra(SIGN_IN_AS);

        binding.headerTitle.setText(signInAs);

        binding.signInSignUpCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    ProgressBarClass.getInstance().showProgress(SignInActivity.this);
                    fetchUserDataFromFirebase(signInAs);
                }
            }
        });

    }

    private void fetchUserDataFromFirebase(String signInAs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(signInAs)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isUserFound = false;
                        if (task.isSuccessful()) {
                            String username = binding.etUserName.getText().toString();
                            String password = binding.etPassword.getText().toString();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String resultName = doc.getString(NAME);
                                String resultPassword = doc.getString(PASSWORD);
                                if (username.equalsIgnoreCase(resultName) &&
                                        password.equalsIgnoreCase(resultPassword)) {
                                    ProgressBarClass.getInstance().dismissProgress();
                                    isUserFound = true;
                                    preferenceClass.putString(NAME, username);
                                    preferenceClass.putString(PASSWORD, password);
                                    preferenceClass.putString(FIRESTORE_DOC_ID, doc.getId());
                                    Toast.makeText(SignInActivity.this, "User Logged in",
                                            Toast.LENGTH_SHORT).show();
                                    if (INSTRUCTOR.equalsIgnoreCase(signInAs)) {
                                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(SignInActivity.this, StudentHomeActivity.class);
                                        startActivity(intent);
                                    }
                                    break;
                                }
                            }
                            if (!isUserFound) {
                                createUserInFirebase(signInAs);

                            }
                            for (int i=1;i<=5;i++){

                            }

                        } else {
                            ProgressBarClass.getInstance().dismissProgress();
                            Toast.makeText(SignInActivity.this, "Error, Please try again1", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createUserInFirebase(String signInAs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put(NAME, binding.etUserName.getText().toString());
        user.put(PASSWORD, binding.etPassword.getText().toString());

        db.collection(signInAs)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        ProgressBarClass.getInstance().dismissProgress();
                        preferenceClass.putString(NAME, binding.etUserName.getText().toString());
                        preferenceClass.putString(PASSWORD, binding.etPassword.getText().toString());
                        preferenceClass.putString(FIRESTORE_DOC_ID, documentReference.getId());
                        Toast.makeText(SignInActivity.this, "User Created - " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                        if (INSTRUCTOR.equalsIgnoreCase(signInAs)) {
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(SignInActivity.this, StudentHomeActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ProgressBarClass.getInstance().dismissProgress();
                        Toast.makeText(SignInActivity.this, "Error, Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validate() {
        boolean validate = true;
        if (binding.etUserName.getText().length() == 0) {
            binding.etUserName.setError(getString(R.string.txt_please_enter_this_field));
            validate = false;
        }
        if (binding.etPassword.getText().length() == 0) {
            binding.etPassword.setError(getString(R.string.txt_please_enter_this_field));
            validate = false;
        }
        return validate;
    }
}