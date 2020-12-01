package com.kaku.wcv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.kaku.library.R;

import java.util.Calendar;
import java.util.Date;

// The plan
//*-----------------------------------------*
//                  SPACE                   *
//*-----------------------------------------*
//                  TEXT                    *
//*-----------------------------------------*
//               TEXT SPACE                 *
//*-----------------------------------------*
//                  RADIUS                  *
//*-----------------------------------------*
//                   |                      *
//                   |                      *
//                   |                      *
//        ---------(x,y)--------            *
//                   |                      *
//                   |                      *
//                   |                      *
//*-----------------------------------------*
//                  RADIUS                  *
//*-----------------------------------------*
//               TEXT SPACE                 *
//*-----------------------------------------*
//                  TEXT                    *
//*-----------------------------------------*
//                  SPACE                   *
//*-----------------------------------------*



public class WeatherChartView extends View {

    /**
     * x-axis collection
     */
    private float mXAxis[] = new float[6];

    /**
     * Daytime y-axis collection
     */
    private float mYAxisDay[] = new float[6];

    /**
     * Night y-axis collection
     */
    private float mYAxisNight[] = new float[6];

    /**
     * x,y axis set number
     */
    private static final int LENGTH = 6;

    /**
     * Daytime temperature collection
     */
    private int mTempDay[] = new int[6];

    /**
     * Night temperature collection
     */
    private int mTempNight[] = new int[6];

    /**
     * Control height
     */
    private int mHeight;

    /**
     * font size
     */
    private float mTextSize;

    /**
     * Circle radius
     */
    private float mRadius;

    /**
     * Circle radius today
     */
    private float mRadiusToday;

    /**
     * Text moving distance
     */
    private float mTextSpace;

    /**
     * Daytime polyline color
     */
    private int mColorDay;

    /**
     * Night polyline color
     */
    private int mColorNight;

    /**
     * Screen density
     */
    private float mDensity;

    /**
     * Blank space on the side of the control
     */
    private float mSpace;

    /**
     * Line brush
     */
    private Paint mLinePaint;

    /**
     * Point brush
     */
    private Paint mPointPaint;

    /**
     * Font brush
     */
    private Paint mTextPaint;

    public WeatherChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressWarnings("deprecation")
    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeatherChartView);
        float densityText = getResources().getDisplayMetrics().scaledDensity;
        mTextSize = a.getDimensionPixelSize(R.styleable.WeatherChartView_textSize,
                (int) (14 * densityText));
        mColorDay = a.getColor(R.styleable.WeatherChartView_dayColor,
                getResources().getColor(R.color.colorAccent));
        mColorNight = a.getColor(R.styleable.WeatherChartView_nightColor,
                getResources().getColor(R.color.colorPrimary));

        int textColor = a.getColor(R.styleable.WeatherChartView_textColor, Color.WHITE);
        a.recycle();

        mDensity = getResources().getDisplayMetrics().density;
        mRadius = 3 * mDensity;
        mRadiusToday = 5 * mDensity;
        mSpace = 3 * mDensity;
        mTextSpace = 10 * mDensity;

        float stokeWidth = 2 * mDensity;
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(stokeWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public WeatherChartView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mHeight == 0) {
            // Set control height, x-axis collection
            setHeightAndXAxis();
        }
        // Calculate the y-axis aggregate value
        computeYAxisValues();
        // Draw a line chart during the day
        drawChart(canvas, mColorDay, mTempDay, mYAxisDay, 0);
        // Draw a line chart at night
        drawChart(canvas, mColorNight, mTempNight, mYAxisNight, 1);
    }

    /**
     * Calculate the y-axis aggregate value
     */
    private void computeYAxisValues() {
        // Minimum temperature during the day
        int minTempDay = mTempDay[0];
        // Maximum temperature during the day
        int maxTempDay = mTempDay[0];
        for (int item : mTempDay) {
            if (item < minTempDay) {
                minTempDay = item;
            }
            if (item > maxTempDay) {
                maxTempDay = item;
            }
        }

        // Lowest temperature for storage at night
        int minTempNight = mTempNight[0];
        // Store the highest temperature at night
        int maxTempNight = mTempNight[0];
        for (int item : mTempNight) {
            if (item < minTempNight) {
                minTempNight = item;
            }
            if (item > maxTempNight) {
                maxTempNight = item;
            }
        }

        // The lowest temperature during the day and night
        int minTemp = minTempNight < minTempDay ? minTempNight : minTempDay;
        // The highest temperature during the day and night
        int maxTemp = maxTempDay > maxTempNight ? maxTempDay : maxTempNight;

        // Number of servings (comprehensive temperature difference between day and night)
        float parts = maxTemp - minTemp;
        // The distance from the end of the y-axis to the end of the control
        float length = mSpace + mTextSize + mTextSpace + mRadius;
        // y-axis height
        float yAxisHeight = mHeight - length * 2;

        // When the temperature is the same (the dividend cannot be 0)
        if (parts == 0) {
            for (int i = 0; i < LENGTH; i++) {
                mYAxisDay[i] = yAxisHeight / 2 + length;
                mYAxisNight[i] = yAxisHeight / 2 + length;
            }
        } else {
            float partValue = yAxisHeight / parts;
            for (int i = 0; i < LENGTH; i++) {
                mYAxisDay[i] = mHeight - partValue * (mTempDay[i] - minTemp) - length;
                mYAxisNight[i] = mHeight - partValue * (mTempNight[i] - minTemp) - length;
            }
        }
    }

    /**
     * Draw a line chart
     *
     * @param canvas canvas
     * @param color  Drawing color
     * @param temp   Temperature set
     * @param yAxis  y-axis collection
     * @param type   Polyline type: 0, daytime; 1, nighttime
     */
    private void drawChart(Canvas canvas, int color, int temp[], float[] yAxis, int type) {
        mLinePaint.setColor(color);
        mPointPaint.setColor(color);

        int alpha1 = 102;
        int alpha2 = 255;
        for (int i = 0; i < LENGTH; i++) {
            // Draw a line
            if (i < LENGTH - 1) {
                // yesterday
                if (false &&i == 0) {
                    mLinePaint.setAlpha(alpha1);
                    // Set dotted line effect
                    mLinePaint.setPathEffect(new DashPathEffect(new float[]{2 * mDensity, 2 * mDensity}, 0));
                    // path
                    Path path = new Path();
                    // Path starting point
                    path.moveTo(mXAxis[i], yAxis[i]);
                    // Path connected to
                    path.lineTo(mXAxis[i + 1], yAxis[i + 1]);
                    canvas.drawPath(path, mLinePaint);
                } else {
                    mLinePaint.setAlpha(alpha2);
                    mLinePaint.setPathEffect(null);
                    canvas.drawLine(mXAxis[i], yAxis[i], mXAxis[i + 1], yAxis[i + 1], mLinePaint);
                }
            }

            // Draw a point
            if (i != 1 &&type!=1) {
                // yesterday
                if (false && i == 0) {
                    mPointPaint.setAlpha(alpha1);
                    canvas.drawCircle(mXAxis[i], yAxis[i], mRadius, mPointPaint);
                } else {
                    mPointPaint.setAlpha(alpha2);
                    canvas.drawCircle(mXAxis[i], yAxis[i], mRadius, mPointPaint);
                }
                // Nowadays
            } else {
                mPointPaint.setAlpha(alpha2);
                canvas.drawCircle(mXAxis[i], yAxis[i], mRadiusToday, mPointPaint);
            }

            // Calligraphy
            // yesterday
            if (false && i == 0) {
                mTextPaint.setAlpha(alpha1);
                drawText(canvas, mTextPaint, i, temp, yAxis, type);
            } else {
                mTextPaint.setAlpha(alpha2);
                drawText(canvas, mTextPaint, i, temp, yAxis, type);
            }
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas    canvas
     * @param textPaint brush
     * @param i         index
     * @param temp      Temperature set
     * @param yAxis     y-axis collection
     * @param type      Polyline type: 0, daytime; 1, nighttime
     */
    private void drawText(Canvas canvas, Paint textPaint, int i, int[] temp, float[] yAxis, int type) {
        switch (type) {
            case 0:
                // Display daytime temperature
                canvas.drawText(temp[i] + "", mXAxis[i], yAxis[i] - mRadius - mTextSpace, textPaint);
                break;
            case 1:
                // Show night temperature
                canvas.drawText(days[i] , mXAxis[i], yAxis[i] + mTextSpace + mTextSize, textPaint);
                break;
        }
    }

    /**
     * Set height, x-axis collection
     */
    private void setHeightAndXAxis() {
        mHeight = getHeight();
        // Control width
        int width = getWidth();
        // Wide per serving
        float w = width / 12;
        mXAxis[0] = w;
        mXAxis[1] = w * 3;
        mXAxis[2] = w * 5;
        mXAxis[3] = w * 7;
        mXAxis[4] = w * 9;
        mXAxis[5] = w * 11;
    }

    /**
     * Set day temperature
     *
     * @param tempDay Temperature array collection
     */
    public void setTempDay(int[] tempDay) {
        mTempDay = tempDay;
    }

    /**
     * Set night temperature
     *
     * @param tempNight Temperature array collection
     */
    public void setTempNight(int[] tempNight) {
        mTempNight = tempNight;
    }

    String [] days=DateTimeHelper.get6days(true);
}

class DateTimeHelper {

    public static Date getToday()
    {
        Date d=new Date();
        return new Date(d.getYear(),d.getMonth(),d.getDate());
    }

    public static Date add(Date d,int n)
    {
        Calendar c=Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH,n);
        return c.getTime();

    }

    public static Date[] get6days()
    {
        Date d=getToday();
        Date [] days=new Date[6];
        for (int i=0;i<6;i++)
        {
            days[i]=add(d,i-2);
        }
        return days;
    }

    public static String[] get6days(boolean returnString)
    {
        Date d=getToday();

        String [] days=new String[6];
        for (int i=0;i<6;i++)
        {
            Date t=add(d,i-2);
            days[i]=t.getMonth()+1+"."+t.getDate();
        }
        return days;
    }
}
