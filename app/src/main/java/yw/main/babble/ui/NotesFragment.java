package yw.main.babble.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import yw.main.babble.font.FontDrawActivity;
import yw.main.babble.notes.NoteActivity;
import yw.main.babble.notes.NotesAdapter;
import yw.main.babble.notes.NotesBuilder;
import yw.main.babble.R;

public class NotesFragment extends Fragment {
    ArrayList<NotesBuilder> notesList;
    private NotesAdapter nAdapter;
    private SwipeMenuListView listView;
    public static final String NOTE_INDEX = "NOTE_INDEX";
    public static final int SAVE_ENTRY = 1;

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userId;

    // shared prefs
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get app-wide shared prefs
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_notes, container, false);

        // set firebase things
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
            userId = firebaseUser.getUid();

        db = FirebaseFirestore.getInstance();

        // TODO: Add Snackbar
        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), NoteActivity.class);
                startActivityForResult(myIntent, NotesFragment.SAVE_ENTRY);
            }
        });

        listView = root.findViewById(R.id.notes);

        // DB
        db.collection("users").document(userId)
                .collection("notes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                notesList = new ArrayList<>();
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        NotesBuilder note = document.toObject(NotesBuilder.class);
                        notesList.add(note);
                    }
                    // set adapter
                    nAdapter = new NotesAdapter(notesList, getActivity());
                    listView.setAdapter(nAdapter);
                } else {
                    Log.d("exs", "Error getting documents: ", task.getException());
                }
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dpToPx(120));
                // set a icon
                deleteItem.setIcon(android.R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);


        // set listener for swipe actions
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // Delete from adapter
                        nAdapter.remove(position);
                        nAdapter.notifyDataSetChanged();

                        // Delete local files
                        String filename = "Note" + position + ".txt";
                        File dir = getActivity().getFilesDir();
                        File file = new File(dir, filename);
                        file.delete();


                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        // Get a more detailed view on any clicked item in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                Intent intent;
                intent = new Intent(getActivity(), NoteActivity.class);
                // This will change once we have a real db
                intent.putExtra(NOTE_INDEX, position);
                startActivityForResult(intent, SAVE_ENTRY);
            }
        });

        return root;
    }


//    private void loadFirebase() {
//        DocumentReference docRef = db.collection("notes").document(userId);
//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w("Emma", "Listen failed.", e);
//                    return;
//                }
//
//                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
//                        ? "Local" : "Server";
//
//                if (snapshot != null && snapshot.exists()) {
//                    Log.d("Emma", source + " data: " + snapshot.getData());
//                } else {
//                    Log.d("Emma", source + " data: null");
//                }
//            }
//        });
//    }

    public void onDataSetChanged() {
        notesList.clear();
        nAdapter.notifyDataSetChanged();
    }

    // Put them back after config change
    @Override
    public void onResume() {
        super.onResume();

        // put the entries back in the adapter
        if (notesList != null) {
            nAdapter = new NotesAdapter(notesList, getActivity());
            listView.setAdapter(nAdapter);
        }
    }

    // duplicate method - try to reduce or do in thread
    public String Open(String fileName) {
        String content = "";
        try {
            InputStream in = getActivity().openFileInput(fileName);
            if (in != null) {
                InputStreamReader tmp = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(tmp);
                String str;
                StringBuilder buf = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                }
                in.close();

                content = buf.toString();
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (Throwable t) {
            Toast.makeText(getActivity(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        return content;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_ENTRY) {
            if (resultCode == Activity.RESULT_OK) {
                onDataSetChanged();
                // TODO: For a new note just add it
            }
        }
    }
}


