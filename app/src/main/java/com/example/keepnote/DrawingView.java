package com.example.keepnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class DrawingView extends View {
    public static final int PEN = 0;
    public static final int BRUSH = 1;
    public static final int HIGHLIGHTER = 2;

    private int currentTool = PEN;
    private Paint paint;
    private Path currentPath;
    private ArrayList<DrawingPath> paths = new ArrayList<>();
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        currentPath = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        updatePaintSettings();
    }

    private void updatePaintSettings() {
        switch (currentTool) {
            case PEN:
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(5f);
                paint.setAlpha(255);
                break;
            case BRUSH:
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(15f);
                paint.setAlpha(255);
                break;
            case HIGHLIGHTER:
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(25f);
                paint.setAlpha(150); // Semi-transparent
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background
        canvas.drawBitmap(canvasBitmap, 0, 0, paint);

        // Draw all saved paths
        for (DrawingPath dp : paths) {
            paint.setColor(dp.color);
            paint.setStrokeWidth(dp.strokeWidth);
            paint.setAlpha(dp.alpha);
            canvas.drawPath(dp.path, paint);
        }

        // Draw the current path
        canvas.drawPath(currentPath, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(currentPath, paint);
                paths.add(new DrawingPath(currentPath, paint.getColor(),
                        paint.getStrokeWidth(), paint.getAlpha()));
                currentPath = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setCurrentTool(int tool) {
        this.currentTool = tool;
        updatePaintSettings();
    }

    public int getCurrentTool() {
        return currentTool;
    }

    public void clearCanvas() {
        paths.clear();
        if (canvasBitmap != null) {
            canvasBitmap.eraseColor(Color.TRANSPARENT);
        }
        invalidate();
    }

    private static class DrawingPath {
        Path path;
        int color;
        float strokeWidth;
        int alpha;

        DrawingPath(Path path, int color, float strokeWidth, int alpha) {
            this.path = new Path(path); // Create a new copy
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.alpha = alpha;
        }
    }
}