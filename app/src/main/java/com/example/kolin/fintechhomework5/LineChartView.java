package com.example.kolin.fintechhomework5;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kolin on 29.10.2017.
 */

public class LineChartView extends View {

    private String labelAxisX;
    private String labelAxisY;

    private float labelAxisTextSize;

    private boolean showGrid = false;

    private int axisLineColor = Color.BLACK;
    private int gridLineColor = Color.GRAY;
    private int chartLineColor = Color.RED;

    private float axisLineWidth = 2f;
    private float gridLineWidth = 1f;

    private Paint paintAxis;
    private Paint paintGrid;
    private Paint paintLabels;
    private Paint paintDividingLabels;
    private Paint paintChartLine;

    private RectF commonRectF;
    private RectF graphicRectF;

    //Length for dividing lines
    private float dividingLength = 8f;
    //Padding for dividing label
    private float dividingLabelPadding = 25f;
    //Step - count of parts on axis
    private int step = 5;

    // Dividing labels for X Axis
    private float[] XDividingLabels;
    // Dots for X Axis in graphicRectF
    private float[] XAxisDots;

    // Dividing labels for Y Axis
    private float[] YDividingLabels;
    // Dots for Y Axis in graphicRectF
    private float[] YAxisDots;

    // X offset
    private float xOffset = 0;
    // X lables offset
    private float xLabelOffset = 0;
    private float yOffset = 0;
    private float yLabelOffset = 0;

    private List<Point> points;

    public LineChartView(Context context) {
        super(context);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {

            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LineChartView,
                    0,
                    0);

            try {
                labelAxisX = typedArray.getString(R.styleable.LineChartView_labelAxisX);
                labelAxisY = typedArray.getString(R.styleable.LineChartView_labelAxisY);
                showGrid = typedArray.getBoolean(R.styleable.LineChartView_grid, false);
                step = typedArray.getInt(R.styleable.LineChartView_step, 5);
                labelAxisTextSize = typedArray.getDimension(R.styleable.LineChartView_labelTextSize, DensityUtils.spToPx(getContext(), 12));
                chartLineColor = typedArray.getColor(R.styleable.LineChartView_chartLineColor, Color.RED);
            } finally {
                typedArray.recycle();
            }

            init();
        }
    }

    private void init() {

        if (labelAxisY == null || labelAxisX == null){
            labelAxisX = "label X";
            labelAxisY = "label Y";
        }

        paintAxis = new Paint();
        paintAxis.setStrokeWidth(axisLineWidth);
        paintAxis.setColor(axisLineColor);
        paintAxis.setStyle(Paint.Style.STROKE);
        paintAxis.setAntiAlias(true);


        paintGrid = new Paint();
        paintGrid.setStrokeWidth(gridLineWidth);
        paintGrid.setColor(gridLineColor);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setAntiAlias(true);

        paintChartLine = new Paint();
        paintChartLine.setStrokeWidth(4f);
        paintChartLine.setColor(chartLineColor);
        paintChartLine.setStyle(Paint.Style.STROKE);
        paintChartLine.setAntiAlias(true);

        paintLabels = new Paint();
        paintLabels.setAntiAlias(true);
        paintLabels.setTextSize(labelAxisTextSize);
        paintLabels.setStyle(Paint.Style.FILL);
        paintLabels.setColor(axisLineColor);
        paintLabels.setTextAlign(Paint.Align.CENTER);

        paintDividingLabels = new Paint();
        paintDividingLabels.setAntiAlias(true);
        paintDividingLabels.setTextSize(labelAxisTextSize / 2);
        paintDividingLabels.setStyle(Paint.Style.FILL);
        paintDividingLabels.setColor(axisLineColor);
        paintDividingLabels.setTextAlign(Paint.Align.CENTER);

        commonRectF = new RectF();
        graphicRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(widthMeasureSpec);

        commonRectF.set(0, 0, width, height);
        graphicRectF.set(0 + labelAxisTextSize * 2, 0, width, height - labelAxisTextSize * 2);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calculate();

        drawAxis(canvas);
        drawLabels(canvas);
        drawDividing(canvas);

        if (showGrid)
            drawGrid(canvas);

        drawChartLine(canvas);
    }

    private void drawChartLine(Canvas canvas) {

        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return ((Float) o1.getX()).compareTo(o2.getX());
            }
        });


        for (int i = 0; i < points.size() - 1; i++) {

            Point current = points.get(i);
            Point next = points.get(i + 1);

            float x1 = current.getX();
            float y1 = current.getY();

            float factorX = xOffset / xLabelOffset;
            float factorY = yOffset / yLabelOffset;

            //calculate start X
            float chartX1 = (x1 * factorX - factorX * XDividingLabels[0]) + XAxisDots[0];
            //calculate start Y
            float chartY1 = YAxisDots[0] - (y1 * factorY - factorX * YDividingLabels[0]);

            float x2 = next.getX();
            float y2 = next.getY();

            //calculate stop X
            float chartX2 = (x2 * factorX - factorX * XDividingLabels[0]) + XAxisDots[0];
            //calculate stop Y
            float chartY2 = YAxisDots[0] - (y2 * factorY - factorX * YDividingLabels[0]);

            canvas.drawLine(chartX1, chartY1, chartX2, chartY2, paintChartLine);

        }

    }

    /**
     * draw grid lines
     */
    private void drawGrid(Canvas canvas) {

        //Paint vertical lines
        for (int i = 1; i < step; i++) {
            canvas.drawLine(XAxisDots[i], graphicRectF.bottom - dividingLength, XAxisDots[i], 0, paintGrid);
            canvas.drawLine(graphicRectF.left + dividingLength, YAxisDots[i], graphicRectF.right, YAxisDots[i], paintGrid);
        }
    }

    private void drawDividing(Canvas canvas) {

        drawXDividing(canvas);
        drawYDividing(canvas);
    }

    /**
     * Draw dividing lines on X axis
     */
    private void drawXDividing(Canvas canvas) {

        for (int i = 0; i <= step; i++) {

            float dot = XAxisDots[i];

            canvas.drawLine(
                    dot, graphicRectF.bottom - dividingLength,
                    dot, graphicRectF.bottom + dividingLength,
                    paintAxis
            );

            if (i == step)
                dot -= 20f;

            canvas.drawText(formatFloat(XDividingLabels[i]), dot, graphicRectF.bottom + dividingLabelPadding, paintDividingLabels);
        }
    }

    /**
     * Draw dividing lines on Y axis
     */
    private void drawYDividing(Canvas canvas) {

        for (int i = 0; i <= step; i++) {

            float dot = YAxisDots[i];

            canvas.drawLine(
                    graphicRectF.left - dividingLength,
                    dot,
                    graphicRectF.left + dividingLength,
                    dot, paintAxis
            );

            if (i == step)
                dot += 20f;

            canvas.drawText(formatFloat(YDividingLabels[i]), graphicRectF.left - dividingLabelPadding, dot, paintDividingLabels);

        }
    }

    /**
     * Calculate necessary data for chart
     */
    private void calculate() {
        if (points == null)
            return;

        int size = step + 1;

        XAxisDots = new float[size];
        XDividingLabels = new float[size];

        YAxisDots = new float[size];
        YDividingLabels = new float[size];

        //calculate min X and max X
        float minX = getMinX(points);
        float maxX = getMaxX(points);

        //calculate offsets
        float stepsX = Math.abs((maxX - minX) / step);
        float stepsXAxis = Math.abs((graphicRectF.right - graphicRectF.left) / step);
        this.xOffset = stepsXAxis;
        this.xLabelOffset = stepsX;

        float minY = getMinY(points);
        float maxY = getMaxY(points);

        float stepsY = Math.abs((maxY - minY) / step);
        float stepsYAxis = Math.abs((graphicRectF.top - graphicRectF.bottom) / step);
        this.yOffset = stepsYAxis;
        this.yLabelOffset = stepsY;

        fillNecessaryXAxisData(minX, maxX, stepsX, stepsXAxis);
        fillNecessaryYAxisData(minY, maxY, stepsY, stepsYAxis);
    }

    /**
     * calculate dots and labels on X axis
     */
    private void fillNecessaryXAxisData(float minX, float maxX, float stepX, float stepsXAxis) {
        XDividingLabels[0] = minX;
        XAxisDots[0] = graphicRectF.left;

        for (int i = 1; i <= step; i++) {
            XAxisDots[i] = XAxisDots[i - 1] + stepsXAxis;
            XDividingLabels[i] = XDividingLabels[i - 1] + stepX;
        }
    }

    /**
     * calculate dots and labels on Y axis
     */
    private void fillNecessaryYAxisData(float minY, float maxY, float stepY, float stepsYAxis) {
        YDividingLabels[0] = minY;
        YAxisDots[0] = graphicRectF.bottom;

        for (int i = 1; i <= step; i++) {
            YAxisDots[i] = YAxisDots[i - 1] - stepsYAxis;
            YDividingLabels[i] = YDividingLabels[i - 1] + stepY;
        }
    }


    private void drawAxis(Canvas canvas) {
        canvas.drawRect(graphicRectF, paintAxis);
    }

    private void drawLabels(Canvas canvas) {

        //Draw y axis label
        canvas.rotate(90);
        canvas.drawText(labelAxisY, commonRectF.centerY(), -15f, paintLabels);
        //Draw x axis label
        canvas.rotate(-90);
        canvas.drawText(labelAxisX, commonRectF.centerX(), commonRectF.bottom - labelAxisTextSize / 2, paintLabels);
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        if (points != null && !points.isEmpty()) {
            this.points = points;
            invalidate();
        } else
            throw new RuntimeException("Points can not be null!");
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        invalidate();
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        if (step >= 10) {
            this.step = 10;
        } else if (step <= 1) {
            this.step = 2;
        } else
            this.step = step;

        invalidate();
    }

    public String getLabelAxisX() {
        return labelAxisX;
    }

    public void setLabelAxisX(@NonNull String labelAxisX) {
        this.labelAxisX = labelAxisX;
        invalidate();
    }

    public String getLabelAxisY() {
        return labelAxisY;
    }

    public void setLabelAxisY(@NonNull String labelAxisY) {
        this.labelAxisY = labelAxisY;
        invalidate();
    }

    public float getLabelAxisTextSize() {
        return labelAxisTextSize;
    }

    public int getChartLineColor() {
        return chartLineColor;
    }

    public void setChartLineColor(int chartLineColor) {
        this.chartLineColor = chartLineColor;
        invalidate();
    }

    private float getMinX(List<Point> points) {
        float min = points.get(0).getX();

        for (Point p : points) {
            if (min > p.getX())
                min = p.getX();

        }

        return min;
    }

    private float getMaxX(List<Point> points) {
        float max = points.get(0).getX();

        for (Point p : points) {
            if (max < p.getX())
                max = p.getX();

        }

        return max;
    }

    private float getMinY(List<Point> points) {
        float min = points.get(0).getY();

        for (Point p : points) {
            if (min > p.getY())
                min = p.getY();

        }

        return min;
    }

    private float getMaxY(List<Point> points) {
        float max = points.get(0).getY();

        for (Point p : points) {
            if (max < p.getY())
                max = p.getY();

        }

        return max;
    }

    private String formatFloat(float f) {
        return String.format("%.1f", f);
    }
}
