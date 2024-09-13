package com.msd.notebook.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.msd.notebook.common.Constants.FIRESTORE_DOC_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR_NAME;
import static com.msd.notebook.common.Constants.NAME;
import static com.msd.notebook.common.Constants.STUDENT;
import static com.msd.notebook.common.Constants.YOUR_INSTRUCTORS;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.msd.notebook.adapter.InstructorAdapter;
import com.msd.notebook.common.Constants;
import com.msd.notebook.common.PreferenceClass;
import com.msd.notebook.common.ProgressBarClass;
import com.msd.notebook.databinding.ActivityStudentHomeBinding;
import com.msd.notebook.models.Instructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentHomeActivity extends AppCompatActivity {

    ActivityStudentHomeBinding binding;
    PreferenceClass preferenceClass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean isExists = false;
    String instructorName = "";

    InstructorAdapter adapter;

    List<Instructor> instructorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        preferenceClass = new PreferenceClass(StudentHomeActivity.this);
        //setting the options for gms barcode
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .enableAutoZoom()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                .build();

        //click listener for opening barcode
        binding.addInstructorFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(StudentHomeActivity.this, options);
                scanner
                        .startScan()
                        .addOnSuccessListener(
                                barcode -> {
                                    getInstructor(barcode.getRawValue());
                                })
                        .addOnCanceledListener(
                                () -> {
                                    Toast.makeText(StudentHomeActivity.this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                                })
                        .addOnFailureListener(
                                e -> {
                                    Toast.makeText(StudentHomeActivity.this, "Error, Please scan again", Toast.LENGTH_SHORT).show();
                                });
            }
        });

        // instructors
        adapter = new InstructorAdapter(StudentHomeActivity.this, new InstructorAdapter.InstructorItemClick() {
            @Override
            public void itemClick(Instructor instructor) {
                Intent intent = new Intent(StudentHomeActivity.this, InstructorFilesActivity.class);
                intent.putExtra(INSTRUCTOR_ID, instructor.getId());
                intent.putExtra(INSTRUCTOR_NAME, instructor.getInstructorName());
                startActivity(intent);
            }

            @Override
            public void itemCardClick(Instructor instructor) {
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StudentHomeActivity.this);
        binding.filesRecyclerView.setLayoutManager(linearLayoutManager);
        binding.filesRecyclerView.setAdapter(adapter);

        getYourInstructorList();
    }


    private void getInstructor(String instructorId) {

        isExists = false;
        instructorName = "";

        db.collection(Constants.INSTRUCTOR)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override

                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ProgressBarClass.getInstance().dismissProgress();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.getId()
                                        .equalsIgnoreCase(instructorId)) {
                                    instructorName = document.get(NAME).toString();
                                    isExists = true;
                                }
                            }

                            if (isExists) {
                                Toast.makeText(StudentHomeActivity.this, "Instructor found", Toast.LENGTH_SHORT).show();
                                addInstructorToStudentList(instructorId);
                            } else {
                                Toast.makeText(StudentHomeActivity.this, "Instructor not found, please try again", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void addInstructorToStudentList(String instructorId) {
        Map<String, Object> file = new HashMap<>();
        file.put(INSTRUCTOR_ID, instructorId);
        file.put(INSTRUCTOR_NAME, instructorName);

        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(STUDENT)
                .document(userDoc)
                .collection(YOUR_INSTRUCTORS)
                .add(file)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(StudentHomeActivity.this, "Instructor Added", Toast.LENGTH_SHORT).show();
                        instructorList.clear();
                        getYourInstructorList();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StudentHomeActivity.this, "Error, Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getYourInstructorList() {
        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(STUDENT)
                .document(userDoc)
                .collection(YOUR_INSTRUCTORS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ProgressBarClass.getInstance().dismissProgress();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Instructor instructor = new Instructor(document.getData().get(INSTRUCTOR_ID).toString(),
                                        document.getData().get(INSTRUCTOR_NAME).toString()
                                );
                                instructorList.add(instructor);
                            }
                            adapter.setInstructorList(instructorList);
                        } else {
                            Log.e("HomeFragment", "Error getting documents.", task.getException());
                        }

//                        if (filesList.isEmpty()) {
//                            binding.fileText.setText("Your list is empty");
//                        }
                    }
                });
    }
}