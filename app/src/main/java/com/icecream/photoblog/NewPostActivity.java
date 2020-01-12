package com.icecream.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;

    private Uri postImageUri = null;
    private ProgressBar newPostProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("새로운 포스팅");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostBtn = findViewById(R.id.post_btn);
        newPostProgress = findViewById(R.id.new_post_progress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null) {
                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randomName = FieldValue.serverTimestamp().toString();

                    /////////////
                    StorageReference storageRef = Storage.getRefersenceFromUrl("gs://photoblog-9fc4f.appspot.com").child("post_images/" + randomName);
                    final UploadTask uploadTask;
                    Uri file = null;
                    uploadTask = storageRef.putFile(file);
                    final ProgressDialog progressDialog = new ProgressDialog(NewPostActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
                    progressDialog.setMessage("업로드중...");
                    progressDialog.show();
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.v("알림", "사진 업로드 실패");
                            progressDialog.dismiss();
                            exception.printStackTrace();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Log.v("알림", "사진 업로드 성공 ");
                            progressDialog.dismiss();
                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("image_url", uploadTask.getResult().toString());
                            postMap.put("desc", desc);
                            postMap.put("user_id", current_user_id);
                            //postMap.put("timestamp", randomName);

                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(NewPostActivity.this, "포스트가 게시되었습니다", Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    } else {

                                    }
                                    newPostProgress.setVisibility(View.INVISIBLE);

                                }
                            });
                        }
                    });
                    /////////////////

/*
                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri);
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("image_url", uri);
                            postMap.put("desc", desc);
                            postMap.put("user_id", current_user_id);
                            //postMap.put("timestamp", randomName);

                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(NewPostActivity.this, "포스트가 게시되었습니다", Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    } else {

                                    }
                                    newPostProgress.setVisibility(View.INVISIBLE);

                                }
                            });

                        }
                    });


 */
                } else {
                    Toast.makeText(NewPostActivity.this, "이미지와 모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();

                }

            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
