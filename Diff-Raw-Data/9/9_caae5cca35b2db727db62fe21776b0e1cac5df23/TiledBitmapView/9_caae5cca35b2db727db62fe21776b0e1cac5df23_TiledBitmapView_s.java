 package net.nologin.meep.ca.view;
 
 import android.graphics.*;
 import android.view.*;
 import android.content.Context;
 import android.util.AttributeSet;
 import net.nologin.meep.ca.model.Tile;
 import static net.nologin.meep.ca.util.Utils.log;
 
 public abstract class TiledBitmapView extends SurfaceView implements SurfaceHolder.Callback {
 
     GestureDetector gestureDetector;
     ScaleGestureDetector scaleDetector;
 
     Paint paint_bg;
     Paint paint_msgText;
     Paint paint_gridLine;
     Paint paint_debugBG;
 
     TileGenerationThread tgThread;
 
     ScreenState state;
 
     TileProvider tileProvider;
 
     private float mScaleFactor = 0.5f;
     private int mOffsetX = 0, mOffsetY = 0;
 
     public TiledBitmapView(Context context, AttributeSet attrs) {
 
         super(context, attrs);
 
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         state = new ScreenState();
         tgThread = new TileGenerationThread(holder, this);
 
         tileProvider = getTileProvider();
 
         // background paint
         paint_bg = new Paint();
         paint_bg.setColor(Color.DKGRAY); // LTGRAY
         paint_bg.setStyle(Paint.Style.FILL);
 
         // background status text paint (needed?)
         paint_msgText = new Paint();
         paint_msgText.setColor(Color.WHITE);
         paint_msgText.setTextSize(20);
         paint_msgText.setAntiAlias(true);
         paint_msgText.setTextAlign(Paint.Align.CENTER);
 
         // background paint
         paint_debugBG = new Paint();
         paint_debugBG.setColor(Color.BLACK);
         paint_debugBG.setStyle(Paint.Style.FILL);
         paint_debugBG.setAlpha(140);
 
         // grid line
         paint_gridLine = new Paint();
         paint_gridLine.setColor(Color.LTGRAY); // DKGRAY
         paint_gridLine.setStyle(Paint.Style.STROKE);
         paint_gridLine.setStrokeWidth(1);
 
         gestureDetector = new GestureDetector(new GestureListener());
         scaleDetector = new ScaleGestureDetector(context,new ScaleListener());
 
 
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
         state.width = width;
         state.height = height;
 
         // TODO: how many of a buffer?
         int horz_tiles = width / tileProvider.getTileSize() + 2;
         int vert_tiles = height / tileProvider.getTileSize() + 2;
 
         state.maxX = horz_tiles;
         state.maxY = vert_tiles;
 
         // offset halfway across horizontal
         int half = horz_tiles / 2;
         state.minX -= half;
         state.maxX = horz_tiles - half; // in case of odd number
 
         // offset the canvas so the 0,0 tile is centered horizontally
         mOffsetX = (width - tileProvider.getTileSize()) / 2;
 
         if (tileProvider != null) {
             tileProvider.onSurfaceChange(width, height);
         }
 
     }
 
 
     class TileGenerationThread extends Thread {
 
         private final SurfaceHolder holder;
         private TiledBitmapView view;
         private boolean running = false;
 
 
 
         public TileGenerationThread(SurfaceHolder holder, TiledBitmapView view) {
             this.holder = holder;
             this.view = view;
         }
 
         public void setRunning(boolean running) {
             this.running = running;
         }
 
 
 
         @Override
         public void run() {
 
             Canvas c;
             while (running) {
 
                 c = null;
 
                 try {
                     c = holder.lockCanvas(null);
                     if(c == null){
                         continue; // is this right?
                     }
                     synchronized (holder) {
 
                         view.doDraw(c);
 
                         tileProvider.renderNext();
 
 
                     }
                     Thread.sleep(5); // so we can interact in a reasonable time
 
                     // saves me commenting out the interrupt catch block if I comment out the sleep() temporarily
                     if(2>3){
                         throw new InterruptedException("bark bark");
                     }
 
 
                 } catch (InterruptedException e) {
                     // nop
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent bitmap
                     if (c != null) {
                         holder.unlockCanvasAndPost(c);
                     }
                 }
             }
 
         }
 
 
     }
 
 
     public void doDraw(Canvas canvas) {
 
         super.onDraw(canvas);
 
         canvas.save();
 
         // draw BG
         canvas.drawRect(new Rect(0, 0, state.width, state.height), paint_bg);
 
         if (tileProvider != null) {
 
             for(int tilePosX=state.minX;tilePosX<=state.maxX;tilePosX++){
 
                 for(int tilePosY=state.minY;tilePosY<=state.maxY;tilePosY++){
 
                     int size = tileProvider.getTileSize();
                    int x = tilePosX * size + mOffsetX;
                    int y = tilePosY * size + mOffsetY;
 
 
                     Tile t = tileProvider.getTile(tilePosX,tilePosY);
                     if(t == null){
                         continue;
                     }
 
                     if (t.renderFinished()) {
 
                         //bitmap.setPixels(t.bitmap, 0, tileSize, xOff, yOff, tileSize, tileSize);
                         canvas.drawBitmap(t.bitmap,x ,y ,null);
 
                         // TODO: remove or make debug dependent
                         // canvas.drawRect(t.getRect(x,y), paint_gridLine);
 
                     } else {
 
                         canvas.drawRect(t.getRect(x,y), paint_gridLine);
 
                         String fmt1 = "Tile(%d,%d)";
                         String msg1 = String.format(fmt1, tilePosX, tilePosY);
                         canvas.drawText(msg1, x + (size/2), y + (size/2), paint_msgText);
 
                     }
 
 
                 }
 
             }
 
 
 
 
 
 
             //canvas.drawBitmap(bitmap, mOffsetX, mOffsetY, null);
 
 
         }
 
         drawDebugBox(canvas);
 
         canvas.restore();
 
     }
 
     private void drawDebugBox(Canvas canvas) {
 
         // draw a bunch of debug stuff
         String fmt1 = "%dx%d, s=%1.3f";
         String fmt2 = "offset x=%d y=%d";
         String fmt3 = "tiles [%d,%d -> %d,%d]";
         String msg1 = String.format(fmt1, state.width, state.height,mScaleFactor);
         String msg2 = String.format(fmt2,mOffsetX, mOffsetY);
         String msg3 =  String.format(fmt3,state.minX,state.minY,state.maxX,state.maxY);
         String msg4 = tileProvider.toString();
 
         float boxWidth = 300, boxHeight = 120;
 
         float debug_x = state.width - boxWidth;
         float debug_y = state.height - boxHeight;
 
         canvas.drawRect(debug_x, debug_y, state.width, state.height, paint_debugBG);
 
         canvas.drawText(msg1, debug_x + boxWidth / 2, debug_y + 30, paint_msgText);
         canvas.drawText(msg2, debug_x + boxWidth / 2, debug_y + 55, paint_msgText);
         canvas.drawText(msg3, debug_x + boxWidth / 2, debug_y + 80, paint_msgText);
         canvas.drawText(msg4, debug_x + boxWidth / 2, debug_y + 105, paint_msgText);
 
     }
 
 
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         if (!tgThread.isAlive()) {
             tgThread = new TileGenerationThread(holder, this);
             tgThread.setRunning(true);
             tgThread.start();
         }
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Based on android example 'LunarLander' app
         // we have to tell tgThread to shut down & wait for it to finish, or else
         // it might touch the Surface after we return and explode
         boolean retry = true;
         tgThread.setRunning(false);
         while (retry) {
             try {
                 tgThread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 // loop until we've
             }
         }
     }
 
 
     @Override // register GD
     public boolean onTouchEvent(MotionEvent me) {
 
         invalidate();
 
         gestureDetector.onTouchEvent(me);
         scaleDetector.onTouchEvent(me);
 
         return true;
     }
 
 
     class ScreenState {
 
         int height;
         int width;
 
         int minX = 0, maxX = 0, minY = 0, maxY;
 
     }
 
     // http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
     class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
         @Override
         public boolean onScale(ScaleGestureDetector detector) {
 
             mScaleFactor *= detector.getScaleFactor();
 
             // Don't let the object get too small or too large.
             mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
 
             log("Scale factor now " + mScaleFactor + " - " + tgThread.running);
 
 
             return true;
         }
     }
 
 
     class GestureListener extends GestureDetector.SimpleOnGestureListener {
 
         @Override
         public void onShowPress(MotionEvent motionEvent) {
 
             log("show press");
 
         }
 
         @Override
         public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
 
             //log("scroll x=" + distanceX + ", y=" + distanceY);
             mOffsetX -= (int)distanceX;
             mOffsetY -= (int)distanceY;
 
 
 
             return true;
         }
 
         @Override
         public void onLongPress(MotionEvent motionEvent) {
 
             log("long press");
 
         }
 
         @Override
         public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
 
             log("fling");
 
             return true;
         }
 
         @Override
         public boolean onDoubleTap(MotionEvent e) {
 
             log("double tap");
 
             return true;
         }
 
         @Override
         public boolean onSingleTapConfirmed(MotionEvent e) {
 
             log("single tap");
 
             return false;
         }
 
     }
 
     public abstract TileProvider getTileProvider();
 
     public interface TileProvider {
 
         public void onSurfaceChange(int newWidthPx, int newHeightPx);
 
         public int getTileSize();
 
         public Tile getTile(int x, int y);
 
         public void renderNext();
 
 
     }
 }
