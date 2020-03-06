package yw.main.babble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import yw.main.babble.R;

public class ThemeChangeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        setContentView(R.layout.activity_theme_change);
    }

    public void onResume(){
        super.onResume();

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
}
