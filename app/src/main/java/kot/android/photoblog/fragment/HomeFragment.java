package kot.android.photoblog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import kot.android.photoblog.R;
import kot.android.photoblog.adapter.BlogRecyclerAdapter;
import kot.android.photoblog.model.BlogPost;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeFragment extends Fragment {
    private static final String FRAGMENT_NAME = HomeFragment.class.getSimpleName();
    private static final String TAG = FRAGMENT_NAME;

    private RecyclerView recyclerBlog;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private List<BlogPost> blogPosts;
    private List<BlogPost> dsBlog = new ArrayList<>();
    private View view;

    private FirebaseFirestore firebaseFirestore;
    private ListenerRegistration blogChangeListener;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        Log.i(TAG, FRAGMENT_NAME + " onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, FRAGMENT_NAME + " onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        Log.i(TAG, FRAGMENT_NAME + " onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerBlog = view.findViewById(R.id.recyclerBlog);

        Log.i(TAG, FRAGMENT_NAME + " onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        blogPosts = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPosts);
        recyclerBlog.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerBlog.setHasFixedSize(true);
        recyclerBlog.setAdapter(blogRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();


        Query firstQuery = firebaseFirestore.
                collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        blogChangeListener = firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.i(TAG, "Listen failed.", e);
                    return;
                }
                if (!documentSnapshots.isEmpty()) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            blogPosts.add(blogPost);
                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
                dsBlog.clear();
                dsBlog.addAll(blogPosts);
            }
        });

        Log.i(TAG, FRAGMENT_NAME + " onStart");


    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Home");
        Log.i(TAG, FRAGMENT_NAME + " onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, FRAGMENT_NAME + " onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i(TAG, FRAGMENT_NAME + " onStop");
        blogChangeListener.remove();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, FRAGMENT_NAME + " onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, FRAGMENT_NAME + " onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, FRAGMENT_NAME + " onDetach");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.mnu_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<BlogPost> dsTim = new ArrayList<>();
                for(BlogPost blogPost : dsBlog){
                    if(blogPost.getDescription().contains(s)){
                        dsTim.add(blogPost);
                    }
                }
                blogPosts.clear();
                blogPosts.addAll(dsTim);
                blogRecyclerAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

}
