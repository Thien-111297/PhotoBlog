package kot.android.photoblog.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kot.android.photoblog.CommentsActivity;
import kot.android.photoblog.MainActivity;
import kot.android.photoblog.NewPostActivity;
import kot.android.photoblog.R;
import kot.android.photoblog.model.BlogPost;
import maes.tech.intentanim.CustomIntent;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<BlogPost> blogPostList;
    private List<BlogPost> blogPostListFull;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private String googleUserId;

    public BlogRecyclerAdapter(List<BlogPost> blogPostList) {
        this.blogPostList = blogPostList;
        blogPostListFull = new ArrayList<>(blogPostList);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_blog, parent, false);
        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        //Description
        String descData = blogPostList.get(position).getDescription();
        holder.txtDesc.setText(descData);

        //UserName,UserImage
        String id = blogPostList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.txtUsername.setText(userName);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.default_image);
                    Glide.with(context).applyDefaultRequestOptions(requestOptions).load(userImage).into(holder.imgUserImage);
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        //Time
        try {
            long millisecond = blogPostList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
            holder.txtBlogDate.setText(dateString);
        } catch (Exception e) {
        }

        //Thumbnail
        String thumbUri = blogPostList.get(position).getThumb();
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.default_image);
        String imageUrl = blogPostList.get(position).getImage_url();
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(imageUrl)
                .thumbnail(Glide.with(context).load(thumbUri))
                .into(holder.imgBlog);

        //BlogpostId
        final String blogPostId = blogPostList.get(position).blogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();

        //GoogleUserId
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        if (acct != null) {

            googleUserId = acct.getId();

        }

        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();

                        holder.txtLikeCount.setText(count + " ");
                    } else {
                        holder.txtLikeCount.setText(0 + " ");
                    }
                }
            }
        });


        //Get Likes
        if (googleUserId == null) {
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.mipmap.like_accent));
                        } else {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.mipmap.like_grey));
                        }
                    }
                }
            });
        } else {
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(googleUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.mipmap.like_accent));
                        } else {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.mipmap.like_grey));
                        }
                    }
                }
            });
        }
        //Like handling
        if (googleUserId == null) {
            holder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userId(currentUserId, blogPostId);
                }
            });
        } else {
            holder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userId(googleUserId, blogPostId);
                }
            });
        }

        //Handling Comment
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
                CustomIntent.customType(context, "left-to-right");
            }
        });

        //Get comments count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();

                        holder.txtCommentCount.setText(count + " ");
                    } else {
                        holder.txtCommentCount.setText(0 + " ");
                    }
                }
            }
        });
    }

    private void userId(final String id, final String blogPostId) {
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {
                    Map<String, Object> likeMap = new HashMap<>();
                    likeMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts")
                            .document(blogPostId)
                            .collection("Likes")
                            .document(id)
                            .set(likeMap);
                } else {
                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(id).delete();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtBlogDate;
        private TextView txtDesc, txtLikeCount, txtCommentCount;
        private ImageView imgBlog;
        private TextView txtUsername;
        private CircleImageView imgUserImage;
        private ImageView btnLike, btnComment;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDesc = itemView.findViewById(R.id.txtBlogDesc);
            imgBlog = itemView.findViewById(R.id.imgBlog);
            txtUsername = itemView.findViewById(R.id.txtUserName);
            imgUserImage = itemView.findViewById(R.id.imgUserImage);
            txtBlogDate = itemView.findViewById(R.id.txtBlogDate);

            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtCommentCount = itemView.findViewById(R.id.txtCommentsCount);
            btnComment = itemView.findViewById(R.id.btnComment);

        }

    }


}
