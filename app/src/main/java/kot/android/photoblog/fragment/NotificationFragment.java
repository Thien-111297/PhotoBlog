package kot.android.photoblog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import kot.android.photoblog.R;
import kot.android.photoblog.adapter.NotificationRecyclerAdapter;
import kot.android.photoblog.model.Notifications;

public class NotificationFragment extends Fragment {
    private static final String TAG  = NotificationFragment.class.getSimpleName();
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView notificationRecycler;
    private Context context;
    private List<Notifications> list;
    private NotificationRecyclerAdapter adapter;
    public NotificationFragment (){

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container,false);
        notificationRecycler = view.findViewById(R.id.notificationRecycler);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        list = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        adapter = new NotificationRecyclerAdapter(list);
        notificationRecycler.setHasFixedSize(true);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationRecycler.setAdapter(adapter);

        firebaseFirestore.collection("Notifications").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        Notifications notifications = doc.toObject(Notifications.class);
                        list.add(notifications);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Notifications");
    }
}
