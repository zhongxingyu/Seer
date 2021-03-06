 package net.wigle.wigleandroid;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.api.IMapController;
 import org.osmdroid.api.IMapView;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import dalvik.system.DexClassLoader;
 
 /**
  * show a map!
  */
 public final class MappingActivity extends Activity {
   private static class State {
     private boolean locked = true;
     private boolean firstMove = true;
     private IGeoPoint oldCenter = null;
     private int oldZoom = Integer.MIN_VALUE;
   }
   private final State state = new State();
   
   private IMapController mapControl;
   private IMapView mapView;
   private Handler timer;
   private AtomicBoolean finishing;
   private Location previousLocation;
   private int previousRunNets;
   private MyLocationOverlay myLocationOverlay = null;
   
   private static final GeoPoint DEFAULT_POINT = new GeoPoint( 41950000, -87650000 );
   private static final int MENU_EXIT = 12;
   private static final int MENU_ZOOM_IN = 13;
   private static final int MENU_ZOOM_OUT = 14;
   private static final int MENU_TOGGLE_LOCK = 15;
   private static final int MENU_TOGGLE_NEWDB = 16;
   private static final int MENU_LABEL = 17;
   private static final int MENU_FILTER = 18;
   
   private static final int SSID_FILTER = 102;
     
   /** Called when the activity is first created. */
   @Override
   public void onCreate( final Bundle savedInstanceState ) {
     super.onCreate( savedInstanceState );
     setContentView( R.layout.map );
     finishing = new AtomicBoolean( false );
     
     // media volume
     this.setVolumeControlStream( AudioManager.STREAM_MUSIC );  
     
     final Object stored = getLastNonConfigurationInstance();
     IGeoPoint oldCenter = null;
     int oldZoom = Integer.MIN_VALUE;
     if ( stored != null && stored instanceof State ) {
       // pry an orientation change, which calls destroy, but we set this in onRetainNonConfigurationInstance
       final State retained = (State) stored;
       state.locked = retained.locked;
       state.firstMove = retained.firstMove;
       oldCenter = retained.oldCenter;
       oldZoom = retained.oldZoom;
     }
     
     setupMapView( oldCenter, oldZoom );
     setupTimer();
   }
   
   @Override
   public Object onRetainNonConfigurationInstance() {
     ListActivity.info( "MappingActivity: onRetainNonConfigurationInstance" );
     // save the map info
     state.oldCenter = mapView.getMapCenter();
     state.oldZoom = mapView.getZoomLevel();
     // return state class to copy data from
     return state;
   }
   
   private void setupMapView( final IGeoPoint oldCenter, final int oldZoom ) {
     // view
     final RelativeLayout rlView = (RelativeLayout) this.findViewById( R.id.map_rl );
 
     // tryEvil();
     
     // possibly choose goog maps here
     mapView = new OpenStreetMapViewWrapper( this );    
     
     if ( mapView instanceof MapView ) {
       MapView osmMapView = (MapView) mapView;
       rlView.addView( osmMapView );
       osmMapView.setBuiltInZoomControls( true );
       osmMapView.setMultiTouchControls( true );
       
       // my location overlay
       myLocationOverlay = new MyLocationOverlay( getApplicationContext(), osmMapView );
       myLocationOverlay.setLocationUpdateMinTime( ListActivity.LOCATION_UPDATE_INTERVAL );
       myLocationOverlay.setDrawAccuracyEnabled( false );
       osmMapView.getOverlays().add( myLocationOverlay );
     }
     
     // controller
     mapControl = mapView.getController();
     final IGeoPoint centerPoint = getCenter( this, oldCenter, previousLocation );
     int zoom = 16;
     if ( oldZoom >= 0 ) {
       zoom = oldZoom;
     }
     else {
       final SharedPreferences prefs = getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
       zoom = prefs.getInt( ListActivity.PREF_PREV_ZOOM, zoom );
     }
     mapControl.setCenter( centerPoint );
     mapControl.setZoom( zoom );
     mapControl.setCenter( centerPoint );
     
     ListActivity.info("done setupMapView");
   }
   
   public static IGeoPoint getCenter( final Context context, final IGeoPoint priorityCenter,
       final Location previousLocation ) {
     
     IGeoPoint centerPoint = DEFAULT_POINT;
     final Location location = ListActivity.lameStatic.location;
     final SharedPreferences prefs = context.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
     if ( priorityCenter != null ) {
       centerPoint = priorityCenter;
     }
     else if ( location != null ) {
       centerPoint = new GeoPoint( location );
     }
     else if ( previousLocation != null ) {
       centerPoint = new GeoPoint( previousLocation );
     }
     else {
       // ok, try the saved prefs
       float lat = prefs.getFloat( ListActivity.PREF_PREV_LAT, Float.MIN_VALUE );
       float lon = prefs.getFloat( ListActivity.PREF_PREV_LON, Float.MIN_VALUE );
       if ( lat != Float.MIN_VALUE && lon != Float.MIN_VALUE ) {
         centerPoint = new GeoPoint( lat, lon );
       }    
     }
     
     return centerPoint;
   }
   
   private void setupTimer() {
     if ( timer == null ) {
       timer = new Handler();
       final Runnable mUpdateTimeTask = new Runnable() {
         public void run() {              
             // make sure the app isn't trying to finish
             if ( ! finishing.get() ) {
               final Location location = ListActivity.lameStatic.location;
               if ( location != null ) {
                 if ( state.locked ) {
                   // ListActivity.info( "mapping center location: " + location );
   								final GeoPoint locGeoPoint = new GeoPoint( location );
   								if ( state.firstMove ) {
   								  mapControl.setCenter( locGeoPoint );
   								  state.firstMove = false;
   								}
   								else {
   								  mapControl.animateTo( locGeoPoint );
   								}
                 }
                 else if ( previousLocation == null || previousLocation.getLatitude() != location.getLatitude() 
                     || previousLocation.getLongitude() != location.getLongitude() 
                     || previousRunNets != ListActivity.lameStatic.runNets) {
                   // location or nets have changed, update the view
                   if ( mapView instanceof View ) {
                     ((View) mapView).postInvalidate();
                   }
                 }
                 // set if location isn't null
                 previousLocation = location;
               }
               
               previousRunNets = ListActivity.lameStatic.runNets;
               
               TextView tv = (TextView) findViewById( R.id.stats_run );
               tv.setText( "Run: " + ListActivity.lameStatic.runNets );
               tv = (TextView) findViewById( R.id.stats_new );
               tv.setText( "New: " + ListActivity.lameStatic.newNets );
               tv = (TextView) findViewById( R.id.stats_dbnets );
               tv.setText( "DB: " + ListActivity.lameStatic.dbNets );
               
               final long period = 1000L;
               // info("wifitimer: " + period );
               timer.postDelayed( this, period );
             }
             else {
               ListActivity.info( "finishing mapping timer" );
             }
         }
       };
       timer.removeCallbacks( mUpdateTimeTask );
       timer.postDelayed( mUpdateTimeTask, 100 );
     }
   }
     
   @Override
   public void finish() {
     ListActivity.info( "finish mapping." );
     finishing.set( true );
     
     super.finish();
   }
   
   @Override
   public void onDestroy() {
     ListActivity.info( "destroy mapping." );
     finishing.set( true );
     
     // save zoom
     final SharedPreferences prefs = this.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
     final Editor edit = prefs.edit();
     edit.putInt( ListActivity.PREF_PREV_ZOOM, mapView.getZoomLevel() );
     edit.commit();
     
     super.onDestroy();
   }
   
   @Override
   public void onPause() {
     ListActivity.info( "pause mapping." );
     myLocationOverlay.disableMyLocation();
     myLocationOverlay.disableCompass();
     
     super.onPause();
   }
   
   @Override
   public void onResume() {
     ListActivity.info( "resume mapping." );
     myLocationOverlay.enableCompass();    
     myLocationOverlay.enableMyLocation();
     
     super.onResume();
   }
   
   /* Creates the menu items */
   @Override
   public boolean onCreateOptionsMenu( final Menu menu ) {
     MenuItem item = null;
     final SharedPreferences prefs = this.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
     final boolean showNewDBOnly = prefs.getBoolean( ListActivity.PREF_MAP_ONLY_NEWDB, false );
     
     String name = state.locked ? "Turn Off Lockon" : "Turn On Lockon";
     item = menu.add(0, MENU_TOGGLE_LOCK, 0, name);
     item.setIcon( android.R.drawable.ic_menu_mapmode );
         
     String nameDB = showNewDBOnly ? "Show Run&New" : "Show New Only";
     item = menu.add(0, MENU_TOGGLE_NEWDB, 0, nameDB);
     item.setIcon( android.R.drawable.ic_menu_edit );
     
     item = menu.add(0, MENU_LABEL, 0, "Toggle Labels");
     item.setIcon( android.R.drawable.ic_dialog_info );
     
     item = menu.add(0, MENU_EXIT, 0, "Exit");
     item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );    
     
     item = menu.add(0, MENU_FILTER, 0, "SSID Filter");
     item.setIcon( android.R.drawable.ic_menu_search );
     
     item = menu.add(0, MENU_ZOOM_IN, 0, "Zoom in");
     item.setIcon( android.R.drawable.ic_menu_add );
     
     item = menu.add(0, MENU_ZOOM_OUT, 0, "Zoom out");
     item.setIcon( android.R.drawable.ic_menu_revert );
     
     
     return true;
   }
 
   /* Handles item selections */
   @Override
   public boolean onOptionsItemSelected( final MenuItem item ) {
       switch ( item.getItemId() ) {
         case MENU_EXIT: {
           MainActivity.finishListActivity( this );
           finish();
           return true;
         }
         case MENU_ZOOM_IN: {
           int zoom = mapView.getZoomLevel();
           zoom++;
           mapControl.setZoom( zoom );
           return true;
         }
         case MENU_ZOOM_OUT: {
           int zoom = mapView.getZoomLevel();
           zoom--;
           mapControl.setZoom( zoom );
           return true;
         }
         case MENU_TOGGLE_LOCK: {
           state.locked = ! state.locked;
           String name = state.locked ? "Turn Off Lock-on" : "Turn On Lock-on";
           item.setTitle( name );
           return true;
         }
         case MENU_TOGGLE_NEWDB: {
           final SharedPreferences prefs = this.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
           final boolean showNewDBOnly = ! prefs.getBoolean( ListActivity.PREF_MAP_ONLY_NEWDB, false );
           Editor edit = prefs.edit();
           edit.putBoolean( ListActivity.PREF_MAP_ONLY_NEWDB, showNewDBOnly );
           edit.commit();
           
           String name = showNewDBOnly ? "Show Run&New" : "Show New Only";
           item.setTitle( name );
           return true;
         }
         case MENU_LABEL: {
           final SharedPreferences prefs = this.getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
           final boolean showLabel = ! prefs.getBoolean( ListActivity.PREF_MAP_LABEL, true );
           Editor edit = prefs.edit();
           edit.putBoolean( ListActivity.PREF_MAP_LABEL, showLabel );
           edit.commit();
           return true;
         }
         case MENU_FILTER: {
           showDialog( SSID_FILTER );
           return true;
         }
       }
       return false;
   }
   
   
   
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_BACK) {
       ListActivity.info( "onKeyDown: not quitting app on back" );
       MainActivity.switchTab( this, MainActivity.TAB_LIST );
       return true;
     }
     return super.onKeyDown(keyCode, event);
   }
   
   @Override
   public Dialog onCreateDialog( int which ) {
     switch ( which ) {
       case SSID_FILTER:
         final Dialog dialog = new Dialog( this );
 
         dialog.setContentView( R.layout.filterdialog );
         dialog.setTitle( "SSID Filter" );
         
         ListActivity.info("make new dialog");
         final SharedPreferences prefs = getSharedPreferences( ListActivity.SHARED_PREFS, 0 );
         final EditText regex = (EditText) dialog.findViewById( R.id.edit_regex );
         regex.setText( prefs.getString( ListActivity.PREF_MAPF_REGEX, "") );
         
         final CheckBox invert = MainActivity.prefSetCheckBox( this, dialog, R.id.showinvert, 
             ListActivity.PREF_MAPF_INVERT, false );
         final CheckBox open = MainActivity.prefSetCheckBox( this, dialog, R.id.showopen, 
             ListActivity.PREF_MAPF_OPEN, true );
         final CheckBox wep = MainActivity.prefSetCheckBox( this, dialog, R.id.showwep, 
             ListActivity.PREF_MAPF_WEP, true );
         final CheckBox wpa = MainActivity.prefSetCheckBox( this, dialog, R.id.showwpa, 
             ListActivity.PREF_MAPF_WPA, true );
         final CheckBox cell = MainActivity.prefSetCheckBox( this, dialog, R.id.showcell, 
             ListActivity.PREF_MAPF_CELL, true );
         final CheckBox enabled = MainActivity.prefSetCheckBox( this, dialog, R.id.enabled, 
             ListActivity.PREF_MAPF_ENABLED, true );
         
         Button ok = (Button) dialog.findViewById( R.id.ok_button );
         ok.setOnClickListener( new OnClickListener() {
             public void onClick( final View buttonView ) {  
               try {                
                 final Editor editor = prefs.edit();
                 editor.putString( ListActivity.PREF_MAPF_REGEX, regex.getText().toString() );
                 editor.putBoolean( ListActivity.PREF_MAPF_INVERT, invert.isChecked() );
                 editor.putBoolean( ListActivity.PREF_MAPF_OPEN, open.isChecked() );
                 editor.putBoolean( ListActivity.PREF_MAPF_WEP, wep.isChecked() );
                 editor.putBoolean( ListActivity.PREF_MAPF_WPA, wpa.isChecked() );
                 editor.putBoolean( ListActivity.PREF_MAPF_CELL, cell.isChecked() );
                 editor.putBoolean( ListActivity.PREF_MAPF_ENABLED, enabled.isChecked() );
                 editor.commit();
                 dialog.dismiss();
               }
               catch ( Exception ex ) {
                 // guess it wasn't there anyways
                 ListActivity.info( "exception dismissing filter dialog: " + ex );
               }
             }
           } );
         
         Button cancel = (Button) dialog.findViewById( R.id.cancel_button );
         cancel.setOnClickListener( new OnClickListener() {
             public void onClick( final View buttonView ) {  
               try {
                 regex.setText( prefs.getString( ListActivity.PREF_MAPF_REGEX, "") );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.showinvert, 
                     ListActivity.PREF_MAPF_INVERT, false );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.showopen, 
                     ListActivity.PREF_MAPF_OPEN, true );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.showwep, 
                     ListActivity.PREF_MAPF_WEP, true );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.showwpa, 
                     ListActivity.PREF_MAPF_WPA, true );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.showcell, 
                     ListActivity.PREF_MAPF_CELL, true );
                 MainActivity.prefSetCheckBox( MappingActivity.this, dialog, R.id.enabled, 
                     ListActivity.PREF_MAPF_ENABLED, true );
                 
                 dialog.dismiss();
               }
               catch ( Exception ex ) {
                 // guess it wasn't there anyways
                 ListActivity.info( "exception dismissing filter dialog: " + ex );
               }
             }
           } );
         
         return dialog;
       default:
         ListActivity.error( "unhandled dialog: " + which );
     }
     return null;
   }
   
   private void tryEvil() {
    final String apiKey = "hiuhhkjhkjhkjh";
     //Object foo = new com.google.android.maps.MapView( this, apiKey );
     try {
       File file = new File("/sdcard/com.google.android.maps.jar");
       ListActivity.info("file exists: " + file.exists() + " " + file.canRead());
       //DexFile df = new DexFile(file);
       
       DexClassLoader cl = new DexClassLoader("/system/framework/com.google.android.maps.jar:/sdcard/evil.jar",
           "/sdcard/", null, MappingActivity.class.getClassLoader() );
       // this is abstract, doesn't seem like we can reflect into it, proxy only works for interfaces :(
 //      Class<?> mapActivityClass = cl.loadClass("com.google.android.maps.MapActivity");
       
       Class<?> mapActivityClass = cl.loadClass("EvilMap");
       Constructor<?> constructor = mapActivityClass.getConstructor(Activity.class);
       Object mapActivity = constructor.newInstance( this );
      ListActivity.info("mapActivity: " + mapActivity.getClass().getName());
      Method create = mapActivity.getClass().getMethod("onCreate", Bundle.class);
      create.invoke(mapActivity, new Bundle());
       
 //      final InvocationHandler handler = new InvocationHandler() {
 //        public Object invoke( Object object, Method method, Object[] args ) {
 //          ListActivity.info("invoke: " + method.getName() );
 //          return null;
 //        }
 //      };
 //      Object mapActivity = Proxy.newProxyInstance( mapActivityClass.getClassLoader(), 
 //                                         new Class[]{ mapActivityClass }, handler );
       
       Class<?> foo = cl.loadClass("com.google.android.maps.MapView");
       constructor = foo.getConstructor(Context.class, String.class);
       Object googMap = constructor.newInstance( mapActivity, apiKey );
       ListActivity.info("googMap: " + googMap);
 
     }
     catch ( Exception ex)  {
       ListActivity.error("ex: " + ex, ex);
     }
         
   }
   
 }
