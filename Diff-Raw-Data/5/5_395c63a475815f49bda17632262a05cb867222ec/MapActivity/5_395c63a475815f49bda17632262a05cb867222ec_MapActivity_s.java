 package fr.itinerennes.ui.activity;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.util.constants.OverlayConstants;
 import org.slf4j.Logger;
 import org.slf4j.impl.AndroidLoggerFactory;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import fr.itinerennes.ITRPrefs;
 import fr.itinerennes.ItineRennesConstants;
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.database.Columns;
 import fr.itinerennes.ui.views.ItinerennesMapView;
 import fr.itinerennes.ui.views.overlays.LocationOverlay;
 import fr.itinerennes.utils.ResourceResolver;
 
 /**
  * This is the main activity. Uses the <code>main_map.xml</code> layout and displays a menu bar on
  * top and a map view at center of the screen.
  * 
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public class MapActivity extends ItineRennesActivity implements OverlayConstants {
 
     /** The event logger. */
     private static final Logger LOGGER = AndroidLoggerFactory.getLogger(MapActivity.class);
 
     /** Intent parameter name to pass the map zoom level to set. */
     public static final String INTENT_SET_MAP_ZOOM = String.format("%s.setMapZoom",
             MapActivity.class.getName());
 
     /** Intent parameter name to pass the map latidute to set. */
     public static final String INTENT_SET_MAP_LAT = String.format("%s.setMapLatitude",
             MapActivity.class.getName());
 
     /** Intent parameter name to pass the map longitude to set. */
     public static final String INTENT_SET_MAP_LON = String.format("%s.setMapLongitude",
             MapActivity.class.getName());
 
     /**
      * Intent parameter name to use to set the type of something to find in the overlay and to set
      * selected.
      */
     public static final String INTENT_SELECT_BOOKMARK_TYPE = String.format("%s.selectBookmarkType",
             MapActivity.class.getName());
 
     /**
      * Intent parameter name to use to set the identifier of something to find in the overlay and to
      * set selected.
      */
     public static final String INTENT_SELECT_BOOKMARK_ID = String.format("s.selectBookmarkId",
             MapActivity.class.getName());
 
     /** Activity request code for preload. */
     public static final int ACTIVITY_REQUEST_PRELOAD = 0;
 
     /** Duration of toast messages. */
     private static final int TOAST_DURATION = 300;
 
     /** The map view. */
     private ItinerennesMapView map;
 
     /** The my location overlay. */
     private LocationOverlay myLocation;
 
     /** Location manager. */
     private LocationManager locationManager;
 
     /**
      * Called when activity starts. Displays the view.
      * <p>
      * {@inheritDoc}
      * </p>
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.start");
         }
         super.onCreate(savedInstanceState);
 
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
         setContentView(R.layout.act_map);
 
         this.map = (ItinerennesMapView) findViewById(R.id.map);
 
         this.myLocation = map.getMyLocationOverlay();
 
         map.setMultiTouchControls(true);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onCreate.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onResume()
      */
     @Override
     protected final void onResume() {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onResume.start");
         }
 
         final SharedPreferences sharedPreferences = getApplicationContext().getITRPreferences();
         final int zoomToRestore = sharedPreferences.getInt(ITRPrefs.MAP_ZOOM_LEVEL,
                 ItineRennesConstants.CONFIG_DEFAULT_ZOOM);
         map.getController().setZoom(zoomToRestore);
         final int latToRestore = sharedPreferences.getInt(ITRPrefs.MAP_CENTER_LAT,
                 ItineRennesConstants.CONFIG_RENNES_LAT);
         final int lonToRestore = sharedPreferences.getInt(ITRPrefs.MAP_CENTER_LON,
                 ItineRennesConstants.CONFIG_RENNES_LON);
         map.getController().setCenter(new GeoPoint(latToRestore, lonToRestore));
 
         // if a location provider is enabled and follow location is activated in preferences, the
         // follow location feature is enabled
         if (sharedPreferences.getBoolean(ITRPrefs.MAP_SHOW_LOCATION, true)
                 && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager
                         .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
             myLocation.enableFollowLocation();
         }
 
         if (!sharedPreferences.getBoolean(ITRPrefs.OVERLAY_BUS_ACTIVATED, true)) {
             map.getMarkerOverlay().hide(TypeConstants.TYPE_BUS);
         }
         if (!sharedPreferences.getBoolean(ITRPrefs.OVERLAY_BIKE_ACTIVATED, true)) {
             map.getMarkerOverlay().hide(TypeConstants.TYPE_BIKE);
         }
         if (!sharedPreferences.getBoolean(ITRPrefs.OVERLAY_SUBWAY_ACTIVATED, true)) {
             map.getMarkerOverlay().hide(TypeConstants.TYPE_SUBWAY);
         }
 
         super.onResume();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onResume.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onPause()
      */
     @Override
     protected final void onPause() {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onPause.start");
         }
         super.onPause();
 
         // saving in preferences the state of the map (center, follow location and zoom)
         final SharedPreferences.Editor edit = getApplicationContext().getITRPreferences().edit();
         edit.putInt(ITRPrefs.MAP_CENTER_LAT, map.getMapCenter().getLatitudeE6());
         edit.putInt(ITRPrefs.MAP_CENTER_LON, map.getMapCenter().getLongitudeE6());
         edit.putInt(ITRPrefs.MAP_ZOOM_LEVEL, map.getZoomLevel());
         edit.putBoolean(ITRPrefs.MAP_SHOW_LOCATION, myLocation.isMyLocationEnabled());
 
         // save current displayed overlays
         edit.putBoolean(ITRPrefs.OVERLAY_BUS_ACTIVATED,
                 map.getMarkerOverlay().isMarkerTypeVisible(TypeConstants.TYPE_BUS));
         edit.putBoolean(ITRPrefs.OVERLAY_BIKE_ACTIVATED, map.getMarkerOverlay()
                 .isMarkerTypeVisible(TypeConstants.TYPE_BIKE));
         edit.putBoolean(ITRPrefs.OVERLAY_SUBWAY_ACTIVATED, map.getMarkerOverlay()
                 .isMarkerTypeVisible(TypeConstants.TYPE_SUBWAY));
 
         // save modifications
         edit.commit();
 
         myLocation.disableFollowLocation();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onPause.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onNewIntent(android.content.Intent)
      */
     @Override
     protected final void onNewIntent(final Intent intent) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onNewIntent.start");
         }
 
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             final String query = intent.getStringExtra(SearchManager.QUERY);
 
             final Intent i = new Intent(getApplicationContext(), SearchResultsActivity.class);
             i.putExtra(SearchResultsActivity.INTENT_QUERY, query);
             startActivity(i);
         } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 
             final SharedPreferences.Editor edit = getApplicationContext().getITRPreferences()
                     .edit();
 
             // if intent asks for a specific zoom level, its values overrides the ones
             // saved in preferences
 
             int newZoom = ItineRennesConstants.CONFIG_DEFAULT_ZOOM;
             int newLat = ItineRennesConstants.CONFIG_RENNES_LAT;
             int newLon = ItineRennesConstants.CONFIG_RENNES_LON;
 
             if (intent.hasExtra(INTENT_SET_MAP_LAT) && intent.hasExtra(INTENT_SET_MAP_LON)) {
                 // center coordinates are send in the intent
                 newZoom = intent.getIntExtra(INTENT_SET_MAP_ZOOM, map.getZoomLevel());
                 newLat = intent.getIntExtra(INTENT_SET_MAP_LAT, map.getMapCenter().getLatitudeE6());
                 newLon = intent
                         .getIntExtra(INTENT_SET_MAP_LON, map.getMapCenter().getLongitudeE6());
             } else if (intent.getData() != null) {
                 // we come from a search suggestion click, so we get in database the item clicked
                 final Cursor c = getApplicationContext().getMarkerDao().getMarker(
                         intent.getData().getLastPathSegment());
 
                 if (c != null && c.moveToFirst()) {
                     newLat = c.getInt(c.getColumnIndex(Columns.MarkersColumns.LATITUDE));
                     newLon = c.getInt(c.getColumnIndex(Columns.MarkersColumns.LONGITUDE));
                     newZoom = ItineRennesConstants.CONFIG_DEFAULT_ZOOM;
 
                     c.close();
                 }
 
             }
 
             edit.putInt(ITRPrefs.MAP_CENTER_LAT, newLat);
             edit.putInt(ITRPrefs.MAP_CENTER_LON, newLon);
             edit.putInt(ITRPrefs.MAP_ZOOM_LEVEL, newZoom);
 
             // disable follow location in preferences because we are explicitly centering the
             // map on
             // a
             // location.
             // if not, it will be activated again in onResume().
             edit.putBoolean(ITRPrefs.MAP_SHOW_LOCATION, false);
 
             edit.commit();
         }
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onNewIntent.end");
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public final boolean onOptionsItemSelected(final MenuItem item) {
 
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.menu_layers:
             showDialog(Dialogs.SELECT_LAYERS);
             return true;
         case R.id.menu_bookmarks:
             startActivity(new Intent(this, BookmarksActivity.class));
             return true;
         case R.id.menu_about:
             showDialog(Dialogs.ABOUT);
             return true;
         case R.id.menu_search:
             onSearchRequested();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Creates the option menu with the following items.
      * <ul>
      * <li>about</li>
      * </ul>
      * <p>
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
 
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.map_menu, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * Click method handler invoked when a click event is detected on the my location button.
      * 
      * @param button
      *            the button view on which the event was detected
      */
     public final void onMyLocationButtonClick(final View button) {
 
         if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
             Toast.makeText(this, R.string.location_service_disabled, TOAST_DURATION).show();
             ((ToggleButton) button).setChecked(false);
         } else {
             myLocation.toggleFollowLocation();
         }
 
     }
 
     /**
      * Click method handler invoked when a click event is detected on the search button.
      * 
      * @param button
      *            the button view on which the event was detected
      */
     public final void onSearchButtonClick(final View button) {
 
         onSearchRequested();
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreateDialog(int)
      */
     @Override
     protected final Dialog onCreateDialog(final int id) {
 
         AlertDialog dialog;
         switch (id) {
         case Dialogs.SELECT_LAYERS:
 
             final HashMap<String, String> markerLabels = map.getMarkerOverlay()
                     .getMarkerTypesLabel();
             final String[] options = new String[markerLabels.size()];
             final boolean[] selections = new boolean[markerLabels.size()];
             final HashMap<String, String> labelstoType = new HashMap<String, String>(
                     markerLabels.size());
 
             final Iterator<Entry<String, String>> it = markerLabels.entrySet().iterator();
             int i = 0;
             while (it.hasNext()) {
                 final Entry<String, String> entry = it.next();
                 options[i] = entry.getValue();
                 selections[i] = map.getMarkerOverlay().isMarkerTypeVisible(entry.getKey());
                 labelstoType.put(entry.getValue(), entry.getKey());
                 i++;
             }
 
             final Map<String, Boolean> results = new HashMap<String, Boolean>();
 
             final AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(R.string.select_layer);
             // TJHU faire une icone calque builder.setIcon(id);
 
             builder.setMultiChoiceItems(options, selections,
                     new DialogInterface.OnMultiChoiceClickListener() {
 
                         @Override
                         public void onClick(final DialogInterface dialog, final int which,
                                 final boolean isChecked) {
 
                             results.put(options[which], isChecked);
                         }
                     });
             builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
                 @Override
                 public void onClick(final DialogInterface dialog, final int which) {
 
                     for (final Entry<String, Boolean> userInput : results.entrySet()) {
                         if (userInput.getValue()) {
                             map.getMarkerOverlay().show(labelstoType.get(userInput.getKey()));
                         } else {
                             map.getMarkerOverlay().hide(labelstoType.get(userInput.getKey()));
                         }
 
                     }
                     map.getMarkerOverlay().onMapMove(map);
                 }
 
             });
 
             dialog = builder.create();
             break;
         case Dialogs.ABOUT:
             final AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
             aboutBuilder.setTitle(R.string.menu_about).setCancelable(true);
             final View aboutView = getLayoutInflater().inflate(R.layout.act_about, null);
             aboutBuilder.setView(aboutView);
             final Drawable icDialogHelp = ResourceResolver.fromMenuToDialogIcon(this,
                     android.R.drawable.ic_menu_help);
             aboutBuilder.setIcon(icDialogHelp);
             dialog = aboutBuilder.create();
             break;
         default:
             dialog = null;
         }
         return dialog;
     }
 
     /**
      * Class containing dialog boxes contants.
      * 
      * @author Jérémie Huchet
      */
     private static final class Dialogs {
 
         /** Dialog identifier for layers selection. */
         private static final int SELECT_LAYERS = 0;
 
         /** "About" dialog box. */
         private static final int ABOUT = 1;
     }
 
 }
