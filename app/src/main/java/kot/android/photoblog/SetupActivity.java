package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kot.android.photoblog.AppUtility.ProgressBarUtility;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = SetupActivity.class.getSimpleName();

    //View
    private Toolbar setupToolbar;
    private CircleImageView imgSetup;
    private EditText edtSaveSetupName;
    private Button btnSaveSetupAccount;

    // Firebase Handling
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    private ProgressBarUtility progressBarUtility;
    private boolean isChange = false;
    private Uri mainImageUri = null;
    private static final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_FOLDER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Lấy thông tin ng dùng
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // Trỏ đến root
        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseFirestore = FirebaseFirestore.getInstance();

        addControls();
        addEvents();


        getDataFromFirestore(userId);
    }


    private void getDataFromFirestore(String userId) {
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);
                        edtSaveSetupName.setText(name);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(requestOptions).load(image).into(imgSetup);
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FirestoreError" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addControls() {
        imgSetup = findViewById(R.id.imgAccountImage);
        btnSaveSetupAccount = findViewById(R.id.btnSaveSetupAccount);
        edtSaveSetupName = findViewById(R.id.edtSaveSetupName);

        progressBarUtility = new ProgressBarUtility(SetupActivity.this);

    }

    private void addEvents() {
        imgSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestRunTimePermissions();
                imagePicker();
            }
        });
        btnSaveSetupAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarUtility.show();

                final String userName = edtSaveSetupName.getText().toString();
                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                    if (isChange) {

                        String userid = mAuth.getCurrentUser().getUid();
                        StorageReference imagePath = storageReference.child("profile_images").child(userid + " .jpg");
                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFirestore(task, userName);

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error " + error, Toast.LENGTH_LONG).show();
                                    progressBarUtility.hide();
                                }
                            }
                        });
                    } else {
                        storeFirestore(null, userName);

                    }

                } else {
                    progressBarUtility.hide();
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.vibrate_animation);
                    edtSaveSetupName.setAnimation(animation);
                    edtSaveSetupName.setError("Xin moi nhap ten va chon anh");
                }
            }
        });

    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, final String userName) {

        if (task != null) {

            Task<Uri> uriTask = task.getResult().getMetadata().getReference().getDownloadUrl();
            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    setDataToFireStore(userName, uri);
                    progressBarUtility.hide();
                }
            });

        } else {
            setDataToFireStore(userName, mainImageUri);
        }
        Snackbar.make(findViewById(android.R.id.content), "thanh cong", Snackbar.LENGTH_SHORT).show();

    }

    private void setDataToFireStore(String userName, Uri uri) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", uri.toString());
        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    sendToMain();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FirestoreError" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void requestRunTimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SetupActivity.this, permissions[0])) {
                Snackbar.make(findViewById(android.R.id.content), "Ứng dụng cần quyền để tiếp tục", Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(SetupActivity.this, permissions, REQUEST_FOLDER);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(SetupActivity.this, permissions, REQUEST_FOLDER);
            }
        } else {
            imagePicker();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                isChange = true;
                imgSetup.setImageURI(mainImageUri);
                btnSaveSetupAccount.setEnabled(true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SetupActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_intent_animation, R.anim.exit_intent_animation);
    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.enter_intent_animation, R.anim.exit_intent_animation);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveFromMain();
    }

    private void receiveFromMain() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("name");
            String email = bundle.getString("email");
            String id = bundle.getString("id");
            String image = bundle.getString("image");
            setGoogleUserDataToFirestore(name, email, id, image);
        }
    }

    private void setGoogleUserDataToFirestore(final String name, final String email, String id, final String image) {
        if (id != null) {
            Map<String, String> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("image", image);
            userMap.put("email", email);
            firebaseFirestore.collection("Users").document(id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        ConstraintLayout constraintLayout = findViewById(R.id.setupLayout);
                        ConstraintSet set = new ConstraintSet();
                        EditText editText = new EditText(getApplicationContext());
                        editText.setId(View.generateViewId());
                        constraintLayout.addView(editText, 0);
                        set.clone(constraintLayout);
                        set.connect(editText.getId(), ConstraintSet.TOP, imgSetup.getId(), ConstraintSet.BOTTOM, 16);
                        set.connect(editText.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START, 8);
                        set.connect(editText.getId(), ConstraintSet.END, constraintLayout.getId(), ConstraintSet.END, 8);
                        set.applyTo(constraintLayout);
                        editText.setText(email);
                        editText.setTextColor(getResources().getColor(R.color.colorPrimary));
                        editText.setEnabled(false);
                        edtSaveSetupName.setEnabled(false);
                        btnSaveSetupAccount.setClickable(false);
                        imgSetup.setClickable(false);
                        edtSaveSetupName.setText(name);
                        Glide.with(getApplicationContext()).load(image).into(imgSetup);
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "FirestoreError" + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            return;
        }
    }

}
