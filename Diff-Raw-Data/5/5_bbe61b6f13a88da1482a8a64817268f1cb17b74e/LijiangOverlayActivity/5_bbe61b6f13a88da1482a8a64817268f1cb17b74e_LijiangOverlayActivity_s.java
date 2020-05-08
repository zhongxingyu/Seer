 package com.utopia.lijiang;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.baidu.mapapi.GeoPoint;
 import com.baidu.mapapi.MapView;
 import com.baidu.mapapi.OverlayItem;
 import com.utopia.lijiang.baidu.BaiduItemizedOverlay;
 import com.utopia.lijiang.baidu.BaiduLongPressItemizedOverlay;
 import com.utopia.lijiang.baidu.BaiduMapActivity;
 import com.utopia.lijiang.global.Status;
 
 public abstract class LijiangOverlayActivity extends BaiduMapActivity implements Observer {
 	
 	protected static String CURRENT_CITY = "";
 	protected final static int MAX_SEARCHING_SECOND = 1000*5;
 	
 	protected MapView mMapView = null;
 	protected View mPopView = null;	
 	
 	TextView popName = null;
 	TextView popAddress = null;
 	Button popSave = null;
 	View showDetailIndicator = null;
 	View detailView = null;
 	
 	BaiduItemizedOverlay userOverlay = null;
 	BaiduItemizedOverlay searchOverlay = null;
 	BaiduItemizedOverlay customOverlay = null;
 
 	GeoPoint tappedPoint = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	    
 		initialMapView();
 		//Listen Status' change
 	    Status.getCurrentStatus().addObserver(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		onResume(true);
 	}
 	
 	protected void onResume(boolean clearState){
 		super.onResume();
 		if(clearState){
 			initialOverlays();
 			hidePopView();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		
 		//Remove Status Listen
 		Status.getCurrentStatus().deleteObserver(this);
 	}
 	
 	@Override
 	public MapView getMapView() {
 		// TODO Auto-generated method stub
 		return mMapView;
 	}
 	
 	@Override
 	public void update(Observable observable, Object data) {
 		showCurrentLocation();
 	}
 	
 	@Override
 	public boolean onTapped(int i, OverlayItem item) {
 		Log.d("lijiang","onTapped");
 		popName.setText(item.getTitle());
 		popAddress.setText(item.getSnippet());
 
 		tappedPoint = item.getPoint();	
 		
 		mMapView.getController().setCenter(item.getPoint());	
 		mMapView.updateViewLayout(mPopView,
                 new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                 		item.getPoint(), 0, 0-BaiduItemizedOverlay.MARKER_HEIGHT, MapView.LayoutParams.BOTTOM_CENTER));
 		
 		setPopViewToSummary();
 		mPopView.setVisibility(View.VISIBLE);
 		
 		return true;
 	}
 
 	@Override
 	public void onTapping(GeoPoint pt, MapView v) {
 		// TODO Auto-generated method stub
 		Log.d("lijiang","onTapping");
 		mPopView.setVisibility(View.GONE);
 	}
 	
 	
 	public void setPopViewToSummary(){
 		showDetailIndicator.setVisibility(View.VISIBLE);
 		detailView.setVisibility(View.GONE);
 	}
 	
 	public void setPopViewToDetail(){
 		showDetailIndicator.setVisibility(View.GONE);
 		detailView.setVisibility(View.VISIBLE);
 	}
 	
 	protected void initialMapView(){
 		mMapView =(MapView)findViewById(R.id.mapView);
 	    mMapView.setBuiltInZoomControls(false);
 	    mMapView.setDrawOverlayWhenZooming(true);
 
 	    initialOverlays();
 	    attachPopView();
 	}
 
 	private void initialOverlays() {
 		customOverlay = 
 	    		new BaiduLongPressItemizedOverlay(LijiangOverlayActivity.this,this.getResources().getDrawable(R.drawable.marker_rounded_grey));
 		searchOverlay = 
 				new BaiduItemizedOverlay(LijiangOverlayActivity.this,getResources().getDrawable(R.drawable.marker_rounded_blue));
 		searchOverlay.isShowNumber = true;
 		
 		userOverlay = 
 				new BaiduItemizedOverlay(LijiangOverlayActivity.this,getResources().getDrawable(R.drawable.marker_rounded_red));
 		
 		mMapView.getOverlays().clear();
 		mMapView.getOverlays().add(customOverlay);
 		mMapView.getOverlays().add(searchOverlay);
 		mMapView.getOverlays().add(userOverlay);
 	}
 	
 	protected void attachPopView(){
 		mPopView=super.getLayoutInflater().inflate(R.layout.popview2, null);
 		mPopView.setVisibility(View.GONE);
 		
 		popName=(TextView)mPopView.findViewById(R.id.popName);
 		popAddress=(TextView)mPopView.findViewById(R.id.popAddress);
 		showDetailIndicator = (View)mPopView.findViewById(R.id.popShowDetail);
 		detailView = (View)mPopView.findViewById(R.id.popDetail);
 		
 		mMapView.addView(mPopView,
                 new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                 		null, MapView.LayoutParams.BOTTOM_CENTER));
 		mPopView.setVisibility(View.GONE);
 	}
 	
 	protected Boolean showCurrentLocation(){
 	    Location loc = Status.getCurrentStatus().getLocation();
 	    if(loc ==null){
 	    	return false;
 	    }
 	    
		GeoPoint pt = new GeoPoint((int)loc.getLatitude(), (int)loc.getLongitude());
 		String title = this.getString(R.string.myLocation);
 		String message = pt.getLatitudeE6()+":"+pt.getLongitudeE6();
 		OverlayItem item = new OverlayItem(pt,title,message);
 		
 		List<OverlayItem> items = new ArrayList<OverlayItem>();
 		items.add(item);
 		userOverlay.setItems(items);
 		
 		setCenter(pt);	
 		return true;
 	}
 	
 	protected void setCenter(GeoPoint pt){
 		mMapView.getController().setCenter(pt);
 	}
 	
 	protected void hidePopView()
 	{
 		mPopView.setVisibility(View.GONE);
 	}
 	
 }
