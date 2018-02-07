package com.example.vista.treasurefinder;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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



public class CircleHalo extends View {

    private void initProperty(AttributeSet attrs){
        TypedArray tArray= context.obtainStyledAttributes(attrs, R.styleable.Circle_Halo);
        mR=tArray.getInteger(R.styleable.Circle_Halo_r,getWidth()/2);
        bgColor=tArray.getColor(R.styleable.Circle_Halo_bgColor, ContextCompat.getColor(context,R.color.cpbg));
        colors = getResources().getIntArray(R.array.haloColors);
        splits = getResources().getIntArray(R.array.haloSplit);
        fgColor = colors[0];
    }


    public CircleHalo(Context context, AttributeSet attrs){
        super(context,attrs);
        this.context=context;
        this.paint=new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        initProperty(attrs);
        maxWidth = -1;
        minWidth = -1;

        uper= new Handler(){
            @Override
            public void handleMessage(Message msg){
               setStrokeWidth_r(msg.what);
            }
        };
    }

    public void setListener(StatementChangeListener listener) {
        this.listener = listener;
    }

    public  synchronized void setStrokeWidth(int min, int max, int curr){
        //calculate the new width with the given interval and current value
        setStrokeWidth((curr-min)*(maxWidth-minWidth)/(max-min)+minWidth);
    }
    private synchronized void setStrokeWidth_r(int width){
        if(width>=minWidth&&width<=maxWidth) {
            this.strokeWidth_r = width;
            int ratio = 100*(width-minWidth)/(maxWidth-minWidth);
            //
            int splitIn=0;
            for(int i = 1; i < splits.length;i++){
                if(ratio <= splits[i]) {
                    splitIn = i - 1;
                    break;
                }
            }
            fgColor = getMiddleColor(colors[splitIn],colors[splitIn+1],splits[splitIn],splits[splitIn+1],ratio);
            if(listener!=null)
                listener.onStatementChange(splitIn);
            Log.d("Colors",String.format("%d 0x%x 0x%x 0x%x %d\n",splitIn,colors[splitIn],colors[splitIn+1],fgColor,ratio));
            super.invalidate();
        }
    }
    private synchronized void setStrokeWidth(int width){
        //draw the change in 6 time.
        final int st = strokeWidth_r;
        final double step = (width-st)/6.0;
        new Thread(){
            @Override
            public void run(){
                double r=st;
                for(int i=0;i<6;i++) {
                    r+=step;
                    uper.sendEmptyMessage((int) r);

                    try {
                        sleep(20);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }.start();
    }


    @Override
    protected void onDraw(Canvas canvas){
        //draw view,
        //draw two circle
        if(maxWidth==-1){
            int i=getWidth();
            maxWidth = i/2;
            minWidth = i/60;
            strokeWidth_r = minWidth;
        }
        super.onDraw(canvas);
        int center=getWidth()/2;
        int strokeWidth= strokeWidth_r;//(45~4)
        mR=getWidth()/5;

        //draw the inner circle
        this.paint.setColor(bgColor);
        this.paint.setStrokeWidth(10);
        canvas.drawCircle(center, center, mR/2, this.paint);

        //draw the outer circle
        this.paint.setColor(fgColor);
        this.paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(center, center, (mR+strokeWidth)/2, this.paint);

    }

    /**
     * convert hex ARGB value to values of R,G & B
     * */
    private static int getColorB(int color){
        return color & 0xff;
    }
    private static int getColorG(int color){
        return (color>>8) & 0xff;
    }
    private static int getColorR(int color){
        return (color>>16)&0xff;
    }

    /**
     * combine values of R,G & B to a hex ARGB value
     * */
    private static int getColor(int r,int g, int b){
        return 0xff000000|(r<<16)|(g<<8)|b;
    }

    /**
     * mix two single color (R or G or B) with the  percentage ratio
     * */
    private static int mixSigColor(int c1,int c2,int ratio){//0~100
        return 0xff & (((c2*ratio)+(c1*(100-ratio)))/100);
    }

    /**
     * calculate the intermediate hex color of the start color and end color with the split value of each and current split value
     *  0 <= stRatio <= split <= edRatio <=100
     * */
    private static int getMiddleColor(int stColor,int edColor,int stRatio,int edRatio, int split){
        int ratio = (100*(split-stRatio))/(edRatio-stRatio);
        int stR = getColorR(stColor), edR = getColorR(edColor);
        int stG = getColorG(stColor), edG = getColorG(edColor);
        int stB = getColorB(stColor), edB= getColorB(edColor);
        int mR = mixSigColor(stR,edR,ratio), mG = mixSigColor(stG,edG,ratio), mB= mixSigColor(stB,edB,ratio);
        return getColor(mR,mG,mB);
    }


    public interface StatementChangeListener{
        void onStatementChange(int state);
    };

    private StatementChangeListener listener;
    private int mR;
    private int bgColor;
    private int fgColor;
    private int[] colors,splits;
    private int strokeWidth_r, maxWidth,minWidth;
    private Context context;
    private Paint paint;
    private Handler uper;
}
