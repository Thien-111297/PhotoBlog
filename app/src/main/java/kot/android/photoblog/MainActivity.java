package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import kot.android.photoblog.fragment.AccountFragment;
import kot.android.photoblog.fragment.HomeFragment;
import kot.android.photoblog.fragment.NotificationFragment;
import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity {
    private static final String ACTIVITY_NAME = MainActivity.class.getSimpleName();
    private static final String TAG = ACTIVITY_NAME;

    private Toolbar toolbar;
    private FloatingActionButton floatAdd;
    private BottomNavigationView mainBottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    public String currentUserId;

    private GoogleSignInClient mGoogleSignInClient;

    private Uri imageUri = null;
    private String userName;
    public String googleUserId;
    private String userEmail;
    private String userImageUrl;




    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.mainToolBar);
        floatAdd = findViewById(R.id.floatAdd);

        mainBottomNav = findViewById(R.id.mainBottomNav);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Blog");


        dataFromGoogleUser();


        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        replaceFragment(homeFragment);

        if (mAuth.getCurrentUser() != null) {

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.mnu_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.mnu_notification:
                            replaceFragment(notificationFragment);
                            return true;
                        case R.id.mnu_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }

                }
            });

            floatAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    Bundle bundle = new Bundle();
                    userBundle(bundle);
                    newPostIntent.putExtras(bundle);
                    startActivity(newPostIntent);
                    CustomIntent.customType(MainActivity.this, "bottom-to-up");


                }
            });
        }
        Log.i(TAG, ACTIVITY_NAME + " onCreate");
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sentToLogin();
        } else {
            currentUserId = mAuth.getCurrentUser().getUid();
            if (googleUserId == null) {
                alreadySetupProfile(currentUserId);
            } else {
                alreadySetupProfile(googleUserId);
            }
        }

        Log.i(TAG, ACTIVITY_NAME + " onStart");


    }

    private void alreadySetupProfile(String id) {

        firebaseFirestore.collection("Users")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                floatAdd.setVisibility(View.INVISIBLE);
                                Snackbar.make(findViewById(android.R.id.content), "Bạn chưa cài đặt tài khoản", Snackbar.LENGTH_INDEFINITE)
                                        .setTextColor(getColor(R.color.colorPrimary))
                                        .setAction("CÀI ĐẶT", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                                                if(googleUserId!=null) {
                                                    Bundle bundle = new Bundle();
                                                    userBundle(bundle);
                                                    intent.putExtras(bundle);
                                                }
                                                startActivity(intent);
                                                CustomIntent.customType(MainActivity.this, "fadein-to-fadeout");

                                            }
                                        })
                                        .show();

                            } else {
                                floatAdd.setVisibility(View.VISIBLE);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(MainActivity.this, "FirestoreError" + error, Toast.LENGTH_SHORT).show();
                        }
                    }

                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_setting:
                setup();
                break;
            case R.id.mnu_search:
                break;
            case R.id.mnu_logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup() {
        Intent settingIntent = new Intent(MainActivity.this, SetupActivity.class);
        Bundle bundle = new Bundle();
        userBundle(bundle);
        settingIntent.putExtras(bundle);

        startActivity(settingIntent);
        overridePendingTransition(R.anim.enter_intent_animation, R.anim.exit_intent_animation);
    }

    private void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        sentToLogin();
    }

    private void sentToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void dataFromGoogleUser() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (acct != null) {
            userName = acct.getDisplayName();
            userImageUrl = acct.getPhotoUrl().toString();
            userEmail = acct.getEmail();
            googleUserId = acct.getId();

        }

    }

    private void userBundle(Bundle bundle) {
        bundle.putString("name", userName);
        bundle.putString("email", userEmail);
        bundle.putString("id", googleUserId);
        bundle.putString("image", userImageUrl);
    }


    @Override
    protected void onResume() {
        super.onResume();


        Log.i(TAG, ACTIVITY_NAME + " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, ACTIVITY_NAME + " onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, ACTIVITY_NAME + " onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, ACTIVITY_NAME + " onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, ACTIVITY_NAME + " onRestart");
    }
}
