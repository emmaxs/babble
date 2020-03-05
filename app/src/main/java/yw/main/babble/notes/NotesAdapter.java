package yw.main.babble.notes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import yw.main.babble.R;

public class NotesAdapter extends BaseAdapter {

    // EMOTION CONSTANTS
    public static final String JOY = "JOY";
    public static final String FEAR = "FEAR";
    public static final String SADNESS = "SADNESS";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String TENTATIVE = "TENTATIVE";
    public static final String ANALYTICAL = "ANALYTICAL";
    public static final String CONFIDENT = "CONFIDENT";
    public static final String ANGER = "ANGER";

    private ArrayList<NotesBuilder> notesList;
    private Activity activity;

    public class ViewHolder{
        public TextView title, content;
        public ImageView icon;
    }

    public NotesAdapter(ArrayList notesList, Activity activity) {
        this.notesList = notesList;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.content = convertView.findViewById(R.id.content);
            viewHolder.icon = convertView.findViewById(R.id.imgIcon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the Note
        NotesBuilder note = notesList.get(position);
        viewHolder.title.setText(note.getTitle());
        viewHolder.content.setText(note.getContent());
        viewHolder.icon.setImageResource(getDrawableFromEmotion(note.getEmotion()));

        return convertView;
    }

    public int getDrawableFromEmotion(String emotion) {
        switch (emotion) {
            case JOY:
                return R.drawable.joy;
            case SADNESS:
                return R.drawable.sadness;
            case FEAR:
                return R.drawable.fear;
            case ANGER:
                return R.drawable.anger;
            case TENTATIVE:
                return R.drawable.tentative;
            case ANALYTICAL:
                return R.drawable.analytical;
            case CONFIDENT:
                return R.drawable.confident;
            default:
                return R.drawable.unknown;
        }
    }

    @Override
    public int getCount() {
        return notesList.size();
    }

    @Override
    public NotesBuilder getItem(int position) {
        return notesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(int position) {
        notesList.remove(position);
        notifyDataSetChanged();
    }

    public void addEntry(NotesBuilder note) {
        notesList.add(note);
        notifyDataSetChanged();
    }

}
