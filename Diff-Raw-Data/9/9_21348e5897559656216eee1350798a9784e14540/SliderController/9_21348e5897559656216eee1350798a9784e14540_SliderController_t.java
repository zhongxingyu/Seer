 package com.smarthome;
 
 import android.view.View;
 
 public class SliderController extends Room implements ActionListener {
 	SmartHomeActivity activity;
 	LightGesture lightGesture;
 	public SliderController(float x, float y, int id, SmartHomeActivity activity) {
 		super(x, y, id, activity);
 		this.activity = activity;
 		//this.gestures.add(new DebugGesture(0, 0, 800, 480, "debug", this));
 		this.gestures.add(new DebugGesture(100, 100, 209, 220, "on", this));
 		this.gestures.add(new DebugGesture(210, 100, 319, 220, "off", this));
 		this.gestures.add(new DebugGesture(320, 100, 448, 220, "target", this));
 		this.gestures.add(new DebugGesture(450, 100, 578, 220, "room", this));
 		this.gestures.add(new DebugGesture(590, 100, 700, 220, "all", this));
 		this.gestures.add(new DebugGesture(100, 250, 700, 280, "red", this));
 		this.gestures.add(new DebugGesture(100, 300, 700, 330, "green", this));
 		this.gestures.add(new DebugGesture(100, 350, 700, 380, "blue", this));
 		this.gestures.add(new DebugGesture(0, 0, 800, 480, "leave", this) {
 			public void appear(SmartHomeActivity activity) {
				createSample(R.drawable.light, activity);
 				activity.imagePane.addView(images.get(0));
 			}
 			public void disappear(SmartHomeActivity activity) {
 				activity.imagePane.removeView(images.get(0));
 			}
 		});
 	}
 	public void actionPerformed(String action, SmartHomeActivity activity) {
 		if (action.equals("enter")) {
 			activity.room.disappear(activity);
 			appear(activity);
 			activity.isSlider = true;
 		}
 		if (action.equals("leave")) {
 			disappear(activity);
 			activity.isSlider = false;
 			activity.room.appear(activity);
 			/*
 			activity.image.setVisibility(View.INVISIBLE);
 			activity.label.setText("Info:");
 			activity.isDebug = false;
 			if (activity.room == null) activity.room = activity.rooms.get(0);
 			return;
 			*/
 		}
 		if (lightGesture == null) return;
 		if (action.equals("on")) {
 			lightGesture.setLight(true);
 		}
 		if (action.equals("off")) {
 			lightGesture.setLight(false);
 		}
 	}
 }
