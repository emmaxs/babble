package yw.main.babble.font;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import yw.main.babble.R;

public class FontDrawActivity extends Activity {
    private BitmapBuilderAndSaver builderAndSaver;
    private DrawingView dv ;
    private TextView letterHint;
    private int currCharIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_draw);

        builderAndSaver = new BitmapBuilderAndSaver();
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
        ((Button) findViewById(R.id.button_nextLetter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextLetter();
            }
        });
        ((Button) findViewById(R.id.button_brush_size)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrushSize();
            }
        });
        ((Button) findViewById(R.id.button_brush_color)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrushColor();
            }
        });
        letterHint = (TextView) findViewById(R.id.letter_hint_text_view);
        updateHint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGlyph();
        saveFont();
    }

    private void updateHint() {
        letterHint.setText("Write " + Glyphs.ALL_GLYPHS[currCharIndex] + ". Remember to use whole screen!");
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
        builderAndSaver.putGlyph(dv.getScaledBitmap(), Glyphs.ALL_GLYPHS[currCharIndex]);
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


    private void changeBrushColor() {
        ColorPickerDialogBuilder.with(this)
                .setTitle("Choose Stroke Color")
                .initialColor(Color.BLACK)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        Toast.makeText(FontDrawActivity.this, "Chose Color 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                    }
                }).setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        dv.setStrokeColor(lastSelectedColor);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).build().show();
    }

    private void changeBrushSize() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radio_dialog_layout);
        RadioGroup rg = dialog.findViewById(R.id.dialog_radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("onCheckedChanged", String.valueOf(checkedId));
                switch (checkedId) {
                    case R.id.radio_button_small:
                        dv.setStrokeSize(20);
                        break;
                    case R.id.radio_button_medium:
                        dv.setStrokeSize(50);
                        break;
                    case R.id.radio_button_large:
                        dv.setStrokeSize(100);
                        break;
                }
                dialog.cancel();
            }
        });
        dialog.show();
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
