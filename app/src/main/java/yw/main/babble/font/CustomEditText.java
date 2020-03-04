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
//        canvas.drawColor(Color.WHITE);
        int glyphPacking = (int) (getWidth() / (Glyphs.CHAR_BITMAP_WIDTH*TEXT_SIZE))-1;
        String result = getText().toString();
        int counter = 0;
        for(int i = 0; i < result.length(); i++) {
            if(result.charAt(i) == '\n')
                counter = 0;
            if(counter == glyphPacking) {
                if(result.charAt(i) != ' ') {
                    if(result.lastIndexOf(' ') == -1)
                        result = result.substring(0, i) + "\n" + result.substring(i);
                    else
                        result = result.substring(0, result.substring(0, i).lastIndexOf(' ')) + "\n" + result.substring(result.substring(0, i).lastIndexOf(' ')+1);
                } else {
                    result = result.substring(0, i) + "\n" + result.substring(i+1);
                }
                counter = 0;
            }
            counter++;
        }

        Log.d("draw()",result);
        glyphs.drawString(canvas, result, 10, 30, TEXT_SIZE);
    }
}
