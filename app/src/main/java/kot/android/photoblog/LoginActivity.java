package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

import kot.android.photoblog.AppUtility.ProgressBarUtility;
import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {
    //View
    private TextInputLayout edtLogEmail, edtLogPassword;
    private Button btnLogin, btnRegister;
    //Firebase
    private FirebaseAuth mAuth;
    private SignInButton btnGoogleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    private CoordinatorLayout coordinatorLayout;

    //AppUtility
    private ProgressBarUtility progressBarUtility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addControls();
        addEvents();
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

    }

    private void addControls() {
        edtLogEmail         = findViewById(R.id.edtLogEmail);
        edtLogPassword      = findViewById(R.id.edtLogPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        btnRegister         = findViewById(R.id.btnRegister);
        coordinatorLayout   = findViewById(R.id.coordinatorLayout);
        btnGoogleSignIn     = findViewById(R.id.btnGoogleSignIn);

        progressBarUtility = new ProgressBarUtility(LoginActivity.this);
    }

    private void addEvents() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                CustomIntent.customType(LoginActivity.this, "left-to-right");
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarUtility.show();
                login();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }


    private void login() {
        String email = edtLogEmail.getEditText().getText().toString().trim();
        String password = edtLogPassword.getEditText().getText().toString().trim();
        if (validInput(email, password)) {
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBarUtility.hide();
                            sendToMain();
                        } else {
                            progressBarUtility.hide();
                            Toast.makeText(LoginActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        } else {
            progressBarUtility.hide();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        CustomIntent.customType(this,"left-to-right");
        finish();
    }

    private void signInWithGoogle() {
        Intent dangNhapIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(dangNhapIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignResult(task);

        }
    }

    private void handleSignResult(Task<GoogleSignInAccount> task) {

        try {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            Toast.makeText(LoginActivity.this, "Dang nhap google thanh cong", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acc) {
        if (acc != null) {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
            mAuth.signInWithCredential(authCredential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, " that bai", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout),"Moi ban chon tai khoan",Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.coordinatorLayout)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show();
        }
    }

    private boolean validInput(String email, String password) {
        if (email.isEmpty()) {
            edtLogEmail.getEditText().setError("Moi nhap vao Email");
            edtLogEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtLogEmail.setError("Xin hay nhap dung dinh dang Email (email@gmail.com)");
            edtLogEmail.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            edtLogPassword.setError("Moi nhap vao password");
            edtLogPassword.requestFocus();
            return false;
        } else {
            edtLogPassword.setError(null);
            edtLogEmail.setError(null);
            return true;
        }
    }
}
