package com.msd.notebook.fragments;

import static android.app.Activity.RESULT_OK;
import static com.msd.notebook.common.Constants.DOCUMENT_REQUEST;
import static com.msd.notebook.common.Constants.FILE_EXT;
import static com.msd.notebook.common.Constants.FILE_NAME;
import static com.msd.notebook.common.Constants.FILE_URL;
import static com.msd.notebook.common.Constants.FIRESTORE_DOC_ID;
import static com.msd.notebook.common.Constants.INSTRUCTOR;
import static com.msd.notebook.common.Constants.YOUR_INSTRUCTORS;
import static com.msd.notebook.common.Constants.YOUR_UPLOADS;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.msd.notebook.adapter.FileAdapter;
import com.msd.notebook.common.PreferenceClass;
import com.msd.notebook.common.ProgressBarClass;
import com.msd.notebook.databinding.FragmentHomeBinding;
import com.msd.notebook.models.InstructorFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorHomeFragment extends Fragment {

    FragmentHomeBinding binding;
    PreferenceClass preferenceClass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<InstructorFiles> filesList = new ArrayList<>();


    FileAdapter adapter;

    public InstructorHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceClass = new PreferenceClass(getContext());

        binding.addFileFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openingDocumentintent();
            }
        });

        adapter = new FileAdapter(getContext(), true, new FileAdapter.FileBtnClick() {
            @Override
            public void btnClick(InstructorFiles file) {
                //delete code
                removeProductFromFirestore(file);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.filesRecyclerView.setLayoutManager(linearLayoutManager);
        binding.filesRecyclerView.setAdapter(adapter);

        getYourFiles();

    }

    private void getYourFiles() {

        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(INSTRUCTOR)
                .document(userDoc)
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

    private void openingDocumentintent() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, DOCUMENT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DOCUMENT_REQUEST && resultCode == RESULT_OK) {
            uploadFileToFirebase(data);
        }
    }

    private void uploadFileToFirebase(Intent data) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri uri = data.getData();
        File file = new File(uri.getPath());

        String instructorDocId = preferenceClass.getString(FIRESTORE_DOC_ID);

        String fileName = System.currentTimeMillis() + "";

        StorageReference reference = storageRef.child(instructorDocId + "/" +
                fileName + "." + getfileExtension(uri));
        reference.putFile(data.getData())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;

                        Uri url = uriTask.getResult();

                        saveFileInFireStoreUploads(fileName, url, getfileExtension(uri));

                        Toast.makeText(getContext(), "File Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded: " + (int) progress + "%");
                    }
                });
    }

    private void removeProductFromFirestore(InstructorFiles file) {
        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(INSTRUCTOR)
                .document(userDoc)
                .collection(YOUR_UPLOADS)
                .document(file.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(),
                                "File Removed ", Toast.LENGTH_SHORT).show();
                        filesList.clear();
                        getYourFiles();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),
                                "Error, Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFileInFireStoreUploads(String fileName, Uri url, String ext) {
        Map<String, Object> file = new HashMap<>();
        file.put(FILE_NAME, fileName);
        file.put(FILE_URL, url);
        file.put(FILE_EXT, ext);

        String userDoc = preferenceClass.getString(FIRESTORE_DOC_ID);

        db.collection(INSTRUCTOR)
                .document(userDoc)
                .collection(YOUR_UPLOADS)
                .add(file)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "File Added", Toast.LENGTH_SHORT).show();

                        filesList.clear();
                        getYourFiles();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error, Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //https://stackoverflow.com/questions/37951869/how-to-get-the-file-extension-in-android
    private String getfileExtension(Uri uri) {
        String extension;
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }
}