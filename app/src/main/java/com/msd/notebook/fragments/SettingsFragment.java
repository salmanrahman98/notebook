package com.msd.notebook.fragments;

import static com.msd.notebook.common.Constants.FIRESTORE_DOC_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR;
import static com.msd.notebook.common.Constants.PASSWORD;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.msd.notebook.R;
import com.msd.notebook.common.PreferenceClass;
import com.msd.notebook.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;
    PreferenceClass preferenceClass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceClass = new PreferenceClass(getContext());

        binding.updateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.etPassword.getText().toString().equals("")) {
                    binding.etPassword.setError("Please enter new password");
                } else {
                    updatePassword();
                }
            }
        });
    }

    private void updatePassword() {
        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(INSTRUCTOR)
                .document(userDoc)
                .update(PASSWORD, binding.etPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(),
                                getContext().getString(R.string.updated_password_successfully), Toast.LENGTH_SHORT).show();
                        binding.etPassword.setText("");
                    }
                });
    }
}