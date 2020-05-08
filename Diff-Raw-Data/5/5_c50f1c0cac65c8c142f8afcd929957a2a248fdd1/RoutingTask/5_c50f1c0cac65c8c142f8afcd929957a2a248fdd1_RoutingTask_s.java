 package eu.trentorise.smartcampus.osm.android.util;
 
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.util.Log;
 import eu.trentorise.smartcampus.osm.android.ResourceProxy;
 import eu.trentorise.smartcampus.osm.android.ResourceProxy.bitmap;
 import eu.trentorise.smartcampus.osm.android.bonuspack.overlays.ExtendedOverlayItem;
 import eu.trentorise.smartcampus.osm.android.bonuspack.overlays.ItemizedOverlayWithBubble;
 import eu.trentorise.smartcampus.osm.android.bonuspack.routing.MapQuestRoadManager;
 import eu.trentorise.smartcampus.osm.android.bonuspack.routing.Road;
 import eu.trentorise.smartcampus.osm.android.bonuspack.routing.RoadManager;
 import eu.trentorise.smartcampus.osm.android.bonuspack.routing.RoadNode;
 import eu.trentorise.smartcampus.osm.android.views.MapView;
 import eu.trentorise.smartcampus.osm.android.views.overlay.OverlayItem;
 import eu.trentorise.smartcampus.osm.android.views.overlay.PathOverlay;
 /**
  * Class to get a route between a start and a destination point, going through a list of waypoints. It uses MapQuest open, public and free API, based on OpenStreetMap data. 
  * See http://open.mapquestapi.com/guidance <BR>
  * This class contains an AsyncTask that permits to get the route
  * You have to allocate a new AsyncTask like this: <BR>
  * RoutingTask myTask = new RoutingTask(Context, MapView, draw);<BR>
  * myTask.execute(ArrayList<GeoPoint>);<BR>
  * @return a PathOverlay
  * @author Dylan Stenico
  */
 public class RoutingTask extends AsyncTask<ArrayList<GeoPoint>,Integer,PathOverlay> {
 
 
 	private ResourceProxy mProxy;
 	private Context mContext;
 	private ProgressDialog dialog;
 	private Road road;
 	private MapView mapView;
 	private static boolean stop = false;
 	private boolean draw;
 	ArrayList<GeoPoint> myList;
 	/**
 	 * @param mapViewO
 	 * a MapView object
 	 * @param draw
 	 * set as true only to draw the markers on the PathOverlay
 	 */
 	public RoutingTask(Context context, MapView mapView, boolean Drawmarker) {
 		super();
 		mContext = context;
 		dialog = new ProgressDialog(mContext);
 		this.mapView = mapView;
 		this.draw = Drawmarker;
 		mProxy = mapView.getResourceProxy();
 	}
 
 	@Override
 	protected void onPreExecute() {
 		// TODO visualizzare il progress dialog
 		dialog.setMessage("Loading...");
 		dialog.show();
 	}
 
 	@Override
 	protected PathOverlay doInBackground(ArrayList<GeoPoint>... params) {
 
 		myList = new ArrayList<GeoPoint>(params[0]);
 
 		RoadManager roadManager = new MapQuestRoadManager();
 		road = roadManager.getRoad(params[0]);
 		roadManager.addRequestOption("routeType=pedestrian");
 		return RoadManager.buildRoadOverlay(road, mapView.getContext());
 	}
 
 	@Override
 	protected void onPostExecute(PathOverlay result) {
 
 		if(road.mNodes.size() > 0){
 			try{
 				mapView.getOverlays().add(result);
 				mapView.invalidate();
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 			if(draw){
 				final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
 				ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(mContext, roadItems, mapView);
 				mapView.getOverlays().add(roadNodes);
 				//			Drawable marker = mContext.getResources().getDrawable(R.drawable.marker_node);
 				//			int markerIcon = eu.trentorise.smartcampus.osm.android.R.drawable.marker_node;
 				Drawable icon = mProxy.getDrawable(bitmap.marker_node);
 				for (int i=0; i<road.mNodes.size(); i++){
 					RoadNode node = road.mNodes.get(i);
 					Log.d("time", Double.toString(node.mDuration));
					Log.d("time", Integer.toString(i));
 					ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(node.mInstructions, "Time: " +fromSecondToString((int)node.mDuration)+ "\nLenght: " + fromKilometersToMeters(node.mLength), node.mLocation);
					nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
 					nodeMarker.setMarker(icon);
 					roadNodes.addItem(nodeMarker);
 				}
 			}
 		}
 		else{
 			if(!stop){
 				RoutingTask route = new RoutingTask(mContext, mapView, draw);
 				route.execute(myList);
 				stop = true;
 			}
 			else
 				stop = false;
 		}
 		mapView.invalidate();
 		if(dialog.isShowing())
 			dialog.dismiss();
 	}
 
 	private String fromKilometersToMeters(double kilometers){
 		String toReturn = "";
 		int km = (int) Math.floor(kilometers);
 		int m = (int) ((kilometers - km) * 1000);
 		if(km > 0) toReturn += km + "km ";
 		return toReturn + m + "m";
 	}
 	private String fromSecondToString(int second){
 		String toReturn = "";
 		int sec = second % 60;
 		int min = ((second - sec) / 60) % 60;
 		int hour =  (int) Math.floor(second / 3600);
 		if(hour > 0) toReturn += hour +"h ";
 		if(min > 0)  toReturn += min +"m ";
 		return toReturn + sec + "s";
 	}
 }
