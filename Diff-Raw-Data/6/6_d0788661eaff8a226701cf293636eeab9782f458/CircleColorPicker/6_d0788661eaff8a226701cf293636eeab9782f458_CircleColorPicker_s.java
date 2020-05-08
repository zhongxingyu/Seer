 package com.digdug.colorpicker;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.os.Build;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CircleColorPicker extends ViewBase implements ColorPicker {
     private static final int WIDTH = 10;
     int[] mHueList = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };
     ColorRing[] rings = new ColorRing[3];
     private float[] hsv = new float[] { 0f, 1f, 1f };
     private PointF mCenter;
     private float mRadius;
     private float mRingWidth;
     private Paint mPaint;
     private ColorListener mCallback;
 
     public CircleColorPicker(Context context) {
         this(context, null);
     }
 
     public CircleColorPicker(Context context, AttributeSet attrs) {
         this(context, attrs, android.R.style.TextAppearance_DeviceDefault);
     }
 
     public CircleColorPicker(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         hsv = new float[] { 0f, 1f, 1f };
         updateHues();
         // rings[3] = new ColorRing(this, new int[] { Color.WHITE, Color.WHITE });
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     protected void init(Context context) {
         setLayerType(View.LAYER_TYPE_SOFTWARE, null);
     }
 
     float startVal = -1.0f;
     float startAngle = -1.0f;
     int startRing = -1;
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getActionMasked() == MotionEvent.ACTION_UP ||
             event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
             startVal = -1.0f;
             startRing = -1;
             startAngle = -1;
         } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
             startRing = getRingNum(event.getX(), event.getY());
             startVal = rings[startRing].getValue();
             startAngle = getValFromPoint(event.getX(), event.getY());
         }
 
         if (startRing >= 0) {
             float val = startVal + getValFromPoint(event.getX(), event.getY()) - startAngle;
             if (val > 1.0) val -= 1.0;
             else if (val < 0.0) val += 1.0;
 
             rings[startRing].setValue(val);
 
             hsv[0] = 360 - rings[0].getValue()*360;
             hsv[1] = Math.abs((rings[1].getValue() - 0.5f) * 2f);
             hsv[2] = Math.abs((rings[2].getValue() - 0.5f) * 2f);
 
             updateHues();
             invalidate();
             if (mCallback != null) {
                 mCallback.onChange(Color.HSVToColor(hsv));
             }
             return true;
         }
         return false;
     }
 
     private void updateHues() {
         if (rings[0] == null) {
             rings[0] = new ColorRing(this, getHueList(hsv[1], hsv[2]));
             rings[0].setValue(hsv[0]/360);
         } else {
             rings[0].setColors(getHueList(hsv[1], hsv[2]));
         }
 
         int h1 = Color.HSVToColor(new float[] { hsv[0], 1f, hsv[2] });
         int h2 = Color.HSVToColor(new float[] { hsv[0], 0f, hsv[2] });
         if (rings[1] == null) {
             rings[1] = new ColorRing(this, new int[] { h1, h2, h1 });
             rings[1].setValue(1f - hsv[1]/2);
         } else
             rings[1].setColors(new int[] { h1, h2, h1 });
 
         h1 = Color.HSVToColor(new float[] { hsv[0], hsv[1], 1f });
         h2 = Color.HSVToColor(new float[] { hsv[0], hsv[1], 0f });
         if (rings[2] == null) {
             rings[2] = new ColorRing(this, new int[] { h1, h2, h1 });
             rings[2].setValue(1f - hsv[2]/2);
         } else
             rings[2].setColors(new int[]{h1, h2, h1});
     }
 
     private int getRingNum(float x, float y) {
         PointF center = getCenter();
         float dx = x - center.x;
         float dy = y - center.y;
 
         float dist = (float) Math.sqrt(dx*dx + dy*dy);
         return rings.length - 1 - (int) Math.floor( dist/getRingWidth() );
     }
 
     private float getValFromPoint(float x, float y) {
         PointF center = getCenter();
         float dx = x - center.x;
         float dy = y - center.y;
 
         float h = (float) (Math.atan(dy/dx)*180/Math.PI);
         if (dx < 0) {
             h = 180 + h;
         } else {
             if (h < 0) h = 360 + h;
         }
         return h/360;
     }
 
     private PointF getCenter() {
         if (mCenter == null) {
             mCenter = new PointF(getWidth()/2,  getHeight()/2);
         }
         return mCenter;
     }
 
     private float getRadius() {
         if (mRadius == 0) {
             PointF c = getCenter();
             mRadius = Math.min(c.x - getPaddingLeft() - getPaddingRight(),
                     c.y - getPaddingTop() - getPaddingBottom());
         }
         return mRadius;
     }
 
     private float getRingWidth() {
         if (mRingWidth == 0) {
             mRingWidth = getRadius() / rings.length;
         }
         return mRingWidth;
     }
 
     public void onDraw(Canvas canvas) {
         PointF c = getCenter();
         float r = getRadius();
         float width = getRingWidth();
         r -= width/2;
 
         Paint p = getPaint();
         p.setStyle(Paint.Style.STROKE);
         p.setColor(Color.WHITE);
         p.setStrokeWidth(getRingWidth()/5);
         for (int i = 0; i < rings.length; i++) {
             rings[i].setCenter(c);
             rings[i].setRadius(r - width * i);
             rings[i].setWidth(width);
             rings[i].draw(canvas);
 
             canvas.drawCircle(c.x, c.y, r - width*i - width/2, p);
         }
 
         p.setStyle(Paint.Style.FILL);
         p.setColor(Color.HSVToColor(hsv));
         canvas.drawRect(c.x, c.y - 10, c.x + r + width/2, c.y + 10, p);
 
         p.setStyle(Paint.Style.STROKE);
         p.setColor(Color.WHITE);
         p.setStrokeWidth(3);
         canvas.drawRect(c.x, c.y - 10, c.x + r + width/2, c.y + 10, p);
     }
 
     private Paint getPaint() {
         if (mPaint != null) {
             return mPaint;
         }
 
         mPaint = new Paint();
         return mPaint;
     }
 
     private int[] getHueList(float sat, float val) {
         for (int i = 0; i < mHueList.length; i++) {
             float[] f = new float[3];
             f[0] = 360*i/(mHueList.length-1);
             f[1] = sat;
             f[2] = val;
             mHueList[i] = Color.HSVToColor(f);
         }
         return mHueList;
     }
 
     @Override
     public int getColor() {
         return Color.HSVToColor(hsv);
     }
 
     @Override
     public void setColor(int color) {
         Color.colorToHSV(color, hsv);
        rings[0].setValue(hsv[0]/360);
        rings[1].setValue(1f - hsv[1]/2);
        rings[2].setValue(1f - hsv[2]/2);
         updateHues();
         if (mCallback != null) {
             mCallback.onChange(Color.HSVToColor(hsv));
         }
         invalidate();
     }
 
     @Override
     public void setColorChangeListener(ColorListener listener) {
         mCallback = listener;
 
     }
 }
