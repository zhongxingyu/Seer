 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.MenuItemCompat;
 import android.view.*;
 import android.widget.Toast;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Result;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.BillingListHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RawHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RequestHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RequestNN;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.CustomTileDownloader;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Overlays.NodeOverlay;
 import org.mapsforge.android.maps.MapActivity;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
 import org.mapsforge.android.maps.mapgenerator.tiledownloader.MapnikTileDownloader;
 import org.mapsforge.android.maps.mapgenerator.tiledownloader.OpenCycleMapTileDownloader;
 import org.mapsforge.android.maps.mapgenerator.tiledownloader.OsmarenderTileDownloader;
 import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
 import org.mapsforge.android.maps.overlay.OverlayWay;
 import org.mapsforge.core.GeoPoint;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 public class MapScreen extends MapActivity implements Session.Listener {
 	private MapView mapView;
 	private ArrayWayOverlay wayOverlay;
 	private Session session;
 	public static final int REQUEST_NODEMODEL = 0;
 	public static final int REQUEST_NODE = 1;
 	public static final int REQUEST_CONSTRAINTS = 2;
 	private NodeOverlay nodeOverlay;
 	private RequestHandler handler = null;
 
 	private LocationManager locManager;
 
 	private boolean isInstantRequest;
 	private boolean backIsDeleteMarker;
 
 	private final ArrayList<RequestNN> requestList = new ArrayList<RequestNN>();
 
 	private final Observer requestListener = new Observer() {
 		@Override
 		public void onCompleted(RawHandler caller, Object object) {
 			handler = null;
 			Edit edit = new SetResultEdit(session, (Result) object);
 			edit.perform();
 			setProgressBarIndeterminateVisibility(false);
 		}
 
 		@Override
 		public void onError(RawHandler caller, Object object) {
 			handler = null;
 			setProgressBarIndeterminateVisibility(false);
 			Toast.makeText(getApplicationContext(), object.toString(), Toast.LENGTH_LONG).show();
 		}
 	};
 
 	private final Observer nnsListener = new Observer() {
 		@Override
 		public void onCompleted(RawHandler caller, Object object) {
 			Edit edit = new UpdateNNSEdit(session, ((RequestNN) caller).getNode(), ((Result) object).getPoints().get(0).getGeoPoint());
 			edit.perform();
 			requestList.remove((RequestNN) caller);
 		}
 
 		@Override
 		public void onError(RawHandler caller, Object object) {
 			Toast.makeText(getApplicationContext(), object.toString(),
 					Toast.LENGTH_LONG).show();
 			requestList.remove((RequestNN) caller);
 		}
 	};
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(Session.IDENTIFIER, session);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		boolean isFirstStart = savedInstanceState == null;
 		// If we get created for the first time we get our data from the intent
 		Bundle data = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
 		session = (Session) data.getSerializable(Session.IDENTIFIER);
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		// setting properties of the mapview
 		setContentView(R.layout.activity_mapscreen);
 		mapView = (MapView) findViewById(R.id.mapView);
 		mapView.setClickable(true);
 		mapView.setLongClickable(true);
 		mapView.setBuiltInZoomControls(true);
 		mapView.getFileSystemTileCache().setPersistent(false);
 
 		initializeHandler();
 
 		setupWayOverlay();
 
 		setupGPS(isFirstStart);
 
 		mapView.getOverlays().add(nodeOverlay);
 
 		session.registerListener(NodeOverlay.class, nodeOverlay);
 		session.registerListener(MapScreen.class, this);
 	}
 
 	private String tileServer;
 	private String offlineMapLocation;
 	private MapScreenPreferences.MapGenerator mapGenerator;
 
 	private void setupMapView(SharedPreferences preferences) {
 		String newTileServer = preferences.getString("tile_server", MapScreenPreferences.defaultTileServer);
 		String newOfflineMapLocation = preferences.getString("offline_map_location", MapScreenPreferences.defaultMapLocation);
 		MapScreenPreferences.MapGenerator newMapGenerator = MapScreenPreferences.MapGenerator.valueOf(preferences.getString("map_generator", MapScreenPreferences.MapGenerator.MAPNIK.name()));
 
 		if (mapGenerator != newMapGenerator) {
 			switch (newMapGenerator) {
 				case MAPNIK:
 					mapView.setMapGenerator(new MapnikTileDownloader());
 					break;
 				case OSMANDER:
 					mapView.setMapGenerator(new OsmarenderTileDownloader());
 					break;
 				case OPENCYCLE:
 					mapView.setMapGenerator(new OpenCycleMapTileDownloader());
 					break;
 			}
 		}
 		if (newMapGenerator == MapScreenPreferences.MapGenerator.CUSTOM &&
 				((newMapGenerator != mapGenerator) || !tileServer.equals(newTileServer))) {
 			try {
 				mapView.setMapGenerator(new CustomTileDownloader(new URL(newTileServer), (byte) 17));
 			} catch (MalformedURLException e) {
 				mapView.setMapGenerator(new MapnikTileDownloader());
 				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 			}
 		} else if (newMapGenerator == MapScreenPreferences.MapGenerator.FILE &&
 				((newMapGenerator != mapGenerator) || !offlineMapLocation.equals(newOfflineMapLocation))) {
 			if (mapGenerator != newMapGenerator) {
 				mapView.setMapGenerator(new DatabaseRenderer());
 			}
 			if (!mapView.setMapFile(offlineMapLocation)) {
 				mapView.setMapGenerator(new MapnikTileDownloader());
 				Toast.makeText(this, getResources().getString(R.string.map_file_error), Toast.LENGTH_LONG).show();
 			}
 		}
 
 		tileServer = newTileServer;
 		mapGenerator = newMapGenerator;
 		offlineMapLocation = newOfflineMapLocation;
 	}
 
 	private void setupGPS(boolean isFirstStart) {
 		Location loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		GeoPoint gpsGeoPoint = null;
 
 		if (loc != null) {
 			gpsGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
 		}
 
 		// setting up LocationManager and set MapFocus on lastknown GPS-Location
 		if (isFirstStart && gpsGeoPoint != null) {
 			mapView.getController().setCenter(gpsGeoPoint);
 		}
 
 		nodeOverlay = new NodeOverlay(this, session, gpsGeoPoint);
 	}
 
 	private void setupWayOverlay() {
 		Path p = new Path();
 		p.moveTo(4.f, 0.f);
 		p.lineTo(0.f, -4.f);
 		p.lineTo(8.f, -4.f);
 		p.lineTo(12.f, 0.f);
 		p.lineTo(8.f, 4.f);
 		p.lineTo(0.f, 4.f);
 
 		Paint wayDefaultPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
 		wayDefaultPaintOutline.setStyle(Paint.Style.STROKE);
 		wayDefaultPaintOutline.setColor(Color.BLUE);
 		wayDefaultPaintOutline.setAlpha(160);
 		wayDefaultPaintOutline.setStrokeWidth(5.f);
 		wayDefaultPaintOutline.setStrokeJoin(Paint.Join.ROUND);
 		// Too slow needs optimization
 		//wayDefaultPaintOutline.setPathEffect(new ComposePathEffect(
 		//				new PathDashPathEffect(p, 12.f, 0.f, PathDashPathEffect.Style.ROTATE),
 		//				new CornerPathEffect(30.f)));
 
 		// create the WayOverlay and add the ways
 		wayOverlay = new ArrayWayOverlay(wayDefaultPaintOutline, null);
 		mapView.getOverlays().add(wayOverlay);
 		Result result = session.getResult();
 		if (result != null) {
 			addPathToMap(result.getWay());
 		}
 	}
 
 	// ----------------Menu-----------------
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mapscreenmenu, menu);
 		MenuItemCompat.setShowAsAction(menu.findItem(R.id.calculate), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent myIntent;
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.nodelist:
 				myIntent = new Intent(this, NodelistScreen.class);
 				myIntent.putExtra(Session.IDENTIFIER, session);
 				startActivityForResult(myIntent, REQUEST_NODEMODEL);
 				return true;
 			case R.id.reset:
 				if (handler != null) {
 					handler.cancel(true);
 					setProgressBarIndeterminateVisibility(false);
 					handler = null;
 				}
 				Edit edit = new ClearEdit(session);
 				edit.perform();
 				return true;
 			case R.id.calculate:
 				performRequest(true);
 				return true;
 			case R.id.gps:
 				GeoPoint pos = nodeOverlay.getGpsPosition();
 				if (pos != null)
 					mapView.getController().setCenter(nodeOverlay.getGpsPosition());
 				return true;
 			case R.id.algorithm_constraints:
 				myIntent = new Intent(this, AlgorithmConstraintsScreen.class);
 				myIntent.putExtra(Session.IDENTIFIER, session);
 				startActivityForResult(myIntent, REQUEST_CONSTRAINTS);
 				return true;
 			case R.id.back:
 				finish();
 				return true;
 			case R.id.settings:
 				startActivity(new Intent(this, MapScreenPreferences.class));
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 
 	}
 
 	private void performRequest(boolean force) {
 		if (handler != null && !force)
 			return;
 
 		if (handler != null)
 			handler.cancel(true);
 
 		RequestHandler h = session.performRequest(requestListener, force);
 		if (h != null) {
 			handler = h;
 			setProgressBarIndeterminateVisibility(true);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			int NodeModelsize = session.getNodeModel().size();
 
 			if (NodeModelsize > 0 && backIsDeleteMarker) {
 				Edit edit = new RemoveNodeEdit(session, NodeModelsize - 1);
 				edit.perform();
 				return true;
 			} else {
 				return super.onKeyDown(keyCode, event);
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Edit edit;
 		switch (requestCode) {
 			case REQUEST_NODEMODEL:
 				switch (resultCode) {
 					case RESULT_OK:
 						edit = new ChangeNodeModelEdit(session, (NodeModel) data.getExtras().getSerializable(NodeModel.IDENTIFIER));
 						edit.perform();
 						break;
 				}
 				break;
 			case REQUEST_NODE:
 				switch (resultCode) {
 					case RESULT_OK:
 						edit = new UpdateNodeEdit(session, data.getExtras().getInt("index"), (Node) data.getSerializableExtra("node"));
 						edit.perform();
 						break;
 					case EditNodeScreen.RESULT_DELETE:
 						edit = new RemoveNodeEdit(session, data.getExtras().getInt("index"));
 						edit.perform();
 						break;
 				}
 			case REQUEST_CONSTRAINTS:
 				switch (resultCode) {
 					case RESULT_OK:
 						// TODO
 				}
 		}
 	}
 
 	public void addPathToMap(GeoPoint[][] points) {
 		wayOverlay.clear();
 		wayOverlay.addWay(new OverlayWay(points));
 	}
 
 	public void performNNSearch(Node node) {
 		requestList.add((RequestNN) new RequestNN(nnsListener, session, node).execute());
 	}
 
 	private void initializeHandler() {
 		handler = (RequestHandler) getLastNonConfigurationInstance();
 
 		if (handler != null) {
 			handler.setListener(requestListener);
 			setProgressBarIndeterminateVisibility(true);
 		} else {
 			setProgressBarIndeterminateVisibility(false);
 		}
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return handler;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// 5 minutes, 50 meters
 		locManager.removeUpdates(nodeOverlay);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		//-----get mapScreen_Preferences
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 
 		isInstantRequest = preferences.getBoolean("is_instant_request", false);
 		backIsDeleteMarker = preferences.getBoolean("back_is_delete_marker", true);
 
 		setupMapView(preferences);
 
 		// 5 minutes, 50 meters
 		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 50, nodeOverlay);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		nodeOverlay.setContext(null);
 
 		mapView.getOverlays().remove(nodeOverlay);
 		mapView.getOverlays().remove(wayOverlay);
 
 		session.removeListener(NodeOverlay.class);
 		session.removeListener(MapScreen.class);
 
 		if (handler != null)
 			handler.setListener(null);
 
 		for (RequestNN request : requestList) {
 			request.setListener(null);
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// Only disable the button, if we don't have an action bar. Version < HONEYCOMB
 		if (Build.VERSION.SDK_INT < 11) {
 			menu.findItem(R.id.calculate).setEnabled(session.canPerformRequest());
 		}
 		menu.findItem(R.id.algorithm_constraints).setVisible(
 				!session.getSelectedAlgorithm().getConstraintTypes().isEmpty());
 
 		menu.findItem(R.id.back).setVisible(backIsDeleteMarker);
 
 		menu.findItem(R.id.gps).setVisible(nodeOverlay.getGpsPosition() != null);
 
 		return true;
 	}
 
 	@Override
 	public void onChange(final int change) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				if (0 < (change & Session.RESULT_CHANGE)) {
 					if (session.getResult() == null) {
 						wayOverlay.clear();
 					} else {
 						addPathToMap(session.getResult().getWay());
 					}
 				}
 				if (0 < (change & Session.DND_CHANGE) && isInstantRequest) {
 					performRequest(false);
 				}
 				if (0 < (change & Session.MODEL_CHANGE)) {
 					if (isInstantRequest) {
 						performRequest(true);
 					}
 					if (0 == (change & Session.ADD_CHANGE)) {
 						Edit edit = new SetResultEdit(session, null);
 						edit.perform();
 					}
 				}
 			}
 		});
 	}
 }
