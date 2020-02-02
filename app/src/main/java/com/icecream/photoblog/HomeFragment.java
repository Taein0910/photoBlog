package com.icecream.photoblog;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastvisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
       blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);

if(firebaseAuth.getCurrentUser() != null) {

    firebaseFirestore = FirebaseFirestore.getInstance();

    blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            Boolean reachedBottom = recyclerView.canScrollVertically(-1);

            if(reachedBottom) {

                String desc = lastvisible.getString("desc");
                Toast.makeText(container.getContext(), "Reached : "+desc, Toast.LENGTH_SHORT).show();

                loadMorePost();

            }
        }
    });


    Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
    firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

            if(isFirstPageFirstLoad) {
                lastvisible = documentSnapshots.getDocuments()
                        .get(documentSnapshots.size() - 1);
            }


            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                if (doc.getType() == DocumentChange.Type.ADDED) {

                    String blogPostId = doc.getDocument().getId();
                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                    blog_list.add(blogPost);
                    blogRecyclerAdapter.notifyDataSetChanged();

                }

            }

            isFirstPageFirstLoad = false;
        }
    });
}

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost() {
        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .startAfter(lastvisible);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);

                            if(isFirstPageFirstLoad) {
                                blog_list.add(blogPost);
                            } else {
                                blog_list.add(0, blogPost);
                            }

                            blogRecyclerAdapter.notifyDataSetChanged();

                        }

                    }
                }
            }
        });

    }

}
