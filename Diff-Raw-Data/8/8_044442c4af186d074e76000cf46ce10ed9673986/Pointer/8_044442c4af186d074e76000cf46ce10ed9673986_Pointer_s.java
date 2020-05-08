 package com.llama3d.elements.touch;
 
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 import com.llama3d.main.display.DisplayCache;
 
 public class Pointer implements OnTouchListener {
 
 	// ===================================================================
 	// Private Static Fields
 	// ===================================================================
 
 	private static double[] xTemp = new double[5];
 	private static double[] yTemp = new double[5];
 
 	// ===================================================================
 	// Public Static Fields
 	// ===================================================================
 
 	public static int[] x = new int[5];
 	public static int[] y = new int[5];
 	public static int[] speedX = new int[5];
 	public static int[] speedY = new int[5];
 	public static boolean[] down = new boolean[5];
 	public static int id = -1;
 
 	// ===================================================================
 	// Fields
 	// ===================================================================
 
 	// ===================================================================
 	// Public Methods
 	// ===================================================================
 
 	public static void update() {
		
 	}
 
 	public void onTouchEvent(MotionEvent mainMotionEvent) {
 		// // ======== Get New Stuff / Rest Old Stuff ========
 		// // ======== Get MotionEvent Action ========
 		int action = mainMotionEvent.getAction() & MotionEvent.ACTION_MASK;
 		int pix = (mainMotionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 		int pid = pix;// mainMotionEvent.getPointerId(pix);
 		// // ======== Select MotionEvent Action ========
 		switch (action) {
 		// ======== Any Down Event ========
 		case MotionEvent.ACTION_DOWN:
 			// ======== Any Down Event ========
 		case MotionEvent.ACTION_POINTER_DOWN:
 			xTemp[pid] = mainMotionEvent.getX(pix);
 			yTemp[pid] = mainMotionEvent.getY(pix);
 
 			Pointer.id = pid;
 			Pointer.x[pid] = (int) (mainMotionEvent.getX(pix) - DisplayCache.w / 2);
			Pointer.y[pid] = (int) (mainMotionEvent.getX(pix) - DisplayCache.h / 2);
 			Pointer.down[pid] = true;
 
 			for (PointerListener handler : PointerManager.touchListeners) {
 				handler.pointerDown();
 			}
 			break;
 		// ======== Any Up Event ========
 		case MotionEvent.ACTION_UP:
 			// ======== Any Up Event ========
 		case MotionEvent.ACTION_POINTER_UP:
 			Pointer.down[pid] = false;
 			break;
 		// ======== Any Move Event ========
 		case MotionEvent.ACTION_MOVE:
 			Pointer.x[pid] = (int) (mainMotionEvent.getX(pix) - DisplayCache.w / 2);
			Pointer.y[pid] = (int) (mainMotionEvent.getX(pix) - DisplayCache.h / 2);
 			Pointer.speedX[pid] = (int) (mainMotionEvent.getX(pix) - xTemp[pid]);
 			Pointer.speedY[pid] = (int) (mainMotionEvent.getY(pix) - yTemp[pid]);
 			for (PointerListener handler : PointerManager.touchListeners) {
 				handler.pointerMove();
 			}
 			xTemp[pid] = mainMotionEvent.getX(pix);
 			yTemp[pid] = mainMotionEvent.getY(pix);
 			break;
 		// ======== Any Cancle Event ========
 		case MotionEvent.ACTION_CANCEL:
 			break;
 		}
 	}
 
 	// // ===================================================================
 	// // Private Methods
 	// // ===================================================================
 	//
 	// private void getPinch() {
 	//
 	// if (TouchHandler.count == 2) {
 	// // ======== Correct Pointers? ========
 	// if (TouchHandler.down[0] && TouchHandler.down[1]) {
 	// // ======== Save First Distance ========
 	// float distance1 = this.distance(TouchHandler.x[0] -
 	// TouchHandler.speedX[0], TouchHandler.y[0] - TouchHandler.speedY[0],
 	// TouchHandler.x[1] - TouchHandler.speedX[1], TouchHandler.y[1] -
 	// TouchHandler.speedY[1]);
 	// // ======== Get Start & Current Distance ========
 	// float distance2 = this.distance(TouchHandler.x[0], TouchHandler.y[0],
 	// TouchHandler.x[1], TouchHandler.y[1]);
 	// // ======== Calculate Pinch ========
 	// if (distance1 > 0) {
 	// TouchHandler.pinch = distance2 / distance1;
 	//
 	// TouchHandler.pinchX = (TouchHandler.x[0] + TouchHandler.x[1]) / 2f;
 	// TouchHandler.pinchY = (TouchHandler.y[0] + TouchHandler.y[1]) / 2f;
 	// TouchHandler.pinchXRelative = (TouchHandler.xRelative[0] +
 	// TouchHandler.xRelative[1]) / 2f;
 	// TouchHandler.pinchYRelative = (TouchHandler.yRelative[0] +
 	// TouchHandler.yRelative[1]) / 2f;
 	//
 	// TouchHandler.pinchSpeedX = +(TouchHandler.speedX[0] +
 	// TouchHandler.speedX[1]) / 2f;
 	// TouchHandler.pinchSpeedY = +(TouchHandler.speedY[0] +
 	// TouchHandler.speedY[1]) / 2f;
 	// TouchHandler.pinchSpeedXRelative = +(TouchHandler.speedXRelative[0] +
 	// TouchHandler.speedXRelative[1]) / 2f;
 	// TouchHandler.pinchSpeedYRelative = +(TouchHandler.speedYRelative[0] +
 	// TouchHandler.speedYRelative[1]) / 2f;
 	//
 	// if (Math.abs(TouchHandler.pinch - 1.0) > 0.01) {
 	// TouchHandler.pinched = true;
 	// }
 	// return;
 	// }
 	// }
 	// }
 	// // ======== Reset Pinch ========
 	// TouchHandler.pinch = 0;
 	// TouchHandler.pinched = false;
 	// }
 	//
 	// private void getAngle() {
 	//
 	// if (TouchHandler.count == 2) {
 	// // ======== Correct Pointers? ========
 	// if (TouchHandler.down[0] && TouchHandler.down[1]) {
 	// float angle1 = this.angle(TouchHandler.x[0] - TouchHandler.speedX[0],
 	// TouchHandler.y[0] - TouchHandler.speedY[0], TouchHandler.x[1] -
 	// TouchHandler.speedX[1], TouchHandler.y[1] - TouchHandler.speedY[1]);
 	// // ======== Get Start & Current Distance ========
 	// float angle2 = this.angle(TouchHandler.x[0], TouchHandler.y[0],
 	// TouchHandler.x[1], TouchHandler.y[1]);
 	// // ======== Calculate Angle ========
 	// TouchHandler.angle = angle1 - angle2;
 	// return;
 	// }
 	// }
 	//
 	// TouchHandler.angle = 0;
 	//
 	// }
 
 	// ===================================================================
 	// Private Helper Methods
 	// ===================================================================
 
 	private float distance(float px1, float py1, float px2, float py2) {
 		float dx = px1 - px2;
 		float dy = py1 - py2;
 		return (float) Math.sqrt(dx * dx + dy * dy);
 	}
 
 	private float angle(float px1, float py1, float px2, float py2) {
 		float dx = px1 - px2;
 		float dy = py1 - py2;
 		return (float) Math.toDegrees(Math.atan2(dy, dx));
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		return false;
 	}
 }
