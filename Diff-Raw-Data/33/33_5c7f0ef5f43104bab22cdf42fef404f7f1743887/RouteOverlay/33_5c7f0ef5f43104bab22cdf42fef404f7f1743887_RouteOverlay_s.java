 package net.djmacgyver.bgt.map;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class RouteOverlay extends Overlay {
 	private GeoPoint[] points;
 	private Paint paint, trackPaint;
 	private MapView view;
 	private int from = -1, to = -1;
 	
 	public RouteOverlay(MapView view) {
 		this.view = view;
 	}
 	
 	public synchronized void setPoints(GeoPoint[] points) {
 		this.points = points;
 		// zoom in to the route
 		if (points.length == 0) return;
 		int minLat = 90000000, maxLat = -90000000, minLon = 180000000, maxLon = -180000000;
 		for (int i = 0; i < points.length; i++) {
 			minLat = Math.min(minLat, points[i].getLatitudeE6());
 			maxLat = Math.max(maxLat, points[i].getLatitudeE6());
 			minLon = Math.min(minLon, points[i].getLongitudeE6());
 			maxLon = Math.max(maxLon, points[i].getLongitudeE6());
 		}
 		view.getController().zoomToSpan(maxLat - minLat, maxLon - minLon);
 		view.getController().setCenter(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
 	}
 	
 	private Paint getRegularPaint() {
 		if (paint == null) {
 			paint = new Paint();
 			paint.setAntiAlias(true);
 			paint.setColor(Color.BLUE);
 			paint.setAlpha(64);
 			paint.setStrokeWidth(2);
 		}
 		return paint;
 	}
 	
 	private Paint getTrackPaint() {
 		if (trackPaint == null) {
 			trackPaint = new Paint();
 			trackPaint.setAntiAlias(true);
			trackPaint.setAlpha(64);
			trackPaint.setStrokeWidth(6);
 			trackPaint.setColor(Color.rgb(255, 192, 0));
 		}
 		return trackPaint;
 	}
 	
 	@Override
 	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, shadow);
 		if (points == null) return;
 
 		Point previousPoint = null;
 		
		for (int i = 0; i < points.length; i++) {
 			Point point = new Point();
 			mapView.getProjection().toPixels(points[i], point);
 			if (previousPoint != null) {
				// if the current part of the map is occupied, draw it twice:
				// once in highlighted, once in regular
				if (i >= from && i <= to)
					canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, getTrackPaint());
 				canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, getRegularPaint());
 			}
 			previousPoint = point;
 		}
 	}
 	
 	public void setBetween(int from, int to) {
 		this.from = from;
 		this.to = to;
 	}
 }
