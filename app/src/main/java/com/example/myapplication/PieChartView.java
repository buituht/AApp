package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Slice> slices = new ArrayList<>();
    private RectF rectF = new RectF();

    public static class Slice {
        float value;
        int color;
        String label;

        public Slice(float value, int color, String label) {
            this.value = value;
            this.color = color;
            this.label = label;
        }
    }

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(List<Slice> slices) {
        this.slices = slices;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices.isEmpty()) return;

        float total = 0;
        for (Slice slice : slices) {
            total += slice.value;
        }

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 * 0.8f;
        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);

        float startAngle = 0;
        for (Slice slice : slices) {
            float sweepAngle = (slice.value / total) * 360f;
            paint.setColor(slice.color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
        
        // Draw center hole for Donut effect
        paint.setColor(Color.WHITE);
        canvas.drawCircle(width / 2, height / 2, radius * 0.6f, paint);
    }
}
