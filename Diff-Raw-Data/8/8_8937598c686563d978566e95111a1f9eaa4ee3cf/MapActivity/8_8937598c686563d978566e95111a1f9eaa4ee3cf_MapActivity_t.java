 package me.kevinwells.darxen;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import me.kevinwells.darxen.loaders.FindSite;
 import me.kevinwells.darxen.loaders.LoadRadar;
 import me.kevinwells.darxen.loaders.LoadShapefile;
 import me.kevinwells.darxen.loaders.LoadSites;
 import me.kevinwells.darxen.model.Color;
 import me.kevinwells.darxen.model.RadarData;
 import me.kevinwells.darxen.model.RadarDataModel;
 import me.kevinwells.darxen.model.RenderData;
 import me.kevinwells.darxen.model.RenderModel;
 import me.kevinwells.darxen.model.ShapefileConfig;
 import me.kevinwells.darxen.model.ShapefileId;
 import me.kevinwells.darxen.renderables.LinearShapefileRenderable;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 
 public class MapActivity extends SherlockFragmentActivity {
 	
 	private TextView mTitle;
 	private RadarView mRadarView;
 	private RenderModel mModel;
     private LocationManager locationManager;
     private LocationListener locationListener;
     
     private ImageView btnFirst;
     private ImageView btnPrevious;
     private ImageView btnPlay;
     private ImageView btnNext;
     private ImageView btnLast;
     
     private LatLon mPosition;
     private ArrayList<RadarSite> mRadarSites;
 	
     private RadarSite mRadarSite;
     private RadarDataModel mRadarData;
     
     private static final int TASK_LOAD_SITES = 0;
     private static final int TASK_FIND_SITE = 1;
     private static final int TASK_LOAD_RADAR = 2;
     private static final int TASK_LOAD_SHAPEFILES = 3;
     
     private List<ShapefileInfo> mShapefiles;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
         	//hide notification area
         	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
         setContentView(R.layout.main);
         
         mRadarView = new RadarView(this, getSupportLoaderManager());
         mModel = mRadarView.getModel();
         ((FrameLayout)findViewById(R.id.container)).addView(mRadarView);
         
         mTitle = (TextView)findViewById(R.id.title);
         
         btnFirst = (ImageView)findViewById(R.id.btnFirst);
         btnFirst.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btnFirst_clicked();
 			}
 		});
         
         btnPrevious = (ImageView)findViewById(R.id.btnPrevious);
         btnPrevious.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btnPrevious_clicked();
 			}
 		});
         
         btnPlay = (ImageView)findViewById(R.id.btnPlay);
         btnPlay.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btnPlay_clicked();
 			}
 		});
         
         btnNext = (ImageView)findViewById(R.id.btnNext);
         btnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btnNext_clicked();
 			}
 		});
         
         btnLast = (ImageView)findViewById(R.id.btnLast);
         btnLast.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btnLast_clicked();
 			}
 		});
         
         Prefs.unsetLastUpdateTime();
         
         loadSites();
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	getSupportMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		
 		boolean visible = mRadarData != null && mRadarData.getCount() > 1;
 		
 		if (visible) {
 			boolean hasPrevious = mRadarData.hasPrevious();
 			btnPrevious.setEnabled(hasPrevious);
 			btnFirst.setEnabled(hasPrevious);
 			
 			boolean hasNext = mRadarData.hasNext();
 			btnNext.setEnabled(hasNext);
 			btnLast.setEnabled(hasNext);
 			
 			btnPlay.setImageResource(mRadarData.isAnimating() ? R.drawable.action_pause : R.drawable.action_play);
 		}
 		
 		findViewById(R.id.animation).setVisibility(visible ? View.VISIBLE : View.GONE);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			update();
 			break;
 		default:
 			return false;
 		}
 		return true;
 	}
 	
 	private void btnFirst_clicked() {
 		mRadarData.moveFirst();
 	}
 	
 	private void btnPrevious_clicked() {
 		mRadarData.movePrevious();
 	}
 	
 	private void btnPlay_clicked() {
 		if (mRadarData.isAnimating()) {
 			mRadarData.stopAnimation();
 		} else {
 			mRadarData.startAnimation();
 		}
 		invalidateOptionsMenu();
 	}
 	
 	private void btnNext_clicked() {
 		mRadarData.moveNext();
 	}
 	
 	private void btnLast_clicked() {
 		mRadarData.moveLast();
 	}
 	
 	private void update() {
 		if (mRadarSite == null)
 			return;
 		
 		if (!DataPolicy.shouldUpdate(Prefs.getLastUpdateTime(), new Date())) {
 			Toast.makeText(this, "Patience, my young Padawan", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		setSupportProgressBarIndeterminateVisibility(true);
 		
 		reloadRadar();
 	}
 	
 	private void updateLocation(Location location) {
 		if (location == null)
 			return;
 		
 		boolean initSite = mPosition == null;
 		Log.v(C.TAG, location.toString());
 		mPosition = new LatLon(location.getLatitude(), location.getLongitude());
 		
 		if (initSite)
 			initSite();
		
		mRadarView.setLocation(mPosition);
 	}
 
     protected void onResume() {
     	super.onResume();
     	
         locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         locationListener = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) {
 				updateLocation(location);
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				new AlertDialog.Builder(MapActivity.this)
 	        	.setTitle(R.string.location_services_title)
 	        	.setMessage(R.string.location_services_message)
 	        	.setCancelable(true)
 	        	.setOnCancelListener(new DialogInterface.OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						finish();
 					}
 				})
 	        	.setPositiveButton(R.string.do_it, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						startActivity(intent);
 					}
 				})
 				.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				}).create().show();
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {}
 
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				if (status != LocationProvider.AVAILABLE) {
 					//TEMPORARILY_UNAVAILABLE
 					//AVAILABLE
 					//mRadarView.setLocation(null);
 				}
 			}
 		};
 
         Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         updateLocation(location);
 		
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
         //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
     	
     	mRadarView.onResume();
     }
     
     protected void onPause() {
     	super.onPause();
     	
     	locationManager.removeUpdates(locationListener);
     	mRadarView.setLocation(null);
     	
     	mRadarView.onPause();
     }
     
     private void initSite() {
     	if (mRadarSites == null || mPosition == null)
     		return;
     	
     	findSite();
     }
     
     private void loadSites() {
     	getSupportLoaderManager().initLoader(TASK_LOAD_SITES, null, mTaskLoadSitesCallbacks);
     }
 
     private LoaderManager.LoaderCallbacks<ArrayList<RadarSite>> mTaskLoadSitesCallbacks =
     		new LoaderManager.LoaderCallbacks<ArrayList<RadarSite>>() {
 		@Override
 		public Loader<ArrayList<RadarSite>> onCreateLoader(int id, Bundle args) {
 			return LoadSites.createInstance(MapActivity.this);
 		}
 		@Override
 		public void onLoadFinished(Loader<ArrayList<RadarSite>> loader, ArrayList<RadarSite> radarSites) {
 			MapActivity.this.mRadarSites = radarSites;
 			initSite();
 		}
 		@Override
 		public void onLoaderReset(Loader<ArrayList<RadarSite>> loader) {
 			MapActivity.this.mRadarSites = null;
 		}
     };
     
 	private void findSite() {
 		Bundle args = FindSite.bundleArgs(mRadarSites, mPosition);
 		getSupportLoaderManager().initLoader(TASK_FIND_SITE, args, mTaskFindSiteCallbacks);
 	}
 	
     private LoaderManager.LoaderCallbacks<RadarSite> mTaskFindSiteCallbacks =
     		new LoaderManager.LoaderCallbacks<RadarSite>() {
 		@Override
 		public Loader<RadarSite> onCreateLoader(int id, Bundle args) {
 			return FindSite.createInstance(MapActivity.this, args);
 		}
 		@Override
 		public void onLoadFinished(Loader<RadarSite> loader, RadarSite radarSite) {
 			mRadarSite = radarSite;
 			RenderData data = mModel.getWritable();
 			data.setCenter(mRadarSite.center);
 			mModel.commit();
 			
 			loadRadar();
 	        loadShapefiles(radarSite.center);
	        
	        if (mPosition != null)
	        	mRadarView.setLocation(mPosition);
 		}
 		@Override
 		public void onLoaderReset(Loader<RadarSite> loader) {
 			mRadarSite = null;
 		}
     };
 
 	private void loadRadar() {
 		if (mRadarData == null) {
 			mRadarData = new RadarDataModel();
 			mRadarData.setCallbacks(mRadarModelListener);
 		}
 		
 		Bundle args = LoadRadar.bundleArgs(mRadarSite, mRadarData);
 		getSupportLoaderManager().initLoader(TASK_LOAD_RADAR, args, mTaskLoadRadarCallbacks);
 	}
 	
 	private void reloadRadar() {
 		if (mRadarData == null) {
 			mRadarData = new RadarDataModel();
 			mRadarData.setCallbacks(mRadarModelListener);
 		}
 		
 		Bundle args = LoadRadar.bundleArgs(mRadarSite, mRadarData);
 		getSupportLoaderManager().restartLoader(TASK_LOAD_RADAR, args, mTaskLoadRadarCallbacks);
 		
 		Prefs.setLastUpdateTime(new Date());
 	}
 	
     private LoaderManager.LoaderCallbacks<RadarDataModel> mTaskLoadRadarCallbacks =
     		new LoaderManager.LoaderCallbacks<RadarDataModel>() {
 		@Override
 		public Loader<RadarDataModel> onCreateLoader(int id, Bundle args) {
 			return LoadRadar.createInstance(MapActivity.this, args);
 		}
 		@Override
 		public void onLoadFinished(Loader<RadarDataModel> loader, RadarDataModel data) {
 			if (data == null) {
 				finish();
 				return;
 			}
 			
 			if (data != mRadarData) {
 				mRadarData = data;
 				data.setCallbacks(mRadarModelListener);
 			}
 			
 			updateCurrentFrame();
 			setSupportProgressBarIndeterminateVisibility(false);
 		}
 		@Override
 		public void onLoaderReset(Loader<RadarDataModel> loader) {
 			mRadarView.setDataFile(null);
 		}
     };
     
     private void updateCurrentFrame() {
 		RadarData data = mRadarData.getCurrentData();
 		
 		mTitle.setText(new String(data.getDataFile().header).replace("\n", ""));
 		mRadarView.setDataFile(data);
 		
 		invalidateOptionsMenu();
     }
     
     private RadarDataModel.RadarDataModelListener mRadarModelListener = new RadarDataModel.RadarDataModelListener() {
 		@Override
 		public void onUpdated() {
 			invalidateOptionsMenu();
 		}
 		@Override
 		public void onCurrentChanged(long time) {
 			updateCurrentFrame();
 		}
 	};
     
     private void loadShapefiles(LatLon center) {
     	if (mShapefiles != null) {
     		for (ShapefileInfo info : mShapefiles) {
     			RenderData data = mModel.getWritable();
     			data.removeUnderlay(info.mUnderlay);
     			data.removeOverlay(info.mOverlay);
     			mModel.commit();
 			}
     	}
 	
     	mShapefiles = new ArrayList<ShapefileInfo>();
     	{
         	//states
     		ShapefileConfig config = new ShapefileConfig(R.raw.states_shp, R.raw.states_dbf, R.raw.states_shx, ShapefileId.STATE_LINES);
     		ShapefileRenderConfig underRenderConfig = new ShapefileRenderConfig(new Color(1.0f, 1.0f, 1.0f), 3.0f, GL10.GL_LINE_STRIP);
     		ShapefileRenderConfig overRenderConfig = new ShapefileRenderConfig(new Color(1.0f, 1.0f, 1.0f, 0.4f), 3.0f, GL10.GL_LINE_STRIP);
     		mShapefiles.add(new ShapefileInfo(config, underRenderConfig, overRenderConfig));
     	}
     	
     	//froyo can't read resources >1MB, like county lines
     	if (Build.VERSION.SDK_INT > 8)
     	{
         	//counties
     		ShapefileConfig config = new ShapefileConfig(R.raw.counties_shp, R.raw.counties_dbf, R.raw.counties_shx, ShapefileId.COUNTY_LINES);
     		ShapefileRenderConfig underRenderConfig = new ShapefileRenderConfig(new Color(0.75f, 0.75f, 0.75f), 1.0f, GL10.GL_LINE_STRIP);
     		ShapefileRenderConfig overRenderConfig = new ShapefileRenderConfig(new Color(0.75f, 0.75f, 0.75f, 0.4f), 1.0f, GL10.GL_LINE_STRIP);
     		mShapefiles.add(new ShapefileInfo(config, underRenderConfig, overRenderConfig));
     	}
     	
     	for (int i = 0; i < mShapefiles.size(); i++) {
     		ShapefileInfo info = mShapefiles.get(i);
     		
     		ShapefileRenderData model = new ShapefileRenderData();
     		
     		LinearShapefileRenderable underlay = new LinearShapefileRenderable(model, info.mUnderlayRenderConfig);
     		LinearShapefileRenderable overlay = new LinearShapefileRenderable(model, info.mOverlayRenderConfig);
 
 			RenderData data = mModel.getWritable();
 			data.addUnderlay(underlay);
 			data.addOverlay(overlay);
 			mModel.commit();
     		info.mUnderlay = underlay;
     		info.mOverlay = overlay;
     		
 			loadShapefile(i, center, info.mConfig, model);
 		}
     }
     
     private void reloadShapefiles(LatLon viewpoint) {
     	for (int i = 0; i < mShapefiles.size(); i++) {
 			int index = TASK_LOAD_SHAPEFILES + i;
 			LoadShapefile loader = LoadShapefile.getInstance(getSupportLoaderManager(), index);
 
 			loader.setViewPoint(viewpoint);
 			loader.startLoading();
 		}	
     }
     
     private void loadShapefile(int index, LatLon center, ShapefileConfig config, ShapefileRenderData data) {
     	Bundle args = LoadShapefile.bundleArgs(center, config, data);
     	int id = TASK_LOAD_SHAPEFILES + index;
     	LoadShapefile loader;
     	loader = LoadShapefile.castInstance(getSupportLoaderManager().initLoader(id, args, mTaskLoadShapefilesCallbacks));
     	loader.setUpdateThrottle(100);
     	
     	mRadarView.setViewpointListener(new ViewpointListener() {
 			@Override
 			public void onViewpointChanged(LatLon viewpoint) {
 				reloadShapefiles(viewpoint);
 			}
 		});    	
     }
     
 	private LoaderManager.LoaderCallbacks<ShapefileRenderData> mTaskLoadShapefilesCallbacks =
     		new LoaderManager.LoaderCallbacks<ShapefileRenderData>() {
 		@Override
 		public Loader<ShapefileRenderData> onCreateLoader(int id, Bundle args) {
 			return LoadShapefile.createInstance(MapActivity.this, args);
 		}
 		@Override
 		public void onLoadFinished(Loader<ShapefileRenderData> loader, ShapefileRenderData data) {
 			int index = loader.getId() - TASK_LOAD_SHAPEFILES;
 			ShapefileInfo info = mShapefiles.get(index);
 
 			if (info.mUnderlay != null) {
 				info.mUnderlay.setData(data);
 			}
 			if (info.mOverlay != null) {
 				info.mOverlay.setData(data);
 			}
 		}
 		@Override
 		public void onLoaderReset(Loader<ShapefileRenderData> loader) {
 			int index = loader.getId() - TASK_LOAD_SHAPEFILES;
 			ShapefileInfo info = mShapefiles.get(index);
 
 			RenderData data = mModel.getWritable();
 			data.removeUnderlay(info.mUnderlay);
 			data.removeOverlay(info.mOverlay);
 			mModel.commit();
 		}
     };
 
     private class ShapefileInfo
     {
     	public ShapefileConfig mConfig;
     	public ShapefileRenderConfig mUnderlayRenderConfig;
     	public ShapefileRenderConfig mOverlayRenderConfig;
     	public LinearShapefileRenderable mUnderlay;
     	public LinearShapefileRenderable mOverlay;
 		
     	public ShapefileInfo(ShapefileConfig config, ShapefileRenderConfig underRenderConfig, ShapefileRenderConfig overRenderConfig) {
 			this.mConfig = config;
 			this.mUnderlayRenderConfig = underRenderConfig;
 			this.mOverlayRenderConfig = overRenderConfig;
 		}
     }
 }
