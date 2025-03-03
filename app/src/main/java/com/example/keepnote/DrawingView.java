package com.example.keepnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {
    private Paint paint;
    private Path currentPath;
    private ArrayList<Path> paths;
    private ArrayList<Integer> strokeTypes;
    private int currentPage = 0;

    // Stroke types
    public static final int PEN = 0;
    public static final int BRUSH = 1;
    public static final int HIGHLIGHTER = 2;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paths = new ArrayList<>();
        strokeTypes = new ArrayList<>();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        startNewPage();
    }

    // Add a new page
    public void startNewPage() {
        currentPath = new Path();
        paths.add(currentPath);
        strokeTypes.add(PEN);
        invalidate();
    }

    // Set the current stroke type (Pen, Brush, Highlighter)
    public void setStrokeType(int strokeType) {
        paint.setStrokeWidth(strokeType == BRUSH ? 16f : 8f);
        paint.setAlpha(strokeType == HIGHLIGHTER ? 128 : 255);
        strokeTypes.set(currentPage, strokeType);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < paths.size(); i++) {
            paint.setAlpha(strokeTypes.get(i) == HIGHLIGHTER ? 128 : 255);
            canvas.drawPath(paths.get(i), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    // Switch to a specific page
    public void setCurrentPage(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < paths.size()) {
            currentPage = pageIndex;
            currentPath = paths.get(pageIndex);
            invalidate();
        }
    }
}
