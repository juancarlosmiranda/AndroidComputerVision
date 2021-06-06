package cameraXExample.frameworkCameraX;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class ResultView extends View {

    private static final String LOG_TAG = ResultView.class.getSimpleName();

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private Paint mPaintText2;
    private Paint mCircle1;

    private ArrayList<Result> mResults;

    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();
        mPaintText2 = new Paint();
        mCircle1 = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // -----------------------
        drawTextScreen(canvas, 10, 20, "TITLE 1");
        drawTextScreen(canvas, 10, 60, "TITLE 2");
        drawTextScreen(canvas, 10, 100, "TITLE 3");
        drawCircleCenterScreen(canvas);
        // -----------------------
    }


    public void drawTextScreen(Canvas canvas, int x_coord, int y_coord, String msgText){
        // -----------------------
        mPaintText2.setColor(Color.GREEN);
        mPaintText2.setStrokeWidth(0);
        mPaintText2.setStyle(Paint.Style.FILL);
        mPaintText2.setTextSize(32);
        canvas.drawText(String.format("%s", msgText, 0.0), x_coord + TEXT_X, y_coord + TEXT_Y, mPaintText2);
        // -----------------------
    }

    public void drawCircleCenterScreen(Canvas canvas){
        // -----------------------
        mCircle1.setColor(Color.GREEN);
        mCircle1.setStrokeWidth(5);
        mCircle1.setStyle(Paint.Style.STROKE);
        mCircle1.setTextSize(32);
        int x_coord = (int) this.getWidth()/2;
        int y_coord = (int) this.getHeight()/2;
        canvas.drawCircle(x_coord, y_coord, 100, mCircle1);
        // -----------------------
    }

    public void setResults(ArrayList<Result> results) {
        mResults = results;
    }
}
