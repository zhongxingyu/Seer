 package info.homepluspower.nearbymetars;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class MetarList extends ItemizedOverlay<MetarItem> {
 	
 	private ArrayList<MetarItem> mOverlays = new ArrayList<MetarItem>();
 	private Context mContext;
 	
 	public MetarList(Drawable defaultMarker, Context context) {
 		super(boundCenter(defaultMarker));
 		mContext = context;
 		populate();
 	}
 	
 	public void addOverlay(MetarItem overlay) {
 		Log.v("NearbyMetars", "Adding overlay item");
 		mOverlays.add(overlay);
		setLastFocusedIndex(-1);
 		populate();
 	}
 	
 	@Override
 	protected MetarItem createItem(int i) {
 		return mOverlays.get(i);
 	}
 
 	@Override
 	public int size() {
 		Log.v("NearbyMetars", "size called, returning " + Integer.toString(mOverlays.size()));
 		return mOverlays.size();
 	}
 
 	@Override
 	protected boolean onTap(int index) {
 		Log.v("NearbyMetars", "Item tapped with index " + Integer.toString(index));
 		OverlayItem item = mOverlays.get(index);
 		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
 		dialog.setTitle(item.getTitle());
 		dialog.setMessage(item.getSnippet());
 		dialog.show();
 		return true;
 	}
 	
 	@Override
 	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
 		if(!shadow) {
 			Log.v("NearbyMetars", "Drawing items");
 			MetarItem item;
 			for(int i=0; i<mOverlays.size(); i++) {
 				item = mOverlays.get(i);
 				item.draw(canvas, mapView);
 			}
 		}
 	}
 	
 	public void reset() {
 		Log.v("NearbyMetars", "Clearing overlay items");
 		mOverlays.clear();
		setLastFocusedIndex(-1);
 		populate();
 	}
 	
 	public void saveListToBundle(Bundle outState) {
 		outState.putParcelableArrayList("metarlist", mOverlays);
 	}
 	
 	public void getListFromBundle(Bundle savedInstanceState) {
 		mOverlays = savedInstanceState.getParcelableArrayList("metarlist");
		setLastFocusedIndex(-1);
 		populate();
 	}
 }
