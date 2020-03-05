package yw.main.babble.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneScore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yw.main.babble.R;

import static yw.main.babble.ui.NotesFragment.CONTENT;
import static yw.main.babble.ui.NotesFragment.ID;
import static yw.main.babble.ui.NotesFragment.NEW_NOTE;
import static yw.main.babble.ui.NotesFragment.TITLE;
import static yw.main.babble.ui.NotesFragment.UPDATE_NOTE;
import static yw.main.babble.ui.NotesFragment.WRITE_MODE;

public class NoteActivity extends AppCompatActivity {
    EditText editText;
    Intent intent;
    NotesBuilder newNote;

    // Passed from firebase
    int mode = NEW_NOTE;
    String title = "Untitled";
    String content;
    String docId;


    // Tone Analysis
    IamAuthenticator authenticator;
    ToneAnalyzer toneAnalyzer;
    ToneOptions options;
    String toastMessage;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userId;

    SharedPreferences sharedPreferences;
    private String detectedTone;

    // for app-wide shared prefs
    public static final String myPrefs = "MyPrefs";
    public static final String myMap = "Map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: switch theme
        setTheme(R.style.JournalTheme);

        // get app-wide shared prefs
        sharedPreferences = getApplicationContext().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);

        editText = findViewById(R.id.EditText);
        intent = getIntent();

        // Set up the note if you are just updating
        if (intent.getIntExtra(WRITE_MODE, 0) == UPDATE_NOTE) {
                // set the mode
                mode = UPDATE_NOTE;
                docId = intent.getStringExtra(ID);
                // TODO: Do something with title
                title = intent.getStringExtra(TITLE);
                // Set the content of the edit text
                editText.setText(intent.getStringExtra(CONTENT));
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        userId = firebaseUser.getUid();

        db = FirebaseFirestore.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get string from Edit Text
                content = editText.getText().toString();
                // Build the tone options
                options = new ToneOptions.Builder().text(content).build();
                // Query the service
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ToneAnalysis toneAnalysis = toneAnalyzer.tone(options).execute().getResult();
                        List<ToneScore> scores = toneAnalysis.getDocumentTone()
                                        .getTones();
                                detectedTone = "";
                                for(ToneScore score:scores) {
                                    if(score.getScore() > 0.5f) {
                                        detectedTone += score.getToneName() + " ";
                                    }
                                }
                                toastMessage =
                                        "The following emotions were detected:\n\n"
                                                + detectedTone.toUpperCase();

                                // Save to firebase
                                // TODO: merge with location
                                switch (mode) {
                                    case NEW_NOTE:
                                        newNote = new NotesBuilder(title, content, detectedTone, 0.0, 0.0);
                                        if (wifiConnection()) {
                                            db.collection("users").document(userId)
                                                    .collection("notes").add(newNote);
                                        }
                                        break;
                                    case UPDATE_NOTE:
                                        if (wifiConnection()) {
                                            DocumentReference notesRef = db.collection("users").document(userId)
                                                    .collection("notes").document(docId);
                                            Map<String,Object> updates = new HashMap<>();
                                            updates.put("content", content);
                                            updates.put("title", title);
                                            updates.put("timestamp", FieldValue.serverTimestamp());
                                            notesRef.update(updates)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("EXS", "DocumentSnapshot successfully updated!");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("EXS", "Error updating document", e);
                                                        }
                                                    });
                                        }
                                        break;
                                }

                                // Run the toast on UI
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(),
                                                toastMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
                setResult(Activity.RESULT_OK, intent);
                // close the activity
                finish();
            }
        });

        // Sentiment analysis
        authenticator = new IamAuthenticator(getString(R.string.tone_api_key));
        toneAnalyzer =  new ToneAnalyzer("2017-09-21", authenticator);
        toneAnalyzer.setServiceUrl(getString(R.string.tone_url));
    }

    public void onResume(){
        super.onResume();
        //TODO: change theme (use sharedprefs)
    }

    // check wifi connection
    private boolean wifiConnection() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi is on
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                return false; // not connected to access point
            }
            return true; // connected to access point
        }
        else {
            return false; // Wi-Fi is off
        }
    }
}
