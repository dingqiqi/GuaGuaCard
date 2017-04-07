package com.example.test.testgagaka;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2016/5/11.
 */
public class GaGaKaView extends View {

    private Context mContext;
    //按下的坐标
    private float mDownX, mDownY;
    //画图的路径与画笔
    private Path mPath;
    private Paint mPaint;
    //背景图片与灰色涂层
    private Bitmap mBgBitmap;
    private Bitmap mfgBitmap;
    //灰色涂层画布
    private Canvas mCanvas;
    //背景字体与画笔
    private String mText = "一等奖";
    private Paint mPaintText;
    //控件高度宽度，用于计算
    private int mWidth;
    private int mHeight;
    //用于获取文字高度，为了居中
    private Rect mRect;
    /**
     * 是否擦除完
     */
    private boolean isDraw = false;

    public GaGaKaView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public GaGaKaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public GaGaKaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(60);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);

        mBgBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);

        mPaintText = new Paint();
        mPaintText.setTextSize(60);
        mPaintText.setAntiAlias(true);
        //加粗
        mPaintText.setFakeBoldText(true);
        mPaintText.setColor(Color.BLACK);
        mPaintText.setTextAlign(Paint.Align.CENTER);

        mRect = new Rect();

        mPaintText.getTextBounds(mText, 0, mText.length(), mRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        Matrix matrix = new Matrix();
        //要强转
        float fx = (float) mWidth / mBgBitmap.getWidth();
        float fy = (float) mHeight / mBgBitmap.getHeight();
        matrix.postScale(fx, fy);

        mBgBitmap = Bitmap.createBitmap(mBgBitmap, 0, 0, mBgBitmap.getWidth(), mBgBitmap.getHeight(), matrix, true);
        mfgBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mfgBitmap);
        mCanvas.drawColor(Color.parseColor("#989898"));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画图片背景
        //canvas.drawBitmap(mBgBitmap, 0, 0, null);
        //画文字背景
        canvas.drawText(mText, mWidth / 2, mHeight / 2 - mRect.height() / 2, mPaintText);

        //是否绘制灰色涂层(以及擦除效果)
        if (!isDraw) {
            drawPath();
            canvas.drawBitmap(mfgBitmap, 0, 0, null);
        }
    }

    private void drawPath() {
        //设置擦出效果
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();

                mPath.moveTo(mDownX, mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();


                float dx = Math.abs(mDownX - x);
                float dy = Math.abs(mDownY - y);

                if (dx > 3 && dy > 3) {
                    mPath.lineTo(x, y);
                }

                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_UP:
                //抬起手去判断像素点
                new Thread(mRunnable).start();
                break;
        }

        invalidate();

        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            int w = mfgBitmap.getWidth();
            int h = mfgBitmap.getHeight();

            int total = w * h;
            int flag = 0;

            int[] pix = new int[total];

            //存放像素数组 offset第一个像素索引引导值 stride每行像素个数(必须大于或等于宽度)
            mfgBitmap.getPixels(pix, 0, w, 0, 0, w, h);

            //计算像素点
            for (int i = 0; i < total; i++) {
                if (pix[i] == 0) {
                    flag++;
                }
            }

            if (total > 0 && flag > 0) {
                int result = flag * 100 / total;
                Log.i("aaa", result + "");

                if (result > 80) {
                    isDraw = true;
                    postInvalidate();
                }
            }

        }
    };

}
