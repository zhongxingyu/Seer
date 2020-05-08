 package com.smarthome;
 
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import rajawali.RajawaliActivity;
 import android.R.integer;
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
 		x1 = (int) g.x1;
 		y1 = (int) g.y1;
 		x2 = (int) g.x2;
 		y2 = (int) g.y2;
 		LayoutParams lp = new LayoutParams(x2 - x1, y2 - y1);
 		lp.setMargins(x1, y1, 0, 0);
 		image.setLayoutParams(lp);
 	}
 
 	public void prepareImageScaled(int x1, int y1, int x2, int y2,
 			ImageView image, int ix, int iy) {
 		LayoutParams lp = new LayoutParams(x2 - x1, y2 - y1);
 		lp.setMargins(x1, y1, 0, 0);
 		image.setLayoutParams(lp);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		//Clear the cache
 		
 		
 		// New try for closing it complete
 		android.os.Process.killProcess(android.os.Process.myPid());
 
 		// Works on smartphone well, doesn'T work on galaxy tab 2 Oo
 		// finish();
 		// System.exit(0);
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
 	public float newX, newY;
 	private boolean firedHandler;
 	private Timer longClick;
 	public Gesture lightGroup;
 
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
 			System.out.println("~~~" + ((isLong) ? "Long " : "") + "Click["
 					+ newX + "/" + newY + "]~~~");
 
 			float distX = Math.abs(newX - oldX);
 			float distY = Math.abs(newY - oldY);
 			if (Math.max(distX, distY) > screenWidth / 8) {
 				String gestureStr = "";
 				if (distX > distY) {
 					if (newX > oldX)
 						gestureStr = "right";
 					else
 						gestureStr = "left";
 				} else {
 					if (newY > oldY)
 						gestureStr = "down";
 					else
 						gestureStr = "up";
 				}
 				if (isDebug && mRenderer != null)
 					;
 				else if (isSlider && mRenderer != null)
 					;
 				else if (room != null)
 					room.gesture(this, gestureStr);
 				return;
 			}
 
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
 
 		// Esszimmer Fenster
 		rooms.add(new Room(10f, -11f, -16f, -75f, 0f, 0f, 5, this));
 
 		// Kche Fenster
 		rooms.add(new Room(26.5f, -11f, -16f, -75f, 0f, 0f, 6, this));
 
 		// Wohnzimmer Fenster
 		rooms.add(new Room(36.5f, -5f, -16f, 85f, -55f, -170f, 7, this));
 
 		// Wohnzimmer Fenster
 		rooms.add(new Room(15f, -29f, -16f, 75f, 0f, -180f, 8, this));
 
 		// Fge die Raumwechsel hinzu
 		rooms.get(0).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(1), "left")); // Esszimmer -> Kche
 		rooms.get(0).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(4), "up")); // Esszimmer -> Flur
 		rooms.get(0).gestures.add(new RoomGesture(270, 0, 600, 100, rooms
 				.get(5), "down"));
 
 		rooms.get(1).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(0),
 				"right")); // Kche ->
 		// Esszimmer
 		rooms.get(1).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(4), "up")); // Kche -> Flur
 		rooms.get(1).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(3), "left")); // Kche -> Wohnzimmer
 		rooms.get(1).gestures.add(new RoomGesture(270, 0, 600, 100, rooms
 				.get(6), "down"));
 
 		rooms.get(2).gestures.add(new RoomGesture(0, 0, 800, 100, rooms.get(4),
 				"down"));
 		rooms.get(2).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(8), "up"));
 
 		rooms.get(3).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(1),
 				"right"));
 		rooms.get(3).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(7), "left"));
 
 		rooms.get(4).gestures.add(new RoomGesture(0, 0, 400, 100, rooms.get(0),
 				"right"));
 		rooms.get(4).gestures.add(new RoomGesture(401, 0, 800, 100, rooms
 				.get(1), "down"));
 		rooms.get(4).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(2), "up"));
 		rooms.get(4).gestures.add(new RoomGesture(700, 0, 800, 480, rooms
 				.get(3), "left"));
 
 		rooms.get(5).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(0), "up"));
 
 		rooms.get(6).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(1), "up"));
 
 		rooms.get(7).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(3), "up"));
 
 		rooms.get(8).gestures.add(new RoomGesture(0, 380, 800, 480, rooms
 				.get(2), "up"));
 
 		// Fge die Lichtsteuerung hinzu
 		LightUnit lu;
 		lu = new LightUnit("dining_light");
 		rooms.get(0).gestures.add(new LightGesture(300, 140, 500, 340, lu));
 		lu = new LightUnit("kitchen_main_light");
 		rooms.get(1).gestures.add(new LightGesture(300, 140, 500, 340, lu));
 		lu = new LightUnit("sleeping_light");
 		rooms.get(2).gestures.add(new LightGesture(320, 160, 520, 360, lu));
 		lu = new LightUnit("lounge_light");
 		rooms.get(3).gestures.add(new LightGesture(320, 160, 520, 360, lu));
 		lu = new LightUnit("corridor_light");
 		rooms.get(4).gestures.add(new LightGesture(300, 180, 500, 380, lu));
 
 		room = rooms.get(0);
 
 		lightGroup = new Gesture();
 
 		for (Room room : rooms) {
 			for (Gesture gesture : room.gestures) {
 				if (gesture instanceof LightGesture) {
 					gesture.parent = lightGroup;
 				}
 			}
 		}
 
 		CurtainUnit cu;
 		cu = new CurtainUnit("blinds_dining_kitchen");
 		rooms.get(5).gestures.add(new CurtainGesture(100, 10, 300, 210, cu));
 		rooms.get(5).gestures.add(new CurtainGesture(515, 10, 715, 210, cu));
 
 		rooms.get(6).gestures.add(new CurtainGesture(70, 10, 270, 210, cu));
 		rooms.get(6).gestures.add(new CurtainGesture(465, 10, 665, 210, cu));
 
		cu = new CurtainUnit("blinds_lounge");
		rooms.get(7).gestures.add(new CurtainGesture(410, 200, 510, 300, cu));
 
 		cu = new CurtainUnit("blinds_sleeping");
 		rooms.get(8).gestures.add(new CurtainGesture(700, 210, 800, 310, cu));
 
 		initializationState |= 2;
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		return true;
 	}
 }
