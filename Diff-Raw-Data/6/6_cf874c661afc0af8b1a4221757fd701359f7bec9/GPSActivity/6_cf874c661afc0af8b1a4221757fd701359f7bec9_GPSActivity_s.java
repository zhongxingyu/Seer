 package com.madp.maps;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import com.madp.meetme.R;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class GPSActivity extends MapActivity {
 
 	protected List<OverlayItem> map_items;
 	protected MapView Mymap;
 	protected List <Overlay> mapOverlays;
 	protected LocationManager locman; 
 	protected LocationListener loclis;
 	protected MapController mapcon;
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 		
 	}
 	public void Init(){
 		Mymap= (MapView) findViewById(R.id.mapView);
         Mymap.setBuiltInZoomControls(true);
         mapOverlays = Mymap.getOverlays();
         map_items = new Vector();
         locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
         loclis= (LocationListener) new GPSListener(this);
         mapcon = (MapController) Mymap.getController();  
 	}
 	
 	public void RemoveMapOverlays(){
 		 mapOverlays.clear() ;
 	}
 
 	public MapView getMap(){
 		return Mymap;
 	}
 	
 	public void DrawAtMap(double lat, double lon, String id, String snippet,int drawable_item) {
 		
 		/*draws a drawable item on the current map on lat lon positions*/
 		Log.i("DrawAtMap, GPSActivity", "will draw "+id +" at lon= "+lon+" lat= "+lat);
         Drawable drawable = this.getResources().getDrawable(drawable_item);
         MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable);       
         
         Mymap.postInvalidate();
         /* the previous version of the map is in-valid;
          * redraw the new version*/
         for(int i=0;i<mapOverlays.size();i++){
         	MyItemizedOverlay s=(MyItemizedOverlay) mapOverlays.get(i);
         	s.DeleteOverlayItem(id);
         }
        OverlayItem overlayitem = new OverlayItem(new GeoPoint((int)lat*1000000,(int)lon*1000000),id,snippet);
        itemizedoverlay.addOverlay(overlayitem);            
 		mapOverlays.add(itemizedoverlay);
         
     }
 	
 }
