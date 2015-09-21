package softwaremobility.darkgeat.objects;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import softwaremobility.darkgeat.sunshine.R;

/**
 * Created by darkgeat on 9/21/15.
 */
public class WindSpeedControl extends View{

    private int width = 150;
    private int height = 150;
    private int direction = 90;
    private Context mContext;

    public WindSpeedControl(Context context) {
        super(context);
        mContext = context;
    }

    public WindSpeedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,90);
        }finally {
            typedArray.recycle();
        }
    }

    public WindSpeedControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,90);
        }finally {
            typedArray.recycle();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WindSpeedControl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,90);
        }finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;

        if(hSpecMode == MeasureSpec.EXACTLY){
            height = hSpecSize;
        }
        myHeight = height;

        if(wSpecMode == MeasureSpec.EXACTLY){
            width = wSpecSize;
        }
        myWidth = width;


        setMeasuredDimension(myWidth,myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Location
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        float size = 19f;
        int pos = (int) (size / 2);

        //Brushes
        Paint blueBrush = new Paint();
        blueBrush.setColor(mContext.getResources().getColor(R.color.sunshine_blue));
        blueBrush.setAntiAlias(true);
        blueBrush.setStrokeWidth(3f);

        Paint whiteBrush = new Paint();
        whiteBrush.setColor(mContext.getResources().getColor(R.color.background_wind_speed));
        whiteBrush.setAntiAlias(true);
        whiteBrush.setStrokeWidth(3f);

        Paint redBrush = new Paint();
        redBrush.setColor(Color.RED);
        redBrush.setAntiAlias(true);
        redBrush.setStrokeWidth(3f);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setFakeBoldText(true);
        textPaint.setStrokeWidth(2f);
        textPaint.setTextSize(size);
        size = textPaint.getTextSize();

        //External Circle
        canvas.drawCircle(x, y, height / 2, blueBrush);
        //Internal Circle
        canvas.drawCircle(x, y, (height / 2) - size, whiteBrush);
        //Indicator Circle
        canvas.drawCircle(x, y, (height / 17), redBrush);
        //canvas.save();
        //canvas.rotate(45);
        //Points for Triangle
        Point a = new Point();
        Point b = new Point();
        Point c = new Point();
        if(direction == 90) { //North
            a = new Point(x, y - (int) ((height / 2) - size));
            b = new Point(x - (height / 17), y);
            c = new Point(x + (height / 17), y);
        }else if(direction == 270){ //South
            a = new Point(x, y + (int) ((height / 2) - size));
            b = new Point(x - (height / 17), y);
            c = new Point(x + (height / 17), y);
        }else if(direction == 180){ //West
            a = new Point(x - (int) ((width / 2) - size), y);
            b = new Point(x, y - (height / 17));
            c = new Point(x, y + (height / 17));
        }else if(direction == 0){ //East
            a = new Point(x + (int) ((width / 2) - size), y);
            b = new Point(x, y - (height / 17));
            c = new Point(x, y + (height / 17));
        }
        Path path = new Path();
        path.moveTo(a.x, a.y);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();
        canvas.drawPath(path, redBrush);
       // canvas.restore();



        canvas.drawText(mContext.getString(R.string.north), x - pos, size, textPaint);
        canvas.drawText(mContext.getString(R.string.south), x - pos, y * 2 - 2, textPaint);
        canvas.drawText(mContext.getString(R.string.east), (x * 2) - size + 2, y + pos, textPaint);
        canvas.drawText(mContext.getString(R.string.west), size - (pos * 2), y + pos, textPaint);
    }
}
