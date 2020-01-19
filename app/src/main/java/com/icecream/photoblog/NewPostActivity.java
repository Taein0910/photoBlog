package com.icecream.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
import io.grpc.Context;

import static com.google.firebase.firestore.FieldValue.*;
import static io.opencensus.tags.TagValue.MAX_LENGTH;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;

    private Uri postImageUri = null;
    private ProgressBar newPostProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;

    private String current_user_id;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseStorage storage = FirebaseStorage.getInstance();

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
                Log.e("debug","onclick");

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null) {
                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randomName = random();






 /*
 StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://photoblog-9fc4f.appspot.com");
 storageReference.child("posts").child(randomName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

 @Override
 public void onSuccess(Uri uri) {

 */

                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postImageUri);
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(final Uri uri) {
                            Toast.makeText(NewPostActivity.this, "이미지를 업로드 중입니다...", Toast.LENGTH_LONG).show();
                            Log.e("debug","onSuccess");

                            File newImageFile = new File(postImageUri.getPath());
                            try {
                                compressedImageFile = new Compressor(NewPostActivity.this)
                                        .setMaxHeight(200)
                                        .setMaxWidth(200)
                                        .setQuality(10)
                                        .compressToBitmap(newImageFile);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            final byte[] thumbData = baos.toByteArray();

                            UploadTask uploadTask = storageReference
                                    .child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", uri.toString());
                                    postMap.put("desc", desc);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("timestamp", FieldValue.serverTimestamp());

                                    Log.e("debug", uri.toString());


                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            Log.e("debug","onComplete");

                                            if(task.isSuccessful()) {
                                                Log.e("debug","isSucessful");
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
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                }
                            });



                        }
                    });


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

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i=0; i<randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
