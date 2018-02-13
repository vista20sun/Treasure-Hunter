package com.example.vista.treasurefinder;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by Yuyang Luo on 2018/1/31.
 * Color changeable View component,
 * hex colors array and splint points array defined in res/value/array.xml
 */


public class ColoredBackground extends View {

    private void initProperty(AttributeSet attrs) {
        TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.BackGround);
        bgColor = tArray.getColor(R.styleable.BackGround_bgColor, ContextCompat.getColor(context, R.color.cpbg));
        colors = getResources().getIntArray(R.array.BgColors);
        splits = getResources().getIntArray(R.array.BgSplit);
        bgColor = colors[0];
    }


    public ColoredBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        currRatio = 0;
        gradation = true;
        this.context = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        initProperty(attrs);
        setShowUp(false);
        setRatio_r(0);
        //setBackgroundColor(colors[0]);
        gradation = true;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                setRatio_r(msg.what);
            }
        };
    }

    public static int getRatio(int st, int ed, int curr) {
        return (curr - st) * 100 / (ed - st);
    }

    public synchronized void setRatio(int min, int max, int curr) {
        //calculate the new width with the given interval and current value
        setRatio(getRatio(min, max, curr));
    }

    private synchronized void setRatio_r(int ratio) {
        if (ratio >= 0 && ratio <= 100) {
            int splitPt = 0;
            for (int i = 1; i < splits.length; i++) {
                if (ratio <= splits[i]) {
                    splitPt = i - 1;
                    break;
                }
            }
            if (gradation) {
                bgColor = getMiddleColor(colors[splitPt], colors[splitPt + 1], splits[splitPt], splits[splitPt + 1], ratio);
                Log.d("Colors", String.format("%d  0x%x 0x%x --- 0x%x %d\n", splitPt, colors[splitPt], colors[splitPt + 1], bgColor, ratio));
            } else {
                if (ratio == 100)
                    splitPt = splits.length - 1;
                bgColor = colors[splitPt];
            }
            currRatio = ratio;
            super.setBackgroundColor(bgColor);
        }
    }

    private synchronized void setRatio(int targetRatio) {
        if (gradation && targetRatio == currRatio)
            return;
        //draw the change in 6 time.
        final int st = currRatio;
        final double step = (targetRatio - st) / 6.0;
        new Thread() {
            @Override
            public void run() {
                double r = st;
                for (int i = 0; i < 6; i++) {
                    r += step;
                    handler.sendEmptyMessage((int) r);
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }.start();
    }

    public void setShowUp(boolean showUp) {
        super.setVisibility(showUp ? VISIBLE : INVISIBLE);
    }

    public boolean isGradation() {
        return gradation;
    }

    public void setGradation(boolean gradation) {
        this.gradation = gradation;
        setRatio(currRatio);
    }

    public int[] getColors() {
        return colors;
    }

    public int[] getSplits() {
        return splits;
    }

    public void setColors(int[] colors, int[] splits) {
        if (colors.length != splits.length) {
            colors = getResources().getIntArray(R.array.BgColors);
            splits = getResources().getIntArray(R.array.BgSplit);
        }
        this.colors = colors;
        this.splits = splits;
        setRatio(currRatio);
    }

    /**
     * convert hex ARGB value to values of R,G & B
     */
    public static int getColorB(int color) {
        return color & 0xff;
    }

    public static int getColorG(int color) {
        return (color >> 8) & 0xff;
    }

    public static int getColorR(int color) {
        return (color >> 16) & 0xff;
    }

    /**
     * combine values of R,G & B to a hex ARGB value
     */
    public static int getColor(int r, int g, int b) {
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * mix two single color (R or G or B) with the  percentage ratio
     */
    private static int mixSigColor(int c1, int c2, int ratio) {//0~100
        return 0xff & (((c2 * ratio) + (c1 * (100 - ratio))) / 100);
    }

    /**
     * calculate the intermediate hex color of the start color and end color with the split value of each and current split value
     * 0 <= stRatio <= split <= edRatio <=100
     */
    private static int getMiddleColor(int stColor, int edColor, int stRatio, int edRatio, int split) {
        int ratio = (100 * (split - stRatio)) / (edRatio - stRatio);
        int stR = getColorR(stColor), edR = getColorR(edColor);
        int stG = getColorG(stColor), edG = getColorG(edColor);
        int stB = getColorB(stColor), edB = getColorB(edColor);
        int mR = mixSigColor(stR, edR, ratio), mG = mixSigColor(stG, edG, ratio), mB = mixSigColor(stB, edB, ratio);
        return getColor(mR, mG, mB);
    }


    private int bgColor, currRatio;
    private int[] colors, splits;
    private Context context;
    private Paint paint;
    private Handler handler;
    private boolean gradation;
}
