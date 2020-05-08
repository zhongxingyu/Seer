 package com.mbcdev.nextluas.activities;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.mbcdev.nextluas.R;
 import com.mbcdev.nextluas.constants.StopConstants;
 import com.mbcdev.nextluas.location.CriteriaHolder;
 import com.mbcdev.nextluas.model.ResultModel;
 import com.mbcdev.nextluas.model.ResultModel.StopTime;
 import com.mbcdev.nextluas.model.StopInformationModel;
 import com.mbcdev.nextluas.net.LocalTranscodeSiteConnector;
 import com.mbcdev.nextluas.net.LuasInfoConnector;
 import com.mbcdev.nextluas.prefs.MultiSelectListPreference;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import butterknife.InjectView;
 import butterknife.Views;
 
 
 public class NextLuasActivity extends Activity implements OnItemSelectedListener, LocationListener, ActionBar.OnNavigationListener {
 
     protected static final int UPDATE_MS = 15000;
     protected static final int UPDATE_DISTANCE = 10;
     private static final int RED_LINE_NAV_INDEX     = 0;
     private static final int GREEN_LINE_NAV_INDEX   = 1;
 
     private ConnectivityManager mConnectivityManager;
     private LuasInfoConnector   mLuasConnector;
     private LocationManager     mLocationManager;
     private ResultModel         mStopModel;
     private Location            mCurrentLocation;
 
     final Handler mLookupHandler = new Handler();
 
     private ArrayAdapter<StopInformationModel> mRedAdapter;
     private ArrayAdapter<StopInformationModel> mGreenAdapter;
 
     private SharedPreferences mSharedPreferences;
 
     @InjectView(R.id.stopSpinner)
     Spinner stopSpinner;
 
     @InjectView(R.id.txtLastUpdated)
     TextView txtLastUpdated;
 
     @InjectView(R.id.tblInbound)
     TableLayout inboundTable;
 
     @InjectView(R.id.tblOutbound)
     TableLayout outboundTable;
 
     @InjectView(R.id.txtProblem)
     TextView txtProblem;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.luas_ads);
 
         Views.inject(this);
 
         getActionBar().setDisplayShowTitleEnabled(false);
         getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         SpinnerAdapter navigationSpinnerAdapter = ArrayAdapter.createFromResource(
                 getActionBar().getThemedContext(),
                 R.array.lineNamesArray,
                 android.R.layout.simple_spinner_dropdown_item);
 
         getActionBar().setListNavigationCallbacks(navigationSpinnerAdapter, this);
 
         mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         mLuasConnector = new LocalTranscodeSiteConnector();
 
         mRedAdapter = fromStopModel(StopConstants.getRedStops());
         mGreenAdapter = fromStopModel(StopConstants.getGreenStops());
 
         String defaultLine = mSharedPreferences.getString(getString(R.string.prefDefaultLineKey), getString(R.string.redLine));
 
         int navIndexToSet = RED_LINE_NAV_INDEX;
 
         if (defaultLine.equals(getString(R.string.greenLine))) {
             navIndexToSet = GREEN_LINE_NAV_INDEX;
         }
 
         getActionBar().setSelectedNavigationItem(navIndexToSet);
 
         mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
         stopSpinner.setOnItemSelectedListener(this);
 
         txtProblem.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(NextLuasActivity.this, ProblemActivity.class));
             }
         });
 
         // Work-around for http://code.google.com/p/android/issues/detail?id=7786
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
             System.setProperty("http.keepAlive", "false");
         }
 
         stopSpinner.setAdapter(getCurrentAdapter());
         handleFilter();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         String provider = mLocationManager.getBestProvider(CriteriaHolder.FINE_CRITERIA, true);
 
         if (provider != null) {
             mLocationManager.requestLocationUpdates(provider, UPDATE_MS, UPDATE_DISTANCE, this);
         }
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mLocationManager.removeUpdates(this);
     }
 
     private void handleFilter() {
 
         boolean lineFilterEnabled = isLineFilteringEnabled();
 
         // We want to reset the adapters in any case
         mRedAdapter = fromStopModel(StopConstants.getRedStops());
         mGreenAdapter = fromStopModel(StopConstants.getGreenStops());
 
         ArrayAdapter<StopInformationModel> currentAdapter = getCurrentAdapter();
 
         if (lineFilterEnabled) {
             String[] filterList = null;
 
             if (redLine()) {
                 filterList = mSharedPreferences.getString(getString(R.string.prefStopListRedKey), "").split(MultiSelectListPreference.SEPARATOR);
             } else if (greenLine()) {
                 filterList = mSharedPreferences.getString(getString(R.string.prefStopListGreenKey), "").split(MultiSelectListPreference.SEPARATOR);
             }
 
             if (filterList.length == 0 || (filterList.length == 1 && filterList[0].equals(""))) {
                 //Ln.d("Filter enabled but no stops selected, defaulting to all");
             } else {
                 List<StopInformationModel> filteredStops = new ArrayList<StopInformationModel>();
 
 
 
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
 
     private boolean isLineFilteringEnabled() {
         return mSharedPreferences.getBoolean(getString(R.string.prefFilterEnableKey), false);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.mainmenu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()){
             case R.id.menuIdHelp:
                 launchHelp();
                 return true;
             case R.id.menuIdSettings:
                 startActivityForResult(new Intent(this, EditPreferenceActivity.class), 8080);
                 return true;
             case R.id.menuRefresh:
                 handleRefreshButton();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == 8080) {
             handleFilter();
         }
 
     }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 
         if (!networkAvailable()) {
             Toast.makeText(this, getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
             return;
         }
 
         StopInformationModel model = (StopInformationModel) parent.getItemAtPosition(pos);
         lookupStopInfo(model);
     }
 
     @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
 
     private void lookupStopInfo(final StopInformationModel model) {
 
         txtLastUpdated.setText(getText(R.string.updating));
 
         Thread t = new Thread() {
             public void run() {
                 try {
                     mStopModel = mLuasConnector.getStopInfo(model.getSuffix(), model.getDisplayName());
 
                     mLookupHandler.post(new Runnable() {
                         public void run() {
                             updateUI();
                         }
                     });
 
                 } catch (IOException e) {
                     mLookupHandler.post(new ErrorRunnable(e));
                 }
             }
         };
         t.start();
     }
 
     protected void updateUI() {
 
         if (mStopModel == null || (mStopModel.getInbound().isEmpty() && mStopModel.getOutbound().isEmpty())) {
             makeInfoDialogue("There was a problem getting the times, perhaps the times on luas.ie are missing?");
             resetUI();
             return;
         }
 
         txtLastUpdated.setText(String.format(getString(R.string.lastUpdate), mStopModel.getFormattedLastUpdated()));
 
         inboundTable.removeAllViews();
         addTimesToTable(inboundTable, mStopModel.getInbound());
 
         outboundTable.removeAllViews();
         addTimesToTable(outboundTable, mStopModel.getOutbound());
     }
 
     private boolean networkAvailable() {
         return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                 || mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
     }
 
     private void resetUI() {
         inboundTable.removeAllViews();
         outboundTable.removeAllViews();
         txtLastUpdated.setText("");
     }
 
     private void handleRefreshButton() {
         StopInformationModel model = (StopInformationModel) stopSpinner.getSelectedItem();
         lookupStopInfo(model);
     }
 
     private void addTimesToTable(TableLayout layout, List<StopTime> stops) {
 
         int[] fontSizes = new int[] {
                 (int)getResources().getDimension(R.dimen.lt_text_large),
                 (int)getResources().getDimension(R.dimen.lt_text_medium),
                 (int)getResources().getDimension(R.dimen.lt_text_small),
                 (int)getResources().getDimension(R.dimen.lt_text_micro)
         };
 
         int stopNumber = 0;
 
         for (StopTime st : stops) {
 
             TableRow tr = new TableRow(this);
             tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 
             LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
             TextView name = new TextView(this);
             name.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
             name.setLayoutParams(lp);
             name.setText(st.getName());
             name.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizes[stopNumber]);
 
             TextView minutes = new TextView(this);
             minutes.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
             minutes.setGravity(Gravity.RIGHT);
             minutes.setLayoutParams(lp);
             minutes.setText(st.getMinutes());
             minutes.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizes[stopNumber]);
 
             tr.addView(name);
             tr.addView(minutes);
 
             layout.addView(tr, lp);
 
             if (stopNumber != fontSizes.length - 1) {
                 stopNumber++;
             }
         }
 
         layout.setColumnStretchable(0, true);
         layout.setColumnShrinkable(1, true);
 
     }
 
 
     @Override
     public void onLocationChanged(Location location) {
         this.mCurrentLocation = location;
         updateStopsWithLocation();
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
         mSharedPreferences.edit().putString(getString(R.string.prefDefaultLineKey), navIndexToLabel()).commit();
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
 
         if (this.mCurrentLocation == null) {
             return;
         }
 
         updateAdapterWithLocation(getCurrentAdapter());
     }
 
     private void updateAdapterWithLocation(ArrayAdapter<StopInformationModel> adapter) {
 
         for (int i = 0; i < adapter.getCount(); i++) {
             StopInformationModel model = adapter.getItem(i);
 
             if (model.getLocation() != null) {
                 float[] results = new float[3];
                 Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), model.getLocation()
                         .getLatitude(), model.getLocation().getLongitude(), results);
 
                 model.setDistanceFromCurrent(results[0] / 1000);
             }
         }
     }
 
     private boolean redLine() {
         return RED_LINE_NAV_INDEX == getActionBar().getSelectedNavigationIndex();
     }
 
     private boolean greenLine() {
         return GREEN_LINE_NAV_INDEX == getActionBar().getSelectedNavigationIndex();
     }
 
     private ArrayAdapter<StopInformationModel> getCurrentAdapter() {
         return redLine() ? mRedAdapter : mGreenAdapter;
     }
 
     @Override
     public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 
         int oldNavIndex = labelToNavIndex();
 
         if (oldNavIndex != itemPosition) {
             resetUI();
             stopSpinner.setAdapter(getCurrentAdapter());
         }
 
         return true;
     }
 
     private String navIndexToLabel() {
 
         String label = getString(R.string.redLine);
 
         if (RED_LINE_NAV_INDEX == getActionBar().getSelectedNavigationIndex()) {
             label = getString(R.string.redLine);
         } else if (GREEN_LINE_NAV_INDEX == getActionBar().getSelectedNavigationIndex()){
             label = getString(R.string.greenLine);
         }
 
         return label;
     }
 
     private int labelToNavIndex() {
 
         int index = RED_LINE_NAV_INDEX;
 
         String currentLabel = mSharedPreferences.getString(getString(R.string.prefDefaultLineKey), getString(R.string.redLine));
 
         if (getString(R.string.redLine).equals(currentLabel)) {
             index = RED_LINE_NAV_INDEX;
         } else if (getString(R.string.greenLine).equals(currentLabel)) {
             index = GREEN_LINE_NAV_INDEX;
         }
 
         return index;
     }
 
     protected void makeInfoDialogue(String error) {
       AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
       alertDialog.setTitle(R.string.connectorDialogTitle);
 
       alertDialog.setMessage(error);
 
       alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
         @Override
         public void onClick(DialogInterface dialog, int which) {
           // OK!
         }
       });
 
       try {
           alertDialog.show();
       } catch (WindowManager.BadTokenException e) {
           //Ln.e("We wanted to show a dialogue but the activity went away.");
       }
 
     }
 
     private class ErrorRunnable implements Runnable {
 
         private String cause;
 
         public ErrorRunnable(Exception e) {
             cause = e.getMessage();
 
             if (cause == null) {
                 cause = "";
             }
 
         }
 
         @Override
         public void run() {
             resetUI();
             makeInfoDialogue("Sorry, there was a network problem looking up the stop. This might be solved by trying again.\n\n" + cause);
         }
 
     }
 }
