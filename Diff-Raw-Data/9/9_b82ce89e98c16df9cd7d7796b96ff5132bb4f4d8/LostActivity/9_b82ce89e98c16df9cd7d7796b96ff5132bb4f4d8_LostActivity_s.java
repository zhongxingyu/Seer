 package at.dornbirn;
 
 import java.io.File;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.mapsforge.android.maps.MapActivity;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
 import org.mapsforge.android.maps.overlay.OverlayWay;
 import org.mapsforge.core.GeoPoint;
 
 import back.DisconnectedPositionUpdater;
 import back.Position;
 import back.PositionStorage;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.ToggleButton;
 
 public class LostActivity extends MapActivity implements Observer{
 	
 	MapView mapView;
 	Position lastPosition = null;
 	ArrayWayOverlay overlay;
 	
 	boolean locked = false;
 	
 	private static final String TAG = LostActivity.class.getSimpleName();
 	
 	/**
 	 * Returns color prepared for painting
 	 * @param color
 	 * @return
 	 */
     public static Paint lineColor(int color) {
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setStyle(Paint.Style.STROKE);
         paint.setColor(color);
         paint.setAlpha(160);
         paint.setStrokeWidth(6);
         paint.setStrokeCap(Paint.Cap.BUTT);
         paint.setStrokeJoin(Paint.Join.ROUND);
         return paint;
     }
     
     /**
      * Adds line on the map
      * @param one
      * @param two
      */
     protected void addLine(GeoPoint one, GeoPoint two){
         GeoPoint[][] points = new GeoPoint[1][2];
         points[0][0] = one;
         points[0][1] = two;        
         OverlayWay way = new OverlayWay(points);
         overlay.addWay(way);
     }
 	
     /**
      * Observe global position storage
      */
     protected void observePositionStorage(){
         PositionStorage positionStorage = ((LostApplication) this.getApplication()).getPositionStorage();
         positionStorage.addObserver(this);
     }
     
     /**
      * creates path by existing data from position storage
      */
     protected void createExistingPath(){
     	PositionStorage positionStorage = ((LostApplication) this.getApplication()).getPositionStorage();
     	List<Position> positions = positionStorage.getAll();
     	if(positions.size() > 1){
     		GeoPoint[][] points = new GeoPoint[1][positions.size()];
     		int counter = 0;
     		for(Position p : positions){
     			points[0][counter] = new GeoPoint(p.getLat(), p.getLon());
     			counter++;
     		}
             OverlayWay way = new OverlayWay(points);
             overlay.addWay(way);
         	try {
     			lastPosition = positionStorage.getLastPosition();
     		} catch (NoSuchFieldException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
     	}
     }
     
     /**
      * Creates overlay on mapView
      */
     protected void createOverlay(){
         Paint outline = lineColor(Color.MAGENTA);
         overlay = new ArrayWayOverlay(null, outline);
         mapView.getOverlays().clear();
         mapView.getOverlays().add(overlay);
        
     }
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.map_view);
         
         mapView = (MapView) findViewById(R.id.mapview);
         
         mapView.setClickable(true);
         mapView.setBuiltInZoomControls(true);
 
         SharedPreferences preferences = getPreferences(MODE_PRIVATE);
         
         Bundle extra = getIntent().getExtras();
         if(extra != null){
         	String focusedMap = extra.getString("map");
         	SharedPreferences.Editor editor = preferences.edit();
         	editor.putString("map", focusedMap);
         	editor.commit();
         }
         
         String focusedMap = preferences.getString("map", "austria.map");
         mapView.setMapFile(new File("/sdcard/maps/" + focusedMap));
         createOverlay();
         observePositionStorage();
         
         /*
          * TODO find out what's causing problem in your emulator(s)
          */
        //startService(new Intent(this, PositionUpdater.class)); // start positioning service
         /*
          * OR turn On DisconnectedPositionUpdater 
          */
          startService(new Intent(this, DisconnectedPositionUpdater.class));
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
     }
     
     @Override
     public void onResume(){
     	super.onResume();
     	try{
     		Bundle extras = this.getIntent().getExtras();
 	    	boolean reseted = extras.getBoolean("reset", false);
 	    	if(reseted){
 	    		createOverlay();
 	    	}
     	}catch(NullPointerException e){ }
     }
 
 	@Override
 	public void update(Observable observable, Object data) {
 		Position p = (Position) data;
 		if(lastPosition != null){
 			addLine(new GeoPoint(lastPosition.getLat(), lastPosition.getLon()), new GeoPoint(p.getLat(), p.getLon()));
 		}
 		lastPosition = p;
 		
 		if(locked)
 		{
 			mapView.setCenter(new GeoPoint(lastPosition.getLat(), lastPosition.getLon()));
 		}
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	new MenuInflater (this).inflate(R.menu.menu1, menu);
     	
 		return super.onCreateOptionsMenu(menu);   
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch (item.getItemId())
     	{
 	    	case R.id.itemStats:
 				Intent intent = new Intent(this, StatisticsActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 				startActivity(intent);
 				return true;
 			case R.id.itemCurrentPlan:
 			/*	Intent intent2 = new Intent(this, LostActivity.class);
 				intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 				startActivity(intent2);*/
 				return true;
 			case R.id.itemSettings:
 				Intent intent3 = new Intent(this, PosViewActivity.class);
 				intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 				startActivity(intent3);
 				return true;
 			case R.id.itemMaps:
 				Intent intent4 = new Intent(this, MapListActivity.class);
 				intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 				startActivity(intent4);
 				return true;
     		
     	}
     
     	return super.onOptionsItemSelected(item);
     }
     
     
     public void bLockClicked(View view)
     {
     	ToggleButton b = (ToggleButton) view;
     	
     	if(b.isChecked())
     	{
     		locked = true;
     		mapView.setClickable(false);
     		
     	}
     	else 
     	{
     		locked = false;
     		mapView.setClickable(true);
     		
     		if(lastPosition != null)
     		{
     			mapView.setCenter(new GeoPoint(lastPosition.getLat(), lastPosition.getLon()));
     		}
     	}
     	
     	
     	
     }
     
     public void bGpsSwitchClicked(View view)
     {
     	ToggleButton b = (ToggleButton) view;
     	
     	if(b.isChecked())
     	{
     	
     		startService(new Intent(this, PositionUpdater.class));
     	}
     	
     	else
     	{
     		stopService(new Intent(this, PositionUpdater.class));    	
     	}
     	
     	
     }
     
     
 }
