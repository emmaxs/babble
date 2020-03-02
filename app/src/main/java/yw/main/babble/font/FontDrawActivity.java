package yw.main.babble.font;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import yw.main.babble.R;
import yw.main.babble.font.BitmapBuilderAndSaver;
import yw.main.babble.font.DrawingView;
import yw.main.babble.font.Glyphs;

public class FontDrawActivity extends Activity {
    private static final int PERMISSIONS_REQUEST = 1;
    private BitmapBuilderAndSaver builderAndSaver = new BitmapBuilderAndSaver();
    private DrawingView dv ;
    private TextView letterHint;
    private int currCharIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_draw);
        checkPermissions();
        builderAndSaver.loadBitmap(this);
        dv = new DrawingView(this);
        ((FrameLayout) findViewById(R.id.font_draw_frame_layout)).addView(dv);
        ((Button) findViewById(R.id.button_clearDrawing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dv.clear();
            }
        });
        ((Button) findViewById(R.id.button_backLetter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDrawing();
            }
        });
        ((Button) findViewById(R.id.button_saveDrawing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDrawing();
            }
        });
        ((Button) findViewById(R.id.button_nextLetter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextLetter();
            }
        });
        letterHint = (TextView) findViewById(R.id.letter_hint_text_view);
        updateHint();
    }

    private void checkPermissions() {

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }
    }


    private void updateHint() {
        letterHint.setText("Please write character " + Glyphs.ALL_GLYPHS[currCharIndex]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                break;

        }
    }

    private void nextLetter() {
        currCharIndex++;
        if(currCharIndex > Glyphs.ALL_GLYPHS.length-1) currCharIndex = Glyphs.ALL_GLYPHS.length-1;
        updateHint();
        saveDrawing();
        dv.clear();
    }

    private void saveDrawing() {
        builderAndSaver.putGlyph(dv.getBitmap(), Glyphs.ALL_GLYPHS[currCharIndex]);
        ((ImageView) findViewById(R.id.font_draw_image_view)).setImageBitmap(builderAndSaver.getBitmap());
        builderAndSaver.saveBitmap(this);
    }

    private void backDrawing() {
        currCharIndex--;
        if(currCharIndex < 0) currCharIndex = 0;
        updateHint();
        saveDrawing();
        dv.clear();
    }

}
