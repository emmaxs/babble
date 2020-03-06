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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import yw.main.babble.notes.NoteActivity;
import yw.main.babble.notes.NotesAdapter;
import yw.main.babble.notes.NotesBuilder;
import yw.main.babble.R;

public class NotesFragment extends Fragment {
    ArrayList<NotesBuilder> notesList;
    private NotesAdapter nAdapter;
    private SwipeMenuListView listView;
    private Spinner sortBySpinner;
    public static final String ID = "ID";
    public static final String CONTENT = "CONTENT";
    public static final String TITLE = "TITLE";
    public static final int SAVE_ENTRY = 1;

    public static final String WRITE_MODE = "WRITE_MODE";
    public static final int NEW_NOTE = 1;
    public static final int UPDATE_NOTE = 2;

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userId;
    private String sortType = "";
    // shared prefs
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get app-wide shared prefs
        sharedPreferences = getActivity().getApplicationContext()
                .getSharedPreferences(NoteActivity.myPrefs, Context.MODE_PRIVATE);

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

        // TODO: Add Snackbar
        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), NoteActivity.class);
                myIntent.putExtra(WRITE_MODE, NEW_NOTE);
                startActivityForResult(myIntent, NotesFragment.SAVE_ENTRY);
            }
        });
        sortBySpinner = root.findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_array, android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(spinnerAdapter);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                doSort(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        listView = root.findViewById(R.id.notes);

        // Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            db = FirebaseFirestore.getInstance();

            // Listen for DB changes
            db.collection("users").document(userId)
                    .collection("notes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("EXS", "Listen failed.", e);
                                return;
                            }

                            notesList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : value) {
                                NotesBuilder note = doc.toObject(NotesBuilder.class);
                                note.setId(doc.getId());
                                notesList.add(note);
                            }
                            // set adapter
                            nAdapter = new NotesAdapter(notesList, getActivity());
                            listView.setAdapter(nAdapter);
                        }
                    });
        }

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
                deleteItem.setIcon(R.drawable.delete);
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
                        db.collection("users").document(userId)
                                .collection("notes").document(nAdapter.getItem(position).getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("EXS", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("EXS", "Error deleting document", e);
                                    }
                                });

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
                intent.putExtra(ID, nAdapter.getItem(position).getId());
                intent.putExtra(CONTENT, nAdapter.getItem(position).getContent());
                intent.putExtra(TITLE, nAdapter.getItem(position).getTitle());
                intent.putExtra(WRITE_MODE, UPDATE_NOTE);
                startActivityForResult(intent, SAVE_ENTRY);
            }
        });

        return root;
    }

    private void doSort(int type) {
        Query.Direction direction = Query.Direction.DESCENDING;
        switch(type) {
            case 0: //Time
                sortType = "timestamp";
                break;
            case 1: //Mood
                sortType = "emotion";
                break;
            case 2: //Location
                sortType = "latitude";
                break;
            case 3: //Alphabetical
                sortType = "title";
                direction = Query.Direction.ASCENDING;
                break;
            case 4: //ID
                sortType = "id";
                break;

        }
        if (firebaseUser != null) {
            Log.d("sorting by",sortType);
            userId = firebaseUser.getUid();

            db = FirebaseFirestore.getInstance();

            // Listen for DB changes
            db.collection("users").document(userId)
                    .collection("notes")
                    .orderBy(sortType, direction)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("EXS", "Listen failed.", e);
                                return;
                            }

                            notesList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : value) {
                                NotesBuilder note = doc.toObject(NotesBuilder.class);
                                note.setId(doc.getId());
                                notesList.add(note);
                            }
                            // set adapter
                            nAdapter = new NotesAdapter(notesList, getActivity());
                            listView.setAdapter(nAdapter);
                        }
                    });
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_ENTRY) {
            if (resultCode == Activity.RESULT_OK) {
            }
        }
    }
}