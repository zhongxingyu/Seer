 package edu.umd.umiacs.newsstand;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class MarkerOverlay extends ItemizedOverlay<OverlayItem> {
     private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
     private final NewsStand _ctx;
     private final TopStories _ts;
  
     public MarkerOverlay(Drawable defaultMarker, Context context) {
         super(boundCenterBottom(defaultMarker));
         if(context.getClass().equals(NewsStand.class))
         {
         	_ctx = (NewsStand)context;
         	_ts = null;
         }else{
         	_ctx = null;
         	_ts = (TopStories)context;
         }
     }
 
     // append new overlay object to mOverlays array
     public void addOverlay(OverlayItem overlay) {
         mOverlays.add(overlay);
         populate();
     }
 
     public void setPctShown(int pct_to_show, Context context) {
         int num_to_show = mOverlays.size() * pct_to_show / 100;
         for (int i=0; i < mOverlays.size(); i++) {
             OverlayItem overlay = mOverlays.get(i);
             Drawable marker = overlay.getMarker(0);
             if (i < num_to_show + 1) {
                 marker.mutate().setAlpha(255);
                 marker.setVisible(true,true);
             }
             else {
                 marker.mutate().setAlpha(0);
                 marker.setVisible(false, true);
             }
         }
     }
 
     // method added to allow passing a drawable "marker" to use for the item
     // this must be done within the class because boundCenterBottom is protected
     public void addOverlay(OverlayItem overlay, Drawable marker) {
         marker.setBounds(-marker.getIntrinsicWidth() / 3,
                -marker.getIntrinsicHeight() * 2 / 3,
                marker.getIntrinsicWidth() / 3,
                 0);
         overlay.setMarker(marker);
         addOverlay(overlay);
     }
 
     @Override
     protected OverlayItem createItem(int i) {
         return mOverlays.get(i);
     }
 
     @Override
     public int size() {
         return mOverlays.size();
     }
 
     @Override
     protected boolean onTap(int index) {
         MarkerOverlayItem item = (MarkerOverlayItem)mOverlays.get(index);
         Drawable marker = item.getMarker(0);
 
         if (!marker.isVisible()) {
             return true;
         }
        if(_ctx != null)
         	_ctx.getPanel().display(item.getPoint(), item.getTitle(), item.getSnippet(), item.getGazID());
        else
         	_ts.getPanel().display(item.getPoint(), item.getTitle(), item.getSnippet(), item.getGazID());
         
         
         return true;
     }
 
     public static class MarkerOverlayItem extends OverlayItem {
         String mGazID;
         public MarkerOverlayItem(GeoPoint point, String title, String snippet, String gaz_id) {
             super(point, title, snippet);
             mGazID = gaz_id;
         }
 
         public String getGazID() {
             return mGazID;
         }
         
         
     }
     
     public GeoPoint getMiddlePoint(){
     	
         int maxLat, minLat, maxLon, minLon;
         maxLat = maxLon = Integer.MIN_VALUE;
         minLat = minLon = Integer.MAX_VALUE;
         for (OverlayItem cur: mOverlays){
         	int curLat = cur.getPoint().getLatitudeE6();
         	int curLon = cur.getPoint().getLongitudeE6();
         	if (curLat < minLat)
         		minLat = curLat;
         	if(curLat > maxLat)
         		maxLat = curLat;
         	if(curLon < minLon)
         		minLon = curLon;
         	if(curLon > maxLon)
         		maxLon = curLon;
         }
     	return (new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2 ));
     }
     
 }
