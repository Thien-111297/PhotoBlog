package kot.android.photoblog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kot.android.photoblog.MainActivity;
import kot.android.photoblog.R;
import kot.android.photoblog.adapter.UserBlogRecyclerAdapter;
import kot.android.photoblog.model.BlogPost;

public class AccountFragment extends Fragment {
    private TextView txtAccountName, txtAccountEmail, txtPostCount;
    private CircleImageView imgAccountImage;
    private RecyclerView recyclerUserPost;
    private List<BlogPost> list;

    private Context context;
    private View view;

    private FirebaseFirestore firebaseFirestore;
    private String googleUserId;
    private String currentUserid;

    private UserBlogRecyclerAdapter recyclerAdapter;

    public AccountFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //current user id
        currentUserid = FirebaseAuth.getInstance().getUid();
        //Google User
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        if (acct != null) {
            googleUserId = acct.getId();
        }
        firebaseFirestore = FirebaseFirestore.getInstance();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);
        txtAccountEmail = view.findViewById(R.id.txtAccountEmail);
        txtPostCount = view.findViewById(R.id.txtPostCount);
        txtAccountName = view.findViewById(R.id.txtAccountName);
        imgAccountImage = view.findViewById(R.id.imgAccountImage);
        recyclerUserPost = view.findViewById(R.id.recyclerUserPost);
        return view;
    }

    private void userData(String id) {
        firebaseFirestore.collection("Users")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().getString("name");
                            String email = task.getResult().getString("email");
                            if (email == null) {
                                txtAccountEmail.setText("No email");
                            } else {
                                txtAccountEmail.setText(email);
                            }
                            String image = task.getResult().getString("image");
                            txtAccountName.setText(name);
                            Glide.with(context).load(image).into(imgAccountImage);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleUserId == null) {
            userData(currentUserid);
        } else {
            userData(googleUserId);
        }
        list = new ArrayList<>();
        recyclerAdapter = new UserBlogRecyclerAdapter(list);
        recyclerUserPost.setHasFixedSize(true);
        recyclerUserPost.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerUserPost.setAdapter(recyclerAdapter);

        firebaseFirestore.collection("Posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {

                        String postId = doc.getId();
                        String userId = doc.getString("user_id");
                        if (userId.equals(googleUserId) || userId.equals(currentUserid)) {
                            BlogPost blogPost = doc.toObject(BlogPost.class).withId(postId);
                            list.add(blogPost);
                        }

                    }
                    recyclerAdapter.notifyDataSetChanged();
                    txtPostCount.setText(String.valueOf(list.size()));
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Account");
    }
}
