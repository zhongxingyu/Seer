 package com.group7.dragonwars;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 import org.json.*;
 
 import android.app.Activity;
 import android.content.*;
 import android.content.res.*;
 import android.graphics.*;
 import android.graphics.PorterDuff.*;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.*;
 import android.widget.ScrollView;
 import android.widget.Toast;
 
 import com.group7.dragonwars.engine.*;
 import com.group7.dragonwars.util.SystemUiHider;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  *
  *      I removed almost all of this, a lot of it was to make the navigation ui
  *      (top bar + any software buttons) appear and disappear and animate,
  *      however now we only want it gone. it was also incredibly boring.
  */
 public class GameActivity extends Activity {
     private static final String TAG = "GameActivity";
     private ScrollView map_scroller;
     private Integer orientation;
     private Boolean orientationChanged = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // remove the title bar (it would normally say something like "Dragon Wars" or "Battle"
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         // remove the status bar (the top bar containing things such as the time, and notifications)
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         Log.d(TAG, "in onCreate");
         setContentView(R.layout.activity_game);
     }
 
 }
 
 class GameView extends SurfaceView implements SurfaceHolder.Callback {
     final private String TAG = "GameView";
     Bitmap bm;
     GameState state;
     Logic logic;
     GameMap map;
     Position selected; // the (coordinates of the) currently selected position
     ScrollOffset scroll_offset; // the offset caused by scrolling, in pixels
     
     DrawingThread dt;
     Paint circle_paint; // used to draw the selection circles (to show which tile is selected)
     Paint move_high_paint; // used to highlight movements
     
     boolean unit_selected; // true if there is a unit at selection
     
     // List<Position> unit_destinations; // probably not best to recompute this every time, or maybe it is, treat as Undefined if !unit_selected
     
     Context context;
     HashMap<String, HashMap<String, Bitmap>> graphics;
     private Integer orientation;
     int tilesize = 64; // the size (in pixels) to draw the square tiles
 
     
     public GameView(Context ctx, AttributeSet attrset) {
         super(ctx, attrset);
         Log.d(TAG, "GameView ctor");
 
         GameView game_view = (GameView) this.findViewById(R.id.game_view);
         GameMap gm = null;
         
         Log.d(TAG, "nulling GameMap");
 
         try {
             gm = MapReader.readMap(readFile(R.raw.testmap)); // ugh
         } catch (JSONException e) {
             Log.d(TAG, "Failed to load the map: " + e.getMessage());
         }
 
         if (gm == null) {
             Log.d(TAG, "gm is null");
             System.exit(1);
         }
 
         Log.d(TAG, "before setMap");
         game_view.setMap(gm);
         this.logic = new Logic();
         this.state = new GameState(map, logic);
 
         context = ctx;
         bm = BitmapFactory.decodeResource(context.getResources(),
                                           R.drawable.ic_launcher);
         SurfaceHolder holder = getHolder();
         this.graphics = new HashMap<String, HashMap<String, Bitmap>>();
         Log.d(TAG, "this.graphics new");
         /* Register game fields */
         this.graphics.put("Fields", new HashMap<String, Bitmap>());
         Log.d(TAG, "after putting empty Fields");
         Boolean b = map == null;
         Log.d(TAG, "map is current null?: " + b.toString());
 
         for (Map.Entry<Character, GameField> ent : this.map.getGameFieldMap().entrySet()) {
             Log.d(TAG, "inside for loop, about to ent.getValue()");
             GameField f = ent.getValue();
             Log.d(TAG, "about to getResources() for " + f.getFieldName());
             Integer resourceID = getResources().getIdentifier(f.getSpriteLocation(),
                                  f.getSpriteDir(),
                                  f.getSpritePack());
             Log.d(TAG, "after getResources()");
             this.graphics.get("Fields").put(f.getFieldName(),
                                             BitmapFactory.decodeResource(context.getResources(),
                                                     resourceID));
             Log.d(TAG, "after putting decoded resource into Fields");
         }
 
         Log.d(TAG, "after fields");
         /* Register units */
         
         this.graphics.put("Units", new HashMap<String, Bitmap>());
 
         for (Map.Entry<Character, Unit> ent : this.map.getUnitMap().entrySet()) {
             Unit f = ent.getValue();
             Log.d(TAG, "about to getResources() for " + f.getUnitName());
             Integer resourceID = getResources().getIdentifier(f.getSpriteLocation(),
                                  f.getSpriteDir(),
                                  f.getSpritePack());
             this.graphics.get("Units").put(f.getUnitName(),
                                            BitmapFactory.decodeResource(context.getResources(),
                                                    resourceID));
         }
         
 
         Log.d(TAG, "after units");
         /* Register buildings */
         this.graphics.put("Buildings", new HashMap<String, Bitmap>());
 
         for (Map.Entry<Character, Building> ent : this.map.getBuildingMap().entrySet()) {
             Building f = ent.getValue();
             Log.d(TAG, "about to getResources() for " + f.getBuildingName());
             Integer resourceID = getResources().getIdentifier(f.getSpriteLocation(),
                                  f.getSpriteDir(),
                                  f.getSpritePack());
             this.graphics.get("Buildings").put(f.getBuildingName(),
                                                BitmapFactory.decodeResource(context.getResources(),
                                                        resourceID));
         }
 
         Log.d(TAG, "after buildings");
         holder.addCallback(this);
         
         selected = new Position(0, 0); // I really hope that it's ok to assume that the map is at least 1*1
         
         unit_selected = map.getField(selected).hostsUnit();
         
         scroll_offset = new ScrollOffset(0f, 0f);
         
         circle_paint = new Paint();
         circle_paint.setStyle(Paint.Style.FILL);
         circle_paint.setARGB(200, 255, 255, 255); // semi-transparent white
         
         move_high_paint = new Paint();
         move_high_paint.setStyle(Paint.Style.FILL);
         move_high_paint.setARGB(150, 0, 0, 255); // semi-transparent white
     }
 
     private List<String> readFile(int resourceid) {
         List<String> text = new ArrayList<String>();
 
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(this
                                                    .getResources().openRawResource(resourceid)));
             String line;
 
             while ((line = in.readLine()) != null)
                 text.add(line);
 
             in.close();
         } catch (FileNotFoundException fnf) {
             System.err.println("Couldn't find " + fnf.getMessage());
             System.exit(1);
         } catch (IOException ioe) {
             System.err.println("Couldn't read " + ioe.getMessage());
             System.exit(1);
         }
 
         return text;
     }
 
     public void setMap(GameMap newmap) {
         this.map = newmap;
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder arg0) {
         dt = new DrawingThread(arg0, context, this);
         dt.setRunning(true);
         dt.start();
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder arg0) {
         dt.setRunning(false);
 
         try {
             dt.join();
         } catch (InterruptedException e) {
             // TODO something, perhaps, but what?
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	int touchX = (int)((event.getX() - scroll_offset.getX()) / tilesize); // the coordinates of the pressed tile
     	int touchY = (int)((event.getY() - scroll_offset.getY()) / tilesize); // taking into account scrolling
         //Log.v(null, "Touched at: (" + touchX + ", " + touchY + ")");
     	// TODO: if ( this is a tap ) {
         	if (event.getAction() == MotionEvent.ACTION_UP) {
                 Log.v(null, "Touch ended at: (" + touchX + ", " + touchY + ") (dimensions are: (" + this.map.getWidth() + "x" + this.map.getHeight() + "))");
 		        Position newselected = new Position(touchX, touchY);
 		        if (this.map.isValidField(touchX, touchY)) {
 		        	Log.v(null, "Setting selection");
 		        	this.selected = newselected;
 		        }
         	}
 		// TODO: } else { scroll the map }
         return true;
     }
 
 
     public RectF getSquare(float x, float y, float length) {
     	return new RectF(x, y, x + length, y + length);
     }
     
     public void doDraw(Canvas canvas) {
         Configuration c = getResources().getConfiguration();
 
         if (orientation == null || orientation != c.orientation) {
             orientation = c.orientation;
             canvas.drawColor(Color.BLACK);
             Log.d(TAG, "Painted canvas black due to orientation change.");
         }
         // canvas.drawColor(Color.BLACK); // Draw black anyway, in order to ensure that there are no leftover graphics
         
         GameField selected_field = map.getField(selected);
         List<Position> unit_destinations = selected_field.hostsUnit() ? logic.destinations(map, selected_field.getUnit()) : new ArrayList<Position>(0);
         
         for (int i = 0; i < map.getWidth(); ++i) {
             for (int j = 0; j < map.getHeight(); j++) {
                 // canvas.drawBitmap(tiles.get(0), size * j, size * i, null); // Draw
             	Position pos = new Position(i, j);
                 GameField gf = map.getField(i, j);
                 String gfn = gf.getFieldName();
                 RectF dest = getSquare(
                 		tilesize * i + scroll_offset.getX(),
                 		tilesize * j + scroll_offset.getY(),
                 		tilesize);
                 canvas.drawBitmap(graphics.get("Fields").get(gfn), null, dest, null);
 
                 if (gf.hostsBuilding()) {
                     Building b = gf.getBuilding();
                     String n = b.getBuildingName();
 
                     canvas.drawBitmap(graphics.get("Buildings").get(n), null, dest, null);
                 }
 
                 if (gf.hostsUnit()) {
                     Unit unit = gf.getUnit();
 
                     if (unit != null) {
                         String un = unit.toString();
                         canvas.drawBitmap(graphics.get("Units").get(un), null, dest, null);
                     }
                 }
             }
         }
         
         
         // perform highlighting
         for (Position pos : unit_destinations) {
         	RectF dest = getSquare(
             		tilesize * pos.getX() + scroll_offset.getX(),
             		tilesize * pos.getY() + scroll_offset.getY(),
             		tilesize);
         	canvas.drawRect(dest, move_high_paint);
         }
         
         // draw the selection shower (bad name)
         int circle_radius = 10;
         RectF select_rect = getSquare(
         		tilesize * selected.getX() + scroll_offset.getX(),
         		tilesize * selected.getY() + scroll_offset.getY(),
         		tilesize);
         canvas.drawCircle(select_rect.left, select_rect.top, circle_radius, circle_paint); // top left
         canvas.drawCircle(select_rect.left, select_rect.bottom, circle_radius, circle_paint); // bottom left
         canvas.drawCircle(select_rect.right, select_rect.top, circle_radius, circle_paint); // top right
         canvas.drawCircle(select_rect.right, select_rect.bottom, circle_radius, circle_paint); // bottom right
 
 
 	// draw the information box
 	drawInfoBox(canvas, null, selected_field, true);
     }
 
    public void drawInfoBox(Canvas canvas, Unit unit, Field field, boolean left) {
         String info = field.getFieldName();
         if (field.hostsBuilding) {
             info = info + " : " + field.getBuilding().getBuildingName();
         }
         Paint text_paint = new Paint();
        text_paint.setARGB(255, 255, 255, 255);
         Rect text_bounds = new Rect();
         text_paint.getTextBounds(info, 0, info.length(), text_bounds);
 
         Paint back_paint = new Paint();
 	back_paint.setStyle(Paint.Style.FILL);
         back_paint.setARGB(150, 0, 0, 0);
         Rect back_rect = new Rect(0, canvas.getHeight() - text_bounds.bottom, text_bounds.right, canvas.getHeight());
 
         canvas.drawRect(back_rect, back_paint);
         canvas.drawText(info, 0, info.length(), back_rect.left, back_rect.top);
    } 
 
 }
 
 class DrawingThread extends Thread {
     boolean run;
     Canvas canvas;
     SurfaceHolder surfaceholder;
     Context context;
     GameView gview;
 
     public DrawingThread(SurfaceHolder sholder, Context ctx, GameView gv) {
         surfaceholder = sholder;
         context = ctx;
         run = false;
         gview = gv;
     }
 
     void setRunning(boolean newrun) {
         run = newrun;
     }
 
     @Override
     public void run() {
         super.run();
 
         while (run) {
             canvas = surfaceholder.lockCanvas();
 
             if (canvas != null) {
                 gview.doDraw(canvas);
                 surfaceholder.unlockCanvasAndPost(canvas);
             }
         }
     }
 
 }
 
 class ScrollOffset { // the Position code, but with Floats
     private Pair<Float, Float> pair;
 
     public ScrollOffset(Float x, Float y) {
         this.pair = new Pair<Float, Float>(x, y);
     }
 
     public Float getX() {
         return this.pair.getLeft();
     }
 
     public Float getY() {
         return this.pair.getRight();
     }
 
     public Boolean equals(ScrollOffset other) {
         return this.getX() == other.getX() && this.getY() == other.getY();
     }
 
     public String toString() {
         return String.format("(%d, %d)", this.pair.getLeft(),
                              this.pair.getRight());
     }
 
 }
 
