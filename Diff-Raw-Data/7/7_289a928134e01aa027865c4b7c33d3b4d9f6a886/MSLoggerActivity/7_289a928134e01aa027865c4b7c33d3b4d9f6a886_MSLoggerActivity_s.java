 package uk.org.smithfamily.mslogger.activity;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 import android.app.*;
 import android.bluetooth.BluetoothAdapter;
 import android.content.*;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.*;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.*;
 
 import uk.org.smithfamily.mslogger.*;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.log.*;
 import uk.org.smithfamily.mslogger.widgets.*;
 
 /**
  * 
  * 
  */
 public class MSLoggerActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener
 {
     private BroadcastReceiver      updateReceiver        = new Reciever();
     private IndicatorManager       indicatorManager;
     TextView                       messages;
     public boolean                 connected;
     static private Boolean         ready                 = null;
     private MSGauge                gauge1;
     private MSGauge                gauge2;
     private MSGauge                gauge3;
     private MSGauge                gauge4;
     private MSGauge                gauge5;
 
     private GestureDetector        gestureDetector;
     private boolean                gaugeEditEnabled;
     boolean                        scrolling;
     private LinearLayout           layout;
     private static final int       SHOW_PREFS            = 124230;
 
     private boolean                registered;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         
         super.onCreate(savedInstanceState);
         checkSDCard();
         
         DebugLogManager.INSTANCE.log(getPackageName(),Log.ASSERT);
         try
         {
             String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
             DebugLogManager.INSTANCE.log(app_ver,Log.ASSERT);
         }
         catch (NameNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
         dumpPreferences();
         setContentView(R.layout.displaygauge);
         messages = (TextView) findViewById(R.id.messages);
 
         findGauges();
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         prefsManager.registerOnSharedPreferenceChangeListener(MSLoggerActivity.this);
 
         indicatorManager = IndicatorManager.INSTANCE;
         indicatorManager.setDisabled(true);
 
         ApplicationSettings.INSTANCE.setDefaultAdapter(BluetoothAdapter.getDefaultAdapter());
         GPSLocationManager.INSTANCE.start();
         ApplicationSettings.INSTANCE.setAutoConnectOverride(null);
 
         registerMessages();
         Intent serverIntent = new Intent(this, StartupActivity.class);
         startActivityForResult(serverIntent, MSLoggerApplication.PROBE_ECU);
     }
 
     /**
      * 
      */
     private void dumpPreferences()
     {
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         Map<String, ?> prefs = prefsManager.getAll();
         for(Entry<String, ?> entry : prefs.entrySet())
         {
             DebugLogManager.INSTANCE.log("Preference:"+entry.getKey()+":"+entry.getValue(), Log.ASSERT);
         }
     }
 
     /**
      * 
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
      * 
      */
     private void finaliseInit()
     {
         initGauges();
 
         checkBTDeviceSet();
         checkSDCard();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         if (ecu != null)
         {
             ecu.start();
         }
 
     }
 
     /**
      * 
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
      * 
      */
     @Override
     protected void onDestroy()
     {
         deRegisterMessages();
         GPSLocationManager.INSTANCE.stop();
         super.onDestroy();
     }
 
     /**
      * 
      */
     @Override
     public void onStop()
     {
         super.onStop();
         saveGauges();
     }
 
     /**
      * 
      */
     private void checkBTDeviceSet()
     {
 
         if (!ApplicationSettings.INSTANCE.btDeviceSelected())
         {
             messages.setText(R.string.please_select);
         }
     }
 
     /**
      * 
      */
     private void saveGauges()
     {
         if (!(gauge1 != null && gauge2 != null && gauge3 != null && gauge4 != null && gauge5 != null))
         {
             findGauges();
         }
         if (gauge1 != null && gauge2 != null && gauge3 != null && gauge4 != null && gauge5 != null)
         {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             Editor editor = prefs.edit();
             if (!gauge1.getName().equals(MSGauge.DEAD_GAUGE_NAME))
                 editor.putString("gauge1", gauge1.getName());
             if (!gauge2.getName().equals(MSGauge.DEAD_GAUGE_NAME))
                 editor.putString("gauge2", gauge2.getName());
             if (!gauge3.getName().equals(MSGauge.DEAD_GAUGE_NAME))
                 editor.putString("gauge3", gauge3.getName());
             if (!gauge4.getName().equals(MSGauge.DEAD_GAUGE_NAME))
                 editor.putString("gauge4", gauge4.getName());
             if (!gauge5.getName().equals(MSGauge.DEAD_GAUGE_NAME))
                 editor.putString("gauge5", gauge5.getName());
             editor.commit();
         }
     }
 
     /**
      * 
      */
     private void loadGauges()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         GaugeRegister.INSTANCE.flush();
         ecu.initGauges();
     }
 
     /**
      * 
      */
     private void initGauges()
     {
         layout = (LinearLayout) (findViewById(R.id.layout));
         findGauges();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         String[] defaultGauges = ecu.defaultGauges();
         gauge1.initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge1", defaultGauges[0]));
         gauge2.initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge2", defaultGauges[1]));
         gauge3.initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge3", defaultGauges[2]));
         gauge4.initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge4", defaultGauges[3]));
         if (gauge5 != null)
             gauge5.initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge5", defaultGauges[4]));
 
         if (gaugeEditEnabled)
         {
             gauge1.setOnTouchListener(new GaugeTouchListener(gauge1));
             gauge2.setOnTouchListener(new GaugeTouchListener(gauge2));
             gauge4.setOnTouchListener(new GaugeTouchListener(gauge4));
             if (gauge5 != null)
                 gauge5.setOnTouchListener(new GaugeTouchListener(gauge5));
 
             gestureDetector = new GestureDetector(new GaugeRotationDetector(this, gauge3));
             OnTouchListener gestureListener = new View.OnTouchListener()
             {
                 public boolean onTouch(View v, MotionEvent event)
                 {
                     if (gestureDetector.onTouchEvent(event))
                     {
                         return true;
                     }
 
                     if (event.getAction() == MotionEvent.ACTION_UP)
                     {
                         if (scrolling)
                         {
                             scrolling = false;
                             GaugeDetails gd = gauge3.getDetails();
                             GaugeRegister.INSTANCE.persistDetails(gd);
                         }
                     }
 
                     return false;
                 }
             };
             gauge3.setOnClickListener(MSLoggerActivity.this);
             gauge3.setOnTouchListener(gestureListener);
         }
         else
         {
             MarkListener l = new MarkListener(layout);
             setTouchListeners(l);
         }
         gauge1.invalidate();
         gauge2.invalidate();
         gauge3.invalidate();
         gauge4.invalidate();
         if (gauge5 != null)
             gauge5.invalidate();
 
     }
 
     /**
      * 
      */
     private void findGauges()
     {
         gauge1 = (MSGauge) findViewById(R.id.g1);
         gauge2 = (MSGauge) findViewById(R.id.g2);
         gauge3 = (MSGauge) findViewById(R.id.g3);
         gauge4 = (MSGauge) findViewById(R.id.g4);
         gauge5 = (MSGauge) findViewById(R.id.g5);
     }
 
     /**
      * 
      * @param l
      */
     private void setTouchListeners(MarkListener l)
     {
         gauge1.setOnTouchListener(l);
         gauge2.setOnTouchListener(l);
         gauge3.setOnTouchListener(l);
         gauge4.setOnTouchListener(l);
         if (gauge5 != null)
             gauge5.setOnTouchListener(l);
     }
 
     /**
      * 
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
         registered = true;
     }
 
     /**
      * 
      */
     private void deRegisterMessages()
     {
         if (registered)
         {
             unregisterReceiver(updateReceiver);
         }
     }
 
     /**
      * 
      */
     protected void processData()
     {
         List<Indicator> indicators;
         if ((indicators = indicatorManager.getIndicators()) != null)
         {
             indicatorManager.setDisabled(false);
             for (Indicator i : indicators)
             {
                 String channelName = i.getChannel();
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
                 if (channelName != null && ecu != null)
                 {
                     double value = ecu.getValue(channelName);
                     i.setCurrentValue(value);
                 }
                 else
                 {
                     i.setCurrentValue(0);
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
             editItem.setTitle(R.string.DisableGaugeEdit);
         }
         else
         {
             editItem.setIcon(R.drawable.ic_menu_enable_gauge_editing);
             editItem.setTitle(R.string.EnableGaugeEdit);
         }
         MenuItem connectionItem = menu.findItem(R.id.forceConnection);
         if (ecuDefinition != null && ecuDefinition.isRunning())
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
         if (ApplicationSettings.INSTANCE.shouldBeLogging())
         {
             loggingItem.setIcon(R.drawable.ic_menu_stop_logging);
             loggingItem.setTitle(R.string.stop_logging);
         }
         else
         {
             loggingItem.setIcon(R.drawable.ic_menu_start_logging);
             loggingItem.setTitle(R.string.start_logging);
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     /**
      * 
      * @param item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         checkBTDeviceSet();
         int itemId = item.getItemId();
         if (itemId == R.id.forceConnection)
         {
             toggleConnection();
             return true;
         }
         else if (itemId == R.id.gaugeEditing)
         {
             toggleEditing();
             return true;
         }
         else if (itemId == R.id.forceLogging)
         {
             toggleLogging();
             return true;
         }
         else if (itemId == R.id.manageDatalogs)
         {
             openManageDatalogs();
             return true;
         }
         else if (itemId == R.id.preferences)
         {
             openPreferences();
             return true;
         }        
         else if (itemId == R.id.resetGauges)
         {
             resetGuages();
             return true;
         }
         else if (itemId == R.id.calibrate)
         {
             openCalibrateTPS();
             return true;
         }
         else if (itemId == R.id.about)
         {
             showAbout();
             return true;
         }
         else if (itemId == R.id.quit)
         {
             quit();
             return true;
         }
         else
         {
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * 
      */
     public void resetGuages()
     {
         new ResetGaugesTask().execute((Void) null);
     }
 
     /**
      * 
      */
     private void toggleEditing()
     {
         if (gaugeEditEnabled)
         {
             saveGauges();
         }
         gaugeEditEnabled = !gaugeEditEnabled;
         initGauges();
     }
 
     /**
      * 
      */
     private void toggleLogging()
     {
         boolean shouldBeLogging = ApplicationSettings.INSTANCE.shouldBeLogging();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (shouldBeLogging)
         {
             ecu.stopLogging();
             sendLogs();
         }
         else
         {
             ecu.startLogging();
         }
         ApplicationSettings.INSTANCE.setLoggingOverride(!shouldBeLogging);
     }
 
     /**
      * 
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
      * 
      */
     private void quit()
     {
         ApplicationSettings.INSTANCE.setAutoConnectOverride(false);
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null)
         {
             ecu.stop();
         }
         sendLogs();
         ApplicationSettings.INSTANCE.resetECUs();
 
         this.finish();
     }
 
     /**
      * 
      */
     private void toggleConnection()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null)
         {
             ecu.toggleConnection();
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
             ApplicationInfo ai;
             ai = pInfo.applicationInfo;
             final String applicationName = (String) (ai != null ? getPackageManager().getApplicationLabel(ai) : "(unknown)");
             title = applicationName + " " + pInfo.versionName;
         }
         catch (NameNotFoundException e)
         {
             e.printStackTrace();
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
             if (requestCode == MSLoggerApplication.PROBE_ECU)
             {
                 completeCreate();
             }
             if (requestCode == SHOW_PREFS)
             {
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
             }
         }
     }
 
     /**
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
      *
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
             dialog.setMessage(getString(R.string.ResetGauges));
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
            initGauges();
         }
     }
 
     /**
      * 
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
      * 
      */
     public class GaugeTouchListener implements OnTouchListener
     {
 
         private MSGauge gauge;
 
         /**
          * 
          * @param gauge
          */
         public GaugeTouchListener(MSGauge gauge)
         {
             this.gauge = gauge;
         }
 
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
             // System.out.println(event);
             if (event.getAction() == MotionEvent.ACTION_DOWN)
             {
                 String g3name = gauge3.getName();
                 gauge3.initFromName(gauge.getName());
                 gauge.initFromName(g3name);
                 gauge.invalidate();
                 gauge3.invalidate();
                 return true;
             }
             return false;
         }
 
     }
 
     /**
      *
      */
     private final class Reciever extends BroadcastReceiver
     {
         /**
          * 
          * @param context
          * @param intent
          */
         @Override
         public void onReceive(Context context, Intent intent)
         {
             String action = intent.getAction();
             Log.i(ApplicationSettings.TAG, "Received :" + action);
             boolean shouldBeLogging = ApplicationSettings.INSTANCE.shouldBeLogging();
 
             if (action.equals(Megasquirt.CONNECTED))
             {
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
                 DebugLogManager.INSTANCE.log(action,Log.INFO);
                 indicatorManager.setDisabled(false);
                 if (shouldBeLogging && ecu != null)
                 {
                     ecu.startLogging();
                 }
             }
             if (action.equals(Megasquirt.DISCONNECTED))
             {
                 DebugLogManager.INSTANCE.log(action,Log.INFO);
                 indicatorManager.setDisabled(true);
                 if (shouldBeLogging)
                 {
                     DatalogManager.INSTANCE.mark("Connection Lost");
                 }
             }
  
             if (action.equals(Megasquirt.NEW_DATA))
             {
                 processData();
             }
             if (action.equals(ApplicationSettings.GENERAL_MESSAGE))
             {
                 String msg = intent.getStringExtra(ApplicationSettings.MESSAGE);
 
                 messages.setText(msg);
                 DebugLogManager.INSTANCE.log("Message : " + msg,Log.INFO);
             }
 
         }
     }
 
     /**
      *
      */
     private class MarkListener implements OnTouchListener
     {
         private LinearLayout layout;
 
         /**
          * 
          * @param layout
          */
         public MarkListener(LinearLayout layout)
         {
             this.layout = layout;
         }
 
         /**
          * 
          * @param v
          * @param event
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
 
 }
