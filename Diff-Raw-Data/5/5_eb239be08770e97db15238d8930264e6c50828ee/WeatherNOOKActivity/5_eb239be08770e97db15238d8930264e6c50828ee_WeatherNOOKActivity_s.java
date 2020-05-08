 package org.spiralman.WeatherNOOK;
 
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jsharkey.sky.ForecastUtils;
 import org.jsharkey.sky.webservice.Forecast;
 import org.jsharkey.sky.webservice.WebserviceHelper;
 import org.json.JSONException;
 import org.spiralman.WeatherNOOK.Location.LocationRetrieval;
 import org.spiralman.WeatherNOOK.Location.ForecastLocation;
 import org.spiralman.WeatherNOOK.Location.ObservationStationDB;
 
 import com.example.android.actionbarcompat.ActionBarActivity;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class WeatherNOOKActivity extends ActionBarActivity {
 	private final int REFRESH_DIALOG = 0;
 	private final int CONFIGURE_DIALOG = 1;
 	private final int ERROR_DIALOG = 2;
 	
 	private final String LOCATION_JSON_KEY = "ForecastLocationJSON";
 	
 	private ProgressDialog m_progressDialog = null;
 	private AlertDialog m_configDialog = null;
 	
 	private ForecastLocation m_location = null;
 	
 	private String m_lastError = null;
 	
 	private String m_refreshMessage = null;
 	
 	private String m_locationFormat = null;
 	private String m_tempFormat = null;
 	private String m_windFormat = null;
 	private String m_humidityFormat = null;
 	private String m_updatedFormat = null;
 	
 	private String m_moreInfoText = null;
 	
 	private LocationRetrieval m_locationRetrieval = null;
 	private LocationInitializeThread m_locationInitThread = null;
 	
 	private RefreshThread m_refreshThread = null;
 	private Runnable m_onStationDBInitComplete = null;
 	
 	private WeatherReport m_report = null;
 	private boolean m_didRestoreReport = false;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Log.d("WeatherNOOK", "Create");
         
         ForecastBinder.prepareFormatStrings(this);
         WebserviceHelper.prepareUserAgent(this);
         
         loadLocation();
         
         m_locationFormat = getString(R.string.locationFormat);
         m_tempFormat = getString(R.string.currentTempFormat);
         m_windFormat = getString(R.string.windFormat);
         m_humidityFormat = getString(R.string.humidityFormat);
         m_updatedFormat = getString(R.string.updatedFormat);
         
         m_moreInfoText = getString(R.string.moreInfo);
         
         setContentView(R.layout.main);
         
         View current = getLayoutInflater().inflate(R.layout.current_conditions, null);
         ListView list = (ListView) findViewById(R.id.forecastList);
         list.addHeaderView(current);
         
         current.setVisibility(View.INVISIBLE);
         
         ObservationStationDB stationDB = new ObservationStationDB(WeatherNOOKActivity.this);
 		stationDB.open();
         
         Object configInstance = getLastNonConfigurationInstance();
         
         // TODO: This has gotten complex enough it should really be a state machine (along with onStart()).
         if( configInstance instanceof LocationInitializeThread ) {
         	Log.d("WeatherNOOK", "Restore with location init thread");
         	m_locationInitThread = (LocationInitializeThread) configInstance;
         	m_locationInitThread.setActivity(this);
         } else {
 	        if( !stationDB.isInitialized() ) {
 				m_locationInitThread = new LocationInitializeThread(this);
 				m_locationInitThread.execute(stationDB);
 			} else {
 				m_locationRetrieval =  new LocationRetrieval(stationDB);
 				
 				if( configInstance instanceof RefreshThread ) {
 					Log.d("WeatherNOOK", "Restore with refresh thread");
 					
 		        	m_refreshThread = (RefreshThread) configInstance;
 		        	m_refreshThread.setActivity(this);
 		        } else if( configInstance instanceof WeatherReport ) {
 		        	Log.d("WeatherNOOK", "Restore with report");
 		        	
 		        	m_report = (WeatherReport) configInstance;
 		        	m_didRestoreReport = true;
 		        }
 			}
         }
     }
     
     @Override
     public void onStart() {
     	super.onStart();
     	
     	Log.d("WeatherNOOK", "Start");
     	
     	if( m_locationRetrieval != null && m_location != null ) {
         	if( m_report != null ) {
         		Date now = new Date();
         		Date refreshAfter = m_report.getCurrentConditions().getRefreshAfter();
         		
         		if( !m_didRestoreReport && refreshAfter != null && now.after(refreshAfter) ) {
         			refresh();
         		} else {
         			displayReport();
         		}
         	} else if( m_refreshThread == null ) {
         		refresh();
         	}
     	}
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	Log.d("WeatherNOOK", "Destroy");
     	
     	if( m_locationRetrieval != null ) {
     		m_locationRetrieval.getObservationStationDB().close();
     	}
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
     	Log.d("WeatherNOOK", "Retain");
     	
     	if( m_refreshThread != null ) {
     		return m_refreshThread;
     	} else if( m_locationInitThread != null ) {
     		return m_locationInitThread;
     	} else {
     		return m_report;
     	}
     }
     
     private void refresh() {
     	if( m_location != null) {
     		m_didRestoreReport = false;
     		
     		m_refreshThread = new RefreshThread(this);
     		m_refreshThread.execute();
     	}
     }
     
     private void showConfigDialog() {
     	showDialog(CONFIGURE_DIALOG);
     }
     
     private void saveLocation() throws JSONException {
     	SharedPreferences settings = getPreferences(MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		editor.putString(LOCATION_JSON_KEY, m_location.toJSON());
         
         editor.commit();
     }
     
     private void loadLocation() {
     	SharedPreferences settings = getPreferences(MODE_PRIVATE);
     	
     	if( settings.contains(LOCATION_JSON_KEY) ) {
     		
     		try {
     			String locationJSON = settings.getString(LOCATION_JSON_KEY, "");
 	        
     			m_location = new ForecastLocation(locationJSON);
     		} catch(Exception e) {
     			promptException("Error loading saved location:", e);
     		}
     	} else {
     		m_location = null;
     	}
     }
     
     private void displayReport() {
     	CurrentConditions conditions = m_report.getCurrentConditions();
 		
 		List< Map<String, Forecast> > forecastMap = new ArrayList< Map<String,Forecast> >();
 		
 		boolean isDaytime = ForecastUtils.isDaytimeNow();
 		
 		for( Forecast forecast : m_report.getForecast() ) {
     		Map<String, Forecast> columnMap = new HashMap<String, Forecast>();
     		columnMap.put("Forecast", forecast);
     		forecastMap.add(columnMap);
     	}
 		
 		SimpleAdapter adapter = new SimpleAdapter(WeatherNOOKActivity.this, forecastMap, R.layout.forecast_entry, new String[] {"Forecast"}, new int[] {R.id.forecastLayout});
         adapter.setViewBinder(new ForecastBinder());
         
         ListView list = (ListView) findViewById(R.id.forecastList);
         list.setAdapter(adapter);
         
         ImageView conditionImage = (ImageView) findViewById(R.id.currentImage);
         TextView conditionLabel = (TextView) findViewById(R.id.currentCondition);
         TextView tempLabel = (TextView) findViewById(R.id.currentTemp);
         TextView windLabel = (TextView) findViewById(R.id.currentWind);
         TextView humidityLabel = (TextView) findViewById(R.id.currentHumidity);
         TextView moreInfoLabel = (TextView) findViewById(R.id.moreInformation);
         TextView updatedLabel = (TextView) findViewById(R.id.updated);
         
         setTitle(String.format(m_locationFormat, m_location.getShortName()));
         
         conditionImage.setImageResource(ForecastUtils.getIconForForecast(conditions.getConditions(), isDaytime));
         conditionLabel.setText(conditions.getConditions());
         moreInfoLabel.setText(Html.fromHtml(String.format("<a href=\"%1$s\">%2$s</a>", m_report.getUrl(), m_moreInfoText)));
         tempLabel.setText(String.format(m_tempFormat, Math.round(conditions.getTemperature())));
         windLabel.setText(String.format(m_windFormat, Math.round(conditions.getWindSpeed()), conditions.getWindDirAbbreviation()));
         
         if( conditions.getHumidity() >= 0 ) {
         	humidityLabel.setVisibility(View.VISIBLE);
         	humidityLabel.setText(String.format(m_humidityFormat, conditions.getHumidity()));
         } else {
         	humidityLabel.setVisibility(View.INVISIBLE);
         }
         
         moreInfoLabel.setMovementMethod(LinkMovementMethod.getInstance());
         updatedLabel.setText(String.format(m_updatedFormat, conditions.getObservationTime()));
         
         View current = findViewById(R.id.currentConditionLayout);
         current.setVisibility(View.VISIBLE);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
 	public boolean onOptionsItemSelected(MenuItem selected) {
 		switch(selected.getItemId()) {
 		case R.id.refresh:
 			Log.d("WeatherNOOK", "refresh");
 			refresh();
 			return true;
 		case R.id.configure:
 			Log.d("WeatherNOOK", "configure");
 			showConfigDialog();
 			return true;
 		default:
 			return super.onOptionsItemSelected(selected);
 		}
 	}
     
     private AlertDialog createConfigDialog() {
     	LayoutInflater inflater = LayoutInflater.from(this);
 		View content = inflater.inflate(R.layout.configure_layout, null);
 		
 		Button find = (Button) content.findViewById(R.id.locationSearch);
 		final EditText location = (EditText) content.findViewById(R.id.location);
 		final ListView resultsList = (ListView) content.findViewById(R.id.locationResults);
 		find.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				try {
 					String locationText = location.getText().toString();
 					if( !TextUtils.isEmpty(locationText))
 					{
 						List<ForecastLocation> locations = m_locationRetrieval.getLocations(locationText);
 						
 						resultsList.setAdapter(new ArrayAdapter<ForecastLocation>(WeatherNOOKActivity.this, android.R.layout.select_dialog_singlechoice, locations.toArray(new ForecastLocation[0])));
 						resultsList.setItemChecked(0, true);
 					}
 				} catch (Exception e) {
 					promptException("Error looking up location:", e);
 				}
 				
 			}
 		});
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose Location");
 		builder.setView(content);
 		builder.setCancelable(true);
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				try {
 					int selectedPos = resultsList.getCheckedItemPosition();
 					if( selectedPos != ListView.INVALID_POSITION ) {
 						m_location = (ForecastLocation) resultsList.getAdapter().getItem(selectedPos);
 						Log.d("WeatherNOOK", "Selected location: " + m_location.toString());
 						Log.d("WeatherNOOK", "Current Condition URL: " + m_location.getObservationStation().getUrl());
 						
 						saveLocation();
 						
 				        refresh();
 					}
 				} catch(Exception e) {
 					promptException("Error saving location:", e);
 				}
 			}
 		});
 		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
     	
     	return builder.create();
     }
     
     protected Dialog onCreateDialog(int id) {
         switch(id) {
         case REFRESH_DIALOG:
         	Log.d("WeatherNOOK", "Create refresh");
         	m_progressDialog = new ProgressDialog(WeatherNOOKActivity.this);
             m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             return m_progressDialog;
         case CONFIGURE_DIALOG:
         	m_configDialog = createConfigDialog();
         	return m_configDialog;
         case ERROR_DIALOG:
         	AlertDialog.Builder builder = new AlertDialog.Builder(WeatherNOOKActivity.this);
			builder.setMessage("Error: " + m_lastError)
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   dialog.cancel();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			return alert;
         default:
             return null;
         }
     }
     
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
     	super.onPrepareDialog(id, dialog);
     	
     	switch(id) {
     	case REFRESH_DIALOG:
     		Log.d("WeatherNOOK", "Prepare refresh");
     		m_progressDialog.setMessage(m_refreshMessage);
     		break;
     	case ERROR_DIALOG:
     		AlertDialog errorDialog = (AlertDialog) dialog;
    		errorDialog.setMessage("Error: " + m_lastError);
     		break;
     	}
     }
     
     public String getRefreshMessage() {
 		return m_refreshMessage;
 	}
 
 	public void setRefreshMessage(String refreshMessage) {
 		m_refreshMessage = refreshMessage;
 	}
 	
 	public void showRefreshDialog() {
 		showDialog(REFRESH_DIALOG);
 	}
 
 	private void hideRefreshDialog() {
     	if( m_progressDialog != null ) {
     		Log.d("WeatherNOOK", "Hiding refresh dialog");
     		
     		m_progressDialog.dismiss();
     	}
     }
     
     private void promptException(String message, Exception e) {
     	logException(e);
     	m_lastError = message + " " + e.toString();
 		showDialog(ERROR_DIALOG);
     }
     
     private void logException(Exception e) {
     	Log.d("WetherNOOK", "Exception: " + e.getMessage());
     	Log.d("WeatherNOOK", e.getClass().toString());
     	for( StackTraceElement element : e.getStackTrace()) {
     		Log.d("WeatherNOOK", "\tat " + element.toString());
     	}
     }
     
     private class LocationInitializeThread extends BlockingAsyncTask<ObservationStationDB, Void, LocationRetrieval> {
 		Exception m_exception = null;
 		
 		WeatherNOOKActivity m_currentActivity = null;
 		
 		public LocationInitializeThread(WeatherNOOKActivity activity) {
 			super(activity);
 			setRefreshMessage("Initializing Weather Station Database...");
 		}
 		
 		public LocationRetrieval doInBackground(ObservationStationDB... stationDBs) {
     		LocationRetrieval locationRetrieval = null;
     		ObservationStationDB stationDB = stationDBs[0];
     		
     		try
             {
 				AssetManager assets = getAssets();
         	
 				stationDB.importStations(new InputStreamReader(assets.open("noaa_weather_station_index.xml")));
     			
     			locationRetrieval = new LocationRetrieval(stationDB);
             }
             catch(Exception e)
             {
             	m_exception = e;
             }
     		
     		return locationRetrieval;
     	}
     	
 		@Override
     	public void notifyComplete(LocationRetrieval result) {
     		m_currentActivity.m_locationRetrieval = result;
     		
     		if( m_currentActivity.m_onStationDBInitComplete != null ) {
     			m_currentActivity.hideRefreshDialog();
     		}
     		
     		if( m_exception == null ) {
     			m_currentActivity.showConfigDialog();
 			} else {
 				m_currentActivity.promptException("Error initializing Weather Station Database:", m_exception);
 			}
     		
     		m_currentActivity.m_locationInitThread = null;
     	}
     }
     
     private class RefreshThread extends BlockingAsyncTask<Void, Void, WeatherReport> {
     	Exception m_exception = null;
     	
     	public RefreshThread(WeatherNOOKActivity activity) {
     		super(activity);
     		setRefreshMessage(String.format("Loading Forecast for %s...", activity.m_location.toString()));
     	}
     	
     	public WeatherReport doInBackground(Void... v) {
     		WeatherReport report = null;
     		try
             {
             	report = WeatherReport.getWeather(m_location);
             }
             catch(Exception e)
             {
             	m_exception = e;
             }
             
             return report;
     	}
     	
     	@Override
     	protected void notifyComplete(WeatherReport report) {
     		
 			m_currentActivity.hideRefreshDialog();
     		
     		if( m_exception == null ) {
     			m_currentActivity.m_report = report;
     			m_currentActivity.displayReport();
     		} else {
     			m_currentActivity.promptException("Error loading weather report:", m_exception);
     		}
     		
     		m_currentActivity.m_refreshThread = null;
     	}
     }
 }
