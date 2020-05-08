 package ua.in.leopard.androidCoocooAfisha;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.text.Html;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 public class TheatersItemizedOverlay extends ItemizedOverlay<TheaterOverlayItem> {
 	
 	private ArrayList<TheaterOverlayItem> mOverlays = new ArrayList<TheaterOverlayItem>();
 	private Drawable marker = null;
 	private Drawable selected_marker = null;
 	private TheaterOverlayItem selected_item = null;
 	private final MapActivity mapActivity;
 	
 	private Paint	innerPaint, borderPaint, textPaint;
 	private static float mGestureThreshold;
 	
 	public TheatersItemizedOverlay(MapActivity mapActivity, Drawable defaultMarker, Drawable selected_marker) {
 		super(boundCenterBottom(defaultMarker));
 		this.mapActivity = mapActivity;
 		this.marker = defaultMarker;
 		this.selected_marker = selected_marker;
 		
 		//get measurement
 		mGestureThreshold = mapActivity.getResources().getDisplayMetrics().density;
 	}
 	
 	public TheaterDB getSelectedTheater(){
 		if (selected_item != null && selected_item.getTheaterObj() != null){
 			return selected_item.getTheaterObj();
 		} else {
 			return null;
 		}
 	}
 	
 	public void addOverlay(TheaterOverlayItem overlay) {
 	    mOverlays.add(overlay);
 	    populate();
 	}
 	
 	@Override
 	protected TheaterOverlayItem createItem(int i) {
 		return mOverlays.get(i);
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		super.draw(canvas, mapView, shadow);
 		drawInfoWindow(canvas, mapView, shadow);
 		boundCenterBottom(marker);
 	}
 	@Override
 	protected boolean onTap(int index) {
 		TheaterOverlayItem item = mOverlays.get(index);
 		if (selected_item != null){
 			selected_item.setMarker(boundCenterBottom(marker));
 		}
 		item.setMarker(boundCenterBottom(selected_marker));
 		selected_item = item;
 		initInfoBlock(item);
 		return true;
 	}
 	
 	private void drawInfoWindow(Canvas canvas, MapView	mapView, boolean shadow) {
 		if (null != selected_item && null != selected_item.getTheaterObj()){
 			if (!shadow){
 				Drawable d = this.marker;
 				Bitmap marker_bitmap = ((BitmapDrawable)d).getBitmap();
 				//  First determine the screen coordinates of the selected MapLocation
 				Point selDestinationOffset = new Point();
 				mapView.getProjection().toPixels(selected_item.getPoint(), selDestinationOffset);
 				//  Setup the info window with the right size & location
 				Paint p = new Paint();
				float text_width = p.measureText(selected_item.getTheaterObj().getTitle()) + 20;
 				int INFO_WINDOW_WIDTH = (int)(text_width * mGestureThreshold + 0.5f);
 				int INFO_WINDOW_HEIGHT = (int)(25 * mGestureThreshold + 0.5f);
 				RectF infoWindowRect = new RectF(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);				
 				int infoWindowOffsetX = selDestinationOffset.x - INFO_WINDOW_WIDTH / 2;
 				int infoWindowOffsetY = selDestinationOffset.y - INFO_WINDOW_HEIGHT - marker_bitmap.getHeight();
 				infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);
 				//  Draw inner info window
 				canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());
 				//  Draw border for info window
 				canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
 				//  Draw the MapLocation's name
 				int TEXT_OFFSET_X = (int)(10 * mGestureThreshold + 0.5f);
 				int TEXT_OFFSET_Y = (int)(16 * mGestureThreshold + 0.5f);
				canvas.drawText(selected_item.getTheaterObj().getTitle(),
 						infoWindowOffsetX + TEXT_OFFSET_X,
 						infoWindowOffsetY + TEXT_OFFSET_Y, 
 						getTextPaint());
 			}
 		}
 	}
 	
 	private void initInfoBlock(TheaterOverlayItem item){
 		if (item.getTheaterObj() != null){
 			TheaterDB theater_obj = item.getTheaterObj();
 			this.mapActivity.setTitle(Html.fromHtml(theater_obj.getTitle()));
 		}
 	}
 	
 	public Paint getInnerPaint() {
 		if ( innerPaint == null) {
 			innerPaint = new Paint();
 			innerPaint.setARGB(225, 75, 75, 75); //gray
 			innerPaint.setAntiAlias(true);
 		}
 		return innerPaint;
 	}
 	
 	public Paint getBorderPaint() {
 		if ( borderPaint == null) {
 			borderPaint = new Paint();
 			borderPaint.setARGB(255, 255, 255, 255);
 			borderPaint.setAntiAlias(true);
 			borderPaint.setStyle(Style.STROKE);
 			borderPaint.setStrokeWidth(2);
 		}
 		return borderPaint;
 	}
 
 	public Paint getTextPaint() {
 		if ( textPaint == null) {
 			textPaint = new Paint();
 			textPaint.setARGB(255, 255, 255, 255);
 			textPaint.setAntiAlias(true);
 			textPaint.setTextSize(12 * mGestureThreshold);
 		}
 		return textPaint;
 	}
 
 	@Override
 	public int size() {
 		return mOverlays.size();
 	}
 
 }
