package kot.android.photoblog.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import kot.android.photoblog.CommentsActivity;
import kot.android.photoblog.R;
import kot.android.photoblog.model.Comments;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {
    private FirebaseFirestore firebaseFirestore;

    private List<Comments> commentsList;
    private Context context;

    public CommentsRecyclerAdapter(List<Comments> comments) {
        this.commentsList = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_comments, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        //User information
        final String userId = commentsList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String name = task.getResult().getString("name");
                String image = task.getResult().getString("image");

                holder.txtUserName.setText(name);
                Glide.with(context).load(image).into(holder.imgUser);
            }
        });

        //Message
        String message = commentsList.get(position).getMessage();
        holder.txtUserComment.setText(message);

        //Time
        try {
            long millisecond = commentsList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy HH:mm", new Date(millisecond)).toString();
            holder.txtCommentDate.setText(dateString);
        } catch (Exception e) {}

    }

    @Override
    public int getItemCount() {
        if (commentsList != null) {
            return commentsList.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtUserComment;
        private TextView txtUserName,txtCommentDate;
        private ImageView imgUser;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserComment = itemView.findViewById(R.id.txtUserComment);
            txtCommentDate = itemView.findViewById(R.id.txtCommentDate);
            imgUser = itemView.findViewById(R.id.imgUser);

        }
    }
}
