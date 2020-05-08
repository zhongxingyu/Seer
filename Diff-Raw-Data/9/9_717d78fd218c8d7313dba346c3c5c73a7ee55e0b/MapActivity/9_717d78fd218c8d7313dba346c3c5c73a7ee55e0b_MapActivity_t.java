 package me.kevinwells.darxen;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import me.kevinwells.darxen.data.DataFile;
 import me.kevinwells.darxen.data.Level3Parser;
 import me.kevinwells.darxen.data.ParseException;
 import me.kevinwells.darxen.shp.DbfFile;
 import me.kevinwells.darxen.shp.DbfFile.DbfRecord;
 import me.kevinwells.darxen.shp.Shapefile;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 
 public class MapActivity extends SherlockActivity {
 	
 	private TextView mTitle;
 	private RadarView mRadarView;
 	
     private LocationManager locationManager;
     private LocationListener locationListener;
     
     private LatLon mPosition;
     private List<RadarSite> mRadarSites;
 	
     private boolean mLayersLoaded;
     
     private RadarSite mRadarSite;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main);
 
         mRadarView = new RadarView(this);
         ((FrameLayout)findViewById(R.id.container)).addView(mRadarView);
         
         mTitle = (TextView)findViewById(R.id.title);
         
         Prefs.unsetLastUpdateTime();
 
         //TODO load cached site from shared prefs
         new LoadSites().execute();
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	getSupportMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
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
 	
 	private void update() {
 		if (mRadarSite == null)
 			return;
 		
 		if (!DataPolicy.shouldUpdate(Prefs.getLastUpdateTime(), new Date())) {
 			Toast.makeText(this, "Patience, my young Padawan", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		setSupportProgressBarIndeterminateVisibility(true);
 		
         new LoadRadar().execute();
 	}
 	
 	private void updateLocation(Location location) {
 		if (location == null)
 			return;
 		
 		boolean initSite = mPosition == null;
 		Log.v(C.TAG, location.toString());
 		mPosition = new LatLon(location.getLatitude(), location.getLongitude());
 		mRadarView.setLocation(mPosition);
 		
 		if (initSite)
 			initSite();
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
     	
     	new FindSite().execute();
     }
     
     private class LoadSites extends AsyncTask<Void, Void, Void> {
 
     	private List<RadarSite> mRadarSites;
     	
 		@Override
 		protected Void doInBackground(Void... params) {
 			
 			mRadarSites = new ArrayList<RadarSite>();
 			
 			InputStream fin = getResources().openRawResource(R.raw.radars_dbf);
 			DbfFile sites = new DbfFile(fin);
 			for (DbfRecord site : sites) {
 				String name = site.getString(0).toUpperCase();
 				double lat = site.getDouble(1);
 				double lon = site.getDouble(2);
 				
 				mRadarSites.add(new RadarSite(name, new LatLon(lat, lon)));
 			}
 			try {
 				sites.close();
 				fin.close();
 			} catch (IOException e) {}
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void res) {
 			MapActivity.this.mRadarSites = mRadarSites;
 			initSite();
 		}
     }
     
     private class FindSite extends AsyncTask<Void, Void, RadarSite> {
 
 		@Override
 		protected RadarSite doInBackground(Void... params) {
 			double[] distances = new double[mRadarSites.size()];
 			
 			for (int i = 0; i < mRadarSites.size(); i++)
 				distances[i] = mPosition.distanceTo(mRadarSites.get(i).center);
 			
 			double minValue = distances[0];
 			int minIndex = 0;
 			for (int i = 1; i < distances.length; i++) {
 				if (distances[i] < minValue) {
 					minValue = distances[i];
 					minIndex = i;
 				}
 			}
 			
 			return mRadarSites.get(minIndex);
 		}
 		
 		@Override
 		protected void onPostExecute(RadarSite radarSite) {
 			mRadarSite = radarSite;
 			update();
 		}
     }
     
     private class LoadRadar extends AsyncTask<Void, Void, DataFile> {
 
 		@Override
 		protected DataFile doInBackground(Void... params) {
 			byte[] data = null;
 			do {
 		        try {
 		        	data = getData(mRadarSite);
 				} catch (IOException e) {
 					Log.e(C.TAG, "Failed to download radar imagery", e);
 				}
 			} while (data == null);
 	        
 	        Level3Parser parser = new Level3Parser();
 	        DataFile file;
 	        try {
 				file = parser.parse(new ByteArrayInputStream(data));
 			} catch (ParseException e) {
 				Log.e(C.TAG, "Failed to parse radar imagery", e);
 				return null;
 			} catch (IOException e) {
 				Log.e(C.TAG, "Failed to parse radar imagery", e);
 				return null;
 			}
 	        
 	        return file;
 		}
 		
 		@Override
 		protected void onPostExecute(DataFile data) {
 			if (data == null) {
 				finish();
 				return;
 			}
 
 	        mTitle.setText(new String(data.header).replace("\n", ""));
 			
 	        if (!mLayersLoaded) {
 	        	new LoadShapefiles(new LatLon(data.description.lat, data.description.lon)).execute();
 	        }
 			
 			mRadarView.setData(data);
 			
 			if (mLayersLoaded) {
 				setSupportProgressBarIndeterminateVisibility(false);
 			}
 			
 			Prefs.setLastUpdateTime(new Date());
 		}
 
 		private byte[] getData(RadarSite radarSite) throws SocketException, IOException {
 	    	ByteArrayOutputStream fout = new ByteArrayOutputStream();
 	        
 	    	FTPClient ftpClient = new FTPClient();
 			ftpClient.connect("tgftp.nws.noaa.gov", 21);
 			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
 				throw new IOException("Failed to connect");
 			ftpClient.login("anonymous", "darxen");
 			
 			ftpClient.changeWorkingDirectory("SL.us008001/DF.of/DC.radar/DS.p19r0/SI." + radarSite.name.toLowerCase());
 			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 			ftpClient.enterLocalPassiveMode();
 			ftpClient.retrieveFile("sn.last", fout);
 			fout.close();
 			ftpClient.disconnect();
 			
 	        return fout.toByteArray();
 	    }
     }
     
 	private class LoadShapefiles extends AsyncTask<Void, Void, Void> {
 		private LatLon mCenter;
 	
 		public LoadShapefiles(LatLon center) {
 			mCenter = center;
 		}
 
 		private ShapefileLayer loadShapefile(ShapefileConfig config) {
 			
 			InputStream fShp = getResources().openRawResource(config.resShp);
 			InputStream fShx = getResources().openRawResource(config.resShx);
 			
 			Shapefile shapefile = new Shapefile(fShp, fShx);
 			try {
 				return new ShapefileLayer(mCenter, shapefile, config);
 			} finally {
 				shapefile.close();
 				
 				try {
 					fShp.close();
 				} catch (IOException e) {}
 
 				try {
 					fShx.close();
 				} catch (IOException e) {}
 			}
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			ShapefileLayer layer;
 			
 			ShapefileConfig[] configs = new ShapefileConfig[] {
 					new ShapefileConfig(R.raw.states_shp, R.raw.states_dbf, R.raw.states_shx,
 							3.0f, new Color(1.0f, 1.0f, 1.0f)),
 					new ShapefileConfig(R.raw.counties_shp, R.raw.counties_dbf, R.raw.counties_shx,
 							1.0f, new Color(0.75f, 0.75f, 0.75f))
 			};
 			
 			for (ShapefileConfig config : configs) {
 				layer = loadShapefile(config);
 				if (layer == null)
 					continue;
 				
 				mRadarView.addLayer(layer);
 			}
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void res) {
 			mLayersLoaded = true;
 
 			setSupportProgressBarIndeterminateVisibility(false);
 		}
 
 	}
 }
