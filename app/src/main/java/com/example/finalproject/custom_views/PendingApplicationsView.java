package com.example.finalproject.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.R;

public class PendingApplicationsView extends View {
    // The number of pending applications:
    private int pendingApplications;

    // Text size of the number:
    private float textSize;

    // Paints for the canvas:
    private Paint circlePaint;
    private Paint textPaint;
    private Rect textBounds;

    // Default text size:
    private static final int DEFAULT_TEXT_SIZE = 16;

    public PendingApplicationsView(Context context) {
        super(context);

        // Set default text size:
        this.textSize = DEFAULT_TEXT_SIZE;
        init();
    }

    public PendingApplicationsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setTextSizeFromAttr(context, attrs, 0, 0);
        init();
    }

    public PendingApplicationsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTextSizeFromAttr(context, attrs, defStyleAttr, 0);
        init();

    }

    public PendingApplicationsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setTextSizeFromAttr(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void setTextSizeFromAttr(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs == null) {
            this.textSize = DEFAULT_TEXT_SIZE;
            return;
        }

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PendingApplicationsView, defStyleAttr, defStyleRes);
        try {
            this.textSize = a.getDimensionPixelSize(R.styleable.PendingApplicationsView_android_textSize, DEFAULT_TEXT_SIZE);
        } finally {
            a.recycle();
        }
        a.close();

    }

    private void init() {
        // Initialize the pending applications number:
        this.pendingApplications = 0;

        // Initialize the circle's paint:
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        // Initialize the text paint:
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        this.textBounds = new Rect();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw anything if the pending applications is 0:
        if (this.pendingApplications <= 0)
            return;

        // Limit the displayed text until 100:
        final String text;
        if (this.pendingApplications >= 100)
            text = "99+";
        else
            text = Integer.toString(this.pendingApplications);

        // Calculate the circle radius based on the text size
        float radius = (textPaint.descent() - textPaint.ascent()) / 2 + textSize / 2;

        // Draw the red circle
        final float cx = getWidth() / 2f, cy = getHeight() / 2f;
        canvas.drawCircle(cx, cy, radius, circlePaint);

        // Get text bounds:
        this.textPaint.getTextBounds(text, 0, text.length(), this.textBounds);

        // Draw the number inside the circle
        final float textX = cx - (textBounds.width() / 2f) - textBounds.left, textY = cy + (textBounds.height() / 2f);
        canvas.drawText(text, textX, textY, textPaint);
    }

    public int getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(int pendingApplications) {
        this.pendingApplications = pendingApplications;
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }
}
