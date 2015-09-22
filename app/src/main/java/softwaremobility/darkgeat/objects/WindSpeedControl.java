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
import android.util.Log;
import android.view.View;

import softwaremobility.darkgeat.sunshine.R;

/**
 * Created by darkgeat on 9/21/15.
 */
public class WindSpeedControl extends View{

    private int width = 150;
    private int height = 150;
    private int direction = Direction.NORTH.value;
    private int x, y;
    private float textSize = 19f;
    private int framesPerSecond = 60;
    private boolean finishAnimation = false;
    private int currentDegrees = 0;
    private Context mContext;
    private Canvas mCanvas;

    public WindSpeedControl(Context context) {
        super(context);
        mContext = context;
        finishAnimation = false;
        postInvalidate();
    }

    public WindSpeedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,0);
        }finally {
            typedArray.recycle();
        }
        finishAnimation = false;
        postInvalidate();
    }

    public WindSpeedControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,0);
        }finally {
            typedArray.recycle();
        }
        finishAnimation = false;
        postInvalidate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WindSpeedControl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs,R.styleable.WindSpeedControl,0,0);
        try{
            direction = typedArray.getInteger(R.styleable.WindSpeedControl_wind_direction,0);
        }finally {
            typedArray.recycle();
        }
        finishAnimation = false;
        postInvalidate();
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
        x = getWidth() / 2;
        y = getHeight() / 2;
        int pos = (int) (textSize / 2);

        //Brushes
        Paint blueBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueBrush.setColor(mContext.getResources().getColor(R.color.sunshine_blue));
        blueBrush.setStrokeWidth(3f);

        Paint whiteBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteBrush.setColor(mContext.getResources().getColor(R.color.background_wind_speed));
        whiteBrush.setStrokeWidth(3f);

        Paint redBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        redBrush.setColor(Color.RED);
        redBrush.setStrokeWidth(3f);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setFakeBoldText(true);
        textPaint.setStrokeWidth(2f);
        textPaint.setTextSize(textSize);
        textSize = textPaint.getTextSize();

        //External Circle
        canvas.drawCircle(x, y, height / 2, blueBrush);
        //Internal Circle
        canvas.drawCircle(x, y, (height / 2) - textSize, whiteBrush);
        //Indicator Circle
        canvas.drawCircle(x, y, (height / 17), redBrush);

        mCanvas = canvas;
        mCanvas.save();

        //Direction labels
        canvas.drawText(mContext.getString(R.string.north), x - pos, textSize, textPaint);
        canvas.drawText(mContext.getString(R.string.south), x - pos, y * 2 - 2, textPaint);
        canvas.drawText(mContext.getString(R.string.east), (x * 2) - textSize + 2, y + pos, textPaint);
        canvas.drawText(mContext.getString(R.string.west), textSize - (pos * 2), y + pos, textPaint);

        //Layers for rotated labels
        Canvas canNE = canvas;
        Canvas canNW = canvas;
        canNE.save();
        canNW.save();

        //Indicator
        if(!finishAnimation)
            setWindSpeedDirection(direction);

        canNE.rotate(45, x, y);
        canNE.drawText(mContext.getString(R.string.north_east), x - (pos * 2), textSize, textPaint);
        canNE.drawText(mContext.getString(R.string.south_west), x - (pos * 2), y * 2 - 2, textPaint);
        canNW.rotate(270, x, y);
        canNW.drawText(mContext.getString(R.string.north_west), x - (pos * 2), textSize, textPaint);
        canNW.drawText(mContext.getString(R.string.south_east), x - (pos * 2), y * 2 - 2, textPaint);
        canvas.restore();

        if (!finishAnimation) {
            postInvalidateDelayed(1000 / framesPerSecond);
        }

    }

    public void setWindSpeedDirection(int direction){
        //----Point for the triangle indicator-------
        Point a = new Point(x, y - (int) ((height / 2) - textSize));
        Point b = new Point(x - (height / 17), y);
        Point c = new Point(x + (height / 17), y);

        Path path = new Path();
        path.moveTo(a.x, a.y);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();

        //Red Brush for the painting
        Paint redBrush = new Paint(Paint.ANTI_ALIAS_FLAG);
        redBrush.setColor(Color.RED);
        redBrush.setStrokeWidth(3f);

        if(currentDegrees != direction) {
            //matrix.postRotate(currentDegrees++,x,y);
            mCanvas.rotate(currentDegrees++, x, y);
        }else {
            finishAnimation = true;
            mCanvas.rotate(direction, x, y);
        }
        mCanvas.drawPath(path, redBrush);
        mCanvas.restore();
    }

    public void setWindSpeedDirection(Direction direction){
        this.direction = direction.value;
    }

    public enum Direction{
        NORTH(0),
        NORTHEAST(45),
        EAST(90),
        SOUTHEAST(135),
        SOUTH(180),
        SOUTHWEST(225),
        WEST(270),
        NORTHWEST(315);

        private int value;
        Direction(int value){
            this.value = value;
        }

    }
}
