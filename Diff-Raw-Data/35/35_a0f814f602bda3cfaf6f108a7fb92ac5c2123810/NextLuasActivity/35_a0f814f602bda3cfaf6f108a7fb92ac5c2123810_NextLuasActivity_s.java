 package com.mbcdev.nextluas.activities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 import roboguice.util.Ln;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.method.LinkMovementMethod;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.inject.Inject;
 import com.mbcdev.nextluas.R;
 import com.mbcdev.nextluas.constants.StopConstants;
 import com.mbcdev.nextluas.location.CriteriaHolder;
 import com.mbcdev.nextluas.model.ResultModel;
 import com.mbcdev.nextluas.model.ResultModel.StopTime;
 import com.mbcdev.nextluas.model.StopInformationModel;
 import com.mbcdev.nextluas.net.LuasInfoConnector;
 import com.mbcdev.nextluas.prefs.MultiSelectListPreference;
 import com.mbcdev.nextluas.sorting.DistanceComparator;
 import com.mbcdev.nextluas.sorting.StopIndexComparator;
 
 public class NextLuasActivity extends AbstractActivity implements OnItemSelectedListener, LocationListener {
 
   @Inject private ConnectivityManager cm;
   @Inject private LuasInfoConnector luasConnector;
   @Inject private LocationManager lm;
   
   private ResultModel stopModel;

  private String currentStop;
  private String currentSuffix;
   private Location currentLocation;
   
   final Handler lookupHandler = new Handler();
  
   private ArrayAdapter<StopInformationModel> redAdapter;
   private ArrayAdapter<StopInformationModel> greenAdapter;
   private String currentMapKey;
 
   private SharedPreferences prefs;
 
   @InjectView(R.id.disclaimer) private TextView disclaimer;
   
   @InjectView(R.id.stopSpinner) private Spinner stopSpinner;
 
   @InjectView(R.id.txtLastUpdated) private TextView txtLastUpdated;
   @InjectView(R.id.txtStopLineInfo) private TextView txtInfo;
   
   @InjectView(R.id.tblInbound) private TableLayout inboundTable;
   @InjectView(R.id.tblOutbound) private TableLayout outboundTable;
   
   @InjectResource(R.string.redLine) String redLine;
   @InjectResource(R.string.greenLine) String greenLine;
   @InjectResource(R.string.prefDefaultLineKey) String defaultLineKey;
   @InjectResource(R.string.prefFilterEnableKey) String filterEnabledKey;
   @InjectResource(R.string.prefDefaultClosestKey) String sortByLocationKey;
   @InjectResource(R.string.lastUpdate) String lastUpdatedAt;
   
   @InjectResource(R.string.prefStopListRedKey) String redFilterKey;
   @InjectResource(R.string.prefStopListGreenKey) String greenFilterKey;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.luas_ads);
 
     prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
     redAdapter = fromStopModel(StopConstants.getRedStops());
     greenAdapter = fromStopModel(StopConstants.getGreenStops());
     currentMapKey = prefs.getString(defaultLineKey, redLine);
 
     currentLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
     disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
 
     stopSpinner.setOnItemSelectedListener(this);
     
     setupSpinner();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     
     String provider = lm.getBestProvider(CriteriaHolder.FINE_CRITERIA, true);
     
     if (provider != null) {
       lm.requestLocationUpdates(provider, UPDATE_MS, UPDATE_DISTANCE, this);
     }
     
     handleFilter();
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     lm.removeUpdates(this);
   }
 
   private void handleFilter() {
 
     boolean filterEnabled = prefs.getBoolean(filterEnabledKey, false);
     ArrayAdapter<StopInformationModel> currentAdapter = getCurrentAdapter();
     
     // We want to reset the adapters in any case
     redAdapter = fromStopModel(StopConstants.getRedStops());
     greenAdapter = fromStopModel(StopConstants.getGreenStops());
     
     if (filterEnabled) {
       String[] filterList = null;
       
       if (redLine()) {
         filterList = prefs.getString(redFilterKey, "").split(MultiSelectListPreference.SEPARATOR);
       } else if (greenLine()) {
         filterList = prefs.getString(greenFilterKey, "").split(MultiSelectListPreference.SEPARATOR);
       }
       
       if (filterList.length == 0 || (filterList.length == 1 && filterList[0].equals(""))) {
         Ln.d("Filter enabled but no stops selected, defaulting to all");
       } else {
         List<StopInformationModel> filteredStops = new ArrayList<StopInformationModel>();
 
         currentAdapter = getCurrentAdapter();
 
         for (String stopName : filterList) {
           for (int i = 0; i < currentAdapter.getCount(); i++) {
             if (stopName.equals(currentAdapter.getItem(i).getDisplayName())) {
               filteredStops.add(currentAdapter.getItem(i));
             }
           }
         } 
         currentAdapter = fromStopModel(filteredStops);
       }
     }
     
     stopSpinner.setAdapter(currentAdapter);
     stopSpinner.setSelection(0);
   }
 
   private void handleSorting() {
     @SuppressWarnings("unchecked")
     ArrayAdapter<StopInformationModel> adapter = (ArrayAdapter<StopInformationModel>) stopSpinner.getAdapter();
 
     boolean sortByProximity = prefs.getBoolean(sortByLocationKey, false);
     
     if (sortByProximity) {
       adapter.sort(new DistanceComparator());
     } else {
       adapter.sort(new StopIndexComparator());
     }
 
     adapter.notifyDataSetChanged();
    stopSpinner.setSelection(0);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getSupportMenuInflater();
     inflater.inflate(R.menu.mainmenu, menu);
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     
   switch (item.getItemId()) {
     case R.id.menuIdChangeLine:
       handleLineSwitch();
       return true;
     case R.id.menuIdHelp:
       launchHelp();
       return true;
     case R.id.menuIdSettings:
       startActivity(new Intent(this, EditPreferenceActivity.class));
       return true;
     case R.id.menuRefresh:
       handleRefreshButton();
       return true;
     default:
       return super.onOptionsItemSelected(item);
     }
     
   }
 
   /**
    * On update of spinner
    */
   @Override
   public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 
     if (!networkAvailable()) {
       Toast.makeText(this, getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
       return;
     }
 
     StopInformationModel model = (StopInformationModel) parent.getItemAtPosition(pos);

    currentStop = model.getDisplayName();
    currentSuffix = model.getSuffix();

    lookupStopInfo(currentSuffix, currentStop);
   }
 
   @Override
   public void onNothingSelected(AdapterView<?> arg0) {}
 
  private void lookupStopInfo(final String stopSuffix, final String stopName) {
     
     txtLastUpdated.setText(getText(R.string.updating));
 
     Thread t = new Thread() {
       public void run() {
         try {
          stopModel = luasConnector.getStopInfo(stopSuffix, stopName);
           lookupHandler.post(lookupRunnable);
         } catch (IOException e) {
           lookupHandler.post(errorRunnable);
         }
       }
     };
     t.start();
   }
 
   
   
   /**
    * Update the UI with results from web call
    */
   protected void updateUI() {
     if (stopModel == null) {
       makeInfoDialogue("Results model was null.");
       return;
     }
   
     txtInfo.setText(currentMapKey);
     txtInfo.setTextColor(redLine() ? Color.RED : Color.GREEN);
 
     txtLastUpdated.setText(String.format(lastUpdatedAt, stopModel.getFormattedLastUpdated()));
 
     inboundTable.removeAllViews();
     addTimesToTable(inboundTable, stopModel.getInbound());
     
     outboundTable.removeAllViews();
     addTimesToTable(outboundTable, stopModel.getOutbound());
   }
 
   private boolean networkAvailable() {
     return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
         || cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
   }
 
   private void setupSpinner() {
     ArrayAdapter<StopInformationModel> adapter = getCurrentAdapter();
     stopSpinner.setAdapter(adapter);
 
     updateStopsWithLocation();
     handleSorting();
     handleFilter();
   }
 
   private void resetUI() {
     inboundTable.removeAllViews();
     outboundTable.removeAllViews();
     txtLastUpdated.setText("");
     txtInfo.setText("");
   }
 
   private void handleLineSwitch() {
     resetUI();
     
     if (redLine()) {
       this.currentMapKey = greenLine;
     } else if (greenLine()) {
       this.currentMapKey = redLine;
     } else {
       this.currentMapKey = redLine;
     }
     
     setupSpinner();
     
   }
 
   private void handleRefreshButton() {
    lookupStopInfo(currentSuffix, currentStop);
   }
 
   private void addTimesToTable(TableLayout layout, List<StopTime> stops) {
 
     int LARGE_FONT = 18;
 
     for (StopTime st : stops) {
       TableRow tr = new TableRow(this);
       tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 
       LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
       TextView name = new TextView(this);
       name.setLayoutParams(lp);
       name.setText(st.getName());
       name.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE_FONT);
 
       TextView minutes = new TextView(this);
       minutes.setGravity(Gravity.RIGHT);
       minutes.setLayoutParams(lp);
       minutes.setText(st.getMinutes());
       minutes.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE_FONT);
 
       tr.addView(name);
       tr.addView(minutes);
 
       layout.addView(tr, lp);
     }
 
     layout.setColumnStretchable(0, true);
     layout.setColumnShrinkable(1, true);
 
   }
 
   // Create runnable for posting
   final Runnable lookupRunnable = new Runnable() {
     public void run() {
       updateUI();
     }
   };
   
   final Runnable errorRunnable = new Runnable() {
     public void run() {
       resetUI();
       Toast.makeText(NextLuasActivity.this, "Sorry, there was a network problem looking up the stop.", Toast.LENGTH_LONG).show();
     };
   };
 
   @Override
   public void onLocationChanged(Location location) {
     this.currentLocation = location;
     updateStopsWithLocation();
     handleSorting();
   }
 
   @Override
   public void onProviderDisabled(String provider) {}
 
   @Override
   public void onProviderEnabled(String provider) {}
 
   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) {}
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     
     prefs.edit().putString(defaultLineKey, currentMapKey).commit();
   }
 
   private ArrayAdapter<StopInformationModel> fromStopModel(List<StopInformationModel> stopList) {
     ArrayAdapter<StopInformationModel> adapter = new ArrayAdapter<StopInformationModel>(this, android.R.layout.simple_spinner_item);
 
     for (StopInformationModel model : stopList) {
       adapter.add(model);
     }
 
     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     return adapter;
   }
 
   private void launchHelp() {
     Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mbcdev.com/luas-times-help/"));
     startActivity(i);
   }
 
   private void updateStopsWithLocation() {
 
     if (this.currentLocation == null || currentMapKey == null) {
       return;
     }
 
     updateAdapterWithLocation(getCurrentAdapter());
   }
 
   private void updateAdapterWithLocation(ArrayAdapter<StopInformationModel> adapter) {
 
     for (int i = 0; i < adapter.getCount(); i++) {
       StopInformationModel model = adapter.getItem(i);
 
       if (model.getLocation() != null) {
         float[] results = new float[3];
         Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), model.getLocation()
             .getLatitude(), model.getLocation().getLongitude(), results);
 
         model.setDistanceFromCurrent(results[0] / 1000);
       }
     }
   }
 
   private boolean redLine () {
     return currentMapKey != null && redLine.equals(currentMapKey);
   }
   
   private boolean greenLine () {
     return currentMapKey != null && greenLine.equals(currentMapKey);
   }
   
   private ArrayAdapter<StopInformationModel> getCurrentAdapter() {
     return redLine() ? redAdapter : greenAdapter;
   }
 }
