package com.curfew;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by pfister on 11/24/13. Adapted from Chris Renke's presentation from Square.
 */
public class ClockDrawable extends Drawable {

    private Paint mDialPaint;
    private int diameter;
    private float minute;
    private float hour;

    public ClockDrawable(int size, int color) {
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mDialPaint.setColor(color);
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeCap(Paint.Cap.ROUND);
        mDialPaint.setStrokeJoin(Paint.Join.ROUND);

        float strokeWidth = (5f / 70f) * size;
        mDialPaint.setStrokeWidth(strokeWidth);
        diameter = (int) (size - strokeWidth);
        setBounds(0, 0, diameter, diameter);
    }

    @Override
    public int getIntrinsicWidth() {
        return diameter;
    }

    @Override
    public int getIntrinsicHeight() {
        return diameter;
    }

    public void setTime(int hour, int minute, int second) {
        this.minute = minute + second / 60.0f;
        this.hour = hour + this.minute / 60.0f;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {

        Rect bounds = getBounds();
        int height = bounds.height();
        int width = bounds.width();
        float centerX = width / 2f;
        float centerY = height / 2f;
        int smallestSize = Math.min(width, height);

        // Draw the outer ring.
        float dialRadius = smallestSize / 2.2f;
        canvas.drawCircle(centerX, centerY, dialRadius, mDialPaint);

        // Draw the hour hand.
        float hourRotation = hour / 12.0f * 360.0f;
        drawHand(canvas, height, centerX, centerY, hourRotation, 18f / 80f);

        // Draw the minute hand
        float minuteRotation = minute / 60.0f * 360.0f;
        drawHand(canvas, height, centerX, centerY, minuteRotation, 24f / 80f);
    }

    private void drawHand(Canvas canvas, int canvasHeight, float centerX, float centerY,
                          float rotation, float relativeHandSize) {
        canvas.save();
        canvas.rotate(rotation, centerX, centerY);

        float handHeight = canvasHeight * relativeHandSize;
        canvas.drawLine(centerX, centerY, centerX, centerY - handHeight, mDialPaint);

        canvas.restore();
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
