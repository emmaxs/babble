package yw.main.babble.notes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import yw.main.babble.R;

public class NotesAdapter extends BaseAdapter {


    private ArrayList<NotesBuilder> notesList;
    private Activity activity;

    public class ViewHolder{
        public TextView title, content;
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the Note
        NotesBuilder note = notesList.get(position);
        viewHolder.title.setText(note.getTitle());
        viewHolder.content.setText(note.getContent());

        return convertView;
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
