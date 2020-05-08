 package com.davidykay.funge;
 
 
 import com.davidykay.funge.interpreter.FungeModel;
 import com.davidykay.funge.interpreter.FungePlane;
 import com.davidykay.funge.interpreter.tiles.IntegerTile;
 import com.davidykay.funge.interpreter.tiles.Tile;
import com.davidykay.funge.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 /**
  * View representing the Befunge game board.
  */
 public class FungeView extends View {
 
   private static final int MARGIN = 4;
   private static final String TAG = "FungeView";
   
   private static final int HEX_COLOR_YELLOW = 0xAABBCCDD;
   private static final int HEX_COLOR_BLUE   = 0xDDCCBBAA;
   private static final int HEX_COLOR_WHITE  = 0xFFFFFFFF;
   private static final int HEX_COLOR_BLACK  = 0x000000FF;
   private static final int HEX_COLOR_RED    = 0xFF0000FF;
 
   // UI
   private Bitmap mCellBackground;
   private Bitmap mExampleCell;
   //private Drawable mDrawableBg;
   private Paint mBmpPaint;
   private Paint mGridPaint;
   private Paint mLinePaint; 
   private Paint mTilePaint; 
   private Paint mTextPaint; 
   
   // Model
   private FungeModel mModel; 
 
   public FungeView(Context context, AttributeSet attrs) {
     super(context, attrs);
     mCellBackground = getResBitmap(R.drawable.lib_cross);
     //mDrawableBg = getResources().getDrawable(R.drawable.lib_bg);
 
     if (mCellBackground != null) {
       mSrcRect.set(0, 0, mCellBackground.getWidth() - 1, mCellBackground.getHeight() - 1);
     }
 
     mTilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     mTilePaint.setColor(HEX_COLOR_BLACK);

     mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     mTextPaint.setColor(HEX_COLOR_RED);
    mTextPaint.setTextSize(20.0f);

     mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     mGridPaint.setColor(
       0xAABBCCDD
       //0x00ff00ff
       );
     mLinePaint = new Paint();
     mLinePaint.setColor(HEX_COLOR_WHITE);
     mLinePaint.setStrokeWidth(5);
     mLinePaint.setStyle(Style.STROKE);
   }
   
   private final Rect mSrcRect = new Rect();
   private final Rect mDstRect = new Rect();
   private final Rect mGridRect = new Rect();
 
   @Override
   protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
 
     int columns = mModel.getWidth();
     int rows = mModel.getHeight();
 
     int cellWidth = getWidth() / columns;
     int cellHeight = getHeight() / rows;
     mGridRect.set(0, 0, cellWidth, cellHeight);
     Log.v(TAG, String.format("Cell Dimensions: (%d, %d)", 
                              cellWidth,
                              cellHeight
                              ));
 
     int xPixels = 0;
     int yPixels = 0;
     for (int y = 0; y < rows; y++) {
       for (int x = 0; x < columns; x++) {
         mGridRect.offsetTo(xPixels, yPixels);
         if (mModel.tileAtLocation(x, y) != null) {
           Tile tile = mModel.tileAtLocation(x, y);
           if (tile instanceof IntegerTile) {
             // If integer, draw number.
             String intString = Integer.toString(((IntegerTile) tile).getInteger());
             float[] coordinates = new float[] { 
               (mGridRect.left + mGridRect.right) / 2,
               (mGridRect.top + mGridRect.bottom) / 2
             };
             canvas.drawPosText(
                 intString,
                 coordinates,
                 mTextPaint);
             Log.v(TAG, String.format("Drawing number %s at: (%f, %f)", 
                                      intString,
                                      coordinates[0],
                                      coordinates[1]
                                     ));
           } else {
             // If other type, draw black.
             canvas.drawRect(mGridRect, mTilePaint);
           }
           Log.v(TAG, String.format("Drawing tile at: (%d, %d)", 
                                    x, y
                                   ));
         } else {
           // If empty, draw a checker.
           if (y % 2 == 0) {
             if (x % 2 == 0) {
               mGridPaint.setColor(HEX_COLOR_YELLOW);
             } else {
               mGridPaint.setColor(HEX_COLOR_BLUE);
             }
           } else {
             if (x % 2 == 0) {
               mGridPaint.setColor(HEX_COLOR_BLUE);
             } else {
               mGridPaint.setColor(HEX_COLOR_YELLOW);
             }
           }
           // Draw the box.
           canvas.drawRect(mGridRect, mGridPaint);
           Log.v(TAG, String.format("Drawing checkers at: (%d, %d, %d, %d)", 
                                    mGridRect.left,
                                    mGridRect.top,
                                    mGridRect.right,
                                    mGridRect.bottom
                                   ));
         }
         
         // Draw the outline.
         canvas.drawLine( mGridRect.left, mGridRect.top, 
                         mGridRect.right, mGridRect.top, mLinePaint);
         canvas.drawLine( mGridRect.right, mGridRect.top, 
                         mGridRect.right, mGridRect.bottom, mLinePaint);
         canvas.drawLine( mGridRect.right, mGridRect.bottom, 
                          mGridRect.left, mGridRect.bottom, mLinePaint); 
         canvas.drawLine( mGridRect.left, mGridRect.bottom, 
                         mGridRect.left, mGridRect.top, mLinePaint);
 
         //mDstRect.offsetTo(MARGIN + x, MARGIN + y);
         //canvas.drawBitmap(mCellBackground, mSrcRect, mDstRect, mBmpPaint);
 
         //canvas.drawLine(x7    , y7 + k, x7 + s3 - 1, y7 + k     , mLinePaint);
         //canvas.drawLine(x7 + k, y7    , x7 + k     , y7 + s3 - 1, mLinePaint);
         xPixels += cellWidth;
       }
       xPixels = 0;
       yPixels += cellHeight;
     }
   }
   
   //@Override
   //protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
   //  // Keep the view squared
   //  int w = MeasureSpec.getSize(widthMeasureSpec);
   //  int h = MeasureSpec.getSize(heightMeasureSpec);
   //  int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
   //  setMeasuredDimension(d, d);
   //}
 
   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
     super.onSizeChanged(w, h, oldw, oldh);
 
     //int sx = (w - 2 * MARGIN) / 3;
     //int sy = (h - 2 * MARGIN) / 3;
 
     //int size = sx < sy ? sx : sy;
 
     //mSxy = size;
     //mOffetX = (w - 3 * size) / 2;
     //mOffetY = (h - 3 * size) / 2;
 
     // Set the destination rect to the "origin" location.
     //mDstRect.set(MARGIN, MARGIN, size - MARGIN, size - MARGIN);
     //mDstRect.set(MARGIN, MARGIN, w - MARGIN, h - MARGIN);
   }
 
   ////////////////////////////////////////
   // Utility Functions
   ////////////////////////////////////////
   
   private Bitmap getResBitmap(int bmpResId) {
     Options opts = new Options();
     opts.inDither = false;
 
     Resources res = getResources();
     Bitmap bmp = BitmapFactory.decodeResource(res, bmpResId, opts);
 
     if (bmp == null && isInEditMode()) {
       // BitmapFactory.decodeResource doesn't work from the rendering
       // library in Eclipse's Graphical Layout Editor. Use this workaround instead.
 
       Drawable d = res.getDrawable(bmpResId);
       int w = d.getIntrinsicWidth();
       int h = d.getIntrinsicHeight();
       bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
       Canvas c = new Canvas(bmp);
       d.setBounds(0, 0, w - 1, h - 1);
       d.draw(c);
     }
 
     return bmp;
   }
 
   public void setModel(FungeModel model) {
 	  // TODO Auto-generated method stub
 	  mModel = model;
   }
 }
