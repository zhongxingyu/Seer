 package org.isageek.dasbrennen.CalibrateScreen;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import android.content.Context;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 public class Calibrator implements OnTouchListener {
 	/* These are the "screen" (240x320) coordinates of the targets */
 	private final int TL_X = 17;
 	private final int TL_Y = 41;
 	private final int BR_X = 216;
 	private final int BR_Y = 304;
 	
 	private Context ctx;
 	
 	private class XY {
 		public int x;
 		public int y;
 	}
 	XY topleft;
 	XY bottomright;
 	
 	public Calibrator (Context ctx) {
 		this.ctx = ctx;
 		
 		topleft = null;
 		bottomright = null;
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		float x = event.getRawX();
 		float y = event.getRawY();
 		
 		int duration = Toast.LENGTH_LONG;
 		CharSequence msg = "touch " + String.valueOf(x) + " " + String.valueOf(y);
 		Toast t = Toast.makeText(ctx, msg, duration);
 		t.show();
 		
 		XY coord = new XY();
 		coord.x = (int) x;
 		coord.y = (int) y;
 		
 		if (x < 160 && y < 240) {
 			this.topleft = coord;
 		} else if (x > 160 && y > 240) {
 			this.bottomright = coord;
 		}
 		recalibrate();
 		
 		return false;
 	}
 	
 	private class ComputeMinMax {
 		public int new_min;
 		public int new_max;
 		
 		public ComputeMinMax(float curmin, float curmax, float e1, float e2, float t1, float t2, float max) {
 			/* Convert coordinate to touch values */
 			e1 = e1 * (curmax - curmin) / max;
 			e1 = e1 + curmin;
 			e2 = e2 * (curmax - curmin) / max;
 			e2 = e2 + curmin;
 			
 			/* Get our linear equation */
 			float m = (e2 - e1) / (t2 - t1);
 			float b = e1 - m*t1;
 			
 			if (b < 0)
 				b = 0;
 			
 			new_min = (int)b;
 			new_max = (int)(m * max + b);
 		}
 	}
 	
 	private int getSysfs(String filename) {
 		int val = 0;
 		try {
 			FileReader f = new FileReader(filename);
 			Scanner s = new Scanner(f);
 			val = s.nextInt();
 		} catch (IOException e) {
 			int duration = Toast.LENGTH_LONG;
 			CharSequence msg = "read fail!";
 			Toast t = Toast.makeText(ctx, msg, duration);
 			t.show();
 		}
 		int duration = Toast.LENGTH_LONG;
 		CharSequence msg = "read " + String.valueOf(val);
 		Toast t = Toast.makeText(ctx, msg, duration);
 		t.show();
 		return val;
 	}
 	
 	private void setSysfs(String filename, int val) {
 		try {
			FileWriter f = new FileWriter(filename);
			f.write(String.valueOf(val));
			f.write("\n");
 		} catch (IOException e) {
 			int duration = Toast.LENGTH_LONG;
 			CharSequence msg = "write fail!";
 			Toast t = Toast.makeText(ctx, msg, duration);
 			t.show();
 		}
 		int duration = Toast.LENGTH_LONG;
 		CharSequence msg = "write " + String.valueOf(val);
 		Toast t = Toast.makeText(ctx, msg, duration);
 		t.show();
 	}
 	
 	private void recalibrate() {
 		if (topleft == null)
 			return;
 		if (bottomright == null)
 			return;
 		
 		/* Compute XMIN and XMAX */
 		int xmin = getSysfs("/sys/class/vogue_ts/xmin");
 		int xmax = getSysfs("/sys/class/vogue_ts/xmax");
 		
 		topleft.x = topleft.x * 240 / 320;
 		bottomright.x = bottomright.x * 240 / 320;
 		ComputeMinMax cmm = new ComputeMinMax(xmin, xmax, topleft.x, bottomright.x, TL_X, BR_X, 240);
 		
 		setSysfs("/sys/class/vogue_ts/xmin", cmm.new_min);
 		setSysfs("/sys/class/vogue_ts/xmax", cmm.new_max);
 		
 		/* Compute YMIN and YMAX */
 		int ymin = getSysfs("/sys/class/vogue_ts/ymin");
 		int ymax = getSysfs("/sys/class/vogue_ts/ymax");
 		
 		topleft.y = topleft.y * 320 / 480;
 		bottomright.y = bottomright.y * 320 / 480;
 		cmm = new ComputeMinMax(ymin, ymax, topleft.y, bottomright.y, TL_Y, BR_Y, 320);
 		
 		setSysfs("/sys/class/vogue_ts/ymin", cmm.new_min);
 		setSysfs("/sys/class/vogue_ts/ymax", cmm.new_max);
 		
 		int duration = Toast.LENGTH_LONG;
 		CharSequence msg = "Recalibrated!";
 		Toast t = Toast.makeText(ctx, msg, duration);
 		t.show();
 	}
 
 }
