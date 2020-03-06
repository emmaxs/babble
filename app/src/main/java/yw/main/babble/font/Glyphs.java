package yw.main.babble.font;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Glyphs {
    private static final String TAG = Glyphs.class.getSimpleName();
    public static final int CHAR_BITMAP_HEIGHT = 180;
    public static final int CHAR_BITMAP_WIDTH = 120;
    private Bitmap bitmap;
    private Map<Character, Bitmap> glyphs = new HashMap<Character, Bitmap>(62);
    public static final char[] CHARACTERS_L = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z' };
    public static final char[] CHARACTERS_U = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z' };
    public static final char[] NUMBERS_AND_PARENTHESES = new char[] { '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', '(', ')', '{', '}', '[', ']'};
    public static final char[] PUNCTUATION = new char[] { '.', ',', ':', ';', '?', '!', '@',
            '#', '$', '%', '^', '&', '*', '-', '_', '=', '+', '\\', '|',
            '\'', '"', '/', '<', '>', '~', '`'};
    public static final char[] ALL_GLYPHS = new char[] {'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '0', '(', ')', '{', '}', '[', ']', '.',
            ',', ':', ';', '?', '!', '@', '#', '$', '%', '^', '&', '*', '-',
            '_', '=', '+', '\\', '|', '\'', '"', '/', '<', '>', '~', '`'};

    public static final Map<Character, Point> CHAR_UPPER_LEFT; //map to store upper left corner location of each char in the bitmap

    static {
        CHAR_UPPER_LEFT = new HashMap<>();
        for(int i = 0; i < 26; i++) {
            CHAR_UPPER_LEFT.put(CHARACTERS_L[i], new Point(i* CHAR_BITMAP_WIDTH, 0));
        }
        for(int i = 0; i < 26; i++) {
            CHAR_UPPER_LEFT.put(CHARACTERS_U[i], new Point(i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT));
        }
        for(int i = 0; i < NUMBERS_AND_PARENTHESES.length; i++) {
            CHAR_UPPER_LEFT.put(NUMBERS_AND_PARENTHESES[i], new Point(i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT *2));
        }
        for(int i = 0; i < PUNCTUATION.length; i++) {
            CHAR_UPPER_LEFT.put(PUNCTUATION[i], new Point(i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT *3));
        }
    }


    public Glyphs(Bitmap bitmap) {
        this.bitmap = bitmap;
        buildMap();
    }

    /**
     * Builds map from characters to font bitmaps
     */
    private void buildMap() {
        for(int i = 0; i < 26; i++) {
            glyphs.put(CHARACTERS_L[i], Bitmap.createBitmap(bitmap, i* CHAR_BITMAP_WIDTH, 0, CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT));
        }
        for(int i = 0; i < 26; i++) {
            glyphs.put(CHARACTERS_U[i], Bitmap.createBitmap(bitmap, i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT, CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT));
        }
        for(int i = 0; i < NUMBERS_AND_PARENTHESES.length; i++) {
            glyphs.put(NUMBERS_AND_PARENTHESES[i], Bitmap.createBitmap(bitmap, i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT *2, CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT));
        }
        for(int i = 0; i < PUNCTUATION.length; i++) {
            glyphs.put(PUNCTUATION[i], Bitmap.createBitmap(bitmap, i* CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT *3, CHAR_BITMAP_WIDTH, CHAR_BITMAP_HEIGHT));
        }
    }

    /**
     * Draws a string on a canvas using the font we have stored
     * @param canvas Canvas on which to draw
     * @param text Text you want to draw
     * @param x X position of upper left corner of text
     * @param y Y position of upper left corner of text
     * @param size Font size - generally 0<size<=1.
     */
    public void drawString(Canvas canvas, String text, int cursorLocation, int x, int y, double size) {
        if(canvas == null) {
            Log.d(TAG, "Canvas Is Null");
        } else {
            int textPosition = 0;
            int i = 0;
            for(; i < text.length(); i++) {
                Character ch = text.charAt(i);
                if(i == cursorLocation) {
                    Paint p = new Paint();
                    p.setColor(Color.BLACK);
                    p.setStrokeWidth(3);
                    p.setAntiAlias(true);
                    Log.d("drawString()","cursorLocationFound");
                    canvas.drawLine((float) (x+(textPosition*CHAR_BITMAP_WIDTH*size)), y, (float) (x+(textPosition*(CHAR_BITMAP_WIDTH*size))), (float) (y+(CHAR_BITMAP_HEIGHT*size)), p);
                }

                if(glyphs.get(ch) != null) {
//                    canvas.drawBitmap(glyphs.get(ch), x + (i* CHAR_BITMAP_WIDTH), y, null);
                    canvas.drawBitmap(glyphs.get(ch), null, new Rect(
                                    (int) (x+(textPosition*(CHAR_BITMAP_WIDTH*size))), y,
                            (int) (x+((textPosition*(CHAR_BITMAP_WIDTH*size) + (CHAR_BITMAP_WIDTH*size)))), (int) (y+(CHAR_BITMAP_HEIGHT*size))),
                            null);
                }
                textPosition++;
                if(ch.equals('\n')) {
                    y+= CHAR_BITMAP_HEIGHT*size;
                    textPosition = 0;
                }
            }
            if(i == cursorLocation) {
                Paint p = new Paint();
                p.setColor(Color.BLACK);
                p.setStrokeWidth(3);
                p.setAntiAlias(true);
                Log.d("drawString()","cursorLocationFound");
                canvas.drawLine((float) (x+(textPosition*CHAR_BITMAP_WIDTH*size)), y, (float) (x+(textPosition*(CHAR_BITMAP_WIDTH*size))), (float) (y+(CHAR_BITMAP_HEIGHT*size)), p);
            }
        }
    }

    /**
     *
     * @return transparent bitmap that is large enough to store all characters
     */
    public static Bitmap getEmptyBitmap() {
        return Bitmap.createBitmap(getEntireBitmapWidth(), getEntireBitmapHeight(), Bitmap.Config.ARGB_8888);
    }

    public static int getEntireBitmapHeight() {
        return CHAR_BITMAP_HEIGHT * 4;
    }

    public static int getEntireBitmapWidth() {
        return CHAR_BITMAP_WIDTH * 26;
    }
}
