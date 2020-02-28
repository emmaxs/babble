package yw.main.babble.font;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatEditText;

public class CustomEditText extends AppCompatEditText {
    private static final double TEXT_SIZE = .5;
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
        String result = "";
        String from = getText().toString();
        for(int i = 0; i < result.length(); i+=glyphPacking) {

        }
//        Log.d("draw()","glyphs " + glyphPacking);
        glyphs.drawString(canvas, getText().toString(), 0, 0, TEXT_SIZE);
    }
}
