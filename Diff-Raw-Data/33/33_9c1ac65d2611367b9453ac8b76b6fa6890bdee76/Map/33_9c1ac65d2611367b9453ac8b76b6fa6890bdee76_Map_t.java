 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2009.
  *   author Alberto Romeu aromeu@prodevelop.es 
  *   author Ruben Blanco rblanco@prodevelop.es 
  *
  *
  * Original version of the code made by Nicolas Gramlich.
  * No header license code found on the original source file.
  * 
  * Original source code downloaded from http://code.google.com/p/osmdroid/
  * package org.andnav.osm.samples;
  * 
  * License stated in that website is 
  * http://www.gnu.org/licenses/lgpl.html
  * As no specific LGPL version was specified, LGPL license version is LGPL v2.1
  * is assumed.
  *  
  * 
  */
 
 package es.prodevelop.gvsig.mini.activities;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.anddev.android.weatherforecast.weather.WeatherCurrentCondition;
 import org.anddev.android.weatherforecast.weather.WeatherForecastCondition;
 import org.anddev.android.weatherforecast.weather.WeatherSet;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnKeyListener;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.text.Editable;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ZoomControls;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedText;
 import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedTextListAdapter;
 import es.prodevelop.gvsig.mini.common.CompatManager;
 import es.prodevelop.gvsig.mini.common.IContext;
 import es.prodevelop.gvsig.mini.common.android.AndroidContext;
 import es.prodevelop.gvsig.mini.context.ItemContext;
 import es.prodevelop.gvsig.mini.context.map.DefaultContext;
 import es.prodevelop.gvsig.mini.context.map.GPSItemContext;
 import es.prodevelop.gvsig.mini.context.map.POIContext;
 import es.prodevelop.gvsig.mini.context.map.RouteContext;
 import es.prodevelop.gvsig.mini.context.map.RoutePOIContext;
 import es.prodevelop.gvsig.mini.context.map.wms.WMSGPSItemContext;
 import es.prodevelop.gvsig.mini.context.map.wms.WMSPOIContext;
 import es.prodevelop.gvsig.mini.context.map.wms.WMSRouteContext;
 import es.prodevelop.gvsig.mini.context.map.wms.WMSRoutePOIContext;
 import es.prodevelop.gvsig.mini.geom.Extent;
 import es.prodevelop.gvsig.mini.geom.Feature;
 import es.prodevelop.gvsig.mini.geom.FeatureCollection;
 import es.prodevelop.gvsig.mini.geom.LineString;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
 import es.prodevelop.gvsig.mini.location.Config;
 import es.prodevelop.gvsig.mini.map.GeoUtils;
 import es.prodevelop.gvsig.mini.map.MapState;
 import es.prodevelop.gvsig.mini.map.ViewPort;
 import es.prodevelop.gvsig.mini.namefinder.Named;
 import es.prodevelop.gvsig.mini.namefinder.NamedMultiPoint;
 import es.prodevelop.gvsig.mini.search.PlaceSearcher;
 import es.prodevelop.gvsig.mini.settings.OnSettingsChangedListener;
 import es.prodevelop.gvsig.mini.settings.Settings;
 import es.prodevelop.gvsig.mini.tasks.Functionality;
 import es.prodevelop.gvsig.mini.tasks.TaskHandler;
 import es.prodevelop.gvsig.mini.tasks.map.GetCellLocationFunc;
 import es.prodevelop.gvsig.mini.tasks.namefinder.NameFinderFunc;
 import es.prodevelop.gvsig.mini.tasks.tiledownloader.TileDownloadCallbackHandler;
 import es.prodevelop.gvsig.mini.tasks.tiledownloader.TileDownloadWaiter;
 import es.prodevelop.gvsig.mini.tasks.twitter.TweetMyLocationFunc;
 import es.prodevelop.gvsig.mini.tasks.weather.WeatherFunctionality;
 import es.prodevelop.gvsig.mini.tasks.yours.YOURSFunctionality;
 import es.prodevelop.gvsig.mini.user.UserContext;
 import es.prodevelop.gvsig.mini.user.UserContextManager;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.util.Utils;
 import es.prodevelop.gvsig.mini.utiles.Constants;
 import es.prodevelop.gvsig.mini.utiles.Tags;
 import es.prodevelop.gvsig.mini.utiles.WorkQueue;
 import es.prodevelop.gvsig.mini.views.overlay.CircularRouleteView;
 import es.prodevelop.gvsig.mini.views.overlay.LongTextAdapter;
 import es.prodevelop.gvsig.mini.views.overlay.NameFinderOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.RouteOverlay;
 import es.prodevelop.gvsig.mini.views.overlay.SlideBar;
 import es.prodevelop.gvsig.mini.views.overlay.TileRaster;
 import es.prodevelop.gvsig.mini.views.overlay.ViewSimpleLocationOverlay;
 import es.prodevelop.gvsig.mini.yours.Route;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.tilecache.IDownloadWaiter;
 import es.prodevelop.tilecache.TileDownloaderTask;
 import es.prodevelop.tilecache.generator.impl.FitScreenBufferStrategy;
 import es.prodevelop.tilecache.layers.Layers;
 import es.prodevelop.tilecache.provider.TileProvider;
 import es.prodevelop.tilecache.provider.filesystem.strategy.ITileFileSystemStrategy;
 import es.prodevelop.tilecache.provider.filesystem.strategy.impl.FileSystemStrategyManager;
 import es.prodevelop.tilecache.provider.filesystem.strategy.impl.FlatXFileSystemStrategy;
 import es.prodevelop.tilecache.renderer.MapRenderer;
 import es.prodevelop.tilecache.renderer.MapRendererManager;
 import es.prodevelop.tilecache.renderer.OSMMercatorRenderer;
 import es.prodevelop.tilecache.renderer.wms.WMSMapRendererFactory;
 import es.prodevelop.tilecache.util.Cancellable;
 import es.prodevelop.tilecache.util.ConstantsTileCache;
 import es.prodevelop.tilecache.util.Utilities;
 
 /**
  * The main activity of the application. Consists on a RelativeLayout with some
  * zoom controls and the TileRaster that manages the MapView
  * 
  * This class manages the Messages sent by Functionalities with the MapHandler
  * 
  * Also has instances of Route and NamedMultiPoint
  * 
  * Needs to be refactored
  * 
  * @author aromeu
  * @author rblanco
  * 
  */
 public class Map extends MapLocation implements GeoUtils, IDownloadWaiter,
 		OnSettingsChangedListener {
 	SlideBar s;
 
 	AlertDialog downloadTileAlert;
 	Cancellable downloadCancellable;
 	Button downloadTilesButton;
 	private TileDownloaderTask t;
 	private SeekBar downTilesSeekBar;
 	private TextView totalTiles;
 	private TextView totalMB;
 	private TextView totalZoom;
 
 	private TileDownloadWaiterDelegate tileWaiter;
 	public static String twituser = null;
 	public static String twitpass = null;
 	private ViewSimpleLocationOverlay mMyLocationOverlay;
 	public TileRaster osmap;
 	public final Route route = new Route();
 	public NamedMultiPoint nameds;
 	private Point nearestPOI;
 	private int indexNP;
 	String datalog = null;
 	public float datatransfer2 = 0;
 	public RelativeLayout rl;
 
 	ImageView ivSwitch;
 	ImageView ivCompassY;
 	ImageView ivCompass;
 	public ProgressDialog dialog2 = null;
 	private boolean recenterOnGPS = false;
 	private boolean backpressed = false;
 	public boolean backpressedroulette = false;
 	private AlertDialog alertP;
 	SensorEventListener mTop = null;
 	// private SensorManager mSensorManager;
 	private MenuItem myNavigator;
 	private MenuItem myLocationButton;
 	private MenuItem myZoomRectangle;
 	private MenuItem mySearchDirection;
 	private MenuItem myDownloadLayers;
 	private MenuItem myDownloadTiles;
 	private MenuItem mySettings;
 	private MenuItem myAbout;
 	private MenuItem myWhats;
 	private MenuItem myLicense;
 	public Handler mHandler;
 	public static ViewPort vp;
 	int nearopt = 0;
 	public static final int ROUTE_CANCELED = 100;
 	public static final int ROUTE_INITED = 101;
 	public static final int WEATHER_INITED = 102;
 	public static final int TWEET_SENT = 103;
 	public static final int SHOW_TWEET_DIALOG = 104;
 	public static final int POI_INITED = 105;
 	public static final int SHOW_POI_DIALOG = 106;
 	public static final int SHOW_ADDRESS_DIALOG = 107;
 	public static final int ROUTE_CLEARED = 108;
 	public static final int POI_LIST = 109;
 	public static final int VOID = 110;
 	public static final int POI_CLEARED = 111;
 	public static final int TWEET_ERROR = 112;
 	public static final int SHOW_TOAST = 113;
 	public static final int SHOW_OK_DIALOG = 114;
 	public static final int GETFEATURE_INITED = 115;
 	public static final int POI_CANCELED = 1;
 	public static final int POI_SUCCEEDED = 2;
 	public static final int ROUTE_SUCCEEDED = 3;
 	public static final int ROUTE_NO_RESPONSE = 4;
 	public static final int ROUTE_NO_CALCULATED = 5;
 	public static final int POI_SHOW = 6;
 	public static final int ROUTE_ORIENTATION_CHANGED = 7;
 	public static final int WEATHER_SHOW = 8;
 	public static final int WEATHER_CANCELED = 9;
 	public static final int WEATHER_ERROR = 10;
 	public static final int CALCULATE_ROUTE = 11;
 	TextView reportView;
 	int cacheCounter = 0;
 	MapState mapState;
 	boolean cleanVisible = false;
 	boolean cleanRoute = false;
 	public boolean navigation = false;
 	public boolean connection = true;
 	boolean listVisible = false;
 	boolean cleanCompassVisible = true;
 	public ZoomControls z = null;
 	public CircularRouleteView c;
 	private ItemContext context;
 	/**
 	 * Whether we currently automatically update the animation.
 	 */
 	boolean mUpdatingAnimation;
 	PowerManager.WakeLock wl;
 	private MapHandler handler = new MapHandler();
 	private final static Logger log = Logger.getLogger(Map.class.getName());
 	private UserContextManager contextManager; // singleton with user contexts
 	// list
 	private UserContext userContext;
 	private IContext aContext;
 	LinearLayout downloadTilesLayout;
 	ProgressBar downloadTilesPB;
 
 	/**
 	 * Called when the activity is first created.
 	 * */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		try {
 			super.onCreate(savedInstanceState);
 			try {
 				CompatManager.getInstance().getRegisteredLogHandler()
 						.configureLogger(log);
 				Settings.getInstance().addOnSettingsChangedListener(this);
 				tileWaiter = new TileDownloadWaiterDelegate(this);
 				log.log(Level.FINE, "on create");
 
 				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 				wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
 						"Prueba de ScreenPower");
 				// Utils.sendExceptionEmail(this, "Esto es una prueba");
 				// SDCardAppender appender = new SDCardAppender();
 				// long milis = System.currentTimeMillis();
 				// appender.setDir(Utils.LOG_DIR);
 				// appender.setFileName("log" + String.valueOf(milis) + ".txt");
 				// appender.setContext(this);
 				// log.addAppender(appender);
 				// log.addAppender(new ConsoleAppender());
 				// log.setLevel(Utils.LOG_LEVEL);
 				// log.log(Level.SEVERE,
 				// "Testing to log error message with Microlog.");
 				MapRendererManager.getInstance().registerMapRendererFactory(
 						new WMSMapRendererFactory());
 				onNewIntent(getIntent());
 				Config.setContext(this.getApplicationContext());
 				ResourceLoader.initialize(this.getApplicationContext());
 
 				aContext = new AndroidContext(this.getApplicationContext());
 				CompatManager.getInstance().registerContext(aContext);
 				Layers.getInstance().initialize(true);
 
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "onCreate", e);
 				// log.log(Level.SEVERE,e.getMessage());
 			} finally {
 				Constants.ROOT_DIR = Environment.getExternalStorageDirectory()
 						.getAbsolutePath();
 			}
 
 			mapState = new MapState(this);
 			this.setContext(new DefaultContext(this));
 
 			// nameFinderTask = new NameFinderTask(this, handler);
 			rl = new RelativeLayout(this);
 			// TMSRenderer t = TMSRenderer.getTMSRenderer(
 			// "http://www.idee.es/wms-c/PNOA/PNOA/1.0.0/PNOA/");
 			loadSettings(savedInstanceState);
 			loadMap(savedInstanceState);
 
 			// Load UserContexts
 			// First create/get UserContextManager singleton instance
 			contextManager = UserContextManager.getInstance();
 			// Create UserContext and register it in the manager
 			userContext = new UserContext();
 			contextManager.Register(userContext);
 			// add new user contexts here
 			// Load previously persisted user context data
 			contextManager.loadContexts();
 			userContext.incExecutionCounter(); // update execution number
 
 			boolean isSaved = false;
 			try {
 				if (savedInstanceState != null)
 					isSaved = savedInstanceState.getBoolean("isSaved", false);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "isSaved: ", e);
 			}
 
 			if (isSaved) {
 				log.log(Level.FINE, "Restoring from previous state");
 				loadRoute(savedInstanceState);
 				loadPois(savedInstanceState);
 				loadUI(savedInstanceState);
 				loadMap(savedInstanceState);
 				loadCenter(savedInstanceState);
 			} else {
 				log.log(Level.FINE, "Not restoring from previous state");
 				loadUI(savedInstanceState);
 				boolean succeed = mapState.load();
 				if (!succeed) {
 					log.log(Level.FINE,
 							"map state was not persisted. Loading Mapnik");
 					this.osmap.setZoomLevel(3);
 					this.osmap.setMapCenter(0, 0);
 					this.osmap.setRenderer(OSMMercatorRenderer
 							.getMapnikRenderer());
 				}
 			}
 
 			this.setContentView(rl);
 			// if (isSaved) setContentView(null);
 			// enableAcelerometer(savedInstanceState);
 
 			/*
 			 * if (!userContext.isUsedCircleMenu()) { // The Circle Context Menu
 			 * (Roulette) has never been displayed, // so let's // give the user
 			 * a hint about it Toast t = Toast.makeText(this, R.string.Map_23,
 			 * Toast.LENGTH_LONG); t.show(); }
 			 */
 
 			// Intercept a possible intent with a search executed from the
 			// search
 			// dialog, towards the application
 			Intent i = getIntent();
 
 			if (Intent.ACTION_SEARCH.equals(i.getAction())) {
 				String query = i.getStringExtra(SearchManager.QUERY);
 				searchInNameFinder(query);
 			}
 
 			int hintId = 0;
 			hintId = userContext.getHintMessage();
 			if (hintId != 0) {
 				Toast t = Toast.makeText(this, hintId, Toast.LENGTH_LONG);
 				t.show();
 			}
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 			LogFeedbackActivity.showSendLogDialog(this);
 		} finally {
 			// this.obtainCellLocation();
 		}
 	}
 
 	/**
 	 * Starts the NameFinderActivity after the NameFinderFunc has finished
 	 * 
 	 * @param descr
 	 *            An array with the description of the results of the NameFinder
 	 *            service
 	 * @param nm
 	 *            A NamedMultiPoint with the full results of the service
 	 * @return True if the activity is started, false if the results were not
 	 *         correct
 	 */
 	public boolean showPOIs(String[] descr, NamedMultiPoint nm) {
 		try {
 			if (descr == null) {
 				log.log(Level.FINE, "show pois with descr null, returning...");
 				return false;
 			}
 
 			if (nm == null || nm.getNumPoints() <= 0) {
 				log.log(Level.FINE, "show pois with nm null, returning...");
 				AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 				dialog2.dismiss();
 				Toast.makeText(this, R.string.Map_1, Toast.LENGTH_LONG).show();
 				// alert.setIcon(R.drawable.pois);
 				// alert.setTitle("Error");
 				// alert.setMessage("No results were found");
 				//
 				// alert.setNegativeButton("OK",
 				// new DialogInterface.OnClickListener() {
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				// }
 				// });
 				//
 				// alert.show();
 				return false;
 			}
 
 			Intent intent = new Intent(this, NameFinderActivity.class);
 			String[] res = new String[nm.getPoints().length];
 
 			for (int i = 0; i < nm.getNumPoints(); i++) {
 				res[i] = ((Named) nm.getPoint(i)).description;
 			}
 			intent.putExtra("test", res);
 
 			startActivityForResult(intent, 0);
 
 			this.nameds = nm;
 			dialog2.dismiss();
 			return true;
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "showPOIS: ", e);
 			return false;
 		} finally {
 			if (dialog2 != null)
 				dialog2.dismiss();
 		}
 	}
 
 	/**
 	 * Calls {@link #showPOIs(String[], NamedMultiPoint)} with the last results
 	 */
 	public void viewLastPOIs() {
 		try {
 			log.log(Level.FINE, "view last pois");
 			String[] desc = new String[nameds.getNumPoints()];
 
 			for (int i = 0; i < nameds.getNumPoints(); i++) {
 				desc[i] = ((Named) nameds.getPoint(i)).description;
 			}
 			showPOIs(desc, nameds);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "viewLastPOIS: ", e);
 		}
 	}
 
 	/**
 	 * Starts the LayersActivity with the MapState.gvTilesPath file
 	 */
 	public void viewLayers() {
 		try {
 			log.log(Level.FINE, "Launching load layers activity");
 			Intent intent = new Intent(this, LayersActivity.class);
 
 			intent.putExtra("loadLayers", true);
 			intent.putExtra("gvtiles", mapState.gvTilesPath);
 
 			startActivityForResult(intent, 1);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "viewLayers: ", e);
 		}
 	}
 
 	/**
 	 * Checks that the route can be calculated and launches route calculation
 	 */
 	public void calculateRoute() {
 		try {
 			log.log(Level.FINE, "calculate route");
 			if (route.canCalculate()) {
 				this.launchRouteCalculation();
 				// User context update
 				this.userContext.setUsedRoutes(true);
 				this.userContext.setLastExecRoutes();
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "calculateRoute: ", e);
 			Toast t = Toast.makeText(this, R.string.Map_2, Toast.LENGTH_LONG);
 			t.show();
 		} finally {
 		}
 	}
 
 	/**
 	 * Launches YOURSFunctionality
 	 */
 	public void launchRouteCalculation() {
 		try {
 			log.log(Level.FINE, "launching route calculation");
 			route.iscancelled = false;
 			YOURSFunctionality yoursFunc = new YOURSFunctionality(this, 0);
 			yoursFunc.onClick(null);
 			userContext.setUsedRoutes(true);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "launchRouteCalculation: ", e);
 		}
 	}
 
 	/**
 	 * Manages the results of the NameFinderActivity, LayersActivity
 	 */
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		try {
 			super.onActivityResult(requestCode, resultCode, intent);
 			log.log(Level.FINE, "onActivityResult (code, resultCode): "
 					+ requestCode + ", " + resultCode);
 			if (intent == null) {
 				log.log(Level.FINE,
 						"intent was null, returning from onActivityResult");
 				return;
 			}
 
 			switch (requestCode) {
 			case 0:
 				switch (resultCode) {
 				case 0:
 					log.log(Level.FINE,
 							"from NameFinderActivity: route from here");
 					int pos = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p = (Named) nameds.getPoint(pos);
 					route.setStartPoint(new Point(p.getX(), p.getY()));
 					calculateRoute();
 					osmap.setMapCenter(p.projectedCoordinates.getX(),
 							p.projectedCoordinates.getY());
 					break;
 				case 1:
 					log.log(Level.FINE,
 							"from NameFinderActivity: route to here");
 					int pos1 = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p1 = (Named) nameds.getPoint(pos1);
 					route.setEndPoint(new Point(p1.getX(), p1.getY()));
 					calculateRoute();
 					osmap.setMapCenter(p1.projectedCoordinates.getX(),
 							p1.projectedCoordinates.getY());
 					break;
 				case 2:
 					log.log(Level.FINE,
 							"from NameFinderActivity: set map center");
 					int pos2 = Integer.parseInt(intent.getExtras().get(
 							"selected").toString());
 					Named p2 = (Named) this.nameds.getPoint(pos2);
 					osmap.setMapCenterFromLonLat(p2.getX(), p2.getY());
 					if (osmap.getMRendererInfo() instanceof OSMMercatorRenderer)
 						osmap.setZoomLevel(p2.zoom);
 					break;
 				}
 
 				break;
 			case 1:
 				switch (resultCode) {
 				case RESULT_OK:
 					log.log(Level.FINE, "from LayersActivity");
 					String layerName = intent.getStringExtra("layer");
 					mapState.gvTilesPath = intent.getStringExtra("gvtiles");
 					if (layerName != null) {
 						log.log(Level.FINE, "load layer: " + layerName);
 						this.osmap.onLayerChanged(layerName);
 					}
 					break;
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Map onActivityResult: ", e);
 		}
 
 	}
 
 	@Override
 	public void onLocationChanged(final Location pLoc) {
 		try {
 			if (pLoc == null)
 				return;
 			log.log(Level.FINE, "onLocationChanged (lon, lat): "
 					+ pLoc.getLongitude() + ", " + pLoc.getLatitude());
 			this.mMyLocationOverlay.setLocation(MapLocation
 					.locationToGeoPoint(pLoc), pLoc.getAccuracy(), pLoc
 					.getProvider());
 			if (recenterOnGPS && pLoc.getLatitude() != 0
 					&& pLoc.getLongitude() != 0 && navigation == true) {
 
 				double[] coords = ConversionCoords.reproject(pLoc
 						.getLongitude(), pLoc.getLatitude(), CRSFactory
 						.getCRS("EPSG:4326"), CRSFactory.getCRS(osmap
 						.getMRendererInfo().getSRS()));
 				osmap.animateTo(coords[0], coords[1]);
 			}
 			// osmap.animateTo(pLoc.getLongitude(), pLoc
 			// .getLatitude());
 			if (mMyLocationOverlay.mLocation != null) {
 				connection = true;
 				if (myNavigator != null)
 					myNavigator.setEnabled(connection);
 			}
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onLocationChanged: ", e);
 		} finally {
 
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(final Menu menu) {
 		try {
 			this.isRendererAllowedToDownloadTiles(osmap.getMRendererInfo());
 			return super.onPrepareOptionsMenu(menu);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu pMenu) {
 		try {
 			mySearchDirection = pMenu.add(0, 0, 0, R.string.Map_3).setIcon(
 					R.drawable.menu00);
 			// pMenu.add(0, 1, 1, "Mock provider options");
 			myLocationButton = pMenu.add(0, 2, 2, R.string.Map_4).setIcon(
 					R.drawable.menu_location);
 			pMenu.add(0, 3, 3, R.string.Map_5).setIcon(R.drawable.menu02);
 			myDownloadTiles = pMenu.add(0, 4, 4, R.string.download_tiles_14)
 					.setIcon(R.drawable.layerdonwload);
 			// myZoomRectangle = pMenu.add(0, 4, 4, R.string.Map_6).setIcon(
 			// R.drawable.mv_rectangle);
 			// pMenu.add(0, 4, 4, "Weather").setIcon(R.drawable.menu03);
 			// pMenu.add(0, 5, 5, "Tweetme").setIcon(R.drawable.menu04);
 			myDownloadLayers = pMenu.add(0, 5, 5, R.string.download_tiles_01)
 					.setIcon(R.drawable.menu_download);
 			myNavigator = pMenu.add(0, 6, 6, R.string.Map_Navigator).setIcon(
 					R.drawable.menu_navigation).setEnabled(connection);
 			// myGPSButton = pMenu.add(0, 7, 7, R.string.Map_27).setIcon(
 			// R.drawable.menu_location).setCheckable(true).setChecked(
 			// true);
 			mySettings = pMenu.add(0, 7, 7, R.string.Map_31).setIcon(
 					android.R.drawable.ic_menu_preferences);
 			myWhats = pMenu.add(0, 8, 8, R.string.Map_30).setIcon(
 					R.drawable.menu_location);
 			myLicense = pMenu.add(0, 9, 9, R.string.Map_29).setIcon(
 					R.drawable.menu_location);
 			myAbout = pMenu.add(0, 10, 10, R.string.Map_28).setIcon(
 					R.drawable.menu_location);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onCreateOptionsMenu: ", e);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		boolean result = true;
 		try {
 			result = super.onMenuItemSelected(featureId, item);
 			switch (item.getItemId()) {
 			case 0:
 				try {
 					Map.this.showSearchDialog();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "showAddressDialog: ", e);
 				}
 				break;
 			case 1:
 				//				
 
 				break;
 			case 2:
 				try {
 
 					// recenterOnGPS = !recenterOnGPS;
 					// // osmap.switchPanMode();
 					//
 					// if (recenterOnGPS) {
 					// log.log(Level.FINE,
 					// "recentering on GPS after check MyLocation on");
 					// myLocation.setIcon(R.drawable.menu01);
 					//
 					// } else {
 					// log
 					// .log(Level.FINE,
 					// "stop recentering on GPS after check MyLocation off"
 					// );
 					// myLocation.setIcon(R.drawable.menu01_2);
 					// }
 					//
 					// if (this.mMyLocationOverlay.mLocation != null &&
 					// recenterOnGPS) {
 					log.log(Level.FINE, "click on my location menu item");
 					if (this.mMyLocationOverlay.mLocation == null
 							|| (this.mMyLocationOverlay.mLocation
 									.getLatitudeE6() == 0 && this.mMyLocationOverlay.mLocation
 									.getLongitudeE6() == 0)) {
 						Toast
 								.makeText(this, R.string.Map_24,
 										Toast.LENGTH_LONG).show();
 						return true;
 					}
 					this.osmap.setZoomLevel(15, true);
 					this.osmap
 							.setMapCenterFromLonLat(
 									this.mMyLocationOverlay.mLocation
 											.getLongitudeE6() / 1E6,
 									this.mMyLocationOverlay.mLocation
 											.getLatitudeE6() / 1E6);
 					// }
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "My location: ", e);
 				}
 				break;
 			case 3:
 				viewLayers();
 				break;
 			case 4:
 				if (Utils.isSDMounted())
 					Map.this.showDownloadTilesDialog();
 				else
 					Toast.makeText(this, R.string.LayersActivity_1,
 							Toast.LENGTH_LONG).show();
 				// try {
 				// log.log(Level.FINE, "switch pan mode");
 				// osmap.switchPanMode();
 				// if (osmap.isPanMode()) {
 				// item.setIcon(R.drawable.mv_rectangle).setTitle(
 				// R.string.Map_6);
 				// } else {
 				// item.setIcon(R.drawable.mv_pan)
 				// .setTitle(R.string.Map_7);
 				// }
 				// } catch (Exception e) {
 				// log.log(Level.SEVERE,"switchPanMode: ", e);
 				// }
 				break;
 			case 5:
 				try {
 					showDownloadDialog();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "OpenWebsite: ", e);
 				}
 				break;
 			case 6:
 				try {
 					recenterOnGPS = !recenterOnGPS;
 
 					myLocationButton.setEnabled(!recenterOnGPS);
 					if (myZoomRectangle != null)
 						myZoomRectangle.setEnabled(!recenterOnGPS);
 					myDownloadLayers.setEnabled(!recenterOnGPS);
 					mySearchDirection.setEnabled(!recenterOnGPS);
 					mySettings.setEnabled(!recenterOnGPS);
 
 					// myGPSButton.setEnabled(!recenterOnGPS);
 
 					if (!recenterOnGPS) {
 						log.log(Level.FINE,
 								"recentering on GPS after check MyLocation on");
 						z.setVisibility(View.VISIBLE);
 						myNavigator.setIcon(R.drawable.menu_navigation);
 
 					} else {
 						log
 								.log(Level.FINE,
 										"stop recentering on GPS after check MyLocation off");//						
 						z.setVisibility(View.INVISIBLE);
 						myNavigator.setIcon(R.drawable.menu_navigation_off);
 					}
 					// if (recenterOnGPS || mMyLocationOverlay.mLocation != null
 					// ) {
 					// log.log(Level.FINE,
 					// "recentering on GPS after check MyLocation on");
 					// myNavigator.setIcon(R.drawable.menu_navigation_off);
 					//
 					// } else {
 					// log
 					// .log(Level.FINE,
 					// "stop recentering on GPS after check MyLocation off"
 					// );
 					// myNavigator.setIcon(R.drawable.menu_navigation);
 					// }
 					//				 
 					if (!navigation) {
 						// wl.acquire();
 						// setRequestedOrientation(ActivityInfo.
 						// SCREEN_ORIENTATION_PORTRAIT);
 						// navigation = true;
 						this.initializeSensor(this, true);
 						this.showNavigationModeAlert();
 					} else {
 						try {
 							if (Settings.getInstance().getBooleanValue(
 									getText(R.string.settings_key_orientation)
 											.toString()))
 								this.stopSensor(this);
 						} catch (Exception e) {
 							log.log(Level.SEVERE, "navigation mode off", e);
 						}
 
 						log.log(Level.FINE, "navigation mode off");
 						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 						navigation = false;
 						wl.release();
 					}
 
 					//				
 					// osmap.switchPanMode();
 					// if (osmap.isPanMode()) {
 					// item.setIcon(R.drawable.mv_rectangle).setTitle(
 					// R.string.Map_6);
 					// } else {
 					// item.setIcon(R.drawable.mv_pan).setTitle(R.string.Map_7);
 					// }
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "switchPanMode: ", e);
 				}
 				break;
 			case 7:
 				Intent i = new Intent(this, SettingsActivity.class);
 				startActivity(i);
 				break;
 			case 8:
 				try {
 					showWhatsNew();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "OpenWebsite: ", e);
 				}
 				break;
 			case 9:
 				try {
 					showLicense();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "OpenWebsite: ", e);
 				}
 				break;
 			case 10:
 				try {
 					showAboutDialog();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "OpenWebsite: ", e);
 				}
 				break;
 			default:
 				break;
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 		return result;
 	}
 
 	private void showNavigationModeAlert() {
 		try {
 			RadioGroup r = new RadioGroup(this);
 			RadioButton r1 = new RadioButton(this);
 			r1.setText(R.string.portrait);
 			r1.setId(0);
 			RadioButton r2 = new RadioButton(this);
 			r2.setText(R.string.landscape);
 			r2.setId(1);
 			r.addView(r1);
 			r.addView(r2);
 			r.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				@Override
 				public void onCheckedChanged(RadioGroup arg0, int arg1) {
 					try {
 						centerOnGPSLocation();
 						wl.acquire();
 						navigation = true;
 						osmap
 								.onLayerChanged(osmap.getMRendererInfo()
 										.getNAME());
 						// final MapRenderer r =
 						// Map.this.osmap.getMRendererInfo();
 						Map.this.osmap.setZoomLevel(17, true);
 						switch (arg1) {
 						case 0:
 							log.log(Level.FINE, "navifation mode vertical on");
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 							break;
 						case 1:
 							log
 									.log(Level.FINE,
 											"navifation mode horizontal on");
 							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 							break;
 						default:
 							break;
 						}
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "onCheckedChanged", e);
 					}
 				}
 
 			});
 			AlertDialog.Builder alertCache = new AlertDialog.Builder(this);
 			alertCache.setView(r).setIcon(R.drawable.menu_navigation).setTitle(
 					R.string.Map_Navigator).setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 
 						}
 
 					}).create();
 			alertCache.show();
 			r1.setChecked(true);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	private void startCache(MapRenderer mr, int minZoom, int maxZoom) {
 		// GPSPoint center = this.osmap.getMapCenter();
 		// Point centerDouble = new Point(center.getLongitudeE6() / 1E6, center
 		// .getLatitudeE6() / 1E6);
 		// centerDouble = TileConversor.latLonToMercator(centerDouble.getX(),
 		// centerDouble.getY());
 		// // Point a = new Point(-0.43, 39.4);
 		// // Point b = new Point(-0.3, 39.53);
 		// Extent e = ViewPort.calculateExtent(centerDouble,
 		// Tags.RESOLUTIONS[this.osmap.getZoomLevel()], this.osmap
 		// .getWidth(), this.osmap.getHeight());
 		//
 		// // Extent e = new Extent(-20037508.3427892430765884088807,
 		// // -20037508.3427892430765884088807,
 		// // 20037508.3427892430765884088807,
 		// // 20037508.3427892430765884088807);
 		// // Extent e = new Extent(-5037508.3427892430765884088807,
 		// // -5037508.3427892430765884088807,
 		// // 37508.3427892430765884088807, 37508.3427892430765884088807);
 		// OSMHandler handler = new OSMHandler();
 		// handler.setURL(new String[] { mr.getBASEURL() });
 		//
 		// // YahooHandler h = new YahooHandler();
 		// // h.setURL(new
 		// // String[]{"http://png.maps.yimg.com/png?t=m&v=4.1&s=256&f=j"});
 		//
 		// // String layerName = this.osmap.getMRendererInfo().getNAME();
 		// String layerName = "estoesunaprueba2";
 		//
 		// try {
 		// es.prodevelop.gvsig.mini.phonecache.Cancellable c =
 		// es.prodevelop.gvsig.mini.phonecache.Utilities
 		// .getNewCancellable();
 		// Grid g = new Grid(handler, e, Tags.RESOLUTIONS[minZoom],
 		// Tags.RESOLUTIONS[maxZoom], layerName, c);
 		// g.addDownloadWaiter(this);
 		//
 		// ThreadPool.getInstance().assign(g);
 		// } catch (IOException exc) {
 		// Log.e("", exc.getMessage());
 		// }
 	}
 
 	/**
 	 * Shows an AlertDialog with the results from WeatherFunctionality
 	 * 
 	 * @param ws
 	 *            The results from WeatherFunctionality
 	 */
 	public void showWeather(WeatherSet ws) {
 		try {
 			log.log(Level.FINE, "showWeather");
 			if (ws == null) {
 				log
 						.log(Level.FINE,
 								"ws == null: Can't get weather. Check another location");
 				Toast.makeText(Map.this, R.string.Map_8, Toast.LENGTH_LONG)
 						.show();
 				return;
 			}
 			if (ws.getWeatherCurrentCondition() == null) {
 				dialog2.dismiss();
 				AlertDialog.Builder alertW = new AlertDialog.Builder(this);
 				alertW.setCancelable(true);
 				alertW.setIcon(R.drawable.menu03);
 				alertW.setTitle(R.string.error);
 				if (ws.place == null || ws.place.compareTo("") == 0) {
 					ws.place = this.getResources().getString(R.string.Map_9);
 				}
 
 				log.log(Level.FINE, "The weather in " + ws.place
 						+ " is not available");
 				alertW.setMessage(String.format(this.getResources().getString(
 						R.string.Map_10), ws.place));
 
 				alertW.setNegativeButton(R.string.ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 							}
 						});
 				alertW.show();
 				return;
 			}
 
 			AlertDialog.Builder alertW = new AlertDialog.Builder(this);
 			alertW.setCancelable(true);
 			alertW.setIcon(R.drawable.menu03);
 			alertW.setTitle(this.getResources().getString(R.string.Map_11)
 					+ " " + ws.place);
 
 			final ListView lv = new ListView(this);
 
 			BulletedTextListAdapter adapter = new BulletedTextListAdapter(this);
 
 			WeatherCurrentCondition wc = ws.getWeatherCurrentCondition();
 
 			adapter.addItem(new BulletedText(new StringBuffer().append(
 					this.getResources().getString(R.string.Map_12)).append(
 					" - ").append(wc.getTempCelcius()).append(" �C").append(
 					"\n").append(wc.getCondition()).append("\n").append(
 					wc.getWindCondition()).append("\n")
 					.append(wc.getHumidity()).toString(), BulletedText
 					.getRemoteImage(new URL("http://www.google.com"
 							+ wc.getIconURL()))).setSelectable(false));
 
 			ArrayList<WeatherForecastCondition> l = ws
 					.getWeatherForecastConditions();
 
 			WeatherForecastCondition temp;
 			for (int i = 0; i < l.size(); i++) {
 				try {
 					temp = l.get(i);
 					adapter.addItem(new BulletedText(new StringBuffer().append(
 							temp.getDayofWeek()).append(" - ").append(
 							temp.getTempMinCelsius()).append(" C").append("/")
 							.append(temp.getTempMaxCelsius()).append(" C")
 							.append("\n").append(temp.getCondition())
 							.toString(), BulletedText.getRemoteImage(new URL(
 							"http://www.google.com" + temp.getIconURL())))
 							.setSelectable(false));
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "showWeather: ", e);
 				}
 			}
 
 			lv.setAdapter(adapter);
 			lv.setPadding(10, 0, 10, 0);
 
 			alertW.setView(lv);
 
 			alertW.setNegativeButton(this.getResources().getString(
 					R.string.back), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 				}
 			});
 
 			alertW.show();
 			dialog2.dismiss();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "showWeather: ", e);
 		} finally {
 		}
 	}
 
 	/**
 	 * Launches the WeatherFunctionality
 	 * 
 	 * @param x
 	 *            The x coordinate
 	 * @param y
 	 *            The y coordinate
 	 * @param SRS
 	 *            The SRS in which the coordinates are expressed
 	 */
 	public void getWeather(double x, double y, String SRS) {
 		try {
 			log.log(Level.FINE, "Launching weather functionality");
 			double[] coords = ConversionCoords.reproject(x, y, CRSFactory
 					.getCRS(SRS), CRSFactory.getCRS("EPSG:4326"));
 
 			WeatherFunctionality w = new WeatherFunctionality(this, 0,
 					coords[1], coords[0]);
 			w.onClick(null);
 			userContext.setUsedWeather(true);
 			userContext.setLastExecWeather();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "getWeather: ", e);
 		}
 	}
 
 	/**
 	 * Shows an AlertDialog indicating that the route calculation has failed
 	 */
 	public void showRouteError() {
 		try {
 			log.log(Level.FINE, "Show route error");
 			dialog2.dismiss();
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setIcon(R.drawable.routes);
 			alert.setTitle(R.string.error);
 			alert.setMessage(R.string.Map_13);
 
 			alert.setNegativeButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			alert.show();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "showRouteError: ", e);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		try {
 			super.onDestroy();
 			log.log(Level.FINE, "onDestroy map activity");
 
 			try {
 				log.log(Level.FINE, "release wake lock");
 				wl.release();
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "release wake lock", e);
 
 			}
 
 			try {
 				log.log(Level.FINE, "persist map");
 				mapState.persist();
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Persist mapstate: ", e);
 			}
 
 			if (dialog2 != null)
 				dialog2.dismiss();
 
 			osmap.clearCache();
 			this.stopSensor(this);
 			if (backpressed) {
 				log.log(Level.FINE, "back key pressed");
 				// cosas a hacer para la limpieza
 
 				// this.getGPSManager().stopLocationProviders();
 				WorkQueue.getInstance().finalize();
 				WorkQueue.getExclusiveInstance().finalize();
 
 				backpressed = false;
 
 			}
 			try {
 				contextManager.saveContexts();
 			} catch (Exception e) {
 				log.log(Level.SEVERE,
 						"onDestroy. contextManager.saveContexts(): ", e);
 			}
 
 			this.destroy();
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onDestroy: ", e);
 		}
 	}
 
 	/**
 	 * Frees memory
 	 */
 	public void destroy() {
 		try {
 			log.log(Level.FINE, "destroy");
 			osmap.destroy();
 			// route.destroy();
 			// nameds.destroy();
 			// osmap = null;
 			// nameds = null;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "destroy", e);
 		}
 	}
 
 	/**
 	 * Load pois stored on a Bundle when the configuration has changed or the
 	 * Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadPois(Bundle outState) {
 		try {
 			log.log(Level.FINE, "loadPOIS from saved instance");
 			int poisSize = outState.getInt("PoisSize");
 			Named[] points = new Named[poisSize];
 			Named n;
 			for (int i = 0; i < poisSize; i++) {
 				try {
 					n = new Named(outState.getDouble("X" + i), outState
 							.getDouble("Y" + i));
 					n.description = outState.getString("Description" + i);
 					n.type = outState.getString("Type" + i);
 					n.id = outState.getInt("Id" + i);
 					n.name = outState.getString("Name" + i);
 					n.category = outState.getString("Category" + i);
 					n.info = outState.getString("Info" + i);
 					n.rank = outState.getInt("Rank" + i);
 					n.id = outState.getInt("In" + i);
 					n.distance = outState.getDouble("Distance" + i);
 					n.projectedCoordinates = new Point(outState.getDouble("PX"
 							+ i), outState.getDouble("PY" + i));
 					points[i] = n;
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "loadPois: ", e);
 				}
 			}
 
 			if (nameds == null) {
 				nameds = new NamedMultiPoint(points);
 			} else {
 				nameds.setPoints(points);
 			}
 
 			cleanVisible = outState.getBoolean("cleanPoisVisible", false);
 			listVisible = outState.getBoolean("listPoisVisible", false);
 			log.log(Level.FINE, "POIS loaded");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "loadPOIS: ", e);
 		}
 	}
 
 	/**
 	 * Save pois when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void savePois(Bundle outState) {
 		try {
 			log.log(Level.FINE, "Save POIS to bundle");
 			int pointNumber = this.nameds.getNumPoints();
 			outState.putInt("PoisSize", pointNumber);
 			Named n;
 			for (int i = 0; i < pointNumber; i++) {
 				try {
 					n = (Named) this.nameds.getPoint(i);
 					outState.putString("Type" + i, n.type);
 					outState.putInt("Id" + i, n.id);
 					outState.putString("Name" + i, n.name);
 					outState.putString("Category" + i, n.category);
 					outState.putString("Info" + i, n.info);
 					outState.putInt("Rank" + i, n.rank);
 					outState.putString("In" + i, n.isIn);
 					outState.putDouble("Distance" + i, n.distance);
 					outState.putString("Description" + i, n.description);
 					outState.putDouble("X" + i, n.getX());
 					outState.putDouble("Y" + i, n.getY());
 					outState.putDouble("PX" + i, n.projectedCoordinates.getX());
 					outState.putDouble("PY" + i, n.projectedCoordinates.getY());
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "savePOIS: ", e);
 				}
 			}
 
 			if (pointNumber > 0) {
 				outState.putBoolean("cleanPoisVisible", true);
 				outState.putBoolean("listPoisVisible", true);
 			}
 			log.log(Level.FINE, "POIS saved");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "savePOIS: ", e);
 		}
 	}
 
 	/**
 	 * Load route stored on a Bundle when the configuration has changed or the
 	 * Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadRoute(Bundle outState) {
 		try {
 			log.log(Level.FINE, "load route from saved instance");
 			boolean isDone = outState.getBoolean("isDone", true);
 
 			double startPointX = outState.getDouble("StartPointX");
 			double startPointY = outState.getDouble("StartPointY");
 			double endPointX = outState.getDouble("EndPointX");
 			double endPointY = outState.getDouble("EndPointY");
 			Point starPoint = new Point(startPointX, startPointY);
 			Point endPoint = new Point(endPointX, endPointY);
 
 			if (starPoint.equals(endPoint)) {
 				log.log(Level.FINE,
 						"start and end point equals. The route will not be loaded"
 								+ "return");
 			}
 
 			route.setStartPoint(starPoint);
 			route.setEndPoint(endPoint);
 			if (!isDone) {
 				log
 						.log(Level.FINE,
 								"Route was calculating before saving instance: Relaunch it");
 				this.launchRouteCalculation();
 				// return;
 			}
 			log.log(Level.FINE, "Loading route points");
 			int routeSize = outState.getInt("RouteSize");
 			double[] xCoords = new double[routeSize];
 			double[] yCoords = new double[routeSize];
 			for (int i = 0; i < routeSize; i++) {
 				xCoords[i] = outState.getDouble("RX" + i);
 				yCoords[i] = outState.getDouble("RY" + i);
 			}
 			LineString line = new LineString(xCoords, yCoords);
 			FeatureCollection f = new FeatureCollection();
 			f.addFeature(new Feature(line));
 			route.setRoute(f);
 
 			cleanRoute = outState.getBoolean("cleanRouteVisible", false);
 			log.log(Level.FINE, "route loaded");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "loadRoute: ", e);
 		}
 	}
 
 	/**
 	 * Save route when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveRoute(Bundle outState) {
 		try {
 			Point routeStart = this.route.getStartPoint();
 			Point routeEnd = this.route.getEndPoint();
 			FeatureCollection r = this.route.getRoute();
 
 			boolean isDone = true;
 			try {
 				Functionality yoursF = getItemContext()
 						.getExecutingFunctionality();
 				if (yoursF != null && yoursF instanceof YOURSFunctionality) {
 					isDone = !yoursF.isActive();
 				}
 				outState.putBoolean("isDone", isDone);
 			} catch (Exception e) {
 
 			}
 			outState.putDouble("StartPointX", routeStart.getX());
 			outState.putDouble("StartPointY", routeStart.getY());
 			outState.putDouble("EndPointX", routeEnd.getX());
 			outState.putDouble("EndPointY", routeEnd.getY());
 
 			boolean save = false;
 			if (r != null && r.getSize() > 0) {
 				Feature f = r.getFeatureAt(0);
 				LineString l = (LineString) f.getGeometry();
 				outState.putInt("RouteSize", l.getXCoords().length);
 				for (int i = 0; i < l.getXCoords().length; i++) {
 					save = true;
 					outState.putDouble("RX" + i, l.getXCoords()[i]);
 					outState.putDouble("RY" + i, l.getYCoords()[i]);
 				}
 			}
 
 			if (save)
 				outState.putBoolean("cleanRouteVisible", true);
 
 			log.log(Level.FINE, "route saved");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "savRoute: ", e);
 		}
 	}
 
 	/**
 	 * Load map info stored on a Bundle when the configuration has changed or
 	 * the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadMap(Bundle outState) {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		try {
 			log.log(Level.FINE, "load map from saved instance");
 			String mapLayer = outState.getString("maplayer");
 			log.log(Level.FINE, "previous layer: " + mapLayer);
//			OSMMercatorRenderer t = OSMMercatorRenderer.getMapnikRenderer();
//			this.osmap = new TileRaster(this, aContext, t, metrics.widthPixels,
//					metrics.heightPixels);
 			osmap.onLayerChanged(mapLayer);
 			this.mMyLocationOverlay.loadState(outState);
 			log.log(Level.FINE, "map loaded");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "loadMap: ", e);
 			OSMMercatorRenderer t = OSMMercatorRenderer.getMapnikRenderer();
 			this.osmap = new TileRaster(this, aContext, t, metrics.widthPixels,
 					metrics.heightPixels);
 
 		} finally {
 			try {
 				if (outState == null)
 					return;
 				String contextName = outState.getString("contextClassName");
 				log.log(Level.FINE, "loading previous context: " + contextName);
 				ItemContext context = (ItemContext) Class.forName(contextName)
 						.newInstance();
 				if (context != null) {
 					context.setMap(this);
 					this.setContext(context);
 				}
 			} catch (IllegalAccessException e) {
 				log.log(Level.SEVERE, "", e);
 			} catch (InstantiationException e) {
 				log.log(Level.SEVERE, "", e);
 			} catch (ClassNotFoundException e) {
 				log.log(Level.SEVERE, "", e);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "", e);
 			}
 		}
 	}
 
 	/**
 	 * Save settings when the configuration has changed or the Activity has been
 	 * restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveSettings(Bundle outState) {
 		try {
 			outState.putFloat("dataTransfer", this.datatransfer2);
 			outState.putBoolean("dataNavigation", this.navigation);
 			outState.putBoolean("dataRecent", this.recenterOnGPS);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "saveSettings: ", e);
 		}
 	}
 
 	/**
 	 * Load settings stored on a Bundle when the configuration has changed or
 	 * the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadSettings(Bundle outState) {
 		try {
 			if (outState == null)
 				return;
 			this.datatransfer2 = outState.getFloat("dataTransfer");
 			this.navigation = outState.getBoolean("dataNavigation");
 			this.recenterOnGPS = outState.getBoolean("dataRecent");
 			if (navigation && recenterOnGPS)
 				wl.acquire();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "loadSettings: ", e);
 		}
 	}
 
 	/**
 	 * Save map info (current layer, GPS location, ItemContext...) when the
 	 * configuration has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveMap(Bundle outState) {
 		try {
 			log.log(Level.FINE, "save map to bundle");
 			outState.putString("maplayer", osmap.getMRendererInfo().getNAME());
 			this.mMyLocationOverlay.saveState(outState);
 			ItemContext context = this.getItemContext();
 			if (context != null)
 				outState.putString("contextClassName", context.getClass()
 						.getName());
 			else
 				outState.putString("contextClassName", DefaultContext.class
 						.getName());
 			log.log(Level.FINE, "map saved");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "saveMap: ", e);
 		}
 	}
 
 	/**
 	 * Synchronize zoomcontrols with TileRaster.MapRenderer zoom level
 	 */
 	public void updateZoomControl() {
 		try {
 			MapRenderer r = Map.this.osmap.getMRendererInfo();
 
 			if (r.getZoomLevel() > r.getZoomMinLevel())
 				z.setIsZoomOutEnabled(true);
 			else
 				z.setIsZoomOutEnabled(false);
 
 			if (r.getZOOM_MAXLEVEL() > r.getZoomLevel()) {
 				z.setIsZoomInEnabled(true);
 			} else {
 				z.setIsZoomInEnabled(false);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "updateZoomControl: ", e);
 		}
 	}
 
 	/**
 	 * Instantiates the UI: TileRaster, ZoomControls, SlideBar in a
 	 * RelativeLayout
 	 * 
 	 * @param savedInstanceState
 	 */
 	public void loadUI(Bundle savedInstanceState) {
 		try {
 			log.log(Level.FINE, "load UI");
 			rl.addView(this.osmap, new RelativeLayout.LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 			z = new ZoomControls(this);
 
 			/* Creating the main Overlay */
 			{
 				this.mMyLocationOverlay = new ViewSimpleLocationOverlay(this,
 						osmap);
 				this.osmap.getOverlays()
 						.add(new NameFinderOverlay(this, osmap));
 				this.osmap.getOverlays().add(new RouteOverlay(this, osmap));
 				this.osmap.getOverlays().add(mMyLocationOverlay);
 			}
 
 			final RelativeLayout.LayoutParams zzParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			zzParams.addRule(RelativeLayout.ALIGN_BOTTOM);
 			zzParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			rl.addView(z, zzParams);
 			z.setId(107);
 			z.setVisibility(View.VISIBLE);
 
 			z.setOnZoomInClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					try {
 						Map.this.osmap.zoomIn();
 
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "onZoomInClick: ", e);
 					}
 				}
 			});
 			z.setOnZoomOutClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					try {
 						Map.this.osmap.zoomOut();
 						// Map.this.osmap.switchPanMode();
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "onZoomOutClick: ", e);
 					}
 				}
 			});
 
 			s = new SlideBar(this, this.osmap);
 
 			final RelativeLayout.LayoutParams slideParams = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.FILL_PARENT);
 			// slideParams.addRule(RelativeLayout.ABOVE,
 			// z.getId());
 			s.setVisibility(View.INVISIBLE);
 			slideParams.addRule(RelativeLayout.ALIGN_TOP);
 			slideParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			slideParams.addRule(RelativeLayout.ALIGN_RIGHT);
 			slideParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 
 			s.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
 					int zoom = (int) Math.floor(progress
 							* (Map.this.osmap.getMRendererInfo()
 									.getZOOM_MAXLEVEL() + 1 - Map.this.osmap
 									.getMRendererInfo().getZoomMinLevel())
 							/ 100);
 					osmap.drawZoomRectangle(zoom);
 				}
 
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 				}
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 					try {
 						int zoom = (int) Math
 								.floor(seekBar.getProgress()
 										* (Map.this.osmap.getMRendererInfo()
 												.getZOOM_MAXLEVEL() + 1 - Map.this.osmap
 												.getMRendererInfo()
 												.getZoomMinLevel()) / 100);
 						// int zoom = ((SlideBar)seekBar).portions;
 						Map.this.updateSlider(zoom);
 						Map.this.osmap.setZoomLevel(zoom, true);
 						Map.this.osmap.cleanZoomRectangle();
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "onStopTrackingTouch: ", e);
 					}
 				}
 			});
 
 			rl.addView(s, slideParams);
 
 			/* Controls */
 			{
 				LayoutInflater factory = LayoutInflater.from(this);
 
 				View ivLayers = (View) factory.inflate(
 						R.layout.layers_image_button, null);
 				ivLayers.setId(117);
 				final RelativeLayout.LayoutParams layersParams = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				layersParams.addRule(RelativeLayout.ALIGN_TOP);
 				layersParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 
 				rl.addView(ivLayers, layersParams);
 
 				ivLayers.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						try {
 							viewLayers();
 						} catch (Exception e) {
 							log.log(Level.SEVERE, "onLayersClick: ", e);
 							osmap.postInvalidate();
 						}
 					}
 				});
 				this.updateSlider();
 			}
 			log.log(Level.FINE, "ui loaded");
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		} catch (OutOfMemoryError ou) {
 			System.gc();
 			log.log(Level.SEVERE, "", ou);
 		}
 	}
 
 	/**
 	 * Loads the last map center and zoom level stored on a Bundle when the
 	 * configuration has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void loadCenter(Bundle savedInstanceState) {
 		try {
 			log.log(Level.FINE, "load center from saved instance");
 			double lat = savedInstanceState.getDouble("lat");
 			double longit = savedInstanceState.getDouble("longit");
 			int zoomlvl = savedInstanceState.getInt("zoomlvl");
 			log.log(Level.FINE, "lat, lon, zoom: " + lat + ", " + longit + ", "
 					+ zoomlvl);
 
 			this.osmap.setMapCenter(longit, lat);
 			this.osmap.setZoomLevel(zoomlvl);
 			log.log(Level.FINE, "lat, lon, zoom: " + longit + ", " + lat + ", "
 					+ zoomlvl);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "loadCenter: ", e);
 			osmap.getMRendererInfo().centerOnBBox();
 		}
 	}
 
 	/**
 	 * Saves the map center and zoom level on a Bundle when the configuration
 	 * has changed or the Activity has been restarted
 	 * 
 	 * @param outState
 	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
 	 */
 	public void saveCenter(Bundle outState) {
 		try {
 			log.log(Level.FINE, "save center to bundle");
 			final Point center = this.osmap.getMRendererInfo().getCenter();
 			final int zoomLevel = this.osmap.getMRendererInfo().getZoomLevel();
 			outState.putDouble("lat", center.getY());
 			outState.putDouble("longit", center.getX());
 			outState.putInt("zoomlvl", zoomLevel);
 			log.log(Level.FINE, "lat, lon, zoom: " + center.getY() + ", "
 					+ center.getX() + ", " + zoomLevel);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "saveCenter: ", e);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		try {
 			log.log(Level.FINE, "onSaveInstanceState");
 			outState.putBoolean("isSaved", true);
 			super.onSaveInstanceState(outState);
 			saveSettings(outState);
 			saveMap(outState);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSaveInstanceState: ", e);
 		}
 		try {
 			saveCenter(outState);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 		try {
 			saveRoute(outState);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 		try {
 			savePois(outState);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/**
 	 * This class Handles messages from Functionalities
 	 * 
 	 * @author aromeu
 	 * @author rblanco
 	 * 
 	 */
 	private class MapHandler extends Handler {
 		@Override
 		public void handleMessage(final Message msg) {
 			try {
 				log.log(Level.FINE, "MapHandler -> handleMessage");
 				final int what = msg.what;
 				switch (what) {
 				case TaskHandler.NO_RESPONSE:
 					log.log(Level.FINE, "task handler NO_RESPONSE");
 					if (dialog2 != null)
 						dialog2.dismiss();
 					Toast
 							.makeText(Map.this, R.string.Map_22,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.SHOW_TOAST:
 					log.log(Level.FINE, "SHOW_TOAST");
 					Toast t = Toast.makeText(Map.this, msg.obj.toString(),
 							Toast.LENGTH_LONG);
 					t.show();
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.SHOW_OK_DIALOG:
 					log.log(Level.FINE, "SHOW_OK_DIALOG");
 					if (dialog2 != null)
 						dialog2.dismiss();
 					Map.this.showOKDialog(msg.obj.toString(),
 							R.string.getFeatureInfo);
 					break;
 				case Map.POI_LIST:
 					log.log(Level.FINE, "POI_LIST");
 					Map.this.viewLastPOIs();
 					break;
 				case Map.POI_CLEARED:
 					log.log(Level.FINE, "POI_CLEARED");
 					Map.this.updateContext(Map.POI_CLEARED);
 					break;
 				case Map.ROUTE_CLEARED:
 					log.log(Level.FINE, "ROUTE_CLEARED");
 					Map.this.updateContext(Map.ROUTE_CLEARED);
 					break;
 				case Map.WEATHER_INITED:
 					log.log(Level.FINE, "WEATHER_INITED");
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_14),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.menu03);
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								log.log(Level.FINE, "weather canceled");
 								Map.this.getItemContext().cancelCurrentTask();
 								dialog2.dismiss();
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.GETFEATURE_INITED:
 					log.log(Level.FINE, "GETFEATURE_INITED");
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							"GetFeatureInfo...", true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.infobutton);
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								log.log(Level.FINE, "getFeature canceled");
 								Map.this.getItemContext().cancelCurrentTask();
 								dialog2.dismiss();
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 
 				case Map.WEATHER_CANCELED:
 					log.log(Level.FINE, "WEATHER_CANCELED");
 					Toast
 							.makeText(Map.this, R.string.Map_15,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.WEATHER_ERROR:
 					log.log(Level.FINE, "WEATHER_ERROR");
 					Toast.makeText(Map.this, R.string.Map_8, Toast.LENGTH_LONG)
 							.show();
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.WEATHER_SHOW:
 					log.log(Level.FINE, "WEATHER_SHOW");
 					Functionality f = Map.this.getItemContext()
 							.getExecutingFunctionality();
 					if (f instanceof WeatherFunctionality)
 						Map.this.showWeather(((WeatherFunctionality) f).ws);
 					else {
 						log.log(Level.FINE, "Nof found Weather functionality");
 					}
 					break;
 				case Map.ROUTE_NO_RESPONSE:
 					log.log(Level.FINE, "ROUTE_NO_RESPONSE");
 					Toast.makeText(Map.this, R.string.server_busy,
 							Toast.LENGTH_LONG).show();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.ROUTE_SUCCEEDED:
 					log.log(Level.FINE, "ROUTE_SUCCEEDED");
 					// ivCleanRoute.setVisibility(View.VISIBLE);
 					osmap.CLEAR_ROUTE = true;
 					if (dialog2 != null)
 						dialog2.dismiss();
 					osmap.getMRendererInfo().reprojectGeometryCoordinates(
 							route.getRoute().getFeatureAt(0).getGeometry(),
 							"EPSG:4326");
 					Map.this.updateContext(Map.ROUTE_SUCCEEDED);
 					osmap.postInvalidate();
 					break;
 				case Map.ROUTE_NO_CALCULATED:
 					log.log(Level.FINE, "ROUTE_NO_CALCULATED");
 					Toast
 							.makeText(Map.this, R.string.Map_16,
 									Toast.LENGTH_LONG).show();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					break;
 				case Map.POI_SHOW:
 					log.log(Level.FINE, "POI_SHOW");
 					Functionality nf = getItemContext()
 							.getExecutingFunctionality();
 					if (nf instanceof NameFinderFunc) {
 						NameFinderFunc n = (NameFinderFunc) nf;
 						osmap.getMRendererInfo().reprojectGeometryCoordinates(
 								n.nm, "EPSG:4326");
 						boolean update = Map.this.showPOIs(n.desc, n.nm);
 						if (update)
 							Map.this.updateContext(Map.POI_SUCCEEDED);
 					} else {
 						log.log(Level.FINE,
 								"Nof found NameFinder functionality");
 					}
 					break;
 				case Map.POI_INITED:
 					log.log(Level.FINE, "POI_INITED");
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_17),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.pois);
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								Map.this.getItemContext().cancelCurrentTask();
 								dialog2.dismiss();
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.ROUTE_CANCELED:
 					log.log(Level.FINE, "ROUTE_CANCELED");
 					route.deleteRoute(false);
 					// route.deleteEndPoint();
 					// route.deleteStartPoint();
 					// yoursFunc.cancel();
 					if (dialog2 != null)
 						dialog2.dismiss();
 					// ivCleanRoute.setVisibility(View.INVISIBLE);
 					Toast
 							.makeText(Map.this, R.string.Map_18,
 									Toast.LENGTH_LONG).show();
 					osmap.postInvalidate();
 					break;
 				case Map.POI_CANCELED:
 					log.log(Level.FINE, "POI_CANCELED");
 					// ivCleanPois.setVisibility(View.INVISIBLE);
 					// ivShowList.setVisibility(View.INVISIBLE);
 					if (dialog2 != null)
 						dialog2.dismiss();
 					Toast.makeText(Map.this, R.string.task_canceled,
 							Toast.LENGTH_LONG).show();
 					nameds = null;
 					osmap.postInvalidate();
 					break;
 				case Map.CALCULATE_ROUTE:
 					log.log(Level.FINE, "CALCULATE_ROUTE");
 					Map.this.calculateRoute();
 					break;
 				case Map.ROUTE_INITED:
 					log.log(Level.FINE, "ROUTE_INITED");
 					route.deleteRoute(false);
 					dialog2 = ProgressDialog.show(Map.this, Map.this
 							.getResources().getString(R.string.please_wait),
 							Map.this.getResources().getString(R.string.Map_19),
 							true);
 					dialog2.setCancelable(true);
 					dialog2.setCanceledOnTouchOutside(true);
 					dialog2.setIcon(R.drawable.routes);
 
 					dialog2.setOnCancelListener(new OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog2) {
 							try {
 								route.deleteRoute(false);
 								// ivCleanRoute.setVisibility(View.INVISIBLE);
 								osmap.postInvalidate();
 								Map.this.getItemContext().cancelCurrentTask();
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "onCancelDialog: ", e);
 							}
 						}
 					});
 					break;
 				case Map.TWEET_SENT:
 					log.log(Level.FINE, "TWEET_SENT");
 					Toast
 							.makeText(Map.this, R.string.Map_20,
 									Toast.LENGTH_LONG).show();
 					break;
 				case Map.TWEET_ERROR:
 					log.log(Level.FINE, "TWEET_ERROR");
 					Toast t1 = Toast.makeText(Map.this, msg.obj.toString(),
 							Toast.LENGTH_LONG);
 					t1.show();
 					break;
 				case Map.SHOW_TWEET_DIALOG:
 					log.log(Level.FINE, "SHOW_TWEET_DIALOG");
 					Map.this.showTweetDialog();
 					break;
 				case Map.SHOW_POI_DIALOG:
 					log.log(Level.FINE, "SHOW_POI_DIALOG");
 					Map.this.showSearchDialog();
 					break;
 				case Map.SHOW_ADDRESS_DIALOG:
 					log.log(Level.FINE, "SHOW_ADDRESS_DIALOG");
 					Map.this.showSearchDialog();
 					break;
 				}
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "handleMessage: ", e);
 				if (dialog2 != null)
 					dialog2.dismiss();
 				// Toast.makeText(Map.this,
 				// "Operation could not finish. Please try again.",
 				// 2000).show();
 			} finally {
 				try {
 					Map.this.clearContext();
 					osmap.invalidate();
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "", e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Shows an AlertDialog to the user to input the query string for NameFinder
 	 * 
 	 * @deprecated
 	 */
 	public void showPOIDialog() {
 		try {
 			log.log(Level.FINE, "showPOIDialog");
 			AlertDialog.Builder alertPOI = new AlertDialog.Builder(this);
 
 			alertPOI.setIcon(R.drawable.poismenu);
 			alertPOI.setTitle(R.string.Map_21);
 
 			final EditText inputPOI = new EditText(this);
 
 			alertPOI.setView(inputPOI);
 
 			alertPOI.setPositiveButton(R.string.search,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							try {
 								Editable value = inputPOI.getText();
 								// Call to NameFinder with the text
 								searchInNameFinder(value.toString());
 
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "clickNameFinder: ", e);
 							}
 							return;
 						}
 					});
 
 			alertPOI.setNegativeButton(R.string.cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 						}
 					});
 
 			alertPOI.show();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/**
 	 * Show an AlertDialog to the user to input the query string for NameFinder
 	 * addresses
 	 */
 	public void showSearchDialog() {
 		try {
 			this.onSearchRequested();
 			// log.log(Level.FINE, "show address dialog");
 			// AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			//
 			// alert.setIcon(R.drawable.menu00);
 			// alert.setTitle(R.string.Map_3);
 			// final EditText input = new EditText(this);
 			// alert.setView(input);
 			//
 			// alert.setPositiveButton(R.string.alert_dialog_text_search,
 			// new DialogInterface.OnClickListener() {
 			// public void onClick(DialogInterface dialog,
 			// int whichButton) {
 			// try {
 			// Editable value = input.getText();
 			// //Call to NameFinder with the text
 			// searchInNameFinder(value.toString());
 			//								
 			// } catch (Exception e) {
 			// log.log(Level.SEVERE,"clickNameFinderAddress: ", e);
 			// }
 			// return;
 			// }
 			// });
 			//
 			// alert.setNegativeButton(R.string.alert_dialog_text_cancel,
 			// new DialogInterface.OnClickListener() {
 			// public void onClick(DialogInterface dialog,
 			// int whichButton) {
 			// }
 			// });
 			//
 			// alert.show();
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/*
 	 * Perform a search for all types of different searches (POI, address,
 	 * search manager,...) using the NameFinder consumer It acts as a fa�ade for
 	 * all searches launched from Map activity to be resolved by the NameFinder
 	 * query: text to be sought
 	 */
 	private void searchInNameFinder(String query) {
 		try {
 			if (!query.trim().equals("")) {
 				PlaceSearcher search;
 
 				if ((nearopt == 0) || (osmap == null)) {
 					search = new PlaceSearcher(this, query);
 				} else {
 					double[] center = osmap.getCenterLonLat();
 					search = new PlaceSearcher(this, query, center[0],
 							center[1]);
 				}
 				/*
 				 * if (nearopt != 0) { NameFinder.parms = query; } else {
 				 * double[] center = osmap.getCenterLonLat(); NameFinder.parms =
 				 * query + " near " + center[1] + "," + center[0]; }
 				 * NameFinderFunc func = new NameFinderFunc( Map.this, 0);
 				 * func.onClick(null);
 				 */
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "searchWithNameFinder: ", e);
 		}
 		return;
 
 	}
 
 	private void instantiateTileDownloaderTask(LinearLayout l, int progress) {
 		try {
 			final boolean updateTiles = ((CheckBox) l
 					.findViewById(R.id.download_tiles_overwrite_check))
 					.isChecked();
 
 			MapRenderer currentRenderer = Map.this.osmap.getMRendererInfo();
 			MapRenderer renderer = Layers.getInstance().getRenderer(
 					currentRenderer.getNAME());
 			
 			String tileName = "tile.gvsig";
 			String dot = ".";
 			String strategy = ITileFileSystemStrategy.QUADKEY;
 			ITileFileSystemStrategy ts = FileSystemStrategyManager.getInstance()
 					.getStrategyByName(strategy);			
 
 			try {
 				tileName = Settings.getInstance().getStringValue(
 						getText(R.string.settings_key_tile_name).toString());
 			} catch (Exception e) {
 
 			}
 
 			try {
 				strategy = Settings.getInstance()
 						.getStringValue(
 								getText(R.string.settings_key_list_strategy)
 										.toString());
 			} catch (Exception e) {
 
 			}
 
 			String tileSuffix = dot + tileName;
 			ts = FileSystemStrategyManager.getInstance().getStrategyByName(strategy);
 			ts.setTileNameSuffix(tileSuffix);
 
 			int fromZoomLevel = currentRenderer.getZoomLevel();
 
 			int totalZoomLevels = currentRenderer.getZOOM_MAXLEVEL()
 					- fromZoomLevel;
 
 			int z = currentRenderer.getZOOM_MAXLEVEL()
 					- currentRenderer.getZoomMinLevel();
 
 			int toZoomLevel = (totalZoomLevels * progress / 100)
 					+ fromZoomLevel;
 
 			Map.this.totalZoom.setText(Map.this
 					.getText(R.string.download_tiles_05)
 					+ " " + fromZoomLevel + "/" + toZoomLevel);
 
 			Extent extent = ViewPort
 					.calculateExtent(currentRenderer.getCenter(),
 							currentRenderer.resolutions[currentRenderer
 									.getZoomLevel()], TileRaster.mapWidth,
 							TileRaster.mapHeight);
 
 			if (extent.area() < currentRenderer.getExtent().area())
 				renderer.setExtent(extent);
 
 			/*
 			 * Once we set the extent, we have to update the center of the
 			 * MapRenderer
 			 */
 			renderer.setCenter(renderer.getExtent().getCenter().getX(),
 					renderer.getExtent().getCenter().getY());
 
 			/*
 			 * Get a new Cancellable instance (one different each time the task
 			 * is executed)
 			 */
 			downloadCancellable = Utilities.getNewCancellable();
 
 			/*
 			 * Instantiate the download waiter. The implementation should
 			 * properly update the UI as the task is processing tiles
 			 */
 
 			TileDownloadCallbackHandler callBackHandler = new TileDownloadCallbackHandler(
 					Map.this);
 			ConstantsTileCache.DEFAULT_NUM_THREADS = 4;
 
 			/*
 			 * Finally instantiate the TileDownloaderTask. Automatically
 			 * launches to the IDownloadWaiter implementation the
 			 * onTotalNumTilesRetrieved event (prior to execute the task)
 			 * 
 			 * This instance will donwload from 0 to 3 zoom level of the whole
 			 * world extent of OSM (Mapnik) server. Not apply any ITileSorter
 			 * nor ITileBufferIntersector and applies the FlatX file system
 			 * strategy with a minimum buffer to fit the map size.
 			 */
 			int mode = TileProvider.MODE_ONLINE;
 			if (updateTiles) {
 				mode = TileProvider.MODE_UPDATE;
 			}
 			t = new TileDownloaderTask(CompatManager.getInstance()
 					.getRegisteredContext(), renderer, fromZoomLevel,
 					toZoomLevel, downloadCancellable, renderer.getExtent(),
 					null, callBackHandler, null, mode,
 					ts,
 					new FitScreenBufferStrategy());
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/**
 	 * Show an AlertDialog to the user to input the query string for NameFinder
 	 * addresses
 	 */
 	public void showDownloadTilesDialog() {
 		try {
 			log.log(Level.FINE, "show address dialog");
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			final LinearLayout l = (LinearLayout) this.getLayoutInflater()
 					.inflate(R.layout.download_tiles, null);
 			this.totalMB = (TextView) l
 					.findViewById(R.id.download_total_transfer_text);
 			this.totalTiles = (TextView) l
 					.findViewById(R.id.download_total_tiles_text);
 			this.totalZoom = (TextView) l
 					.findViewById(R.id.download_zoom_level_text);
 			this.downTilesSeekBar = (SeekBar) l
 					.findViewById(R.id.download_zoom_level_seekbar);
 			this.downTilesSeekBar
 					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 						@Override
 						public void onProgressChanged(SeekBar arg0,
 								int progress, boolean arg2) {
 							try {
 								Map.this.instantiateTileDownloaderTask(l,
 										progress);
 
 							} catch (Exception e) {
 								log.log(Level.SEVERE, e.getMessage());
 							}
 						}
 
 						@Override
 						public void onStartTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onStopTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 					});
 			alert.setIcon(R.drawable.layerdonwload);
 			alert.setTitle(R.string.download_tiles_14);
 			this.downTilesSeekBar.setProgress(50);
 			((CheckBox) l.findViewById(R.id.download_tiles_overwrite_check))
 					.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View arg0) {
 							Map.this.instantiateTileDownloaderTask(l,
 									Map.this.downTilesSeekBar.getProgress());
 						}
 					});
 
 			alert.setView(l);
 
 			alert.setPositiveButton(R.string.download_tiles_14,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							try {
 								Map.this.resetCounter();
 								Map.this.downloadTileAlert = Map.this
 										.getDownloadTilesDialog();
 								Map.this.downloadTileAlert.show();
 								WorkQueue.getExclusiveInstance().execute(t);
 							} catch (Exception e) {
 								log.log(Level.SEVERE,
 										"clickNameFinderAddress: ", e);
 							}
 							return;
 						}
 					});
 
 			alert.setNegativeButton(R.string.alert_dialog_text_cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 
 						}
 					});
 
 			alert.show();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	private AlertDialog getDownloadTilesDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
 				if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK) {
 					Toast.makeText(Map.this, R.string.download_tiles_13,
 							Toast.LENGTH_LONG).show();
 				}
 				return true;
 			}
 
 		});
 
 		builder.setIcon(R.drawable.layerdonwload);
 		builder.setTitle(R.string.download_tiles_14);
 
 		this.downloadTilesLayout = (LinearLayout) this.getLayoutInflater()
 				.inflate(R.layout.download_tiles_pd, null);
 
 		downloadTilesPB = (ProgressBar) this.downloadTilesLayout
 				.findViewById(R.id.ProgressBar01);
 
 		downloadTilesButton = (Button) this.downloadTilesLayout
 				.findViewById(R.id.download_tiles_button);
 
 		downloadTilesButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				Map.this.downloadCancellable.setCanceled(true);
 				Map.this.downloadTileAlert.dismiss();
 			}
 		});
 
 		builder.setView(this.downloadTilesLayout);
 
 		return builder.create();
 	}
 
 	// public void enableAcelerometer(Bundle savedInstanceState) {
 	// try {
 	// log.log(Level.FINE, "enable accelerometer");
 	// // mSensorManager = (SensorManager)
 	// // getSystemService(Context.SENSOR_SERVICE);
 	// // mSensorManager.registerListener(mSensorListener, mSensorManager
 	// // .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 	// // SensorManager.SENSOR_DELAY_UI);
 	// } catch (Exception e) {
 	// log.log(Level.SEVERE,"", e);
 	// }
 	// }
 
 	// private final SensorEventListener mSensorListener = new
 	// SensorEventListener() {
 	//
 	// public void onSensorChanged(SensorEvent event) {
 	// if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 	// double forceThreshHold = 4.0f;
 	// double totalForce = 0.0f;
 	// totalForce += Math.pow(event.values[SensorManager.DATA_X]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce += Math.pow(event.values[SensorManager.DATA_Y]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce += Math.pow(event.values[SensorManager.DATA_Z]
 	// / SensorManager.GRAVITY_EARTH, 2.0);
 	// totalForce = Math.sqrt(totalForce);
 	// if (totalForce > 1.5 && totalForce < forceThreshHold) {
 	// recenterOnGPS = !recenterOnGPS;
 	// if (mMyLocationOverlay.mLocation != null && recenterOnGPS) {
 	// osmap.setZoomLevel(15);
 	// osmap.setMapCenter(mMyLocationOverlay.mLocation);
 	// }
 	// }
 	// }
 	// }
 	//
 	// public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	// }
 	// };
 
 	@Override
 	public void onResume() {
 		try {
 			log.log(Level.FINE, "onResume");
 			super.onResume();
 			if (navigation && recenterOnGPS)
 				wl.acquire();
 			// mSensorManager.registerListener(mSensorListener, mSensorManager
 			// .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 			// SensorManager.SENSOR_DELAY_FASTEST);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onResume: ", e);
 		}
 	}
 
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		try {
 			return this.osmap.onTrackballEvent(event);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onTrackBallEvent: ", e);
 			return false;
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		try {
 			// mSensorManager.unregisterListener(mSensorListener);
 			super.onStop();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onStop: ", e);
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private long last = 0;
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		try {
 			this.osmap.mBearing = (int) event.values[0];
 			long current = System.currentTimeMillis();
 			if (!navigation) {
 				osmap.postInvalidate();
 			} else {
 				if (current - last > Utils.REPAINT_TIME) {
 					osmap.postInvalidate();
 					last = current;
 				}
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSensorChanged: ", e);
 		}
 	}
 
 	private ItemContext updateContextWMS(ItemContext context) {
 		try {
 			if (this.osmap.getMRendererInfo().getType() != MapRenderer.WMS_RENDERER) {
 				if (context instanceof WMSRoutePOIContext)
 					context = new RoutePOIContext(this);
 				else if (context instanceof WMSRouteContext)
 					context = new RouteContext(this);
 				else if (context instanceof WMSPOIContext) {
 					if (route != null
 							&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 						context = new RoutePOIContext(this);
 					} else {
 						context = new POIContext(this);
 					}
 				}
 
 				else if (context instanceof WMSGPSItemContext)
 					if (route != null
 							&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 						context = new RouteContext(this);
 					} else {
 						context = new DefaultContext(this);
 					}
 				else if (context instanceof POIContext)
 					if (route != null
 							&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 						context = new RoutePOIContext(this);
 					}
 
 				this.setContext(context);
 				return context;
 			}
 
 			if (context instanceof RoutePOIContext)
 				context = new WMSRoutePOIContext(this);
 			else if (context instanceof RouteContext)
 				context = new WMSRouteContext(this);
 			else if (context instanceof POIContext)
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					context = new WMSRoutePOIContext(this);
 				} else {
 					context = new WMSPOIContext(this);
 				}
 			else if (context instanceof DefaultContext
 					|| context instanceof GPSItemContext)
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					context = new WMSRouteContext(this);
 				} else {
 					context = new WMSGPSItemContext(this);
 				}
 
 			this.setContext(context);
 			return context;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Instantiates a CircularRouleteView @see Contextable, ItemContext
 	 * 
 	 * @param context
 	 *            The ItemContext that contains the IDs of the Drawables to
 	 *            instantiate buttons and associate Functionalities to it
 	 */
 	public void showContext(ItemContext context) {
 		try {
 			log.log(Level.FINE, "show context");
 
 			context = updateContextWMS(context);
 			if (context == null)
 				return;
 
 			LayoutInflater factory = LayoutInflater.from(this);
 			final int[] viewsID = context.getViewsId();
 			final int size = viewsID.length;
 
 			HashMap h = context.getFunctionalities();
 
 			int id;
 			View view;
 			Functionality func;
 			c = new CircularRouleteView(this, true);
 
 			for (int i = 0; i < size; i++) {
 				try {
 					id = viewsID[i];
 					view = (View) factory.inflate(id, null);
 					func = (Functionality) h.get(id);
 					if (func != null)
 						view.setOnClickListener(func);
 					c.addView(view);
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "show context", e);
 
 				}
 			}
 
 			c.setVisibility(View.VISIBLE);
 			final int count = rl.getChildCount();
 			View v;
 			for (int i = 0; i < count; i++) {
 				v = rl.getChildAt(i);
 				if (v instanceof CircularRouleteView) {
 					rl.removeView(v);
 				}
 			}
 			rl.addView(c);
 
 			// Change user context to store that the CircularRouleteView has
 			// been
 			// shown once at least
 			userContext.setUsedCircleMenu(true);
 			userContext.setLastExecCircle();
 			backpressedroulette = true;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "showContext: ", e);
 		}
 	}
 
 	/**
 	 * Removes the current CircularRouleteView from the RouleteLayout
 	 */
 	public void clearContext() {
 		try {
 			final int size = c.getChildCount();
 
 			View child = null;
 			for (int i = 0; i < size; i++) {
 				try {
 					child = c.getChildAt(i);
 					if (child != null)
 						child.getBackground().setCallback(null);
 					child = null;
 				} catch (Exception e) {
 
 				}
 			}
 
 			rl.removeView(c);
 			// switchSlideBar();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "clearContext: ", e);
 		}
 	}
 
 	/**
 	 * @see MapHandler
 	 * @return The MapHandler
 	 */
 	public Handler getMapHandler() {
 		return handler;
 	}
 
 	/**
 	 * Sets the current ItemContext of the application
 	 * 
 	 * @param context
 	 */
 	public void setContext(ItemContext context) {
 		try {
 			if (context == null) {
 				this.setContext(new DefaultContext(this));
 				log.log(Level.FINE, "setContext: " + "DefaultContext");
 			} else {
 				try {
 					Functionality f = this.getItemContext()
 							.getExecutingFunctionality();
 					if (f != null)
 						context.setExecutingFunctionality(f);
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "setContext", e);
 
 				}
 				this.context = null;
 				this.context = context;
 
 				log.log(Level.FINE, "setContext: "
 						+ context.getClass().getName());
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "setContext: ", e);
 		}
 	}
 
 	/**
 	 * Gets the current ItemContext of the application
 	 * 
 	 * @return
 	 */
 	public ItemContext getItemContext() {
 		return context;
 	}
 
 	/**
 	 * Shows an AlertDialog to the user to input his/her twitter account
 	 * credentials
 	 */
 	public void showTweetDialog() {
 		try {
 			log.log(Level.FINE, "showTweetDialog");
 			LayoutInflater factory = LayoutInflater.from(this);
 			final View textEntryView = factory.inflate(
 					R.layout.alert_dialog_text_entry, null);
 			AlertDialog.Builder alertTweet = new AlertDialog.Builder(this);
 			alertTweet.setView(textEntryView).setIcon(R.drawable.menu04)
 					.setTitle(R.string.alert_dialog_text_entry)
 					.setPositiveButton(R.string.alert_dialog_tweet,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									try {
 
 										EditText etrUserName = (EditText) textEntryView
 												.findViewById(R.id.username_edit);
 										String userName = etrUserName.getText()
 												.toString();
 										EditText etrUserPass = (EditText) textEntryView
 												.findViewById(R.id.password_edit);
 										String userPass = etrUserPass.getText()
 												.toString();
 
 										Map.twituser = userName;
 										Map.twitpass = userPass;
 										getItemContext().getFunctionalityByID(
 												R.layout.twitter_image_button)
 												.onClick(null);
 									} catch (Exception e) {
 										log.log(Level.SEVERE, "twitter: ", e);
 									}
 								}
 							}).setNegativeButton(R.string.alert_dialog_cancel,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 								}
 							}).create();
 			alertTweet.show();
 			userContext.setUsedTwitter(true);
 			userContext.setLastExecTwitter();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "showTweetDialog: ", e);
 		}
 	}
 
 	/**
 	 * This method sinchronizes the SlideBar position with the current
 	 * TileRaster zoom level
 	 */
 	public void updateSlider() {
 		try {
 			int progress = Math
 					.round(this.osmap.getTempZoomLevel()
 							* 100
 							/ (this.osmap.getMRendererInfo().getZOOM_MAXLEVEL() + 1 - Map.this.osmap
 									.getMRendererInfo().getZoomMinLevel()));
 			// if (progress == 0)
 			// progress = 1;
 			this.s.setProgress(progress);
 			this.updateZoomControl();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "updateSlider: ", e);
 		}
 	}
 
 	/**
 	 * Forces to update the SlideBar to an specific zoom level
 	 * 
 	 * @param zoom
 	 *            The zoom level
 	 */
 	public void updateSlider(int zoom) {
 		try {
 			int progress = zoom
 					* 100
 					/ (this.osmap.getMRendererInfo().getZOOM_MAXLEVEL() + 1 - Map.this.osmap
 							.getMRendererInfo().getZoomMinLevel());
 			// if (progress == 0)
 			// progress = 1;
 			this.s.setProgress(progress);
 			this.updateZoomControl();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "updateSlider: ", e);
 		}
 	}
 
 	private int state = Map.VOID;
 
 	/**
 	 * This method applies some logic to update the ItemContext from the current
 	 * Map state Map.VOID: No route and pois calculated Map.ROUTE_SUCCEEDED:
 	 * Route calculated Map.POI_SUCCEEDED: POI calculated Map.POI_CLEARED: POI
 	 * has been cleared Map.ROUTE_CLEARED: Route has been cleared
 	 * 
 	 * @param state
 	 */
 	public void updateContext(int state) {
 		try {
 			log.log(Level.FINE, "updateContext");
 			switch (state) {
 			case Map.VOID:
 				log.log(Level.FINE, "VOID");
 				this.setContext(new DefaultContext(this));
 				break;
 			case Map.ROUTE_SUCCEEDED:
 				log.log(Level.FINE, "ROUTE_SUCEEDED");
 				if (nameds != null && nameds.getNumPoints() > 0)
 					this.setContext(new RoutePOIContext(this));
 				else
 					this.setContext(new RouteContext(this));
 				break;
 			case Map.POI_SUCCEEDED:
 				log.log(Level.FINE, "POI_SUCCEEDED");
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					this.setContext(new RoutePOIContext(this));
 				} else {
 					this.setContext(new POIContext(this));
 				}
 				break;
 			case Map.POI_CLEARED:
 				log.log(Level.FINE, "POI_CLEARED");
 				if (route != null
 						&& route.getState() == Tags.ROUTE_WITH_2_POINT) {
 					this.setContext(new RouteContext(this));
 				} else {
 					this.setContext(new DefaultContext(this));
 				}
 				break;
 			case Map.ROUTE_CLEARED:
 				log.log(Level.FINE, "ROUTE_CLEARED");
 				if (nameds != null && nameds.getNumPoints() > 0) {
 					this.setContext(new POIContext(this));
 				} else {
 					this.setContext(new DefaultContext(this));
 				}
 				break;
 			}
 			updateContextWMS(this.context);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "updateContext: ", e);
 		}
 	}
 
 	/**
 	 * Switchs from visible to invisible an viceversa the SlideBar
 	 */
 	public void switchSlideBar() {
 		try {
 			int size = rl.getChildCount();
 			View v;
 			boolean hasCircularView = false;
 			for (int i = 0; i < size; i++) {
 				v = rl.getChildAt(i);
 				if (v instanceof CircularRouleteView) {
 					hasCircularView = true;
 				}
 			}
 			if (hasCircularView) {
 				clearContext();
 				return;
 			}
 			if (s.getVisibility() == View.VISIBLE) {
 				s.setVisibility(View.INVISIBLE);
 			} else {
 				s.setVisibility(View.VISIBLE);
 			}
 
 			clearContext();
 			// Update context for the Scale bar has been displayed
 			userContext.setLastExecScaleBar();
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "switchSlideBar: ", e);
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration config) {
 		try {
 			log.log(Level.FINE, "onConfigurationChanged");
 			if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
 				this.mMyLocationOverlay
 						.setOffsetOrientation(ViewSimpleLocationOverlay.PORTRAIT_OFFSET_ORIENTATION);
 			} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 				this.mMyLocationOverlay
 						.setOffsetOrientation(ViewSimpleLocationOverlay.LANDSCAPE_OFFSET_ORIENTATION);
 			}
 			super.onConfigurationChanged(config);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onConfigurationChanged: ", e);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		try {
 			if (keyCode == KeyEvent.KEYCODE_BACK) {
 				log.log(Level.FINE, "KEY BACK pressed");
 				if (!backpressedroulette) {
 					backpressed = true;
 					this.onDestroy();
 					Intent i = new Intent();
 					i.putExtra("exit", true);
 					setResult(RESULT_OK, i);
 					finish();
 					// TODO: Hacer esto bien, liberando recursos y cerrando la
 					// aplicaci�n
 
 					// return false;
 					super.onKeyDown(keyCode, event);
 				} else {
 					clearContext();
 					backpressedroulette = false;
 					return true;
 				}
 			}
 			return super.onKeyDown(keyCode, event);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onKeyDown: ", e);
 			return false;
 		}
 		// return false;
 
 	}
 
 	@Override
 	public void onLowMemory() {
 		try {
 			log.log(Level.FINE, "onLowMemory");
 			osmap.getMTileProvider().onLowMemory();
 			super.onLowMemory();
 			Toast.makeText(this, R.string.Map_25, Toast.LENGTH_LONG).show();
 			mapState.persist();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onLowMemory: ", e);
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		GPS_STATUS = status;
 
 	}
 
 	/**
 	 * Launches GetCellLocationFunc
 	 */
 	public void obtainCellLocation() {
 		try {
 			GetCellLocationFunc cellLocationFunc = new GetCellLocationFunc(
 					this, 0);
 			cellLocationFunc.launch();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	public static int GPS_STATUS;
 
 	@Override
 	public void onPause() {
 		try {
 			log.log(Level.FINE, "onPause");
 			super.onPause();
 			if (wl != null && wl.isHeld())
 				wl.release();
 
 		} catch (Exception e)
 
 		{
 			log.log(Level.SEVERE, "onPause: ", e);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent i) {
 		try {
 			if (i == null) {
 				return;
 			}
 
 			try {
 				setIntent(i);
 				if (Intent.ACTION_SEARCH.equals(i.getAction())) {
 					String query = i.getStringExtra(SearchManager.QUERY);
 					// Execute the search (common with POI and address as of
 					// 0.3.0 version)
 					searchInNameFinder(query);
 				} else {
 					String mapLayer = i.getStringExtra("layer");
 					log.log(Level.FINE, "previous layer: " + mapLayer);
 					if (mapLayer != null) {
 						osmap.onLayerChanged(mapLayer);
 						log.log(Level.FINE, "map loaded");
 					}
 				}
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "onNewIntent", e);
 
 			}
 
 			String actionName = i.getAction();
 			if (actionName != null
 					&& actionName.equals("android.intent.action.SEND")) {
 				TweetMyLocationFunc t = new TweetMyLocationFunc(this, 0);
 				t.launch();
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onNewIntent", e);
 		}
 	}
 
 	/**
 	 * persists the MapState
 	 */
 	public void persist() {
 		try {
 			log.log(Level.FINE, "map persist");
 			mapState.persist();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "persist", e);
 		}
 	}
 
 	public ViewSimpleLocationOverlay getmMyLocationOverlay() {
 		return mMyLocationOverlay;
 	}
 
 	public void setmMyLocationOverlay(
 			ViewSimpleLocationOverlay mMyLocationOverlay) {
 		this.mMyLocationOverlay = mMyLocationOverlay;
 	}
 
 	/**
 	 * Sets the TileRaster center on the GPS location
 	 */
 	public void centerOnGPSLocation() {
 		try {
 			final GPSPoint location = this.mMyLocationOverlay.mLocation;
 			if (location != null) {
 				this.osmap.setMapCenter(location);
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/**
 	 * starts the LocationHandler
 	 */
 	@Override
 	public void enableGPS() {
 		try {
 			log.log(Level.FINE, "enableGPS");
 			super.initLocation();
 			boolean enabled = this.isLocationHandlerEnabled();
 			this.myLocationButton.setEnabled(enabled);
 			this.myNavigator.setEnabled(enabled);
 			// this.myGPSButton.setTitle(R.string.Map_27);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	/**
 	 * Stops the location handler
 	 */
 	public void disableGPS() {
 		try {
 			log.log(Level.FINE, "disableGPS");
 			super.disableGPS();
 			boolean enabled = this.isLocationHandlerEnabled();
 			this.myLocationButton.setEnabled(enabled);
 			this.myNavigator.setEnabled(enabled);
 			// this.myGPSButton.setTitle(R.string.Map_26);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	private int previousTime = 0;
 
 	private synchronized void updateDownloadTilesDialog() {
 		try {
 			runOnUiThread(new Runnable() {
 				public void run() {
 					int time = (int) ((System.currentTimeMillis() - tileWaiter
 							.getInitDownloadTime()) / 1000);
 
 					if (time - previousTime < 1 && !isDownloadTilesFinished) {
 						return;
 					}
 
 					if (tileWaiter.getInitDownloadTime() > 0)
 						previousTime = time;
 
 					Map.this.downloadTilesPB.setMax((int) tileWaiter
 							.getTotalTilesToProcess());
 					Map.this.downloadTilesPB.setProgress((int) tileWaiter
 							.getDownloadedNow());
 					long totalDownloaded = Map.this.tileWaiter
 							.getDownloadedNow();
 					long total = Map.this.tileWaiter.getTotalTilesToProcess();
 					int perc = 0;
 					if (total != 0)
 						perc = (int) ((double) totalDownloaded / (double) total * 100.0);
 
 					((TextView) Map.this.downloadTilesLayout
 							.findViewById(R.id.download_perc_text))
 							.setText(perc + "%" + " - "
 									+ tileWaiter.getTilesDownloaded() + "-"
 									+ tileWaiter.getTilesFailed() + "-"
 									+ tileWaiter.getTilesFromFS() + "-"
 									+ tileWaiter.getTilesSkipped() + "-"
 									+ tileWaiter.getTilesDeleted() + "-"
 									+ tileWaiter.getTilesNotFound() + "-" + "/"
 									+ total);
 
 					String downloadedMB = String.valueOf(Map.this.tileWaiter
 							.getBytesDownloaded() / 1024 / 1024);
 
 					if (downloadedMB.length() > 4) {
 						downloadedMB = downloadedMB.substring(0, 4);
 					}
 
 					((TextView) Map.this.downloadTilesLayout
 							.findViewById(R.id.downloaded_mb_text))
 							.setText(Map.this
 									.getText(R.string.download_tiles_09)
 									+ " " + downloadedMB + " MB");
 
 					((TextView) Map.this.downloadTilesLayout
 							.findViewById(R.id.download_time_text))
 							.setText(Map.this
 									.getText(R.string.download_tiles_10)
 									+ " " + time + " s");
 
 					if (totalDownloaded == 0)
 						totalDownloaded = 1;
 
 					int estimated = (int) (total * time / totalDownloaded)
 							- time;
 					((TextView) Map.this.downloadTilesLayout
 							.findViewById(R.id.download_time_estimated_text))
 							.setText(Map.this
 									.getText(R.string.download_tiles_11)
 									+ " " + estimated + " s");
 				}
 			});
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	public double getBytesDownloaded() {
 		return tileWaiter.getBytesDownloaded();
 	}
 
 	public IDownloadWaiter getDownloadWaiter() {
 		return tileWaiter.getDownloadWaiter();
 	}
 
 	public long getDownloadedNow() {
 		return tileWaiter.getDownloadedNow();
 	}
 
 	public long getEndDownloadTime() {
 		return tileWaiter.getEndDownloadTime();
 	}
 
 	public Object getHandler() {
 		return tileWaiter.getHandler();
 	}
 
 	public long getInitDownloadTime() {
 		return tileWaiter.getInitDownloadTime();
 	}
 
 	public int getTilesDeleted() {
 		return tileWaiter.getTilesDeleted();
 	}
 
 	public int getTilesDownloaded() {
 		return tileWaiter.getTilesDownloaded();
 	}
 
 	public int getTilesFailed() {
 		return tileWaiter.getTilesFailed();
 	}
 
 	public int getTilesFromFS() {
 		return tileWaiter.getTilesFromFS();
 	}
 
 	public int getTilesNotFound() {
 		return tileWaiter.getTilesNotFound();
 	}
 
 	public int getTilesSkipped() {
 		return tileWaiter.getTilesSkipped();
 	}
 
 	public long getTotalTilesToProcess() {
 		return tileWaiter.getTotalTilesToProcess();
 	}
 
 	public long getTotalTime() {
 		return tileWaiter.getTotalTime();
 	}
 
 	public void onDownloadCanceled() {
 		tileWaiter.onDownloadCanceled();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Map.this.enableGPS();
 				Toast.makeText(Map.this, R.string.download_tiles_12,
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	public void onFailDownload(String URL) {
 		tileWaiter.onFailDownload(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onFatalError(String URL) {
 		tileWaiter.onFatalError(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onFinishDownload() {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Map.this.enableGPS();
 				isDownloadTilesFinished = true;
 				Map.this.downloadTilesButton
 						.setText(R.string.alert_dialog_text_ok);
 			}
 		});
 	}
 
 	public void onNewMessage(int ID, String message) {
 		switch (ID) {
 		case IContext.DOWNLOAD_CANCELED:
 			this.onDownloadCanceled();
 			break;
 		case IContext.FINISH_DOWNLOAD:
 			this.onFinishDownload();
 			break;
 		case IContext.START_DOWNLOAD:
 			this.onStartDownload();
 			break;
 		case IContext.TILE_DOWNLOADED:
 			this.onTileDownloaded(message);
 			break;
 		case IContext.TOTAL_TILES_COUNT:
 			this.onTotalNumTilesRetrieved(Integer.valueOf(message));
 			break;
 		}
 	}
 
 	private boolean isDownloadTilesFinished = false;
 
 	public void onStartDownload() {
 		this.osmap.getMTileProvider().clearPendingQueue();
 		this.disableGPS();
 		isDownloadTilesFinished = false;
 		this.previousTime = 0;
 		tileWaiter.onStartDownload();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Map.this.downloadTilesButton
 						.setText(R.string.alert_dialog_cancel);
 			}
 		});
 	}
 
 	public void onTileDeleted(String URL) {
 		tileWaiter.onTileDeleted(URL);
 	}
 
 	public void onTileDownloaded(String URL) {
 		tileWaiter.onTileDownloaded(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onTileLoadedFromFileSystem(String URL) {
 		tileWaiter.onTileLoadedFromFileSystem(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onTileNotFound(String URL) {
 		tileWaiter.onTileNotFound(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onTileSkipped(String URL) {
 		tileWaiter.onTileSkipped(URL);
 		this.updateDownloadTilesDialog();
 	}
 
 	public void onTotalNumTilesRetrieved(final long totalNumTiles) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Map.this.tileWaiter.onTotalNumTilesRetrieved(totalNumTiles);
 				Map.this.totalTiles.setText(Map.this
 						.getText(R.string.download_tiles_06)
 						+ " " + totalNumTiles);
 				double totalMB = totalNumTiles * 10 / 1024;
 				Map.this.totalMB.setText(Map.this
 						.getText(R.string.download_tiles_07)
 						+ " " + totalMB);
 			}
 		});
 	}
 
 	public void resetCounter() {
 		tileWaiter.resetCounter();
 		this.updateDownloadTilesDialog();
 	}
 
 	public void updateDataTransfer(int size) {
 		tileWaiter.updateDataTransfer(size);
 	}
 
 	private class TileDownloadWaiterDelegate extends TileDownloadWaiter {
 
 		public TileDownloadWaiterDelegate(IDownloadWaiter w) {
 			super(w);
 			// TODO Auto-generated constructor stub
 		}
 
 	}
 
 	public boolean isRendererAllowedToDownloadTiles(MapRenderer renderer) {
 		try {
 //			String name = renderer.getNAME();
 //			if (name.contains("OPEN STREET MAP") || name.contains("OSMARENDER") /*
 //																				 * ||
 //																				 * name
 //																				 * .
 //																				 * contains
 //																				 * (
 //																				 * "Cloudmade"
 //																				 * )
 //																				 * ||
 //																				 * name
 //																				 * .
 //																				 * contains
 //																				 * (
 //																				 * "Cloudmade Fresh"
 //																				 * )
 //																				 */
 //					|| name.contains("CYCLE MAP")) {
 //				myDownloadTiles.setEnabled(true);
 //				return true;
 //			}
 //			myDownloadTiles.setEnabled(false);
 //			return false;
 			//FIXME: only for debug
 			myDownloadTiles.setEnabled(true);
 			return true;
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 			return false;
 		}
 	}
 
 	public void showToast(int resId) {
 		try {
 			Message m = new Message();
 			m.what = Map.SHOW_TOAST;
 			m.obj = this.getText(resId);
 			this.getMapHandler().sendMessage(m);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		}
 	}
 
 	public void showOKDialog(String textBody, int title) {
 		try {
 			log.log(Level.FINE, "show ok dialog");
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			if (textBody.contains("<html")) {
 				try {
 					WebView wv = new WebView(this);
 					String html = textBody.substring(textBody.indexOf("<html"),
 							textBody.indexOf("html>") + 5);
 
 					wv.loadData(html, "text/html", "UTF-8");
 					alert.setView(wv);
 				} catch (Exception e) {
 					log.log(Level.SEVERE, "", e);
 					ListView l = new ListView(this);
 					l.setAdapter(new LongTextAdapter(this, textBody, true));
 					l.setClickable(false);
 					l.setLongClickable(false);
 					l.setFocusable(false);
 					alert.setView(l);
 				} catch (OutOfMemoryError oe) {
 					onLowMemory();
 					log.log(Level.SEVERE, "", oe);
 					showToast(R.string.MapLocation_3);
 				}
 
 			} else {
 				ListView l = new ListView(this);
 				l.setAdapter(new LongTextAdapter(this, textBody, true));
 				l.setClickable(false);
 				l.setLongClickable(false);
 				l.setFocusable(false);
 				alert.setView(l);
 			}
 
 			alert.setTitle(title);
 
 			alert.setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							try {
 
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "", e);
 							}
 						}
 					});
 
 			alert.show();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "", e);
 		} catch (OutOfMemoryError oe) {
 			onLowMemory();
 			log.log(Level.SEVERE, "", oe);
 			showToast(R.string.MapLocation_3);
 		}
 	}
 
 	@Override
 	public void onSettingChange(String key, Object value) {
 		try {
 			if (key.compareTo(getText(R.string.settings_key_gps).toString()) == 0) {
 				if (Boolean.valueOf(value.toString()).booleanValue()) {
 					this.enableGPS();
 				} else {
 					this.disableGPS();
 				}
 			} else if (key.compareTo(getText(R.string.settings_key_orientation)
 					.toString()) == 0) {
 				if (Boolean.valueOf(value.toString()).booleanValue()) {
 					this.initializeSensor(this);
 				} else {
 					this.stopSensor(this);
 				}
 			} else if (key.compareTo(getText(R.string.settings_key_tile_name)
 					.toString()) == 0) {
 				String current = osmap.getMTileProvider().getMFSTileProvider()
 						.getStrategy().getTileNameSuffix();
 
 				// tile suffix has changed
 				if (current.compareTo("." + value.toString()) != 0) {
 					osmap.instantiateTileProviderfromSettings();
 				}
 
 			} else if (key
 					.compareTo(getText(R.string.settings_key_offline_maps)
 							.toString()) == 0) {
 				if (Boolean.valueOf(value.toString()).booleanValue()) {
 					osmap.getMTileProvider().setMode(TileProvider.MODE_OFFLINE);
 				} else {
 					int mode = Settings.getInstance()
 							.getIntValue(
 									getText(R.string.settings_key_list_mode)
 											.toString());
 					osmap.getMTileProvider().setMode(mode);
 				}
 			} else if (key.compareTo(getText(R.string.settings_key_list_mode)
 					.toString()) == 0) {
 				if (Settings.getInstance().getBooleanValue(
 						getText(R.string.settings_key_offline_maps).toString())) {
 					osmap.getMTileProvider().setMode(TileProvider.MODE_OFFLINE);
 				} else {
 					osmap.getMTileProvider().setMode(
 							Integer.valueOf(value.toString()).intValue());
 				}
 			} else if (key.compareTo(getText(
 					R.string.settings_key_list_strategy).toString()) == 0) {
 				osmap.instantiateTileProviderfromSettings();
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "onSettingChange", e);
 		}
 	}
 
 	private void updateModeTileProvider() {
 
 	}
 }
