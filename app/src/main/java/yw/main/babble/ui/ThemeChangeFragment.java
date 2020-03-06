package yw.main.babble.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import yw.main.babble.R;
import yw.main.babble.font.FontDrawActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemeChangeFragment extends PreferenceFragmentCompat{

    public SharedPreferences sharedPreferences;
    public static final String THEME_PREFS = "theme_prefs";
    public static final String THEME = "theme";

    public ThemeChangeFragment() {
        // Required empty public constructor
    }

    public void onCreatePreferences(Bundle bundle, String string){
        addPreferencesFromResource(R.xml.theme_prefs);
        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);

        Preference pref_paper = findPreference("paper");
        pref_paper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String theme = "JournalTheme";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(THEME, theme);
                //getActivity().recreate();
                return true;
            }
        });

        Preference pref_default = findPreference("default");
        pref_default.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String theme = "DefaultTheme";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(THEME, theme);
                //getActivity().recreate();
                return true;
            }
        });

        Preference pref_hearts = findPreference("hearts");
        pref_hearts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                String theme = "HeartsTheme";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(THEME, theme);
                //getActivity().recreate();
                return true;
            }
        });

        Preference pref_nautical = findPreference("nautical");
        pref_nautical.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String theme = "NauticalTheme";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(THEME, theme);
                //getActivity().recreate();
                return true;
            }
        });
    }

    // context should be app context
    public static String whichTheme(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(THEME_PREFS, 0);
        // return null string if no theme is chosen
        return sharedPreferences.getString(THEME, "");
    }

}
