package yw.main.babble.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneScore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import yw.main.babble.R;
import yw.main.babble.ui.NotesFragment;

/*// Pause the upload
uploadTask.pause();

// Resume the upload
        uploadTask.resume();

// Cancel the upload
        uploadTask.cancel();*/

public class NoteActivity extends AppCompatActivity {
    EditText editText;
    int fileNumber;
    String filename = "";
    Intent intent;

    IamAuthenticator authenticator;
    ToneAnalyzer toneAnalyzer;
    ToneOptions options;
    String textToAnalyze;
    String toastMessage;

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage database;
    private String userId;
    private StorageReference storageReference;

    private String emotions;

    private SharedPreferences sharedPreferences;
    // for app-wide shared prefs
    public static final String myPrefs = "MyPrefs";
    public static final String myMap = "Map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get app-wide shared prefs
        sharedPreferences = getApplicationContext().getSharedPreferences(myPrefs, Context.MODE_PRIVATE);


        editText = findViewById(R.id.EditText);
        intent = getIntent();
        if (intent.getIntExtra(NotesFragment.NOTE_INDEX, 0) != 0) {
            fileNumber = intent.getIntExtra(NotesFragment.NOTE_INDEX, 0) + 1;
        }
        else {
            File[] files = getFilesDir().listFiles();
            fileNumber = files.length + 1;
        }


        // firebase things (we know there wil be a current user since you must be
        // logged in to get to this page
        database = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Create a storage reference from our app
        storageReference = database.getReference();

        userId = firebaseUser.getUid();

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
                                String detectedTones = "";
                                for(ToneScore score:scores) {
                                    if(score.getScore() > 0.5f) {
                                        detectedTones += score.getToneName() + " ";
                                    }
                                }
                                toastMessage =
                                        "The following emotions were detected:\n\n"
                                                + detectedTones.toUpperCase();

                                // need to be able to access these when storing to firebase
                                emotions = detectedTones;

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

                // get a new file name to save (to firebase)
                Save(filename);
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

    //TODO:
    // Change function to work with firebase
    public void Save(String fileName) {

        // all files will exist locally, but not necessarily be shown by notesFragment
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(editText.getText().toString());
            out.close();
            Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        // saving to firebase if wifi is good
        if(wifiConnection()){
            if (FileExists(fileName)){
                Uri file_to_upload = Uri.fromFile(new File(fileName));

                // setting up hierarchy of files: eventually want /users/userID/Note.txt

                // Create a child reference
                // usersRef points to "users"
                StorageReference usersRef = storageReference.child("users");

                // Child references can also take paths
                // userIDreft now points to "users/userID
                // usersRef still points to "users"
                StorageReference userIDref = usersRef.child(userId);

                // usersRef = userIDref.getParent();

                // StorageReference rootRef = userIDref.getRoot();

                StorageReference noteRef = userIDref.child(fileName);
                // database.getReference().child("users").child(userId).child(fileName).putFile(file_to_upload);
                UploadTask uploadTask = noteRef.putFile(file_to_upload);

                // from firebase console
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getBaseContext(), "Failed to save to Firebase...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Toast.makeText(getBaseContext(), "Note saved to Firebase!", Toast.LENGTH_SHORT).show();
                    }
                });

                // TODO
                // add emotions to the file metadata
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("files/txt")
                        .setCustomMetadata("emotion", emotions)
                        .build();

                noteRef.updateMetadata(metadata)
                        .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                // Updated metadata is in storageMetadata
                                Log.d("metadata", "Metadata stored successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d("metadata", "Error: metadata not stored.");
                            }
                        });
            }
        }


        // TODO: if there are any notes stored locally but not in firebase, save them to firebase
        // queue of ids
        // save on connect to wifi



        // Here we need to create or update map, which links users to their text files
        // this is used in notesFragment prepareNotes-- because we want to load files from local storage,
        // we need to know which user is associated with which files in local
        // if the user to files map exists, just need to update it
        if(sharedPreferences.contains(myMap)){
            Map<String, ArrayList<String>> inputMap = loadMapfromPreferences();

            // either the user exists in the map or doesn't
            if (inputMap.containsKey(userId)){
                // just update the ArrayList
                inputMap.get(userId).add(fileName);
            }
            else{
                // add new arraylist with filename in
                ArrayList<String> files = new ArrayList<String>();
                files.add(fileName);
                inputMap.put(userId, files);
            }

        }
        // else, it means no file has been put in for any user yet
        else{
            Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
            // since if the map exists, it means there are no files associated with this user, put the user in
            ArrayList<String> files = new ArrayList<String>();
            files.add(fileName);
            map.put(userId, files);
            saveMaptoPreferences(map);
        }
    }

    public void saveMaptoPreferences(Map<String, ArrayList<String>> map){
        // if the preferences aren't null, save the map object to the preferences
        if (sharedPreferences != null){
            JSONObject json = new JSONObject(map);
            String jsonString = json.toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // if the userid as key already exists in map, then update it. if not, create
            editor.putString(myMap, jsonString);
            editor.commit();
        }
    }

    // specifically loads the user to files map!!
    public Map<String, ArrayList<String>> loadMapfromPreferences(){
        Map<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
        try{
            if(sharedPreferences != null){
                String jsonString = sharedPreferences.getString(myMap, (new JSONObject()).toString());
                JSONObject json = new JSONObject((jsonString));
                Iterator<String> iterator = json.keys();
                while(iterator.hasNext()){
                    String string_key = iterator.next();
                    ArrayList<String> string_array = (ArrayList<String>) json.get(string_key);
                    output.put(string_key, string_array);
                }
            }
        }
        catch(Exception e){e.printStackTrace();}
        return output;
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