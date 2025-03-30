package com.example.keepnote;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class CanvasActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private MaterialButton btnPen, btnBrush, btnHighlighter, btnNewPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        // Initialize views
        drawingView = findViewById(R.id.drawingView);
        btnPen = findViewById(R.id.btnPen);
        btnBrush = findViewById(R.id.btnBrush);
        btnHighlighter = findViewById(R.id.btnHighlighter);
        btnNewPage = findViewById(R.id.btnNewPage);

        // Set initial tool
        setActiveTool(DrawingView.PEN);

        // Tool selection listeners
        btnPen.setOnClickListener(v -> setActiveTool(DrawingView.PEN));
        btnBrush.setOnClickListener(v -> setActiveTool(DrawingView.BRUSH));
        btnHighlighter.setOnClickListener(v -> setActiveTool(DrawingView.HIGHLIGHTER));
        btnNewPage.setOnClickListener(v -> drawingView.clearCanvas());
    }

    private void setActiveTool(int toolType) {
        drawingView.setCurrentTool(toolType);
        updateButtonStates(toolType);
    }

    private void updateButtonStates(int activeTool) {
        btnPen.setSelected(activeTool == DrawingView.PEN);
        btnBrush.setSelected(activeTool == DrawingView.BRUSH);
        btnHighlighter.setSelected(activeTool == DrawingView.HIGHLIGHTER);
    }
}