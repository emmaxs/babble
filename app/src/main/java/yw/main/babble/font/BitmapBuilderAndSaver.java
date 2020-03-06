package yw.main.babble.font;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import yw.main.babble.R;

public class BitmapBuilderAndSaver {
    private static final String FILE_NAME = "font.png";
    private Bitmap bitmap = null;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;
    StorageReference storageRef;
    String filename = "";

    ByteArrayOutputStream baos;


    public BitmapBuilderAndSaver() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            filename = "users/" + userId + ".png";
            // Create a storage reference from our app
            storageRef = FirebaseStorage.getInstance().getReference().child(filename);
            Log.d("exs", "users/" + userId + ".png");
        }

    }

    // TODO: Make async
    public boolean saveBitmap(Context context) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), FILE_NAME));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("saveBitmap()", e.getMessage());
            return false;
        }
    }

    public void loadBitmap(Context context) {
        try {
            File filePath = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            FileInputStream fileInputStream = new FileInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            bitmap = Bitmap.createBitmap(bitmap);
        } catch (Exception e) {
            Log.e("loadBitmap()", e.getMessage());
            if(e.getMessage().contains("No such file or directory")) {
                Log.d("loadBitmap()", "Creating new bitmap...");
                Drawable d = context.getResources().getDrawable(R.drawable.default_font);
                if(d instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) d).getBitmap();
                }
            }
        }
    }

    public void putGlyph(Bitmap character, char letter) {
        Point upperLeft = Glyphs.CHAR_UPPER_LEFT.get(letter);
        int[] bigBoiPixels = new int[bitmap.getWidth()*bitmap.getHeight()];
        bitmap.getPixels(bigBoiPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int[] lilBoiPixels = new int[character.getWidth()*character.getHeight()];
        character.getPixels(lilBoiPixels, 0, character.getWidth(), 0, 0, character.getWidth(), character.getHeight());

        for(int i = upperLeft.x; i < upperLeft.x+Glyphs.CHAR_BITMAP_WIDTH; i++) {
            for(int j = upperLeft.y; j < upperLeft.y+Glyphs.CHAR_BITMAP_HEIGHT; j++) {
                bigBoiPixels[i+(j*bitmap.getWidth())] =
                        lilBoiPixels[(i-upperLeft.x)+((j-upperLeft.y)*character.getWidth())];
            }
        }
        bitmap = Bitmap.createBitmap(bigBoiPixels, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    }

    public Bitmap getGlyph(char letter) {
        Point upperLeft = Glyphs.CHAR_UPPER_LEFT.get(letter);
        int[] pixels = new int[Glyphs.CHAR_BITMAP_WIDTH*Glyphs.CHAR_BITMAP_HEIGHT];
        int[] srcPixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(srcPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for(int i = 0; i < Glyphs.CHAR_BITMAP_WIDTH; i++) {
            for(int j = 0; j < Glyphs.CHAR_BITMAP_HEIGHT; j++) {
                pixels[i+(j*Glyphs.CHAR_BITMAP_WIDTH)] = srcPixels[(i+upperLeft.x)+((j+upperLeft.y)*bitmap.getWidth())];
            }
        }
        return Bitmap.createBitmap(pixels, Glyphs.CHAR_BITMAP_WIDTH, Glyphs.CHAR_BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
