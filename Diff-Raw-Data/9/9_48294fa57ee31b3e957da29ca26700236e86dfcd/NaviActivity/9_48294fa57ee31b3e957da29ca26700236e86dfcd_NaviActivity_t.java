 package de.c3t;
 
 import java.util.List;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class NaviActivity extends MapActivity {
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.navi);
 		MapView mapView = (MapView) findViewById(R.id.mapview);
 		MapController mapController = mapView.getController();
 		mapController.setZoom(15);
 		mapView.setBuiltInZoomControls(true);
 
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
		NaviItemizedOverlay itemizedoverlay = new NaviItemizedOverlay(drawable, this);
 
 		GeoPoint point = new GeoPoint(49764708, 6652758);
 		OverlayItem overlayitem = new OverlayItem(point, "CCC Trier", "Paulinstr. 123");
 
 		itemizedoverlay.addOverlay(overlayitem);
 		mapOverlays.add(itemizedoverlay);
 	}
 
 	/*
 	 * public void onCreate(Bundle savedInstanceState) {
 	 * super.onCreate(savedInstanceState);
 	 * 
 	 * TextView textview = new TextView(this);
 	 * textview.setText("Hier ist später die Karte"); setContentView(textview);
 	 * }
 	 */
 }
