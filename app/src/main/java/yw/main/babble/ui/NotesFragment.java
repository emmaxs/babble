package yw.main.babble.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        // delete - TODO: Right now we click on note
                        Intent intent;
                        intent = new Intent(getActivity(), NoteActivity.class);
                        // This will change once we have a real db
                        intent.putExtra(NOTE_INDEX, position);
                        startActivityForResult(intent, SAVE_ENTRY);
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

        // Font Draw button
        (root.findViewById(R.id.open_draw_activity_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), FontDrawActivity.class);
                startActivity(myIntent);
            }
        });

        return root;
    }

    private void prepareNotes() {
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

}
