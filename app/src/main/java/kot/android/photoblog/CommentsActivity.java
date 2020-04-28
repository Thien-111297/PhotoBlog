package kot.android.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kot.android.photoblog.adapter.CommentsRecyclerAdapter;
import kot.android.photoblog.model.BlogPost;
import kot.android.photoblog.model.Comments;
import maes.tech.intentanim.CustomIntent;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;
    private EditText edtComment;
    private ImageView btnSend, imgCommentImage;
    private RecyclerView commentRecycler;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private TextView txtDescription;

    public static String blogPostId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;
    private String googleUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        commentToolbar = findViewById(R.id.commentToolbar);

        setSupportActionBar(commentToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comments");

        blogPostId = getIntent().getStringExtra("blog_post_id");

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            googleUserId = acct.getId();
        }

        addControls();
        addEvents();
    }

    private void addEvents() {

        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String image = task.getResult().getString("image_url");
                    String desc = task.getResult().getString("description");
                    txtDescription.setText(desc);
                    Glide.with(CommentsActivity.this).load(image).into(imgCommentImage);
                }
            }
        });

        Query query = firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            commentsRecyclerAdapter.notifyDataSetChanged();

                        }
                    }

                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentMessage = edtComment.getText().toString();
                if (!commentMessage.isEmpty()) {
                    if (googleUserId == null) {
                        userComment(currentUserId, edtComment.getText().toString());
                    } else {
                        userComment(googleUserId, edtComment.getText().toString());
                    }

                } else {
                    edtComment.setError("Ban quen binh luan roi");
                }
            }
        });

    }

    private void addControls() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        imgCommentImage = findViewById(R.id.imgCommentsImage);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        commentRecycler = findViewById(R.id.commentRecycler);
        txtDescription = findViewById(R.id.txtDescription);
        txtDescription.requestFocus();

        //RecyclerView
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        commentRecycler.setHasFixedSize(true);
        commentRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentRecycler.setAdapter(commentsRecyclerAdapter);
    }

    private void userComment(String id, String message) {
        Map<String, Object> commentsMap = new HashMap<>();
        commentsMap.put("message", message);
        commentsMap.put("user_id", id);
        commentsMap.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (!task.isSuccessful()) {
                    Snackbar.make(findViewById(android.R.id.content), "Khong the binh luan", Snackbar.LENGTH_SHORT).show();
                } else {
                    edtComment.setText("");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CustomIntent.customType(this, "right-to-left");
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
    protected void onResume() {
        super.onResume();
        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String userName = task.getResult().getString("user_name");
                getSupportActionBar().setTitle(userName + "'s Post");
            }
        });

        edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnSend.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = edtComment.getText().toString().trim();

                if (!input.isEmpty()) {
                    btnSend.setVisibility(View.VISIBLE);
                } else
                    btnSend.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}
