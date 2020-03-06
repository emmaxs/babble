package yw.main.babble.font;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatEditText;

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

    /**
     * Grabs bitmap from files, sets typeface to put cursor in
     * proper place.
     */
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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        int glyphPacking = (int) (getWidth() / (Glyphs.CHAR_BITMAP_WIDTH*TEXT_SIZE))-1;
        glyphs.drawString(canvas, textWrap(getText().toString(), glyphPacking), getSelectionStart(),10, 30, TEXT_SIZE);
    }

    /**
     * Line wrapping method
     * @param text text to wrap
     * @param glyphPacking width of line to wrap to
     * @return text wrapped to fit glyphPacking
     */
    private String textWrap(String text, int glyphPacking) {
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
}
