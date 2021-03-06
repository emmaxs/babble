
package yw.main.babble.notes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneScore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yw.main.babble.R;
import yw.main.babble.ui.ThemeChangeFragment;

import static yw.main.babble.ui.NotesFragment.CONTENT;
import static yw.main.babble.ui.NotesFragment.ID;
import static yw.main.babble.ui.NotesFragment.NEW_NOTE;
import static yw.main.babble.ui.NotesFragment.TITLE;
import static yw.main.babble.ui.NotesFragment.UPDATE_NOTE;
import static yw.main.babble.ui.NotesFragment.WRITE_MODE;

public class NoteActivity extends AppCompatActivity implements LocationListener {
    EditText editText;
    EditText titleEditText;
    Intent intent;
    NotesBuilder newNote;

    // Passed from firebase
    int mode = NEW_NOTE;
    String title = "Untitled";
    String content;
    String docId;

    // Managing the location
    LocationManager locationManager;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private static final int PERMISSIONS_REQUEST = 2;

    // Tone Analysis
    IamAuthenticator authenticator;
    ToneAnalyzer toneAnalyzer;
    ToneOptions options;
    String toastMessage;

    private FirebaseFirestore db;
    private String userId;

    SharedPreferences sharedPreferences;
    private String detectedTone = "";

    // for app-wide shared prefs
    public static final String myPrefs = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions();
        // setting theme
        String theme = ThemeChangeFragment.whichTheme(this);
        switch(theme){
            case "HeartsTheme":
                setTheme(R.style.HeartsTheme);
                break;
            case "JournalTheme":
                setTheme(R.style.JournalTheme);
                break;
            case "NauticalTheme":
                setTheme(R.style.NavalTheme);
                break;
            case "DefaultTheme":
                setTheme(R.style.DefaultTheme);
                break;
            case "":
                break;
        }
        // get app-wide shared prefs
        sharedPreferences = getApplicationContext().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);

        editText = findViewById(R.id.EditText);
        titleEditText = findViewById(R.id.note_activity_title_editText);
        intent = getIntent();

        // Set up the note if you are just updating
        if (intent.getIntExtra(WRITE_MODE, 0) == UPDATE_NOTE) {
            // set the mode
            mode = UPDATE_NOTE;
            docId = intent.getStringExtra(ID);
            title = intent.getStringExtra(TITLE);
            // Set the content of the edit text
            editText.setText(intent.getStringExtra(CONTENT));
            titleEditText.setText(title);
        }
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                title = s.toString();
                Log.d("title updated to", title);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
            userId = firebaseUser.getUid();

        db = FirebaseFirestore.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get string from Edit Text
                if (title != null) {
                    title = titleEditText.getText().toString();
                }

                if (!editText.getText().toString().isEmpty()) {

                    // Only do save if you have content
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
                            double max = 0;
                            // Take the emotion with the highest score
                            for (ToneScore score : scores) {
                                if (score.getScore() > max) {
                                    max = score.getScore();
                                    detectedTone = score.getToneName().toUpperCase();
                                }
                            }
                            toastMessage =
                                    "The following emotions were detected:\n\n"
                                            + detectedTone.toUpperCase();

                            // Save to firebase
                            switch (mode) {
                                case NEW_NOTE:
                                    newNote = new NotesBuilder(title, content, detectedTone, currentLatitude, currentLongitude, null, userId);
                                    Log.d("title", title);
                                    newNote = new NotesBuilder(title, content, detectedTone, currentLatitude, currentLongitude, null, userId);
                                    db.collection("users").document(userId)
                                            .collection("notes").add(newNote);
                                    break;
                                case UPDATE_NOTE:
                                    DocumentReference notesRef = db.collection("users").document(userId)
                                            .collection("notes").document(docId);
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("id", docId);
                                    updates.put("content", content);
                                    updates.put("title", titleEditText.getText().toString());
                                    updates.put("latitude", currentLatitude);
                                    updates.put("longitude", currentLongitude);
                                    updates.put("emotion", detectedTone);
                                    updates.put("timestamp", FieldValue.serverTimestamp());
                                    notesRef.update(updates)
                                            .addOnSuccessListener(aVoid -> Log.d("EXS", "DocumentSnapshot successfully updated!"))
                                            .addOnFailureListener(e -> Log.w("EXS", "Error updating document", e));
                                    break;
                            }

                            // Run the toast on UI
                            runOnUiThread(() -> Toast.makeText(getBaseContext(),
                                    toastMessage, Toast.LENGTH_LONG).show());
                        }
                    });
                }
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
        // setting theme
        // setting theme
        String theme = ThemeChangeFragment.whichTheme(this);
        switch(theme){
            case "HeartsTheme":
                setTheme(R.style.HeartsTheme);
                break;
            case "JournalTheme":
                setTheme(R.style.JournalTheme);
                break;
            case "NauticalTheme":
                setTheme(R.style.NavalTheme);
                break;
            case "DefaultTheme":
                setTheme(R.style.DefaultTheme);
                break;
            case "":
                break;
        }
    }


    private void initLocationManager(){
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            // Log.d provider will print GPS
            assert provider != null;
            locationManager.requestLocationUpdates(provider, 0, 0, this);
            Location location = locationManager.getLastKnownLocation(provider);
            // One situation to use callback manually
            onLocationChanged(location);
        }
        catch (SecurityException e) {
            Log.wtf("Security Exception", Arrays.toString(e.getStackTrace()));
        }
    }

    public void onLocationChanged(Location location) {
        if (location == null) return;
        // location object gets you current latitude and long of phone
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        Log.d("EXS", "Lat: " + currentLatitude + "Long: " + currentLongitude);
    }

    public void onDestroy(){
        super.onDestroy();
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle bundle) {}

    public void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        else
            initLocationManager();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initLocationManager();
        }
    }
}