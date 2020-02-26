package yw.main.babble.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import yw.main.babble.FontDrawActivity;
import yw.main.babble.MainActivity;
import yw.main.babble.NoteActivity;
import yw.main.babble.NotesAdapter;
import yw.main.babble.NotesBuilder;
import yw.main.babble.R;

public class NotesFragment extends Fragment {
    ArrayList<NotesBuilder> notesList = new ArrayList<>();
    private NotesAdapter nAdapter;
    private ListView listView;
    public static final String NOTE_INDEX = "NOTE_INDEX";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_notes, container, false);

        listView = root.findViewById(R.id.notes);

        nAdapter = new NotesAdapter(notesList, getActivity());
        listView.setAdapter(nAdapter);

        // Get a more detailed view on any clicked item in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                Intent intent;
                intent = new Intent(getActivity(), NoteActivity.class);
                // This will change once we have a real db
                intent.putExtra(NOTE_INDEX, position + 1);

//                startActivityForResult(intent, SAVE_ENTRY);
                startActivity(intent);
            }
        });

        ((Button) root.findViewById(R.id.open_draw_activity_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), FontDrawActivity.class);
                startActivity(myIntent);
            }
        });

        // TODO: Add onClick Listener to select notes
        prepareNotes();
        return root;
    }

    private void prepareNotes() {
        File directory;
        directory = getActivity().getFilesDir();
        File[] files = directory.listFiles();
        String theFile;
        for (int f = 1; f <= files.length; f++) {
            theFile = "Note" + f + ".txt";
            NotesBuilder note = new NotesBuilder(theFile, Open(theFile));
            notesList.add(note);
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

}
