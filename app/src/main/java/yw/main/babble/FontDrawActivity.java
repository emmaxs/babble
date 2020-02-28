package yw.main.babble;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import yw.main.babble.font.BitmapBuilderAndSaver;
import yw.main.babble.font.DrawingView;
import yw.main.babble.font.Glyphs;

public class FontDrawActivity extends Activity {
    private BitmapBuilderAndSaver builderAndSaver = new BitmapBuilderAndSaver();
    private DrawingView dv ;
    private TextView letterHint;
    private int currCharIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_draw);
        builderAndSaver.loadBitmap(this);
        dv = new DrawingView(this);
        dv.post(new Runnable() {
            @Override
            public void run() {
                dv.setImage(builderAndSaver.getGlyph(Glyphs.ALL_GLYPHS[currCharIndex]));
            }
        });
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
                prevLetter();
            }
        });
        ((Button) findViewById(R.id.button_saveDrawing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGlyph();
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

    @Override
    protected void onPause() {
        super.onPause();
        saveFont();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    private void updateHint() {
        letterHint.setText("Please write character " + Glyphs.ALL_GLYPHS[currCharIndex]);
    }



    private void nextLetter() {
        saveGlyph();
        currCharIndex++;
        if(currCharIndex > Glyphs.ALL_GLYPHS.length-1) currCharIndex = Glyphs.ALL_GLYPHS.length-1;

        updateHint();
        dv.clear();
        Log.d("nextLetter()", "" + Glyphs.ALL_GLYPHS[currCharIndex]);
        dv.setImage(builderAndSaver.getGlyph(Glyphs.ALL_GLYPHS[currCharIndex]));
    }

    private void saveGlyph() {
        builderAndSaver.putGlyph(dv.getBitmap(), Glyphs.ALL_GLYPHS[currCharIndex]);
    }

    private void prevLetter() {
        saveGlyph();
        currCharIndex--;
        if(currCharIndex < 0) currCharIndex = 0;
        updateHint();

        dv.clear();
        dv.setImage(builderAndSaver.getGlyph(Glyphs.ALL_GLYPHS[currCharIndex]));
    }

    private void saveFont() {
        builderAndSaver.saveBitmap(this);
    }

    private class FontSaveTask extends AsyncTask<Context, Void, Context> {

        @Override
        protected Context doInBackground(Context... contexts) {
            builderAndSaver.saveBitmap(contexts[0]);
            return contexts[0];
        }

        @Override
        protected void onPostExecute(Context aVoid) {
            Toast.makeText(aVoid, "Font Saved Successfully!", Toast.LENGTH_SHORT);
        }
    }
}
