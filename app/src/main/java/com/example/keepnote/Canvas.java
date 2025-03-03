package com.example.keepnote;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Canvas extends AppCompatActivity {

    private DrawingView drawingView;
    private Button btnPen, btnBrush, btnHighlighter, btnNewPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        // Initialize drawing view and buttons
        drawingView = findViewById(R.id.drawingView);
        btnPen = findViewById(R.id.btnPen);
        btnBrush = findViewById(R.id.btnBrush);
        btnHighlighter = findViewById(R.id.btnHighlighter);
        btnNewPage = findViewById(R.id.btnNewPage);

        // Set stroke type to Pen
        btnPen.setOnClickListener(v -> drawingView.setStrokeType(DrawingView.PEN));

        // Set stroke type to Brush
        btnBrush.setOnClickListener(v -> drawingView.setStrokeType(DrawingView.BRUSH));

        // Set stroke type to Highlighter
        btnHighlighter.setOnClickListener(v -> drawingView.setStrokeType(DrawingView.HIGHLIGHTER));

        // Add new page
        btnNewPage.setOnClickListener(v -> drawingView.startNewPage());
    }
}
