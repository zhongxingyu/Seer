 package ch.hearc.devmobile.travelnotebook;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Intent;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.hearc.devmobile.travelnotebook.database.DatabaseHelper;
 import ch.hearc.devmobile.travelnotebook.database.Tag;
 import ch.hearc.devmobile.travelnotebook.database.TagType;
 import ch.hearc.devmobile.travelnotebook.database.TravelItem;
 import ch.hearc.devmobile.travelnotebook.database.Voyage;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.LatLngBounds.Builder;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polygon;
 import com.google.android.gms.maps.model.PolygonOptions;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 
 public class NotebookActivity extends FragmentActivity {
 
 	/********************
 	 * Private Static constants
 	 ********************/
 	private static final String LOGTAG = NotebookActivity.class.getSimpleName();
 	private static final int MAP_BOUNDS_MARGIN = 50;
 	private static final int VOAYAGE_ITEM_LINE_TRANSPARENCY = 50;
 	private static final int VOAYAGE_ITEM_LINE_TRANSPARENCY_SELECTED = 255;
 	private static final int NEW_ITEM_CODE = 110;
 
 	/********************
 	 * Public Static constants
 	 ********************/
 	public static final String TRAVEL_ITEM_ID = "travelItemId";
 	public static final String NOTEBOOKACTIVITY_VOYAGE_ID = "notebookId";
 	public static final String NOTEBOOKACTIVITY_RETURN_ERROR = "error";
 
 	/********************
 	 * Private members
 	 ********************/
 	private GoogleMap googleMap = null;
 	private DatabaseHelper databaseHelper = null;
 
 	private List<MenuElement> drawerListViewItems;
 	private ListView drawerListView;
 	private DrawerLayout drawerLayout;
 	private RelativeLayout drawerPanel;
 	private Voyage currentVoyage;
 	private SupportMapFragment notebookMapView;
 	private TextView notebookTitleTextView;
 	private Geocoder geocoder;
 
 	private Marker lastClickedMarker;
 	private Map<Marker, TravelItem> markers;
 	private Map<TravelItem, Polygon> polygons;
 
 	/********************
 	 * Public methods
 	 ********************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.notebook, menu);
 		return true;
 	}
 
 	/********************
 	 * Protected methods
 	 ********************/
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Hide application title
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// Hide status bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_notebook);
 
 		// Variable instanciation
 		geocoder = new Geocoder(this);
 		markers = new HashMap<Marker, TravelItem>();
 		polygons = new HashMap<TravelItem, Polygon>();
 		lastClickedMarker = null;
 
 		notebookMapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.notebook_mapview);
 
 		notebookMapView.onCreate(savedInstanceState);
 
 		notebookTitleTextView = (TextView) findViewById(R.id.notebookTitleTextView);
 
 		// get the DB Helper first.
 		getDBHelperIfNecessary();
 
 		// Now we can load the notebook to treat in this activity.
 		currentVoyage = Utilities.loadCurrentNotebookFromIntent(getIntent(), databaseHelper, this, LOGTAG);
 
 		// The last task is to setup the UI.
 		setUpMapIfNeeded();
 
 		initButtons();
 		initTitle();
 		buildDrawer();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setUpMapIfNeeded();
 		getDBHelperIfNecessary();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (databaseHelper != null) {
 			OpenHelperManager.releaseHelper();
 			databaseHelper = null;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		notebookMapView.onPause();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == NEW_ITEM_CODE) {
 			switch (resultCode) {
 
 			case RESULT_OK:
 				Toast.makeText(this, "Item saved", Toast.LENGTH_LONG).show();
 				buildDrawer();
 				break;
 
 			case RESULT_CANCELED:
 				Toast.makeText(this, "Canceled !", Toast.LENGTH_LONG).show();
 				break;
 			case NewOnTravelItemActivity.RESULT_FAIL:
 				Log.e(LOGTAG, "result fail");
 				// unused for now
 				break;
 			case NewNotebookActivity.RESULT_SQL_FAIL:
 				Log.e(LOGTAG, "Sql creation fail");
 				Toast.makeText(getApplicationContext(), "Creation failed !", Toast.LENGTH_SHORT).show();
 				break;
 			}
 		}
 	}
 
 	/********************
 	 * Private methods
 	 ********************/
 	private void buildDrawer() {
 		// Travel list
 		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		drawerListViewItems = new ArrayList<MenuElement>();
 		drawerPanel = (RelativeLayout) findViewById(R.id.right_drawer);
 
 		// Add items in the list from the database
 		MenuElement itemMenuElement = null;
 
 		for (final TravelItem item : currentVoyage.getTravelItems()) {
 
 			itemMenuElement = new MenuElement(item.getTitle(), new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// Intent intent = new Intent(
 					// NotebookActivity.this,
 					// NotebookActivity.class);
 					// intent.putExtra(NotebookActivity.this.TRAVEL_ITEM_ID,
 					// item.getId());
 					// intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 					// startActivity(intent);
 
 					startTravelIemActivity(item.getId());
 
 					NotebookActivity.this.drawerLayout.closeDrawer(drawerPanel);
 				}
 
 			});
 			drawerListViewItems.add(itemMenuElement);
 		}
 
 		// gets ListView defined in activity_main.xml
 		drawerListView = (ListView) findViewById(R.id.right_drawer_list);
 
 		// Sets the adapter for the list view
 		drawerListView.setAdapter(new MenuElementArrayAdapter(this, drawerListViewItems));
 
 	}
 
 	private void initButtons() {
 
 		// Get the planning button
 		Button btnGetPlanning = (Button) findViewById(R.id.btn_get_planning);
 		btnGetPlanning.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Intent intent = new Intent(NotebookActivity.this,
 				// PlanningActivity.class);
 				// intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 				// startActivity(intent);
 
 				Toast.makeText(getApplicationContext(), "Planning", Toast.LENGTH_SHORT).show();
 
 				NotebookActivity.this.drawerLayout.closeDrawer(drawerPanel);
 			}
 		});
 
 		// Gets the add planning item button
 		Button btnAddPlanningItem = (Button) findViewById(R.id.btn_add_planing_item);
 		btnAddPlanningItem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Intent intent = new Intent(NotebookActivity.this,
 				// PlanningActivity.class);
 				// intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 				// startActivity(intent);
 
 				Toast.makeText(getApplicationContext(), "Add planning item", Toast.LENGTH_SHORT).show();
 
 				NotebookActivity.this.drawerLayout.closeDrawer(drawerPanel);
 			}
 		});
 
 		// Gets the add travel item button
 		Button btnAddTravelItem = (Button) findViewById(R.id.btn_add_travel_item);
 		btnAddTravelItem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(NotebookActivity.this, NewOnTravelItemActivity.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 				intent.putExtra(NOTEBOOKACTIVITY_VOYAGE_ID, NotebookActivity.this.currentVoyage.getId());
 				startActivityForResult(intent, NEW_ITEM_CODE);
 
 				NotebookActivity.this.drawerLayout.closeDrawer(drawerPanel);
 			}
 		});
 	}
 
	private void loadCurrentNotebookFromIntent() {
		// Add items in the list from the database
		if (getIntent().hasExtra(NOTEBOOKACTIVITY_VOYAGE_ID)) {

			int voyageId = getIntent().getIntExtra(NOTEBOOKACTIVITY_VOYAGE_ID, -1);
			if (voyageId != -1) {
				try {
					this.currentVoyage = databaseHelper.getVoyageDao().queryForId(voyageId);

				}
				catch (SQLException e) {
					e.printStackTrace();
					abortActivityWithError("Voyage query failed: SQL exception");
				}
			}
			else {
				abortActivityWithError("No valid voyage id passed to NotebookActivity!");
			}
		}
		else {
			abortActivityWithError("No voyage id passed to NotebookActivity!");
		}
	}

 	private void initTitle() {
 		String title = currentVoyage.getTitle();
 		notebookTitleTextView.setText(title);
 	}
 
 	private void setUpMapIfNeeded() {
 		if (googleMap == null) {
 			googleMap = notebookMapView.getMap();
 
 			if (googleMap != null) {
 				setUpMap();
 			}
 		}
 	}
 
 	private void setUpMap() {
 
 		// clear the to begin at 0
 		final Builder boundsBuilder = new LatLngBounds.Builder();
 
 		// initialize map events
 		final View view = this.notebookMapView.getView();
 		if (view.getViewTreeObserver().isAlive()) {
 			view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
 				public void onGlobalLayout() {
 
 					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
 
 					LatLngBounds latLngBounds = null;
 					try {
 						latLngBounds = boundsBuilder.build();
 					}
 					catch (IllegalStateException e) {
 						Log.i(LOGTAG, "No LatLng in boundsBuilder: will not center the map");
 					}
 
 					if (latLngBounds != null)
 						googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, MAP_BOUNDS_MARGIN));
 				}
 			});
 		}
 
 		googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
 
 			@Override
 			public boolean onMarkerClick(Marker marker) {
 
 				// unselect a marker (if there is one selected)
 				unselectMarker();
 
 				// select the new marker
 				selectMarker(marker);
 
 				return false;
 			}
 		});
 
 		googleMap.setOnMapClickListener(new OnMapClickListener() {
 
 			@Override
 			public void onMapClick(LatLng latLng) {
 				unselectMarker();
 			}
 		});
 
 		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 
 			@Override
 			public void onInfoWindowClick(Marker marker) {
 				TravelItem travelItem = markers.get(marker);
 				startTravelIemActivity(travelItem.getId());
 			}
 		});
 
 		// Clear the markers list, will be reinitialized directly after.
 		markers.clear();
 
 		for (TravelItem travelItem : currentVoyage.getTravelItems()) {
 			displayTraveItemOnMap(travelItem, boundsBuilder);
 		}
 
 	}
 
 	private void displayTraveItemOnMap(TravelItem travelItem, Builder boundsBuilder) {
 		MarkerOptions markerOptions = new MarkerOptions();
 
 		Tag tag = travelItem.getTag();
 
 		// Append icon of the tag to the marker
 		BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(TagType.getIconRessource(tag.getTagType()));
 		String title = travelItem.getTitle();
 
 		markerOptions.icon(icon).title(title);
 
 		// Set marker positions if the only one position is available
 		if (travelItem.isSingleLocation()) {
 			try {
 				LatLng startPosition = travelItem.getStartLocationPosition(geocoder);
 
 				boundsBuilder.include(startPosition);
 
 				markerOptions.position(startPosition);
 
 				// append marker to the google map
 				Marker marker = googleMap.addMarker(markerOptions);
 
 				// Append markers to the marker map
 				markers.put(marker, travelItem);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			// Set the two markers and trace the line between the the markers.
 			try {
 				LatLng startPosition = travelItem.getStartLocationPosition(geocoder);
 				LatLng endPosition = travelItem.getEndLocationPosition(geocoder);
 
 				boundsBuilder.include(startPosition);
 				boundsBuilder.include(endPosition);
 
 				// append marker only on start position
 				markerOptions.position(startPosition);
 
 				// append line between end and start position
 				int color = Utilities.createTransparancyColor(this.currentVoyage.getColor(), VOAYAGE_ITEM_LINE_TRANSPARENCY);
 				PolygonOptions polygonOptions = new PolygonOptions();
 				polygonOptions.add(startPosition).add(endPosition).strokeColor(color).geodesic(true);
 
 				// append marker and polygon to the google map.
 				Marker marker = googleMap.addMarker(markerOptions);
 				Polygon polygon = googleMap.addPolygon(polygonOptions);
 
 				// Append markers to the marker map and same for the polygon
 				markers.put(marker, travelItem);
 				polygons.put(travelItem, polygon);
 
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void startTravelIemActivity(int id) {
 
 		TravelItem travelItem = null;
 		try {
 			travelItem = databaseHelper.getTravelItemDao().queryForId(id);
 
 			Toast.makeText(getApplicationContext(), travelItem.getDescription(), Toast.LENGTH_SHORT).show();
 		}
 		catch (SQLException e) {
 			Log.i(LOGTAG, "No travelItem with id " + id + " found.");
 		}
 
 	}
 
 	private void selectMarker(Marker marker) {
 		// save the lastClicked marker (necessary for the unselection)
 		lastClickedMarker = marker;
 
 		TravelItem travelItem = markers.get(marker);
 		if (travelItem != null) {
 			Polygon polygon = polygons.get(travelItem);
 			if (polygon != null) {
 				int color = Utilities.createTransparancyColor(currentVoyage.getColor(), VOAYAGE_ITEM_LINE_TRANSPARENCY_SELECTED);
 				polygon.setStrokeColor(color);
 			}
 		}
 	}
 
 	private void unselectMarker() {
 		TravelItem travelItem = markers.get(lastClickedMarker);
 		if (travelItem != null) {
 			Polygon polygon = polygons.get(travelItem);
 			if (polygon != null) {
 				int color = Utilities.createTransparancyColor(currentVoyage.getColor(), VOAYAGE_ITEM_LINE_TRANSPARENCY);
 				polygon.setStrokeColor(color);
 			}
 		}
 		lastClickedMarker = null;
 	}
 
 	private void getDBHelperIfNecessary() {
 		if (databaseHelper == null) {
 			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		}
 	}

	private void abortActivityWithError(String error) {
		Log.e(LOGTAG, error);

		Intent intent = new Intent();
		intent.putExtra(NOTEBOOKACTIVITY_RETURN_ERROR, error);

		this.setResult(RESULT_CANCELED, intent);
		this.finish();
	}
 }
