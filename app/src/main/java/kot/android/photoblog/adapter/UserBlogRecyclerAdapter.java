package kot.android.photoblog.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import kot.android.photoblog.CommentsActivity;
import kot.android.photoblog.R;
import kot.android.photoblog.model.BlogPost;
import maes.tech.intentanim.CustomIntent;

public class UserBlogRecyclerAdapter extends RecyclerView.Adapter<UserBlogRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<BlogPost>blogImageList;

    public UserBlogRecyclerAdapter(List<BlogPost>list){
        this.blogImageList = list;
    }

    @NonNull
    @Override
    public UserBlogRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_blog,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserBlogRecyclerAdapter.ViewHolder holder, int position) {

        String image = blogImageList.get(position).getImage_url();
        final String postId = blogImageList.get(position).blogPostId;
        Glide.with(context).load(image).into(holder.imgBlogImage);

        //Handle click user post
        holder.imgBlogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentsIntent = new Intent(context, CommentsActivity.class);
                commentsIntent.putExtra("blog_post_id",postId);
                context.startActivity(commentsIntent);
                CustomIntent.customType(context,"left-to-right");
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgBlogImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlogImage = itemView.findViewById(R.id.imgBlogImage);
        }
    }
}
