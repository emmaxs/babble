package yw.main.babble.notes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class NotesBuilder {

    private String title, emotion,
            content, id;
    private Double latitude;
    private Double longitude;
    @ServerTimestamp private Timestamp timestamp;

    // EMOTION CONSTANTS
    public static final String JOY = "JOY";
    public static final String FEAR = "FEAR";
    public static final String SADNESS = "SADNESS";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String TENTATIVE = "TENTATIVE";
    public static final String ANALYTICAL = "ANALYTICAL";
    public static final String CONFIDENT = "CONFIDENT";
    public static final String ANGER = "ANGER";

    public NotesBuilder() {}

    public NotesBuilder(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public NotesBuilder(String title, String content, String emotion, Double latitude, Double longitude) {
        this.title = title;
        this.content = content;
        this.emotion = emotion;
        // possibly make geopoint
        this.latitude =latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getEmotion() {
        return emotion;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }



}