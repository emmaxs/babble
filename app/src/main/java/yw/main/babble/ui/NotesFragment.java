package yw.main.babble.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Map;

import yw.main.babble.font.FontDrawActivity;
import yw.main.babble.notes.NoteActivity;
import yw.main.babble.notes.NotesAdapter;
import yw.main.babble.notes.NotesBuilder;
import yw.main.babble.R;

public class NotesFragment extends Fragment {
    ArrayList<NotesBuilder> notesList = new ArrayList<>();
    private NotesAdapter nAdapter;
    private SwipeMenuListView listView;
    public static final String NOTE_INDEX = "NOTE_INDEX";
    public static final int SAVE_ENTRY = 1;

    // firebase
    private FirebaseStorage database;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;

    // shared prefs
    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getApplicationContext()
                        .getSharedPreferences(NoteActivity.myPrefs, Context.MODE_PRIVATE);

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
        database = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Create a storage reference from our app
        storageReference = database.getReference();
        if (firebaseUser != null)
        userId = firebaseUser.getUid();

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

        // do the notes list
//        prepareNotesfromFirebase();
        prepareNotes();

        // set adapter
        nAdapter = new NotesAdapter(notesList, getActivity());
        listView.setAdapter(nAdapter);

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

                        // Delete from firebase
                        StorageReference notesRef = storageReference.child("notes").child(userId).child(filename);
                        // Delete the file
                        notesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d("exs", "we did it!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d("exs", "oh no");
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
                // This will change once we have a real db
                intent.putExtra(NOTE_INDEX, position);
                startActivityForResult(intent, SAVE_ENTRY);
            }
        });

        return root;
    }


    private void prepareNotesfromFirebase(){
        // get everything stored under the notes/userId directory
        StorageReference notesRef = storageReference.child("notes").child(userId);

        notesRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }

                        for (StorageReference item : listResult.getItems()) {
                            try {
                                File localFile = File.createTempFile("notes/"+ userId, "txt");
                                Log.d("exs", localFile.getAbsolutePath());

                                item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Local temp file has been created
                                        Log.d("exs", "we did it!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                        Log.d("exs", "failed");
                                    }
                                });
                            } catch(IOException e) {}
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        Log.d("exs", "failed in list");
                    }
                });

    }

    //TODO: modify for firebase
    // this should be used only when not logging in for the first time
    // when logging in for the first time you need to pull notes from firebase and save those to local
    // (that should be done on a thread)
    //  if notes with the same name already exist in local, must replace them
    // how to check when user first logs in?

    // this method prepares notes from local
    // DO NOT USE WHEN FIRST LOGGING IN
    // first log in must pull from firebase.

//    private void prepareNotes() {
//        // get the user to files map
//        Map<String, ArrayList<String>> user_to_files = loadMapfromPreferences();
//
//        // now take every filename associated with userId string and display it
//        // only display notes if the user has files associated (if statement)
//        if(user_to_files.containsKey(userId)){
//            for (String filename: user_to_files.get(userId)){
//                NotesBuilder note = new NotesBuilder(filename, Open(filename));
//                notesList.add(note);
//            }
//        }
//    }

    // old version
   private void prepareNotes(){
        File directory;
        directory = getActivity().getFilesDir();
        File[] files = directory.listFiles();
        String theFile;

        Log.d("exs", "File length is " + files.length);

        for (int f = 1; f <= files.length; f++) {
            theFile = "Note" + f + ".txt";
            NotesBuilder note = new NotesBuilder(theFile, Open(theFile));
            notesList.add(note);
        }
    }

    public void onDataSetChanged() {
        notesList.clear();
        prepareNotes();
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
            if ( in != null) {
                InputStreamReader tmp = new InputStreamReader( in );
                BufferedReader reader = new BufferedReader(tmp);
                String str;
                StringBuilder buf = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                } in .close();

                content = buf.toString();
            }
        } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
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

    public void saveMaptoPreferences(Map<String, ArrayList<String>> map){
        // if the preferences aren't null, save the map object to the preferences
        if (sharedPreferences != null){
            JSONObject json = new JSONObject(map);
            String jsonString = json.toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // if the userid as key already exists in map, then update it. if not, create
            editor.putString(NoteActivity.myMap, jsonString);
            editor.commit();
        }
    }

    // specifically loads the user to files map!!
//    public Map<String, ArrayList<String>> loadMapfromPreferences(){
//        Map<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
//        try{
//            if(sharedPreferences != null){
//                String jsonString = sharedPreferences.getString(NoteActivity.myMap, (new JSONObject()).toString());
//                JSONObject json = new JSONObject((jsonString));
//                Iterator<String> iterator = json.keys();
//                while(iterator.hasNext()){
//                    String string_key = iterator.next();
//                    ArrayList<String> string_array = (ArrayList<String>) json.get(string_key);
//                    output.put(string_key, string_array);
//                }
//            }
//        }
//        catch(Exception e){e.printStackTrace();}
//        return output;
//    }

}
