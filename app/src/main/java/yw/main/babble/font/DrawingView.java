package yw.main.babble.font;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DrawingView extends View {
    private Paint mPaint;
    private float px, py;
    private float buttonDrawerHeight = 0.1f;
    private float backButtonWidth = 0.2f;
    private float clearButtonWidth = 0.3f;
    private float saveButtonWidth = 0.3f;
    private float nextButtonWidth = 0.2f;
    private static final int DRAW_AREA = 0;
    private static final int BACK_AREA = 1;
    private static final int CLEAR_AREA = 2;
    private static final int SAVE_AREA = 3;
    private static final int NEXT_AREA = 4;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private int startBtnPressLoc = -1;
    Context context;
    private Paint circlePaint;
    private Path circlePath;

    public DrawingView(Context c, Paint paint) {
        super(c);
        context=c;
        mPath = new Path();
        this.mPaint = paint;
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        drawButtons();
    }

    private void drawButtons() {
        Paint btnPaint = new Paint();
        btnPaint.setAntiAlias(true);
        btnPaint.setColor(Color.RED);
        mCanvas.drawRect(0, getHeight()*(1-buttonDrawerHeight), backButtonWidth*getWidth(), getHeight(), btnPaint);
        btnPaint.setColor(Color.MAGENTA);
        mCanvas.drawRect(backButtonWidth*getWidth(), getHeight()*(1-buttonDrawerHeight), (backButtonWidth+clearButtonWidth)*getWidth(), getHeight(), btnPaint);
        btnPaint.setColor(Color.BLUE);
        mCanvas.drawRect((backButtonWidth+clearButtonWidth)*getWidth(), getHeight()*(1-buttonDrawerHeight), (backButtonWidth+clearButtonWidth+saveButtonWidth)*getWidth(), getHeight(), btnPaint);
        btnPaint.setColor(Color.GREEN);
        mCanvas.drawRect(getWidth()*(1-nextButtonWidth),getHeight()*(1-buttonDrawerHeight), getWidth(), getHeight(), btnPaint);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath( mPath,  this.mPaint);
        canvas.drawPath( circlePath,  circlePaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  this.mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                handleUp(x, y);
                break;
        }
        px = x;
        py = y;
        return true;
    }

    private void handleDown(float x, float y) {
        switch(checkArea(x, y)) {
            case DRAW_AREA:
                touch_start(x, y);
                invalidate();
                break;
            case BACK_AREA:
            case CLEAR_AREA:
            case SAVE_AREA:
            case NEXT_AREA:
                startBtnPressLoc = checkArea(x, y);
                break;
        }

    }

    private int checkArea(float x, float y) {
        if(y < (1-buttonDrawerHeight)*getHeight())
            return DRAW_AREA;
        if(x < getWidth() * backButtonWidth)
            return BACK_AREA;
        if(x < getWidth() * (clearButtonWidth + backButtonWidth))
            return CLEAR_AREA;
        if(x < getWidth() * (clearButtonWidth + backButtonWidth + saveButtonWidth))
            return SAVE_AREA;
        return NEXT_AREA;
    }

    private void handleMove(float x, float y) {
        if(checkArea(x, y) != DRAW_AREA && checkArea(px, py) == DRAW_AREA) {
            touch_up();
        } else if (checkArea(x, y) == DRAW_AREA && checkArea(px, py) != DRAW_AREA) {
            touch_start(x, y);
        }
        switch(checkArea(x, y)) {
            case DRAW_AREA:
                touch_move(x, y);
                invalidate();
                break;
            case BACK_AREA:
                break;
            case CLEAR_AREA:
                break;
            case SAVE_AREA:
                break;
            case NEXT_AREA:
                break;
        }

    }

    private void handleUp(float x, float y) {
        switch(checkArea(x, y)) {
            case DRAW_AREA:
                touch_up();
                invalidate();
                break;
            case BACK_AREA:
                if(startBtnPressLoc == BACK_AREA) {
                    back();
                }
                break;
            case CLEAR_AREA:
                if(startBtnPressLoc == CLEAR_AREA) {
                    clear();
                }
                break;
            case SAVE_AREA:
                if(startBtnPressLoc == SAVE_AREA) {
                    save();
                }
                break;
            case NEXT_AREA:
                if(startBtnPressLoc == NEXT_AREA){
                    next();
                }
                break;
        }
    }

    private void next() {
        Log.d("button","next");
    }

    private void save() {
        Log.d("button","save");
        Bitmap drawing = getDrawingCache();
        drawing = ThumbnailUtils.extractThumbnail(drawing, 256, 256);
        SaveBitmapAsyncTask task = new SaveBitmapAsyncTask();
        task.execute(new BitmapAndChar(drawing, 'a'));
    }

    private void clear() {
        Log.d("button","clear");
        setDrawingCacheEnabled(false);
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        invalidate();
        setDrawingCacheEnabled(true);
    }

    private void back() {
        Log.d("button","back");
    }

    private class SaveBitmapAsyncTask extends AsyncTask<BitmapAndChar, Void, Void> {
        @Override
        protected Void doInBackground(BitmapAndChar... bitmaps) {
            //TODO: write bitmap to file with some sorta name like "font_<bitmaps[0].getChar()>.bmp"
            return null;
        }
    }

    private class BitmapAndChar {
        private Bitmap bitmap;
        private char aChar;
        public BitmapAndChar(Bitmap bp, char c) {
            bitmap = bp;
            aChar = c;
        }
        public Bitmap getBitmap() {
            return bitmap;
        }
        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
        public char getaChar() {
            return aChar;
        }
        public void setaChar(char aChar) {
            this.aChar = aChar;
        }
    }
}