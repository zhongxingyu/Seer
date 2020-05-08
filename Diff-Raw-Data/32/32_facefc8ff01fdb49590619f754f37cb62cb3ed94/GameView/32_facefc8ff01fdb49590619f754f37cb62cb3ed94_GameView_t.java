 package com.threewisedroids.superc4;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 import com.threewisedroids.superc4.backend.GameState;
 import com.threewisedroids.superc4.backend.ai.AI;
 
 public class GameView extends BetterSurfaceView {
 
     Paint     paint;
     GameState state        = null;
 
     RectF     gridRect     = new RectF();
 
     Point     selectedCell = new Point();
     boolean   selecting    = false;
 
     boolean   useAI;
 
     Bitmap    playerBitmaps[];
 
     Thread    aiThread;
     AI        ai;
 
     public GameView(Context context) {
         super(context);
     }
 
     public GameView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public GameView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     void drawDiscs(Canvas c) {
         int gridSize = state.getGridSize();
 
         for (int i = 0; i < gridSize; i++)
             for (int j = 0; j < gridSize; j++) {
                 int player = state.getCell(i, j);
                 if (player > 0) {
                     RectF dst = new RectF(getXLine(i), getYLine(j),
                             getXLine(i + 1), getYLine(j + 1));
                     if (player == 3) {
                         paint.setColor(Color.GRAY);
                         c.drawRect(dst, paint);
                         paint.setColor(Color.BLACK);
                     } else {
                         // Draw something...
                         c.drawBitmap(playerBitmaps[player - 1], null, dst,
                                 paint);
                     }
                 }
             }
     }
 
     void drawGrid(Canvas c) {
 
         int gridSize = state.getGridSize();
 
         paint.setColor(Color.BLACK);
 
         for (int i = 0; i <= gridSize; i++) {
 
             float y = getYLine(i);
             c.drawLine(gridRect.left, y, gridRect.right, y, paint);
 
             float x = getXLine(i);
             c.drawLine(x, gridRect.bottom, x, gridRect.top, paint);
         }
     }
 
    void drawSelected(Canvas c) {
        if (selecting && state.isInRange(selectedCell.x, selectedCell.y)) {
            paint.setColor(Color.rgb(200, 200, 200));
            c.drawRect(getXLine(selectedCell.x), gridRect.top,
                    getXLine(selectedCell.x + 1), gridRect.bottom, paint);
            c.drawRect(gridRect.left, getYLine(selectedCell.y), gridRect.right,
                    getYLine(selectedCell.y + 1), paint);

            paint.setColor(Color.rgb(150, 150, 150));
            c.drawRect(getXLine(selectedCell.x),
                    getYLine(selectedCell.y),
                    getXLine(selectedCell.x + 1),
                    getYLine(selectedCell.y + 1), paint);
        }
    }

     void drawWinningCells(Canvas c) {
         if (!state.hasVictory())
             return;
 
         List<Point> winningCells = state.getWinningCells();
 
        if (state.getNextPlayer() == 1)
            paint.setColor(Color.rgb(255, 180, 180));
        else
            paint.setColor(Color.rgb(180, 180, 255));
         for (Point p : winningCells) {
             c.drawRect(getXLine(p.x), getYLine(p.y), getXLine(p.x + 1),
                     getYLine(p.y + 1), paint);
         }
         paint.setColor(Color.BLACK);
     }
 
     public float getXLine(int i) {
         return gridRect.left + i * gridRect.width() / state.getGridSize();
     }
 
     public float getYLine(int j) {
         return gridRect.top + j * gridRect.height() / state.getGridSize();
     }
 
     @Override
     public void onDraw(Canvas c) {
         if (state == null)
             return;
 
         paint.setColor(Color.rgb(255, 255, 255));
         c.drawRect(gridRect, paint);
 
        drawSelected(c);
         drawWinningCells(c);
         drawGrid(c);
         drawDiscs(c);
     }
 
     @Override
     public void onInit() {
         setZOrderOnTop(true);
         holder.setFormat(PixelFormat.TRANSPARENT);
 
         playerBitmaps = new Bitmap[2];
 
         playerBitmaps[0] = BitmapFactory.decodeResource(getResources(),
                 R.drawable.blue_disc);
         playerBitmaps[1] = BitmapFactory.decodeResource(getResources(),
                 R.drawable.red_disc);
 
         paint = new Paint();
         paint.setFilterBitmap(true);
     }
 
     @Override
     public void onResize() {
         float padding = 1;
         float squareSize = Math.min(size.x, size.y) - 1 - 2 * padding;
 
         gridRect.left = (size.x - squareSize) / 2;
         gridRect.top = (size.y - squareSize) / 2;
 
         gridRect.right = (size.x + squareSize) / 2;
         gridRect.bottom = (size.y + squareSize) / 2;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_MOVE:
                 // Start dragging
                 selecting = true;
                 selectedCell.x = (int) android.util.FloatMath.floor(state
                         .getGridSize()
                         * (event.getX() - gridRect.left)
                         / gridRect.width());
                 selectedCell.y = (int) android.util.FloatMath.floor(state
                         .getGridSize()
                         * (event.getY() - gridRect.top)
                         / gridRect.height());
                 break;
             case MotionEvent.ACTION_UP:
                 selecting = false;
                 if (state.canPlay(selectedCell.x, selectedCell.y))
                     play(selectedCell.x, selectedCell.y);
         }
         return true;
     }
 
     public void play(int x, int y) {
         if (state.hasVictory())
             return;
 
         if (useAI && state.getNextPlayer() != 1)
             return;
 
         state.play(selectedCell.x, selectedCell.y);
 
         // Check for a victory
         if (useAI) {
             playAI();
         }
     }
 
     public void playAI() {
         if (!state.hasVictory()) {
             ai = new AI();
             aiThread = new Thread() {
                 @Override
                 public void run() {
                     ai.play(state);
                 }
             };
             aiThread.start();
         }
     }
 
     public void setGameState(GameState state) {
         this.state = state;
     }
 
     public void setUseAI(boolean useAI) {
         this.useAI = useAI;
     }
 
     public void stopThread() {
         if (ai != null)
             ai.stop();
 
         if (aiThread != null && aiThread.isAlive())
             try {
                 aiThread.join();
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
     }
 }
