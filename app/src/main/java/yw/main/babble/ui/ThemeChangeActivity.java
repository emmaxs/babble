package yw.main.babble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import yw.main.babble.R;

public class ThemeChangeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.JournalTheme);
        setTitle("Settings");
        setContentView(R.layout.activity_theme_change);
    }
}
