package yw.main.babble.ui;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import yw.main.babble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemeChangeFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;

    public ThemeChangeFragment() {
        // Required empty public constructor
    }

    public void onCreatePreferences(Bundle bundle, String string){
        addPreferencesFromResource(R.xml.theme_prefs);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

/*    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }*/

}
