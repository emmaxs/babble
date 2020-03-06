package yw.main.babble.font;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.List;

public class CustomEditText extends AppCompatEditText {
    private static final double TEXT_SIZE = .3;
    private Glyphs glyphs;
    private BitmapBuilderAndSaver builderAndSaver;
    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    private void init() {
        builderAndSaver = new BitmapBuilderAndSaver();
        builderAndSaver.loadBitmap(getContext());
        glyphs = new Glyphs(builderAndSaver.getBitmap());
        setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        setTextSize(26f);
        setLineSpacing(0, .8f);
        setTextColor(Color.WHITE);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
//        Log.d("onTextChanged()", text.toString());
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        int glyphPacking = (int) (getWidth() / (Glyphs.CHAR_BITMAP_WIDTH*TEXT_SIZE))-1;


        Log.d("draw()", String.valueOf(getSelectionStart()));
        int[] location = new int[2];
        Log.d("location y", "" + location[1]);
        glyphs.drawString(canvas, packWordsOneWay(getText().toString(), glyphPacking), getSelectionStart(),location[0] + 10, location[1] + 30, TEXT_SIZE);
    }

    private String packWordsOneWay(String text, int glyphPacking) {
        int counter = 0;
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '\n')
                counter = 0;
            if(counter == glyphPacking) {
                if(text.charAt(i) != ' ') {
                    if(text.lastIndexOf(' ') == -1)
                        text = text.substring(0, i) + "\n" + text.substring(i);
                    else
                        text = text.substring(0, text.substring(0, i).lastIndexOf(' ')) + "\n" + text.substring(text.substring(0, i).lastIndexOf(' ')+1);
                } else {
                    text = text.substring(0, i) + "\n" + text.substring(i+1);
                }
                counter = 0;
            }
            counter++;
        }
        return text;
    }

    private String packWordsRecursive(String text, int glyphPacking) {
        if(text.length() <= glyphPacking) {
            return text;
        } else if(text.substring(0, glyphPacking).contains("\n")) {
            return text.substring(0, text.indexOf('\n')) +
                    packWordsRecursive(text.substring(text.indexOf('\n')+1), glyphPacking);
        } else {
            if(text.substring(0, glyphPacking).contains(" ")) {
                return text.substring(0, text.substring(0, glyphPacking).lastIndexOf(' ')) + "\n" +
                        packWordsRecursive(text.substring(text.substring(0, glyphPacking).lastIndexOf(' ')+1), glyphPacking);
            } else {
                return text.substring(0, glyphPacking) + "\n" +
                        packWordsRecursive(text.substring(glyphPacking), glyphPacking);
            }
        }
    }
}
