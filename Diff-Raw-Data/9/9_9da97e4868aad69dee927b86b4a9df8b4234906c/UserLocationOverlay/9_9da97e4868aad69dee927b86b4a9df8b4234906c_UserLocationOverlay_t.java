 package de.frizzware.pacrun;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class UserLocationOverlay extends Overlay {
 	private float mOrientation;
 	private Bitmap mBitmap;
 	private GeoPoint mGeoPoint;
 
 	public UserLocationOverlay(Bitmap bitmap) {
 		mBitmap = bitmap;
 	}
 
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, shadow);
 
		if (mGeoPoint == null || shadow)
			return;
		
 		// translate the GeoPoint to screen pixels
 		Point screenPts = mapView.getProjection().toPixels(mGeoPoint, null);
 
 		// create a rotated copy of the marker
 		Matrix matrix = new Matrix();
 		matrix.postRotate(mOrientation);
 		Bitmap rotatedBmp = Bitmap.createBitmap(mBitmap, 0, 0,
 				mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
 		// add the rotated marker to the canvas
 		canvas.drawBitmap(rotatedBmp,
 				screenPts.x - (rotatedBmp.getWidth() / 2), screenPts.y
 						- (rotatedBmp.getHeight() / 2), null);
 	}
 
 	public void setOrientation(float newOrientation) {
 		mOrientation = newOrientation;
 	}
 
 	public void setGeoPoint(GeoPoint p) {
 		mGeoPoint = p;
 	}
 
 	public GeoPoint getGeoPoint() {
 		return mGeoPoint;
 	}
 }
