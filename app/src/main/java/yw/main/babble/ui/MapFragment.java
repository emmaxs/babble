package yw.main.babble.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import yw.main.babble.MapsActivity;
import yw.main.babble.R;
import yw.main.babble.notes.NotesBuilder;

public class MapFragment extends Fragment {

    private FirebaseFirestore db;
    int[] totalEmotionCount = new int[]{0,0,0,0,0,0,0,0};
    int[] userEmotionCount = new int[]{0,0,0,0,0,0,0,0};


    // EMOTION CONSTANTS
    public static final String JOY = "JOY";
    public static final String FEAR = "FEAR";
    public static final String SADNESS = "SADNESS";
    public static final String ANGER = "ANGER";
    public static final String CONFIDENT = "CONFIDENT";
    public static final String TENTATIVE = "TENTATIVE";
    public static final String ANALYTICAL = "ANALYTICAL";
    public static final String UNKNOWN = "UNKNOWN";

    public static final int ALL_EMOTIONS = 1;
    public static final int YOUR_EMOTIONS = 0;

    ColumnChartView chart;
    ColumnChartData data;
    private Spinner sortBySpinner;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_map, container, false);

//      Button to open the map
        (root.findViewById(R.id.open_map_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });

        // Set the spinner
        sortBySpinner = root.findViewById(R.id.chart_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.chart_array, android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(spinnerAdapter);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateChart(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get the chart view
        chart = root.findViewById(R.id.chart);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                db = FirebaseFirestore.getInstance();
                db.collectionGroup("notes").get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                // ...
                                // Keep list of ones to toss
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    NotesBuilder note = doc.toObject(NotesBuilder.class);
                                    Log.d("EXS", "found note" + note.getEmotion());
                                    switch (note.getEmotion()) {
                                        // Make sure in sync with chart labels
                                        case JOY:
                                            totalEmotionCount[0]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[0]++;
                                            }
                                            break;
                                        case SADNESS:
                                            totalEmotionCount[1]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[1]++;
                                            }
                                            break;
                                        case FEAR:
                                            totalEmotionCount[2]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[2]++;
                                            }
                                            break;
                                        case ANGER:
                                            totalEmotionCount[3]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[3]++;
                                            }
                                            break;
                                        case CONFIDENT:
                                            totalEmotionCount[4]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[4]++;
                                            }
                                            break;
                                        case TENTATIVE:
                                            totalEmotionCount[5]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[5]++;
                                            }
                                            break;
                                        case ANALYTICAL:
                                            totalEmotionCount[6]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[6]++;
                                            }
                                            break;
                                        default:
                                            totalEmotionCount[7]++;
                                            if (note.getUserId() != null && note.getUserId().equals(userId)) {
                                                userEmotionCount[7]++;
                                            }
                                    }
                                }
                                Log.d("exs", userEmotionCount[1] + "" + userEmotionCount[2] + "" + userEmotionCount[7] + "" );
                                // Run the toast on UI
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        generateChart(YOUR_EMOTIONS);
                                    }
                                });
                            }
                        });
            }
        });
        return root;
    }

    public void generateChart(int chartType) {
        // Wait to finish getting the documents

        int numColumns = 8; // There are 8 emotions
        List<Column> columns = new ArrayList<Column>();
        for (int i = 0; i < numColumns; ++i) {

            ArrayList<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            switch (chartType) {
                case ALL_EMOTIONS:
                    values.add(new SubcolumnValue((float) totalEmotionCount[i], ChartUtils.pickColor()));
                    break;
                    default:
                        values.add(new SubcolumnValue((float) userEmotionCount[i], ChartUtils.pickColor()));
            }
            Column column = new Column(values);

            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }
        data = new ColumnChartData(columns);

        chart.setColumnChartData(data);
        chart.setZoomEnabled(false);

    }
}