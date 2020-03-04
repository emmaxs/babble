package yw.main.babble.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneScore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import yw.main.babble.R;
import yw.main.babble.ui.NotesFragment;

public class NoteActivity extends AppCompatActivity {
    EditText editText;
    int fileNumber;
    String filename = "";
    Intent intent;
    NotesBuilder newNote;

    // Tone Analysis
    IamAuthenticator authenticator;
    ToneAnalyzer toneAnalyzer;
    ToneOptions options;
    String textToAnalyze;
    String toastMessage;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String userId;

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


        editText = findViewById(R.id.EditText);
        intent = getIntent();
        if (intent.getIntExtra(NotesFragment.NOTE_INDEX, 0) != 0) {
            fileNumber = intent.getIntExtra(NotesFragment.NOTE_INDEX, 0) + 1;
        }
        else {
            File[] files = getFilesDir().listFiles();
            fileNumber = files.length + 1;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        userId = firebaseUser.getUid();

        db = FirebaseFirestore.getInstance();

        filename = "Note" + fileNumber + ".txt";
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get string from Edit Text
                textToAnalyze = editText.getText().toString();
                // Build the tone options
                options = new ToneOptions.Builder().text(textToAnalyze).build();
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

                                // Save file locally and to firebase
                        // TODO: merge with location
                                newNote = new NotesBuilder(filename, textToAnalyze, detectedTone, 0.0, 0.0);
                                Save(filename);

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
        // set these strings better
        editText.setText(Open(filename));

        // Sentiment analysis
        authenticator = new IamAuthenticator(getString(R.string.tone_api_key));
        toneAnalyzer =  new ToneAnalyzer("2017-09-21", authenticator);
        toneAnalyzer.setServiceUrl(getString(R.string.tone_url));
    }

    public void Save(String fileName) {
        // TODO: remove this part
        // all files will exist locally, but not necessarily be shown by notesFragment
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(newNote.getContent());
            out.close();
            Log.d("exs note save", "saved");
//            Toast.makeText(getBaseContext(), "Note Saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Log.d("exs note save", "error");
//            Toast.makeText(getBaseContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        // saving to firebase if wifi is good
        if (wifiConnection()) {
            db.collection("notes").document(userId).set(newNote);
        }
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

    public boolean FileExists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public String Open(String fileName) {
        String content = "";
        if (FileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName);
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
                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content;
    }
}
