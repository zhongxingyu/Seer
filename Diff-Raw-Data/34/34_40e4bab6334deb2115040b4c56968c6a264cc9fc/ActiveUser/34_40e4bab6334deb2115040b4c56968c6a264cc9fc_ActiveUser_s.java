 package it.giacomos.android.osmer.pro.widgets.map.report;
 
 import it.giacomos.android.osmer.R;
 import android.content.Context;
 import android.content.res.Resources;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class ActiveUser extends DataInterface {
 
 	public boolean isRecent, isQuiteRecent;
 	public String datetime;
 	private MarkerOptions mMarkerOptions;
 	private Marker mMarker;
 	
 	public ActiveUser(String datet, double lat, double lon, 
 			boolean recent, boolean quite_recent)
 	{
 		super(lat, lon, datet);
 		datetime = datet;
 		isRecent = recent;
 		isQuiteRecent = quite_recent;
 	}
 	
 	@Override
 	public int getType() {
 		return TYPE_ACTIVE_USER;
 	}
 
 	@Override
 	public String getLocality() {
 		return "";
 	}
 
 	@Override
 	public boolean isWritable() {
 		return false;
 	}
 
 	@Override
 	public MarkerOptions buildMarkerOptions(Context ctx)
 	{
 		String  title, snippet;
 		Resources res = ctx.getResources();
 
 		mMarkerOptions = new MarkerOptions();
 		mMarkerOptions.position(new LatLng(getLatitude(), getLongitude()));
 		
 		title = res.getString(R.string.activeUser);
 		if(isRecent)
 		{
 			title += " " + res.getString(R.string.inTheLast10Min);
 			mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_circle_medium));
 		}
 		else if(isQuiteRecent)
 		{
 			title += " " + res.getString(R.string.inTheLast20Min);
 			mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_circle_small));
 		}
 		else
 		{
 			title += " " + res.getString(R.string.inTheLastHour);
 			mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_circle_micro));
 		}

		snippet = datetime + ": " + res.getString(R.string.activeUserSeemdAvailableInThisZone);
 		
 		mMarkerOptions.title(title);
 		mMarkerOptions.snippet(snippet);
 
 		return mMarkerOptions;
 	}
 
 	@Override
 	public MarkerOptions getMarkerOptions() 
 	{
 		return mMarkerOptions;
 	}
 
 	@Override
 	public void setMarker(Marker m) 
 	{	
 		mMarker = m;
 	}
 
 	@Override
 	public Marker getMarker() 
 	{
 		return mMarker;
 	}
 
 	@Override
 	public boolean isPublished() {
 		return true;
 	}
 
 }
