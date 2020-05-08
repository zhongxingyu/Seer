 package com.example.day3;
 
 import java.util.ArrayList;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class MapActivity extends com.google.android.maps.MapActivity {
 	
 	MapView view;
 	MapController controller;
	GeoPoint point = new GeoPoint((int) (-6.541671*1E6), (int) (106.640832*1E6));
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
         
        if(this.getIntent().getExtras() != null && 
        		this.getIntent().getExtras().containsKey("POINT")) {
         	int[] latlong = this.getIntent().getExtras().getIntArray("POINT");
         	point = new GeoPoint(latlong[0], latlong[1]);
         }
         
         view = (MapView) findViewById(R.id.map);
         controller = view.getController();
         view.setClickable(true);
         view.setEnabled(true);
         view.setBuiltInZoomControls(true);
         controller.setCenter(point);
         controller.setZoom(14);
         
         final MyLocationOverlay overlay = new MyLocationOverlay(this, view);
         overlay.enableCompass();
         overlay.enableMyLocation();
         overlay.runOnFirstFix(new Runnable() {
 			
 			public void run() {
 				controller.animateTo(overlay.getMyLocation());
 			}
 		});
         view.getOverlays().add(overlay);
         
         Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
         OverlayItems overlay2 = new OverlayItems(drawable);
         
         OverlayItem item = new OverlayItem(point, 
         		"Point 0", "Deskripsi Point A");
         overlay2.addItem(item);
         view.getOverlays().add(overlay2);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_map, menu);
         return true;
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
 
     private class OverlayItems extends ItemizedOverlay<OverlayItem> {
 
 		public OverlayItems(Drawable defaultMarker) {
 			super(boundCenterBottom(defaultMarker));
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			return items.get(i);
 		}
 		
 		@Override
 		public int size() {
 			return items.size();
 		}
 		
 		public void addItem(OverlayItem overlayItem) {
 			items.add(overlayItem);
 			populate();
 		}
     	
     }
 }
