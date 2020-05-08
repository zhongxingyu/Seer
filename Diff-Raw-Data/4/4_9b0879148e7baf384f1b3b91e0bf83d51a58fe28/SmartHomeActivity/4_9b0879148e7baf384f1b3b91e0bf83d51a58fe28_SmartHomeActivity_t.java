 package com.smarthome;
 
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import rajawali.RajawaliActivity;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.TextView;
 
 public class SmartHomeActivity extends RajawaliActivity implements
 		OnTouchListener, OnKeyListener {
 
 	public Room room;
 	public LinkedList<Room> rooms;
 
 	public boolean isSlider = false;
 	public boolean isDebug = false;
 	public SliderController slider;
 	public DebugController debug;
 	public static int screenWidth;
 	public static int screenHeight;
 	public SmartHomeRenderer mRenderer;
 	public TextView label;
 	public ImageView image;
 	public FrameLayout imagePane;
 
 	public CameraController camera = new CameraController();
 
 	public int initializationState = 0;
 
 	public void prepareImage(int x1, int y1, int x2, int y2, ImageView image,
 			int ix, int iy) {
 		DebugGesture g = new DebugGesture(x1, y1, x2, y2, "", debug);
 		x1 = (int)g.x1; y1 = (int)g.y1; x2 = (int)g.x2; y2 = (int)g.y2;
 		LayoutParams lp = new LayoutParams(x2 - x1, y2 - y1);
 		lp.setMargins(x1, y1, 0, 0);
 		image.setLayoutParams(lp);
 	}
 	
 	public void prepareImageScaled(int x1, int y1, int x2, int y2, ImageView image, int ix, int iy) {
 		LayoutParams lp = new LayoutParams(x2 - x1, y2 - y1);
 		lp.setMargins(x1, y1, 0, 0);
 		image.setLayoutParams(lp);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		finish();
 		System.exit(0);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		System.out.println(initializationState + " at "
 				+ Thread.currentThread().getStackTrace()[2].getClassName()
 				+ "."
 				+ Thread.currentThread().getStackTrace()[2].getMethodName());
 
 		if (initializationState == 127)
 			return;
 
 		camera.activity = this;
 
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		screenHeight = size.y;
 		screenWidth = size.x;
 
 		System.out.println("--SCREEN--");
 		System.out.println("Width: " + screenWidth);
 		System.out.println("Height: " + screenHeight);
 
 		setUpRooms();
 
 		mRenderer = new SmartHomeRenderer(this);
 		mRenderer.setSurfaceView(mSurfaceView);
 		super.setRenderer(mRenderer);
 		mSurfaceView.setOnTouchListener(this);
 		mSurfaceView.setOnKeyListener(this);
 		// mSurfaceView.setOnLongClickListener(this);
 		// mSurfaceView.setOnClickListener(this);
 
 		imagePane = new FrameLayout(this);
 		FrameLayout frame = new FrameLayout(this);
 
 		label = new TextView(this);
 		label.setText("Info:");
 		label.setTextSize(20);
 		label.setGravity(Gravity.LEFT);
 		label.setHeight(100);
 		label.setBackgroundColor(Color.TRANSPARENT);
 		// ll.addView(label);
 		frame.addView(label);
 
 		image = new ImageView(this);
 		image.setImageResource(R.drawable.debug);
 		// image.setAdjustViewBounds(true);
 		prepareImage(0, 0, 800, 480, image, 800, 480);
 		image.setScaleType(ScaleType.FIT_XY);
 		imagePane.addView(image);
 
 		mLayout.addView(frame);
 		mLayout.addView(imagePane);
 
 		debug.actionPerformed("enter", this);
 
 		room.appear(this);
 		initializationState |= 1;
 	}
 
 	@Override
 	public boolean onKeyDown(int keycode, KeyEvent event) {
 		if (keycode == KeyEvent.KEYCODE_MENU) {
 			/*
 			 * AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
 			 * .setMessage("Test") .setTitle("Menu dialog");
 			 * dialogBuilder.create().show();
 			 */
 			if (isDebug) {
 				debug.actionPerformed("leave", this);
 			} else {
 				debug.actionPerformed("enter", this);
 			}
 		}
 		return super.onKeyDown(keycode, event);
 	}
 
 	private float oldX, oldY;
 	private float newX, newY;
 	private boolean firedHandler;
 	private Timer longClick;
 
 	public boolean onTouch(View v, final MotionEvent event) {
 		Timer longClick = new Timer();
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			firedHandler = false;
			newX = oldX = event.getX();
			newY = oldY = event.getY();
 			longClick.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					runOnUiThread(new Runnable() {
 						public void run() {
 							SmartHomeActivity.this.click(true);
 						}
 					});
 				}
 			}, 400);
 		} else if (event.getAction() == MotionEvent.ACTION_UP) {
 			newX = event.getX();
 			newY = event.getY();
 			longClick.cancel();
 			click(false);
 		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			newX = event.getX();
 			newY = event.getY();
 		}
 		return true;
 	}
 
 	public void click(boolean isLong) {
 		if (!firedHandler) {
 			firedHandler = true;
 			System.out.println("~~~" + ((isLong) ? "Long " : "") + "Click~~~");
 
 			if (isDebug && mRenderer != null)
 				debug.fire(newX, newY, this, isLong);
 			else if (isSlider && mRenderer != null)
 				slider.fire(newX, newY, this, isLong);
 			else if (room != null)
 				room.fire(newX, newY, this, isLong);
 		}
 	}
 
 	private void setUpRooms() {
 		System.out.println(initializationState + " at "
 				+ Thread.currentThread().getStackTrace()[2].getClassName()
 				+ "."
 				+ Thread.currentThread().getStackTrace()[2].getMethodName());
 
 		if (initializationState == 127)
 			return;
 
 		debug = new DebugController(10f, 10f, 99, this);
 
 		slider = new SliderController(10f, 10f, 99, this);
 
 		rooms = new LinkedList<Room>();
 
 		// Fge Rume hinzu
 
 		// Raum: Esszimmer
 		rooms.add(new Room(7f, -8f, 0, this));
 
 		// Raum: Kche
 		rooms.add(new Room(25.5f, -8f, 1, this));
 
 		// Raum: Schlafzimmer
 		rooms.add(new Room(12.0f, -30.5f, 2, this));
 
 		// Raum: Wohnzimmer
 		rooms.add(new Room(41.5f, -8f, 3, this));
 
 		// Raum: Flur
 		rooms.add(new Room(17.5f, -19f, 4, this));
 
 		// Fge die Raumwechsel hinzu
 		rooms.get(0).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(1))); // Esszimmer -> Kche
 		rooms.get(0).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(4))); // Esszimmer -> Flur
 
 		rooms.get(1).gestures
 				.add(new RoomGesture(0, 0, 100, 480, rooms.get(0))); // Kche ->
 																		// Esszimmer
 		rooms.get(1).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(4))); // Kche -> Flur
 		rooms.get(1).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(3))); // Kche -> Wohnzimmer
 
 		rooms.get(2).gestures
 				.add(new RoomGesture(0, 0, 800, 100, rooms.get(4)));
 		rooms.get(3).gestures
 				.add(new RoomGesture(0, 0, 100, 480, rooms.get(1)));
 
 		rooms.get(4).gestures
 				.add(new RoomGesture(0, 0, 400, 100, rooms.get(0)));
 		rooms.get(4).gestures.add(new RoomGesture(401, 0, 800, 100, rooms
 				.get(1)));
 		rooms.get(4).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(2)));
 
 		// Fge die Lichtsteuerung hinzu
 		rooms.get(0).gestures.add(new LightGesture(300, 140, 500, 340,
 				"dining_light"));
 		rooms.get(1).gestures.add(new LightGesture(300, 140, 500, 340,
 				"kitchen_main_light"));
 		rooms.get(2).gestures.add(new LightGesture(320, 160, 520, 360,
 				"sleeping_light"));
 		rooms.get(3).gestures.add(new LightGesture(320, 160, 520, 360,
 				"lounge_light"));
 		rooms.get(4).gestures.add(new LightGesture(300, 180, 500, 380,
 				"corridor_light"));
 
 		room = rooms.get(0);
 
 		initializationState |= 2;
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
