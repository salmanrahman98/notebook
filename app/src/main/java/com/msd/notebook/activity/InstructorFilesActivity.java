package com.msd.notebook.activity;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.msd.notebook.common.Constants.FILE_EXT;
import static com.msd.notebook.common.Constants.FILE_NAME;
import static com.msd.notebook.common.Constants.FILE_URL;
import static com.msd.notebook.common.Constants.INSTRUCTOR;
import static com.msd.notebook.common.Constants.INSTRUCTOR_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR_NAME;
import static com.msd.notebook.common.Constants.YOUR_UPLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.msd.notebook.adapter.FileAdapter;
import com.msd.notebook.common.PreferenceClass;
import com.msd.notebook.common.ProgressBarClass;
import com.msd.notebook.databinding.ActivityInstructorFilesBinding;
import com.msd.notebook.models.InstructorFiles;

import java.util.ArrayList;
import java.util.List;

public class InstructorFilesActivity extends AppCompatActivity {

    ActivityInstructorFilesBinding binding;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference ref;

    FileAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    PreferenceClass preferenceClass;
    List<InstructorFiles> filesList = new ArrayList<>();
    String instructorId = "";
    String instructorName = "";

    ProgressBarClass progressBarClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInstructorFilesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        instructorId = getIntent().getStringExtra(INSTRUCTOR_ID);
        instructorName = getIntent().getStringExtra(INSTRUCTOR_NAME);

        binding.headerTitle.setText("Files of Prof. " + instructorName);

        preferenceClass = new PreferenceClass(this);

        adapter = new FileAdapter(InstructorFilesActivity.this, false, new FileAdapter.FileBtnClick() {
            @Override
            public void btnClick(InstructorFiles file) {
                //student file download
                downloadFile(file);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InstructorFilesActivity.this);
        binding.filesRecyclerView.setLayoutManager(linearLayoutManager);
        binding.filesRecyclerView.setAdapter(adapter);

        getInstructorFiles();

    }

    private void downloadFile(InstructorFiles file) {
        ProgressBarClass.getInstance().showProgress(InstructorFilesActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        ref = storageReference.child(instructorId + "/" + file.getFileName() + "."
                + file.getFileExtenstion());

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(InstructorFilesActivity.this, file.getFileName(),
                        file.getFileExtenstion(),
                        DIRECTORY_DOWNLOADS,
                        file.getFileUrl()
                );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InstructorFilesActivity.this, "On Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);
        downloadmanager.enqueue(request);
        ProgressBarClass.getInstance().dismissProgress();
        Toast.makeText(this, "File Downloaded", Toast.LENGTH_SHORT).show();
    }

    private void getInstructorFiles() {
        db.collection(INSTRUCTOR)
                .document(instructorId)
                .collection(YOUR_UPLOADS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ProgressBarClass.getInstance().dismissProgress();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                InstructorFiles file = new InstructorFiles(document.getId(),
                                        document.getData().get(FILE_NAME).toString(),
                                        document.getData().get(FILE_URL).toString(),
                                        document.getData().get(FILE_EXT).toString()
                                );
                                filesList.add(file);
                            }
                            adapter.setFiles(filesList);
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