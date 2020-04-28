package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kot.android.photoblog.AppUtility.ProgressBarUtility;
import maes.tech.intentanim.CustomIntent;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout edtRegEmail,edtRegPassword,edtRegConfirm;
    Button btnCreate, btnLoginActivity;
    FirebaseAuth mAuth;

    private ProgressBarUtility progressBarUtility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        addControls();
        addEvents();
    }

    private void addEvents() {
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarUtility.show();
                String email = edtRegEmail.getEditText().getText().toString();
                String pass = edtRegPassword.getEditText().getText().toString();
                String confirmPass = edtRegConfirm.getEditText().getText().toString();
                if(inputValid(email,pass,confirmPass)){
                if(!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(pass)&& !TextUtils.isEmpty(confirmPass)){
                    if(pass.equals(confirmPass)){
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressBarUtility.hide();
                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    CustomIntent.customType(RegisterActivity.this,"fadein-to-fadeout");
                                }else {
                                    progressBarUtility.hide();
                                    Toast.makeText(RegisterActivity.this,"Error: "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }else {
                        progressBarUtility.hide();
                        Toast.makeText(RegisterActivity.this,"Mat khau khong trung khop",Toast.LENGTH_SHORT).show();
                    }
                }

            }}
        });
        btnLoginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addControls() {
        btnCreate       = findViewById(R.id.btnCreate);
        btnLoginActivity        = findViewById(R.id.btnLoginActivity);
        edtRegEmail     = findViewById(R.id.edtRegEmail);
        edtRegPassword  = findViewById(R.id.edtRegPassword);
        edtRegConfirm   = findViewById(R.id.edtRegConfirm);

        progressBarUtility = new ProgressBarUtility(RegisterActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        CustomIntent.customType(this,"left-to-right");
        finish();
    }

    private boolean inputValid(String email, String password, String confirm){
        if(email.isEmpty()){
            edtRegEmail.setError("Moi nhap vao Email");
            edtRegEmail.requestFocus();
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtRegEmail.setError("Xin hay nhap dung dinh dang Email (email@gmail.com)");
            edtRegEmail.requestFocus();
            return false;
        } else if(password.isEmpty()){
            edtRegPassword.setError("Moi nhap vao password");
            edtRegPassword.requestFocus();
            return false;
        } else if(!confirm.equals(password)){
            edtRegConfirm.setError("Mat khau chua khop");
            edtRegConfirm.requestFocus();
            return false;
        } else if(confirm.isEmpty()){
            edtRegConfirm.setError("Moi nhap lai mat khau");
            edtRegConfirm.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this,"right-to-left");
    }
}
