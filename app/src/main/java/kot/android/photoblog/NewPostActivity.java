package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
import kot.android.photoblog.AppUtility.ProgressBarUtility;
import maes.tech.intentanim.CustomIntent;

public class NewPostActivity extends AppCompatActivity {
    // View
    private Toolbar postToolBar;
    private TextInputLayout edtDescription;
    private ImageView imgPost;
    private Button btnPost;
    private Uri postImageUri;

    // Firebase
    private StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;


    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String currentGoogleUserId;

    private Bitmap compressBitmapImage;
    private String randomName;

    // AppUltility
    ProgressBarUtility progressBarUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        randomName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        addControls();
        addEvents();
    }

    private void addControls() {
        postToolBar = findViewById(R.id.postToolBar);
        setSupportActionBar(postToolBar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtDescription = findViewById(R.id.edtDescription);
        imgPost = findViewById(R.id.imgPost);
        btnPost = findViewById(R.id.btnPost);

        progressBarUtility = new ProgressBarUtility(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                imgPost.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(NewPostActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void addEvents() {
        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarUtility.show();
                final String desc = edtDescription.getEditText().getText().toString().trim();

                if (!TextUtils.isEmpty(desc) && postImageUri != null && desc.length() <= 100) {

                    StorageReference filePath = storageReference.child("post_images").child(randomName + " jpg");
                    if (currentGoogleUserId == null) {
                        filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskUserName) {
                                            if(taskUserName.isSuccessful()) {
                                                String userName = taskUserName.getResult().getString("name");
                                                storePostToFirestore(task, desc, currentUserId,userName);
                                            } else {
                                                Toast.makeText(NewPostActivity.this,"No No No",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                } else {
                                    progressBarUtility.hide();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(NewPostActivity.this, "Error " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    firebaseFirestore.collection("Users").document(currentGoogleUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskUserName) {
                                            if(taskUserName.isSuccessful()) {
                                                String userName = taskUserName.getResult().getString("name");
                                                storePostToFirestore(task, desc, currentGoogleUserId,userName);
                                            } else {
                                                Toast.makeText(NewPostActivity.this,"No No No",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    progressBarUtility.hide();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(NewPostActivity.this, "Error " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.vibrate_animation);
                    edtDescription.setAnimation(animation);
                    edtDescription.setError("Xin mời nhập miêu tả (không quá 100 ký tự) và chọn ảnh");

                    progressBarUtility.hide();
                }
            }
        });
    }


    private void storePostToFirestore(final Task<UploadTask.TaskSnapshot> task, final String desc, final String id, final String name) {
        if (task != null) {
            Task<Uri> uriTask = task.getResult().getMetadata().getReference().getDownloadUrl();
            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {

                    File newImageFile = new File(postImageUri.getPath());
                    try {
                        compressBitmapImage = new Compressor(NewPostActivity.this)
                                .setMaxHeight(100)
                                .setMaxWidth(100)
                                .setQuality(2)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumbData = baos.toByteArray();

                    final UploadTask uploadTask = storageReference
                            .child("post_images")
                            .child("thumbs")
                            .child(randomName + ".jpg")
                            .putBytes(thumbData);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri thumbUri) {
                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", uri.toString());
                                    postMap.put("thumb", thumbUri.toString());
                                    postMap.put("description", desc);
                                    postMap.put("user_id", id);
                                    postMap.put("user_name",name.toLowerCase());
                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore
                                            .collection("Posts")
                                            .add(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBarUtility.hide();

                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    CustomIntent.customType(NewPostActivity.this, "up-to-bottom");
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBarUtility.hide();
                                                    Snackbar.make(findViewById(android.R.id.content), "Tải post thất bại", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarUtility.hide();
                            Snackbar.make(findViewById(android.R.id.content), "Có lỗi xảy ra", Snackbar.LENGTH_SHORT).show();
                        }
                    });


                }
            });

        } else {
            progressBarUtility.hide();
            imgPost.setImageResource(R.drawable.default_image);
        }

    }

    private void receiveFromMain() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentGoogleUserId = bundle.getString("id");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        receiveFromMain();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CustomIntent.customType(NewPostActivity.this, "up-to-bottom");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }

        return super.onOptionsItemSelected(item);

    }
    private void getUserName(String id){


    }
}
