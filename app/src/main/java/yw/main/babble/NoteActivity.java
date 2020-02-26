package yw.main.babble;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.tone_analyzer.v3.model.ToneScore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import yw.main.babble.ui.NotesFragment;

public class NoteActivity extends AppCompatActivity {
    EditText editText;
    int fileNumber;
    String filename = "";

    IamAuthenticator authenticator;
    ToneAnalyzer toneAnalyzer;
    ToneOptions options;
    String textToAnalyze;
    String toastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.EditText);
        Intent intent = getIntent();
        if (intent != null) {
            fileNumber = intent.getIntExtra(NotesFragment.NOTE_INDEX, 0);
        }
        else {
            File[] files = getFilesDir().listFiles();
            fileNumber = files.length + 1;
        }

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

                // get a new file name to save
                Save(filename);
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


    // Change function to work with firebase
    public void Save(String fileName) {
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(editText.getText().toString());
            out.close();
            Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
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
