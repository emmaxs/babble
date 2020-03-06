package yw.main.babble.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
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
    int[] emotionCount = new int[]{0,0,0,0,0,0,0,0};


    // EMOTION CONSTANTS
    public static final String JOY = "JOY";
    public static final String FEAR = "FEAR";
    public static final String SADNESS = "SADNESS";
    public static final String ANGER = "ANGER";
    public static final String CONFIDENT = "CONFIDENT";
    public static final String TENTATIVE = "TENTATIVE";
    public static final String ANALYTICAL = "ANALYTICAL";
    public static final String UNKNOWN = "UNKNOWN";

    String[] labelArray = new String[]{JOY, SADNESS, FEAR, ANGER, CONFIDENT, TENTATIVE, ANALYTICAL, UNKNOWN};

    ColumnChartView chart;
    ColumnChartData data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_map, container, false);

//         Maybe remove this button
        (root.findViewById(R.id.open_map_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
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
                                            emotionCount[0]++;
                                            break;
                                        case SADNESS:
                                            emotionCount[1]++;
                                            break;
                                        case FEAR:
                                            emotionCount[2]++;
                                            break;
                                        case ANGER:
                                            emotionCount[3]++;
                                            break;
                                        case CONFIDENT:
                                            emotionCount[4]++;
                                            break;
                                        case TENTATIVE:
                                            emotionCount[5]++;
                                            break;
                                        case ANALYTICAL:
                                            emotionCount[6]++;
                                            break;
                                        default:
                                            emotionCount[7]++;
                                    }
                                }
                                // Run the toast on UI
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        generateChart();
                                    }
                                });
                            }
                        });
            }
        });
        return root;
    }

    public void generateChart() {
        // Wait to finish getting the documents

        int numColumns = 8; // There are 8 emotions
        List<Column> columns = new ArrayList<Column>();
        for (int i = 0; i < numColumns; ++i) {

            ArrayList<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue((float) emotionCount[i], ChartUtils.pickColor()));
            Column column = new Column(values);

            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }
        data = new ColumnChartData(columns);

        chart.setColumnChartData(data);
        chart.setZoomEnabled(false);

    }
}