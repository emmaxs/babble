package yw.main.babble.font;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Glyphs {
    private static final String TAG = Glyphs.class.getSimpleName();
    public static final int CHAR_BITMAP_HEIGHT = 12;
    public static final int CHAR_BITMAP_WIDTH = 8;
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

    public static final Map<Character, Point> CHAR_UPPER_LEFT;
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

    public void buildMap() {
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
    public void drawString(Canvas canvas, String text, int x, int y) {
        if(canvas == null) {
            Log.d(TAG, "Canvas Is NUll");
        } else {
            for(int i = 0; i < text.length(); i++) {
                Character ch = text.charAt(i);
                if(glyphs.get(ch) != null) {
                    canvas.drawBitmap(glyphs.get(ch), x + (i* CHAR_BITMAP_WIDTH), y, null);
                }
            }
        }
    }
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
