package yw.main.babble.font;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public DrawingView(Context c) {
        super(c);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(100);
        context=c;
        mPath = new Path();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath( mPath,  this.mPaint);
        canvas.drawPath( circlePath,  circlePaint);
    }



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
        return true;
    }

    public void setImage(Bitmap character) {
        mCanvas.drawBitmap(character, null, new Rect(0, 0, getWidth(), getHeight()), mPaint);
    }
    private void handleDown(float x, float y) {
        touch_start(x, y);
        invalidate();
    }

    private void handleMove(float x, float y) {
        touch_move(x, y);
        invalidate();
    }

    private void handleUp(float x, float y) {
        touch_up();
        invalidate();
    }

    public Bitmap getBitmap() {
        Bitmap drawing = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(drawing);
        this.draw(canvas);
        int width = drawing.getWidth();
        int height = drawing.getHeight();
        int[] pixels = new int[width*height];
        drawing.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int x = 0; x < pixels.length; x++) {
            if(pixels[x] == Color.WHITE) pixels[x] = 0;
        }
        return Bitmap.createScaledBitmap(Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888), Glyphs.CHAR_BITMAP_WIDTH, Glyphs.CHAR_BITMAP_HEIGHT, false);
    }

    public void clear() {
        setDrawingCacheEnabled(false);
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
        invalidate();
        setDrawingCacheEnabled(true);
    }

}