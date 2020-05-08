 package com.smarthome;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 public abstract class Gesture {
 	public static HashSet<Gesture> gestures = new HashSet<Gesture>();
 	public float x1, y1, x2, y2;
 	public Gesture parent;
 	public LinkedList<ImageView> images = new LinkedList<ImageView>();
 	public static HashSet<Gesture> byParent(Gesture parent) {
 		HashSet<Gesture> result = new HashSet<Gesture>();
 		for (Gesture gesture : gestures)
 			if (gesture.parent == parent) result.add(gesture);
 		return result;
 	}
 	public Gesture() {gestures.add(this);}
 	public Gesture(float x1, float y1, float x2, float y2) {
 		gestures.add(this);
 		this.x1 = x1 * SmartHomeActivity.screenWidth / 800;
 		this.y1 = y1 * SmartHomeActivity.screenWidth / 800;
 		this.x2 = x2 * SmartHomeActivity.screenHeight / 480;
 		this.y2 = y2 * SmartHomeActivity.screenHeight / 480;
 	}
 	public boolean match(float x, float y) {
 		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
 	}
 	public boolean fire(float x, float y, SmartHomeActivity activity, boolean isLong) {
 		if (match(x, y)) {
 			click(activity, isLong);
 			System.out.println("Event " + toString() + " fired at [" + x + ", " + y + "]");
 		}
 		else return false;
 		return true;
 	}
 	public void click(SmartHomeActivity activity, boolean isLong) {
 		imitate(activity, this, isLong);
 	}
 	public void imitate(SmartHomeActivity activity, Gesture parent, boolean isLong) {
 		for (Gesture gesture : byParent(this)) {
 			gesture.imitate(activity, parent, isLong);
 		}
 	}
 	public String toString() {
 		return "Gesture: [" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + "]";
 	}
 	public void createSample(int resource, SmartHomeActivity activity) {
 		if (images.size() == 0) {
 	        ImageView image = new ImageView(activity);
	        image.setImageResource(R.drawable.lamp);
 	        activity.prepareImage((int)x1, (int)y1, (int)x2, (int)y2, image, 128, 128);
 	        image.setScaleType(ScaleType.FIT_XY);
 	        images.add(image); 
 		}
 	}
 	public void appear(SmartHomeActivity activity) {
 		
 	}
 	public void disappear(SmartHomeActivity activity) {
 		
 	}
 }
