 package com.smarthome;
 
 import java.util.LinkedList;
 
 import rajawali.RajawaliActivity;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.View.OnKeyListener;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SmartHomeActivity extends RajawaliActivity implements OnTouchListener, OnKeyListener {
 
 	public Room room;
 	public LinkedList<Room> rooms;
 	
 	public boolean isDebug = false;
 	public DebugController debug;
 	public static int screenWidth;
 	public static int screenHeight;
 	public SmartHomeRenderer mRenderer;
 	public TextView label;
 	public ImageView image;
 	
 	public CameraController camera = new CameraController();
 	
 	public void prepareImage(float x1, float y1, float x2, float y2, ImageView image, int ix, int iy) {
 		DebugGesture g = new DebugGesture(x1, y1, x2, y2, "", debug);
 		x1 = g.x1; y1 = g.y1; x2 = g.x2; y2 = g.y2;
 		float sx = (x2 - x1) / ix;
 		float sy = (y2 - y1) / iy;
 		Matrix matrix = new Matrix();
 		matrix.setScale(sx, sy);
 		matrix.setTranslate(x1, y1);
 		image.setImageMatrix(matrix);
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	Display display = getWindowManager().getDefaultDisplay();
     	Point size = new Point();
     	display.getSize(size);
     	screenHeight = size.y;
     	screenWidth = size.x;
     	
     	System.out.println("--SCREEN--");
     	System.out.println("Width: "+screenWidth);
     	System.out.println("Height: "+screenHeight);
     	
     	setUpRooms();
     	
     	mRenderer = new SmartHomeRenderer(this);
     	mRenderer.setSurfaceView(mSurfaceView);
     	super.setRenderer(mRenderer);
     	mSurfaceView.setOnTouchListener(this);
     	mSurfaceView.setOnKeyListener(this);
     	
     	LinearLayout ll = new LinearLayout(this);
     	ll.setOrientation(LinearLayout.VERTICAL);
         ll.setGravity(Gravity.TOP);
         
         FrameLayout frame = new FrameLayout(this);
         
         
         label = new TextView(this);
         label.setText("Info:");
         label.setTextSize(20);
         label.setGravity(Gravity.LEFT);
         label.setHeight(100);
         label.setBackgroundColor(Color.TRANSPARENT);
         //ll.addView(label);
         frame.addView(label);
         
         image = new ImageView(this);
         image.setImageResource(R.drawable.debug);
         image.setScaleType(ScaleType.MATRIX);
         prepareImage(0, 0, 800, 480, image, 800, 480);
         ll.addView(image);
         
         mLayout.addView(frame);
         mLayout.addView(ll);
         
         debug.actionPerformed("enter", this);
     }
     
     @Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
     public boolean onKeyDown(int keycode, KeyEvent event ) {
      if(keycode == KeyEvent.KEYCODE_MENU){
       /*AlertDialog.Builder dialogBuilder 
       = new AlertDialog.Builder(this)
       .setMessage("Test")
       .setTitle("Menu dialog");
       dialogBuilder.create().show();*/
     	 if(isDebug) {
     		 debug.actionPerformed("leave", this);
     	 }else{
     		 debug.actionPerformed("enter", this);
     	 }
      }
      return super.onKeyDown(keycode,event);  
     }
     
 	public boolean onTouch(View v, MotionEvent event) {
 		if( event.getAction() == MotionEvent.ACTION_DOWN) {
 			if (isDebug && mRenderer != null)
 				debug.fire(event.getX(), event.getY(), this);
 			else if (room != null)
 				room.fire(event.getX(), event.getY(), this);
 		}
 		if(event.getAction() == MotionEvent.ACTION_MOVE) {
 			System.out.println("Moved!");
 		}
 		return super.onTouchEvent(event);
 	}
 
 	private void setUpRooms() {
 		debug = new DebugController(10f, 10f, 99, this);
 		
 		rooms = new LinkedList<Room>();
 		
 		//Fge Rume hinzu
 		
 		//Raum: Esszimmer
 		rooms.add(new Room(7f, -8f, 0));
 		
 		//Raum: Kche
 		rooms.add(new Room(25.5f, -8f, 1));
 		
 		//Raum: Schlafzimmer
 		rooms.add(new Room(12.0f, -30.5f, 2));
 		
 		//Raum: Wohnzimmer
 		rooms.add(new Room(41.5f, -8f, 3));
 		
 		//Raum: Flur
 		rooms.add(new Room(17.5f, -19f, 4));
 		
 		//Fge die Raumwechsel hinzu
 		rooms.get(0).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(1)));	//Esszimmer -> Kche
 		rooms.get(0).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(4)));	//Esszimmer -> Flur
 
 		rooms.get(1).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(0)));	//Kche	-> Esszimmer
 		rooms.get(1).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(4)));	//Kche	-> Flur
 		rooms.get(1).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(3)));	//Kche	-> Wohnzimmer
 		
 		rooms.get(2).gestures.add(new RoomGesture(0, 0, 800, 100, rooms.get(4)));
 		
 		rooms.get(3).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(1)));
 		
 		rooms.get(4).gestures.add(new RoomGesture(0, 0, 400, 100, rooms.get(0)));
 		rooms.get(4).gestures.add(new RoomGesture(401, 0, 800, 100, rooms.get(1)));
 		rooms.get(4).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(2)));
 		
 		//Fge die Lichtsteuerung hinzu
 		rooms.get(0).gestures.add(new LightGesture(100,100,700,380, "dining_light_color"));
 		rooms.get(1).gestures.add(new LightGesture(100,100,700,380, "kitchen_light_color"));
 		rooms.get(2).gestures.add(new LightGesture(100,100,700,380, "lounge_light_color"));
 		rooms.get(3).gestures.add(new LightGesture(100,100,700,380, "sleeping_light_color"));
 		rooms.get(4).gestures.add(new LightGesture(100,100,700,380, "corridor_light_color"));
 		
 		room = rooms.get(0);
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		return true;
 	}
 }
