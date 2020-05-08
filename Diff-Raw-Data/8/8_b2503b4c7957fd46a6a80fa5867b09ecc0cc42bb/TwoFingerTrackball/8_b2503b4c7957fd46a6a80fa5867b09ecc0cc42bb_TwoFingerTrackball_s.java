 package org.tavatar.tavimator;
 
 import android.content.Context;
 import android.opengl.Matrix;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.WindowManager;
 import android.widget.Scroller;
 
 public class TwoFingerTrackball {
 	
 	private static String TAG = "TwoFingerTrackball";
 
 	private Scroller mScroller;
 
 	/**
 	 * The base orientation matrix. Should contain only orientation; no scale or
 	 * translation
 	 */
 	private float[] orientation = new float[16];
 	
 	private float distance = 50;
 	
 	private float minDistance = 10f;
 	private float maxDistance = 100f;
 
 	/**
 	 * A transform from camera coordinates to my local coordinates. Used for
 	 * touch processing
 	 */
 	private float[] cameraToTrackball = new float[16];
 
 	/**
 	 * The rotation that happened since the last frame
 	 */
 	private float[] localFrameRotation = new float[16];
 	private float[] globalFrameRotation = new float[16];
 	private float[] newOrientation = new float[16];
 
 	private float[] scrollVelocity = new float[4];
 	private float[] scrollAxis = new float[4];
 	private float[] flingAxis = new float[4];
 	private static final float ZOOM_FACTOR = 100.0f;
 
 	private int prevFlingX;
 	private int prevFlingY;
 
 	private float mDensity = 1.0f;
 
 	private Context mContext;
 	
 	public static String arrayToString(float[] array) {
 		String ans = "[";
 		for (int i = 0; i < array.length; i++) {			
 			ans += array[i];
 			ans += i==array.length-1 ? "]" : ", ";
 		}
 		return ans;
 	}
 	
 	/**
 	 * Converts touch data into an angular velocity vector, in degrees per unit
 	 * time. The angular velocity vector will be in camera-local coordinates.
 	 * Data is provided in screen coordinates (pixels starting at zero in the
 	 * top left corner). dx, Unit time is arbitrary; it can be one second, one
 	 * frame, or whatever
 	 * 
 	 * @param angularVelocity The computed angular velocity vector is stored here (length 3 + 1 extra for OpenGL)
 	 * @param dx x component of the velocity vector, in pixels per unit time
 	 * @param dy y component of the velocity vector, in pixels per unit time
 	 */
 	public void oneFingerDragToAngularVelocity(float[] angularVelocity, float dx, float dy) {
 		angularVelocity[0] = dy / mDensity / 2f;
 		angularVelocity[1] = dx / mDensity / 2f;
 		angularVelocity[2] = 0.0f;
 		angularVelocity[3] = 1.0f;
 	}
 
 	/**
 	 * Converts touch data into an angular velocity vector, in degrees per unit
 	 * time. The angular velocity vector will be in camera-local coordinates.
 	 * Data is provided in screen coordinates (pixels starting at zero in the
 	 * top left corner). dx, Unit time is arbitrary; it can be one second, one
 	 * frame, or whatever
 	 * 
 	 * @param angularVelocity The computed angular velocity vector is stored here (length 3 + 1 extra for zoom factor)
 	 * @param x1 x position of the first finger
 	 * @param y1 y position of the first finger
 	 * @param dx1 x component of the first finger velocity vector, in pixels per unit time
 	 * @param dy1 y component of the first finger velocity vector, in pixels per unit time
 	 * @param x2 x position of the second finger
 	 * @param y2 y position of the second finger
 	 * @param dx2 x component of the second finger velocity vector, in pixels per unit time
 	 * @param dy2 y component of the second finger velocity vector, in pixels per unit time
 	 */
 	public void twoFingerDragToAngularVelocity(float[] angularVelocity, int x1, int y1, float dx1, float dy1, int x2, int y2, float dx2, float dy2) {
 		// mathematically, r and v should both be divided by
 		// 2, but it ends up canceling out (radius is half
 		// the distance, velocity is the average of the two
 		// measurements, one of which is inverted)
 		int rx = x2-x1; // radius vector, x component
 		int ry = y2-y1; // radius vector, y component
 		int r2 = rx*rx + ry*ry; // radius squared
 		float vx = dx2-dx1; // velocity vector, x component
 		float vy = dy2-dy1; // velocity vector, y component
 		float projection = (rx*vx + ry*vy) / r2; // projection of v onto r, as a fraction of r
 		float vxPerp = vx - projection * rx; // tangential velocity vector, x component
 		float vyPerp = vy - projection * ry; // tangential velocity vector, y component
 
 		angularVelocity[0] = (dy1+dy2) / mDensity / 4f;
 		angularVelocity[1] = (dx1+dx2) / mDensity / 4f;
 		angularVelocity[2] = (float) ((vxPerp*ry - vyPerp*rx) / r2 * 180/Math.PI);
 		angularVelocity[3] = 1.0f + projection;
 	}
 
 	public void angularVelocityToRotationMatrix(float[] matrix, float[] angularVelocity) {
 		Matrix.setIdentityM(matrix, 0);
 		Matrix.rotateM(matrix, 0, angularVelocity[0], 1, 0, 0);
 		Matrix.rotateM(matrix, 0, angularVelocity[1], 0, 1, 0);
 		Matrix.rotateM(matrix, 0, angularVelocity[2], 0, 0, 1);
 	}
 	
 	public TwoFingerTrackball(Context context) {
 		mContext = context;
 
 		Matrix.setIdentityM(orientation, 0);
 		Matrix.setIdentityM(cameraToTrackball, 0);
 
 		mScroller = new Scroller(mContext);
 
 		final DisplayMetrics displayMetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 		this.mDensity = displayMetrics.density;
 	}
 
 	public WindowManager getWindowManager() {
 		return ((WindowManager) mContext
 				.getSystemService(Context.WINDOW_SERVICE));
 	}
 
 	public void setLookAt(float eyeX, float eyeY, float eyeZ, float lookX,
 			float lookY, float lookZ, float upX, float upY, float upZ) {
 		Matrix.setLookAtM(orientation, 0, eyeX, eyeY, eyeZ, lookX, lookY,
 				lookZ, upX, upY, upZ);
 		Matrix.translateM(orientation, 0, eyeX, eyeY, eyeZ);
 		distance = Matrix.length(
 				lookX - eyeX,
 				lookY - eyeY,
 				lookZ - eyeZ);
 	}
 
 	/**
 	 * make a copy if you intend to share the result
 	 * 
 	 * @return
 	 */
 	public float[] getOrientation() {
 		return orientation;
 	}
 	
 	public float getDistance() {
 		return distance;
 	}
 
 	public float[] getCameraToTrackballOrientation() {
 		return cameraToTrackball;
 	}
 
 	public void updateOrientation() {
 		mScroller.computeScrollOffset();
 		int x = mScroller.getCurrX();
 		int y = mScroller.getCurrY();
 
         if (prevFlingX == x && prevFlingY == y)
 			return;
 
 		rotateAboutCameraAxis(x - prevFlingX, flingAxis);
 		distance /= (float)Math.exp((y - prevFlingY) / ZOOM_FACTOR);
 		prevFlingX = x;
 		prevFlingY = y;
 	}
 
 	public void setOrientation(float[] anOrientationMatrix) {
 		System.arraycopy(anOrientationMatrix, 0, orientation, 0, 16);
 	}
 
 	public AnimationOneFingerDragHandler getOneFingerDragHandler() {
 		class OneFingerDragHandler implements AnimationOneFingerDragHandler {
 			@Override
 			public String shortToolName() {
 				return mContext.getResources().getString(R.string.short_tool_name_orbit_camera);
 			}
 	
 			@Override
 			public String toolName() {
 				return mContext.getResources().getString(R.string.one_finger_tool_name_orbit_camera);
 			}
 	
 			@Override
 			public void onOneFingerMove(int x, int y, int dx, int dy) {
 				oneFingerDragToAngularVelocity(scrollVelocity, dx, dy);
 				scrollBy(scrollVelocity);
 			}
 		
 			@Override
 			public void onOneFingerFling(int x, int y, float vx, float vy) {
 				oneFingerDragToAngularVelocity(scrollVelocity, vx, vy);
 				fling(scrollVelocity);
 			}
 		
 			@Override
 			public void onCancel() {
 				mScroller.forceFinished(true);
 			}
 		}
 		return new OneFingerDragHandler();
 	}
 
 	public AnimationTwoFingerDragHandler getTwoFingerDragHandler() {
 		class TwoFingerDragHandler implements AnimationTwoFingerDragHandler {
 			@Override
 			public String shortToolName() {
 				return mContext.getResources().getString(R.string.short_tool_name_orbit_camera);
 			}
 	
 			@Override
 			public String toolName() {
 				return mContext.getResources().getString(R.string.two_finger_tool_name_orbit_camera);
 			}
 	
 			@Override
 			public void onTwoFingerMove(int x1, int y1, int dx1, int dy1, int x2, int y2, int dx2, int dy2) {
 				twoFingerDragToAngularVelocity(scrollVelocity, x1, y1, dx1, dy1, x2, y2, dx2, dy2);
 				scrollBy(scrollVelocity);
 			}
 
 			@Override
 			public void onTwoFingerFling(int x1, int y1, float vx1, float vy1, int x2, int y2, float vx2, float vy2) {
 				twoFingerDragToAngularVelocity(scrollVelocity, x1, y1, vx1, vy1, x2, y2, vx2, vy2);
 				fling(scrollVelocity);
 			}
 
 			@Override
 			public void onCancel() {
 				mScroller.forceFinished(true);
 			}
 		}
 		return new TwoFingerDragHandler();
 	}
 
 	private void scrollBy(float[] angularVelocity) {
 		Matrix.multiplyMV(scrollAxis, 0, cameraToTrackball, 0, angularVelocity, 0);
 		rotateAboutCameraAxis(
 				Matrix.length(angularVelocity[0], angularVelocity[1], angularVelocity[2]),
 				scrollAxis);
 		distance /= angularVelocity[3];
 	}
 
 	private void rotateAboutCameraAxis(float angle, float[] axis) {
 		if (angle == 0.0f) return; // axis is likely to be zero in this case as well. avoids divide by zero errors in rotateM
 		Matrix.setIdentityM(globalFrameRotation, 0);
 		Matrix.rotateM(globalFrameRotation, 0, angle, axis[0], axis[1], axis[2]);
 
 		// Multiply the current rotation by the accumulated rotation, and then
 		// set the accumulated rotation to the result.
 		Matrix.multiplyMM(newOrientation, 0, globalFrameRotation, 0, orientation, 0);
 		setOrientation(newOrientation);
 	}
 
 	private void fling(float[] angularVelocity) {
 		Matrix.multiplyMV(flingAxis, 0, cameraToTrackball, 0, angularVelocity, 0);
 
 		prevFlingX = 0;
 		prevFlingY = 0;
 
 		mScroller.fling(0, 0,
 				(int) Matrix.length(angularVelocity[0], angularVelocity[1], angularVelocity[2]),
 				(int) (Math.log(angularVelocity[3]) * ZOOM_FACTOR), 
 				0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
 	}
 
 }
