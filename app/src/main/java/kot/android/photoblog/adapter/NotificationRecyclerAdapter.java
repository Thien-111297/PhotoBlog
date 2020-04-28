package kot.android.photoblog.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import kot.android.photoblog.R;
import kot.android.photoblog.model.Comments;
import kot.android.photoblog.model.Notifications;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    private FirebaseFirestore firebaseFirestore;

    private List<Notifications> notiList;
    private Context context;

    public NotificationRecyclerAdapter (List<Notifications>notiList){
        this.notiList = notiList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_notification, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String title = notiList.get(position).getTitle();
        holder.txtNotiTitle.setText(title);
        String body = notiList.get(position).getMessage();
        holder.txtBody.setText(body);

        //Time
        try {
            long millisecond = notiList.get(position).getDate().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy HH:ss", new Date(millisecond)).toString();
            holder.txtDate.setText(dateString);
        } catch (Exception e) {}
    }

    @Override
    public int getItemCount() {
        return notiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView txtNotiTitle,txtBody,txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBody = itemView.findViewById(R.id.txtBody);
            txtNotiTitle = itemView.findViewById(R.id.txtNotiTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
