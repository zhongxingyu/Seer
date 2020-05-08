 package uk.org.smithfamily.mslogger.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.ExtGPSManager;
 import uk.org.smithfamily.mslogger.GPSLocationManager;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.comms.Connection;
 import uk.org.smithfamily.mslogger.comms.ConnectionFactory;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.EmailManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import uk.org.smithfamily.mslogger.widgets.BarGraph;
 import uk.org.smithfamily.mslogger.widgets.Gauge;
 import uk.org.smithfamily.mslogger.widgets.GaugeDetails;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 import uk.org.smithfamily.mslogger.widgets.GroupIndicator;
 import uk.org.smithfamily.mslogger.widgets.Histogram;
 import uk.org.smithfamily.mslogger.widgets.Indicator;
 import uk.org.smithfamily.mslogger.widgets.IndicatorManager;
 import uk.org.smithfamily.mslogger.widgets.NumericIndicator;
 import uk.org.smithfamily.mslogger.widgets.TableIndicator;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main activity class where the main window (gauges) are and where the bottom menu is handled
  */
 public class MSLoggerActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener
 {
     private BroadcastReceiver updateReceiver = new Reciever();
     private IndicatorManager indicatorManager;
     private TextView messages;
     private TextView rps;
     private static Boolean ready = null;
 
     private Indicator[] indicators = new Indicator[5];
 
     private boolean gaugeEditEnabled;
     private LinearLayout layout;
     private static final int SHOW_PREFS = 124230;
 
     private boolean registered;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         Connection conn = ConnectionFactory.INSTANCE.getConnection();
 
         // Bluetooth is not supported on this Android device
         if (!conn.connectionPossible())
         {
             finishDialogNoBluetooth();
             return;
         }
         checkSDCard();
 
         setGaugesOrientation();
 
         DebugLogManager.INSTANCE.log(getPackageName(), Log.DEBUG);
         try
         {
             String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
             DebugLogManager.INSTANCE.log(app_ver, Log.ASSERT);
         }
         catch (NameNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
         dumpPreferences();
         setContentView(R.layout.displaygauge);
 
         messages = (TextView) findViewById(R.id.messages);
         rps = (TextView) findViewById(R.id.RPS);
 
         /*
          * Get status message from saved instance, for example when switching from landscape to portrait mode
          */
         if (savedInstanceState != null)
         {
             if (!savedInstanceState.getString("status_message").equals(""))
             {
                 messages.setText(savedInstanceState.getString("status_message"));
             }
 
             if (!savedInstanceState.getString("rps_message").equals(""))
             {
                 rps.setText(savedInstanceState.getString("rps_message"));
             }
 
             if (savedInstanceState.getBoolean("gauge_edit"))
             {
                 gaugeEditEnabled = true;
             }
         }
 
         findGauges();
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         prefsManager.registerOnSharedPreferenceChangeListener(MSLoggerActivity.this);
 
         indicatorManager = IndicatorManager.INSTANCE;
         indicatorManager.setDisabled(true);
 
         ApplicationSettings.INSTANCE.setDefaultAdapter(BluetoothAdapter.getDefaultAdapter());
         GPSLocationManager.INSTANCE.start();
         ApplicationSettings.INSTANCE.setAutoConnectOverride(null);
 
         registerMessages();
        if(ApplicationSettings.INSTANCE.getEcuDefinition() != null)
        {
            initGauges();
        }
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         setGaugesOrientation();
     }
 
     /**
      * Force a screen orientation if specified by the user, otherwise look at the sensor and rotate screen as needed
      */
     private void setGaugesOrientation()
     {
         String activityOrientation = ApplicationSettings.INSTANCE.getGaugesOrientation();
 
         // Force landscape
         if (activityOrientation.equals("FORCE_LANDSCAPE"))
         {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
         // Force portrait
         else if (activityOrientation.equals("FORCE_PORTRAIT"))
         {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         }
         else
         {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
         }
     }
 
     
     /**
      * Save the bottom text views content so they can keep their state while device is rotated
      */
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
 
         outState.putString("status_message", messages.getText().toString());
         outState.putString("rps_message", rps.getText().toString());
         outState.putBoolean("gauge_edit", gaugeEditEnabled);
     }
 
     /**
      * Dump the user preference into the log file for easier debugging
      */
     private void dumpPreferences()
     {
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         Map<String, ?> prefs = prefsManager.getAll();
         for (Entry<String, ?> entry : prefs.entrySet())
         {
             DebugLogManager.INSTANCE.log("Preference:" + entry.getKey() + ":" + entry.getValue(), Log.ASSERT);
         }
     }
 
     /**
      * Complete the initialisation and load/init the indicators
      */
     private void completeCreate()
     {
         if (ready == null)
         {
             new InitTask().execute((Void) null);
         }
         else
         {
             finaliseInit();
         }
     }
 
     /**
      * Finilise the initialisation of the application by initialising the gauges, checking bluetooth, SD card and starting connection to the
      * Megasquirt
      */
     private void finaliseInit()
     {
         initGauges();
 
         checkBTDeviceSet();
         checkSDCard();
     }
 
     /**
      * Check if the SD card is present
      */
     private void checkSDCard()
     {
         boolean cardOK = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
         ApplicationSettings.INSTANCE.setWritable(cardOK);
         if (!cardOK)
         {
             showDialog(2);
         }
     }
 
     /**
      * Clean up messages and GPS when the app is stopped
      */
     @Override
     protected void onDestroy()
     {
         deRegisterMessages();
         GPSLocationManager.INSTANCE.stop();
         super.onDestroy();
     }
 
     /**
      * Save the indicators when the app is stopped
      */
     @Override
     public void onStop()
     {
         super.onStop();
         saveGauges();
     }
 
     /**
      * Ask to select a Bluetooth device is none is selected
      */
     private void checkBTDeviceSet()
     {
         if (!ApplicationSettings.INSTANCE.btDeviceSelected())
         {
             messages.setText(R.string.please_select);
         }
     }
 
     /**
      * Save current selected indicators in preferences
      */
     private void saveGauges()
     {
         if (!(indicators[0] != null && indicators[1] != null && indicators[2] != null && indicators[3] != null))
         {
             findGauges();
         }
         if (indicators[0] != null && indicators[1] != null && indicators[2] != null && indicators[3] != null)
         {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             Editor editor = prefs.edit();
 
             if (!indicators[0].getName().equals(Indicator.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge1", indicators[0].getName());
             }
             if (!indicators[1].getName().equals(Indicator.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge2", indicators[1].getName());
             }
             if (!indicators[2].getName().equals(Indicator.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge3", indicators[2].getName());
             }
             if (!indicators[3].getName().equals(Indicator.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge4", indicators[3].getName());
             }
             if (indicators[4] != null && !indicators[4].getName().equals(Indicator.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge5", indicators[4].getName());
             }
 
             editor.commit();
         }
     }
 
     /**
      * Load the gauges
      */
     private void loadGauges()
     {
         long start = System.currentTimeMillis();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         GaugeRegister.INSTANCE.flush();
         ecu.initGauges();
         DebugLogManager.INSTANCE.log("loadGauges() took "+(System.currentTimeMillis()-start)+"ms",Log.DEBUG);
     }
 
     /**
      * Init the indicators with the proper one saved in preferences, default to firmware defined indicators if preferences are empty
      */
     private void initGauges()
     {
         layout = (LinearLayout) (findViewById(R.id.layout));
         findGauges();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         String[] defaultGauges = ecu.defaultGauges();
        
         indicators[0].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge1", defaultGauges[0]));
         indicators[1].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge2", defaultGauges[1]));
         indicators[2].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge3", defaultGauges[2]));
         indicators[3].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge4", defaultGauges[3]));
 
         if (indicators[4] != null)
         {
             indicators[4].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge5", defaultGauges[4]));
         }
 
         applyWidgetTypeToIndicators();
 
         if (gaugeEditEnabled)
         {
             bindIndicatorsEditEvents();
         }
         else
         {
             MarkListener l = new MarkListener(layout);
             setTouchListeners(l);
         }
 
         for (int i = 0; i < indicators.length; i++)
         {
             if (indicators[i] != null)
             {
                 indicators[i].invalidate();
             }
         }
     }
 
     /**
      * Scan all indicators and make sure they are the type they should be
      */
     public void applyWidgetTypeToIndicators()
     {
         // Look at all indicators and make sure they are the right type
         for (int i = 0; i < indicators.length; i++)
         {
             if (indicators[i] != null)
             {
                 boolean wasWrongType = false;
                 String name = indicators[i].getName();
                 int id = indicators[i].getId();
 
                 GaugeDetails gd = GaugeRegister.INSTANCE.getGaugeDetails(name);
 
                 if (gd.getType().equals(getString(R.string.gauge)) && !(indicators[i] instanceof Gauge))
                 {
                     indicators[i] = new Gauge(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.bargraph)) && !(indicators[i] instanceof BarGraph))
                 {
                     indicators[i] = new BarGraph(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.numeric_indicator)) && !(indicators[i] instanceof NumericIndicator))
                 {
                     indicators[i] = new NumericIndicator(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.histogram)) && !(indicators[i] instanceof Histogram))
                 {
                     indicators[i] = new Histogram(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.table_indicator)) && !(indicators[i] instanceof TableIndicator))
                 {
                     indicators[i] = new TableIndicator(this);
                     wasWrongType = true;
                 }
 
                 if (wasWrongType)
                 {
                     View indicator = findViewById(id);
 
                     // Remove indicator with wrong type
                     ViewGroup parentView = (ViewGroup) indicator.getParent();
                     int index = parentView.indexOfChild(indicator);
                     parentView.removeView(indicator);
 
                     // Add new indicator back in place
                     parentView.addView(indicators[i], index);
 
                     indicators[i].setId(id);
                     indicators[i].initFromName(name);
 
                     LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
                     indicators[i].setLayoutParams(params);
 
                     indicators[i].setFocusable(true);
                     indicators[i].setFocusableInTouchMode(true);
 
                     bindIndicatorsEditEventsToIndex(i);
                 }
             }
         }
     }
 
     /**
      * Replace the instance of an indicator, used in EditGaugeDialog, after modifying indicator details
      * 
      * @param indicator
      * @param indicatorIndex
      */
     public void replaceIndicator(Indicator indicator, int indicatorIndex)
     {
         indicators[indicatorIndex] = indicator;
     }
 
     /**
      * Bind touch listener to indicator
      * 
      * @param i Indicator index to bind edit events to
      */
     public void bindIndicatorsEditEventsToIndex(int i)
     {
         indicators[i].setGestureDetector(new GestureDetector(new IndicatorGestureListener(MSLoggerActivity.this, indicators[i], i)));
 
         OnTouchListener gestureListener = new View.OnTouchListener()
         {
             private Indicator firstIndicator;
 
             /**
              * Determines if given points are inside view
              * 
              * @param x X coordinate of point
              * @param y Y coordinate of point
              * @param view View object to compare
              * @return true If the points are within view bounds, false otherwise
              */
             private boolean isPointInsideView(float x, float y, View view)
             {
                 int location[] = new int[2];
                 view.getLocationOnScreen(location);
                 int viewX = location[0];
                 int viewY = location[1];
 
                 // Point is inside view bounds
                 if (x > viewX && x < viewX + view.getWidth() && y > viewY && y < viewY + view.getHeight())
                 {
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
 
             /**
              * 
              * @param v
              * @param event
              */
             public boolean onTouch(View v, MotionEvent event)
             {
                 if (firstIndicator != null && firstIndicator.getGestureDetector().onTouchEvent(event))
                 {
                     return true;
                 }
                 else if (event.getAction() == MotionEvent.ACTION_DOWN)
                 {
                     this.firstIndicator = ((Indicator) v);
                     return true;
                 }
                 else if (event.getAction() == MotionEvent.ACTION_UP)
                 {
                     Indicator lastIndicator = null;
                     int lastIndexIndicator = 0;
 
                     // Find indicator when the finger was lifted
                     for (int i = 0; lastIndicator == null && i < indicators.length && indicators[i] != null; i++)
                     {
                         if (this.isPointInsideView(event.getRawX(), event.getRawY(), indicators[i]))
                         {
                             lastIndicator = indicators[i];
                             lastIndexIndicator = i;
                         }
                     }
 
                     if (lastIndicator != null && firstIndicator.getId() != lastIndicator.getId())
                     {
                         String firstIndicatorName = firstIndicator.getName();
                         String lastIndicatorName = lastIndicator.getName();
 
                         int firstIndexIndicator = 0;
 
                         // Find first touched indicator index
                         for (int i = 0; i < indicators.length; i++)
                         {
                             if (indicators[i] != null && firstIndicator.getId() == indicators[i].getId())
                             {
                                 firstIndexIndicator = i;
                             }
                         }
 
                         // Remove old last indicator
                         ViewGroup parentLastIndicatorView = (ViewGroup) lastIndicator.getParent();
                         int indexLast = parentLastIndicatorView.indexOfChild(lastIndicator);
                         parentLastIndicatorView.removeView(lastIndicator);
 
                         // Remove old first indicator
                         ViewGroup parentFirstIndicatorView = (ViewGroup) firstIndicator.getParent();
                         int indexFirst = parentFirstIndicatorView.indexOfChild(firstIndicator);
                         parentFirstIndicatorView.removeView(firstIndicator);
 
                         /*
                          * Since we are removing both view at the same time, if both were in the same parent, we need to do some magic to keep them in
                          * the right order.
                          */
                         if (parentFirstIndicatorView == parentLastIndicatorView)
                         {
                             if (indexLast > indexFirst)
                             {
                                 indexLast -= 1;
                             }
                             else
                             {
                                 indexFirst += 1;
                             }
                         }
 
                         // Add first touched indicator in place of last touched indicator
                         parentLastIndicatorView.addView(indicators[lastIndexIndicator], indexLast);
                         parentLastIndicatorView.forceLayout();
 
                         // Add last touched indicator in place of first touched indicator
                         parentFirstIndicatorView.addView(indicators[firstIndexIndicator], indexFirst);
                         parentFirstIndicatorView.forceLayout();
 
                         // Init the indicator with their new indicator details
                         indicators[lastIndexIndicator].initFromName(firstIndicatorName);
                         indicators[firstIndexIndicator].initFromName(lastIndicatorName);
 
                         indicators[lastIndexIndicator].setId(lastIndicator.getId());
                         indicators[firstIndexIndicator].setId(firstIndicator.getId());
 
                         // If indicators weren't the same type, we have to change their types
                         if (!firstIndicator.getType().equals(lastIndicator.getType()))
                         {
                             applyWidgetTypeToIndicators();
                         }
 
                         // Re-map the right indicators with the right views
                         findGauges();
 
                         return true;
                     }
                 }
 
                 return false;
             }
         };
 
         indicators[i].setOnClickListener(MSLoggerActivity.this);
         indicators[i].setOnTouchListener(gestureListener);
     }
 
     /**
      * 
      */
     public void bindIndicatorsEditEvents()
     {
         for (int i = 0; i < indicators.length; i++)
         {
             if (indicators[i] != null)
             {
                 bindIndicatorsEditEventsToIndex(i);
             }
         }
     }
 
     /**
      * Set all the gauges variable with their view
      */
     private void findGauges()
     {
         indicators[0] = (Indicator) findViewById(R.id.g1);
         indicators[1] = (Indicator) findViewById(R.id.g2);
         indicators[2] = (Indicator) findViewById(R.id.g3);
         indicators[3] = (Indicator) findViewById(R.id.g4);
         indicators[4] = (Indicator) findViewById(R.id.g5);
     }
 
     /**
      * Set the touch listener on the gauge
      * 
      * @param l
      */
     private void setTouchListeners(MarkListener l)
     {
         for (int i = 0; i < indicators.length; i++)
         {
             if (indicators[i] != null)
             {
                 indicators[i].setOnTouchListener(l);
             }
         }
     }
 
     /**
      * Register the receiver with the message to receive from the Megasquirt connection
      */
     private void registerMessages()
     {
         IntentFilter connectedFilter = new IntentFilter(Megasquirt.CONNECTED);
         registerReceiver(updateReceiver, connectedFilter);
 
         IntentFilter disconnectedFilter = new IntentFilter(Megasquirt.DISCONNECTED);
         registerReceiver(updateReceiver, disconnectedFilter);
 
         IntentFilter dataFilter = new IntentFilter(Megasquirt.NEW_DATA);
         registerReceiver(updateReceiver, dataFilter);
 
         IntentFilter msgFilter = new IntentFilter(ApplicationSettings.GENERAL_MESSAGE);
         registerReceiver(updateReceiver, msgFilter);
 
         IntentFilter rpsFilter = new IntentFilter(ApplicationSettings.RPS_MESSAGE);
         registerReceiver(updateReceiver, rpsFilter);
 
         IntentFilter toastFilter = new IntentFilter(ApplicationSettings.TOAST);
         registerReceiver(updateReceiver, toastFilter);
 
         IntentFilter unknownEcuFilter = new IntentFilter(Megasquirt.UNKNOWN_ECU);
         registerReceiver(updateReceiver, unknownEcuFilter);
 
         IntentFilter unknownEcuBTFilter = new IntentFilter(Megasquirt.UNKNOWN_ECU_BT);
         registerReceiver(updateReceiver, unknownEcuBTFilter);
 
         IntentFilter probeEcuFilter = new IntentFilter(Megasquirt.PROBE_ECU);
         registerReceiver(updateReceiver, probeEcuFilter);
 
         
         registered = true;
     }
 
     /**
      * Unregister receiver
      */
     private void deRegisterMessages()
     {
         if (registered)
         {
             unregisterReceiver(updateReceiver);
         }
     }
 
     /**
      * Process data got from Megasquirt and update the gauges with it
      */
     protected void processData()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         List<Indicator> indicators;
         if ((indicators = indicatorManager.getIndicators()) != null)
         {
             indicatorManager.setDisabled(false);
             for (Indicator i : indicators)
             {
                 if (i instanceof GroupIndicator)
                 {
                     GroupIndicator gi = (GroupIndicator) i;
                     for (String gaugeName : gi.getGaugeNames())
                     {
                         if (gaugeName != null)
                         {
                             GaugeDetails gd = GaugeRegister.INSTANCE.getGaugeDetails(gaugeName);
                             if (gd != null)
                             {
                                 double value = ecu.getField(gd.getChannel());
                                 gi.setValue(value, gaugeName);
                             }
                             
                         }
                         else
                         {
                             gi.setValue(0, gaugeName);
                         }
                     }
                 }
                 else
                 {
                     String channelName = i.getChannel();
                     if (channelName != null)
                     {
                         double value = ecu.getField(channelName);
                         i.setValue(value);
                     }
                     else
                     {
                         i.setValue(0);
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * @param menu
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     /**
      * Triggered just before the bottom menu are displayed, used to update the state of some menu items
      * 
      * @param menu
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem editItem = menu.findItem(R.id.gaugeEditing);
         Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         editItem.setEnabled(ecuDefinition != null);
         if (gaugeEditEnabled)
         {
             editItem.setIcon(R.drawable.ic_menu_disable_gauge_editing);
             editItem.setTitle(R.string.disable_gauge_edit);
         }
         else
         {
             editItem.setIcon(R.drawable.ic_menu_enable_gauge_editing);
             editItem.setTitle(R.string.enable_gauge_edit);
         }
         MenuItem connectionItem = menu.findItem(R.id.forceConnection);
         if (ecuDefinition != null && ecuDefinition.isConnected())
         {
             connectionItem.setIcon(R.drawable.ic_menu_disconnect);
             connectionItem.setTitle(R.string.disconnect);
         }
         else
         {
             connectionItem.setIcon(R.drawable.ic_menu_connect);
             connectionItem.setTitle(R.string.connect);
         }
 
         MenuItem loggingItem = menu.findItem(R.id.forceLogging);
         loggingItem.setEnabled(ecuDefinition != null);
 
         if (ecuDefinition != null && ecuDefinition.isLogging())
         {
             loggingItem.setIcon(R.drawable.ic_menu_stop_logging);
             loggingItem.setTitle(R.string.stop_logging);
         }
         else
         {
             loggingItem.setIcon(R.drawable.ic_menu_start_logging);
             loggingItem.setTitle(R.string.start_logging);
         }
 
         MenuItem tuningItem = menu.findItem(R.id.tuning);
         tuningItem.setEnabled(ecuDefinition != null);
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     /**
      * Triggered when a bottom menu item is clicked
      * 
      * @param item The clicked menu item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         checkBTDeviceSet();
         int itemId = item.getItemId();
         switch (itemId)
         {
 
         case R.id.forceConnection:
             toggleConnection();
             return true;
         case R.id.gaugeEditing:
             toggleEditing();
             return true;
         case R.id.forceLogging:
             toggleLogging();
             return true;
         case R.id.manageDatalogs:
             openManageDatalogs();
             return true;
         case R.id.tuning:
             openTuning();
             return true;
         case R.id.preferences:
             openPreferences();
             return true;
         case R.id.resetGauges:
             resetGuages();
             return true;
         case R.id.calibrate:
             openCalibrateTPS();
             return true;
         case R.id.about:
             showAbout();
             return true;
         case R.id.quit:
             quit();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Start the background task to reset the gauges
      */
     public void resetGuages()
     {
         new ResetGaugesTask().execute((Void) null);
     }
 
     /**
      * Toggle the gauge editing
      */
     private void toggleEditing()
     {
         // Gauge editing is enabled
         if (gaugeEditEnabled)
         {
             saveGauges();
         }
         // Gauge editing is not enabled
         else
         {
             for (int i = 0; i < 2; i++)
             {
                 Toast.makeText(getApplicationContext(), R.string.edit_gauges_instructions, Toast.LENGTH_LONG).show();
             }
         }
 
         gaugeEditEnabled = !gaugeEditEnabled;
         initGauges();
     }
 
     /**
      * Toggle data logging of the Megasquirt
      */
     private void toggleLogging()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null && ecu.isConnected())
         {
             if (ecu.isLogging())
             {
                 ecu.stopLogging();
                 sendLogs();
             }
             else
             {
                 ecu.startLogging();
             }
         }
     }
 
     /**
      * If the user opted in to get logs sent to his email, we prompt him
      */
     private void sendLogs()
     {
         if (ApplicationSettings.INSTANCE.emailEnabled())
         {
             DatalogManager.INSTANCE.close();
             FRDLogManager.INSTANCE.close();
             List<String> paths = new ArrayList<String>();
             paths.add(DatalogManager.INSTANCE.getAbsolutePath());
             paths.add(FRDLogManager.INSTANCE.getAbsolutePath());
             paths.add(DebugLogManager.INSTANCE.getAbsolutePath());
             String emailText = getString(R.string.email_body);
 
             String subject = String.format(getString(R.string.email_subject), System.currentTimeMillis());
             EmailManager.email(this, ApplicationSettings.INSTANCE.getEmailDestination(), null, subject, emailText, paths);
         }
 
     }
 
     /**
      * Quit the application cleanly by stopping the connection to the Megasquirt if it exists
      */
     private void quit()
     {
         ApplicationSettings.INSTANCE.setAutoConnectOverride(false);
         ExtGPSManager.INSTANCE.stop();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null)
         {
             ecu.stop();
         }
 
         sendLogs();
 
         if (ecu != null)
         {
             ecu.reset();
         }
 
         this.finish();
     }
 
     /**
      * Toggle the connection to the Megasquirt
      */
     private void toggleConnection()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         if (ecu != null && ecu.isConnected())
         {
             ecu.stop();
         }
         else
         {
             ecu.start();
         }
     }
 
     /**
      * Open an about dialog
      */
     private void showAbout()
     {
         Dialog dialog = new Dialog(this);
 
         dialog.setContentView(R.layout.about);
 
         TextView text = (TextView) dialog.findViewById(R.id.text);
         String title = "";
         try
         {
             PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
             ApplicationInfo ai = pInfo.applicationInfo;
 
             final String applicationName = (ai != null) ? getPackageManager().getApplicationLabel(ai).toString() : "(unknown)";
             title = applicationName + " " + pInfo.versionName;
         }
         catch (NameNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
         dialog.setTitle(title);
 
         text.setText(R.string.about_text);
         ImageView image = (ImageView) dialog.findViewById(R.id.image);
         image.setImageResource(R.drawable.icon);
 
         dialog.show();
     }
 
     /**
      * Open the calibrate TPS activity
      */
     private void openCalibrateTPS()
     {
         Intent launchCalibrate = new Intent(this, CalibrateActivity.class);
         startActivity(launchCalibrate);
     }
 
     /**
      * Open the manage datalogs activity
      */
     private void openManageDatalogs()
     {
         Intent lauchManageDatalogs = new Intent(this, ManageDatalogsActivity.class);
         startActivity(lauchManageDatalogs);
     }
 
     /**
      * Open the tuning activity
      */
     private void openTuning()
     {
         Intent launchTuning = new Intent(this, TuningActivity.class);
         startActivity(launchTuning);
     }
 
     /**
      * Open the preferences activity
      */
     private void openPreferences()
     {
         Intent launchPrefs = new Intent(this, PreferencesActivity.class);
         startActivityForResult(launchPrefs, SHOW_PREFS);
     }
 
     /**
      * 
      * @param requestCode
      * @param resultCode
      * @param data
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK)
         {
             switch(requestCode)
             {
             case SHOW_PREFS:
                 Boolean dirty = (Boolean) data.getExtras().get(PreferencesActivity.DIRTY);
                 if (dirty)
                 {
                     Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
                     if (ecuDefinition != null)
                     {
                         ecuDefinition.refreshFlags();
                         GaugeRegister.INSTANCE.flush();
                         ecuDefinition.initGauges();
                         initGauges();
                     }
                 }
                 break;
             
             case MSLoggerApplication.REQUEST_CONNECT_DEVICE:
                 // When DeviceListActivity returns with a device to connect
                 if (resultCode == Activity.RESULT_OK)
                 {
                     // Get the device MAC address
                     String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
     
                     ApplicationSettings.INSTANCE.setECUBluetoothMac(address);
                 }
                 break;
             }
         }
     }
 
     /**
      * Called when a preference change
      * 
      * @param prefs
      * @param key
      */
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
     {
         if (ready == null || !ready)
         {
             return;
         }
         if (key.startsWith("gauge"))
         {
             initGauges();
         }
         Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ApplicationSettings.INSTANCE.btDeviceSelected() && ecuDefinition != null)
         {
             ecuDefinition.refreshFlags();
         }
 
         // When the TEMP/MAP/EGO are changed in preferences, we refresh the ECU flags if we are online
         if ((key.equals("temptype") || key.equals("maptype") || key.equals("egotype")) && ecuDefinition != null)
         {
             ecuDefinition.refreshFlags();
         }
     }
 
     /**
      * 
      * @param v
      */
     @Override
     public void onClick(View v)
     {
     }
 
     /**
      * Background task user to reset gauge to firmware default
      */
     private class ResetGaugesTask extends AsyncTask<Void, Void, Void>
     {
         private ProgressDialog dialog;
 
         /**
          * 
          */
         @Override
         protected void onPreExecute()
         {
             dialog = new ProgressDialog(MSLoggerActivity.this);
             dialog.setMessage(getString(R.string.reset_gauges));
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
 
         /**
          * 
          * @param arg0
          */
         @Override
         protected Void doInBackground(Void... arg0)
         {
             GaugeRegister.INSTANCE.resetAll();
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
             Editor editor = prefs.edit();
             editor.remove("gauge1");
             editor.remove("gauge2");
             editor.remove("gauge3");
             editor.remove("gauge4");
             editor.remove("gauge5");
             editor.commit();
             return null;
         }
 
         /**
          * 
          * @param unused
          */
         @Override
         protected void onPostExecute(Void unused)
         {
             dialog.dismiss();
 
             Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
             if (ecuDefinition != null)
             {
                 initGauges();
             }
         }
     }
 
     /**
      * Background task that load the pages
      */
     private class InitTask extends AsyncTask<Void, Void, Void>
     {
 
         /**
          * 
          * @param result
          */
         @Override
         protected void onPostExecute(Void result)
         {
             super.onPostExecute(result);
             finaliseInit();
             ready = true;
         }
 
         /**
          * 
          * @param params
          */
         @Override
         protected Void doInBackground(Void... params)
         {
             if (ready != null)
             {
                 return null;
             }
             ready = false;
 
             loadGauges();
 
             return null;
         }
     }
 
     /**
      * Receiver that get events from other activities about Megasquirt status and activities
      */
     private final class Reciever extends BroadcastReceiver
     {
         /**
          * When an event is received
          * 
          * @param context
          * @param intent
          */
         @Override
         public void onReceive(Context context, Intent intent)
         {
             String action = intent.getAction();
             boolean autoLoggingEnabled = ApplicationSettings.INSTANCE.getAutoLogging();
 
             if (action.equals(Megasquirt.PROBE_ECU))
             {
                 completeCreate();
                 indicatorManager.setDisabled(false);
             }
             else if (action.equals(Megasquirt.CONNECTED))
             {
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
                 DebugLogManager.INSTANCE.log(action, Log.INFO);
                 indicatorManager.setDisabled(false);
                 if (autoLoggingEnabled && ecu != null)
                 {
                     ecu.startLogging();
                 }
             }
             else if (action.equals(Megasquirt.DISCONNECTED))
             {
                 DebugLogManager.INSTANCE.log(action, Log.INFO);
                 indicatorManager.setDisabled(true);
                 if (autoLoggingEnabled)
                 {
                     DatalogManager.INSTANCE.mark("Connection lost");
                 }
 
                 messages.setText(R.string.disconnected_from_ms);
                 rps.setText("");
 
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
                 if (ecu != null)
                 {
                     ecu.stop();
                 }
             }
             else if (action.equals(Megasquirt.NEW_DATA))
             {
                 processData();
             }
             else if (action.equals(ApplicationSettings.GENERAL_MESSAGE))
             {
                 String msg = intent.getStringExtra(ApplicationSettings.MESSAGE);
 
                 messages.setText(msg);
                 DebugLogManager.INSTANCE.log("Message : " + msg, Log.INFO);
             }
             else if (action.equals(ApplicationSettings.RPS_MESSAGE))
             {
                 String RPS = intent.getStringExtra(ApplicationSettings.RPS);
 
                 rps.setText(RPS + " reads / second");
             }
             else if (action.equals(ApplicationSettings.TOAST))
             {
                 String msg = intent.getStringExtra(ApplicationSettings.TOAST_MESSAGE);
 
                 // The toast is called in a loop so it can be displayed longer (Toast.LENGTH_LONG = 3.5 seconds)
                 for (int j = 0; j < 2; j++)
                 {
                     Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                 }
             }
             else if (action.equals(Megasquirt.UNKNOWN_ECU))
             {
                 if (isFinishing())
                 {
                     return;
                 }
                 AlertDialog.Builder builder = new AlertDialog.Builder(MSLoggerActivity.this);
                 builder.setMessage(R.string.unrecognised_ecu).setTitle(R.string.app_name).setPositiveButton(R.string.bt_ok, new DialogInterface.OnClickListener()
                 {
                     public void onClick(DialogInterface dialog, int id)
                     {
                         constructEmail(ApplicationSettings.INSTANCE.getEcuDefinition().getTrueSignature());
                     }
 
                     private void constructEmail(String trueSignature)
                     {
                         EmailManager.email(MSLoggerActivity.this, "mslogger.android@gmail.com", null, "Unrecognised firmware signature", "An unknown firmware was detected with a signature of '" + trueSignature + "'.\n\nPlease consider this for the next release.", null);
                     }
 
                 }).setNegativeButton(R.string.bt_cancel, new DialogInterface.OnClickListener()
                 {
                     public void onClick(DialogInterface dialog, int which)
                     {
                         dialog.cancel();
                     }
                 });
 
                 AlertDialog alert = builder.create();
                 if (!isFinishing())
                 {
                     alert.show();
                 }
 
             }
             else if (action.equals(Megasquirt.UNKNOWN_ECU_BT))
             {
                 Intent serverIntent = new Intent(MSLoggerActivity.this, DeviceListActivity.class);
                 startActivityForResult(serverIntent, MSLoggerApplication.REQUEST_CONNECT_DEVICE);
             } 
         }
     }
 
     /**
      * Listener used when the user touch the screen to mark the datalog
      */
     private static class MarkListener implements OnTouchListener
     {
         private LinearLayout layout;
 
         /**
          * Constructor
          * 
          * @param layout The layout that will change background then the screen is touch
          */
         public MarkListener(LinearLayout layout)
         {
             this.layout = layout;
         }
 
         /**
          * On touch event
          * 
          * @param v The view that triggered the event
          * @param event Information about the event
          */
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
             Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
             if (ecu != null && ecu.isLogging() && event.getAction() == MotionEvent.ACTION_DOWN)
             {
                 layout.setBackgroundColor(Color.BLUE);
                 layout.invalidate();
                 return true;
             }
             if (event.getAction() == MotionEvent.ACTION_UP)
             {
                 layout.setBackgroundColor(Color.BLACK);
                 layout.invalidate();
                 DatalogManager.INSTANCE.mark("Manual");
                 return true;
             }
             return false;
         }
 
     }
 
     /**
      * It was determinated that the android device don't support Bluetooth, so we tell the user
      */
     public void finishDialogNoBluetooth()
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.no_bt).setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.app_name).setCancelable(false).setPositiveButton(R.string.bt_ok, new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 finish();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 }
