 package myamamic.tp.devsupport.wifi;
 
 import android.content.*;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import myamamic.tp.devsupport.*;
 
import java.util.HashMap;
import java.util.Map;
 
 public class WifiOnOffTest extends BaseTestActivity {
     private static final String TAG = "WifiOnOffTest";
 
     private static final int EVENT_ENABLE_WIFI  = 1;
     private static final int EVENT_DISABLE_WIFI = 2;
 
    @SuppressWarnings("serial")
    private static final Map<Integer, String> sWifiStateMap = new HashMap<Integer, String>() {{
         put(WifiManager.WIFI_STATE_ENABLED,     "Wifi ON");
         put(WifiManager.WIFI_STATE_DISABLING,   "Disabling wifi ...");
         put(WifiManager.WIFI_STATE_DISABLED,    "Wifi OFF");
         put(WifiManager.WIFI_STATE_ENABLING,    "Enabling wifi ...");
         put(WifiManager.WIFI_STATE_UNKNOWN,     "Wifi state unknown");
     }};
 
     private Button mWifiButton;
     private Button mWifiRunningStartButton;
     private Button mWifiRunningStopButton;
     private WifiManager mWm = null;
     private WifiBroadcastReceiver mWifiReceiver = null;
 
     private TextView mWifiState = null;
     private boolean isRunningTest = false;
     private int mPreviousState = WifiManager.WIFI_STATE_UNKNOWN;
 
     private long mWaitWifiDisabled = 30000L;
     private long mWaitWifiEnabled = 30000L;
     private int mWifiOnCount = 0;
     private CountView mWifiOnCountView ;
     private int mWifiOnFailCount = 0;
     private CountView mWifiOnFailCountView ;
     private int mWifiOffCount = 0;
     private CountView mWifiOffCountView;
     private int mWifiOffFailCount = 0;
     private CountView mWifiOffFailCountView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.wifi_onoff_control);
 
         mWifiReceiver = new WifiBroadcastReceiver();
         IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
         registerReceiver(mWifiReceiver, filter);
 
         mWm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
         if(mWm == null) {
             Log.e(TAG, "Cannot get WifiManager handle.");
             finish();
             return;
         }
         initialize();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(mWifiReceiver);
         mWifiReceiver = null;
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         updateWifiButtonState(false, false);
         updateWifiRunningButtonState();
         updateCount();
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         isRunningTest = savedInstanceState.getBoolean("isRunning", false);
         mWifiOnCount = savedInstanceState.getInt("wifiOnCount", 0);
         mWifiOnFailCount = savedInstanceState.getInt("wifiOnFailCount", 0);
         mWifiOffCount = savedInstanceState.getInt("wifiOffCount", 0);
         mWifiOffFailCount = savedInstanceState.getInt("wifiOffFailCount", 0);
         mPreviousState = savedInstanceState.getInt("previousState", WifiManager.WIFI_STATE_UNKNOWN);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean("isRunning", isRunningTest);
         outState.putInt("wifiOnCount", mWifiOnCount);
         outState.putInt("wifiOnFailCount", mWifiOnFailCount);
         outState.putInt("wifiOffCount", mWifiOffCount);
         outState.putInt("wifiOffFailCount", mWifiOffFailCount);
         outState.putInt("previousState", mPreviousState);
     }
 
     @Override
     public void onPositiveButtonClicked() {
         stopWifiRunning();
         super.onPositiveButtonClicked();
     }
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch(msg.what) {
                 case EVENT_ENABLE_WIFI:
                     Log.d(TAG, "HANDLE EVENT_ENABLE_WIFI");
                     enableWifi(true);
                     break;
                 case EVENT_DISABLE_WIFI:
                     Log.d(TAG, "HANDLE EVENT_DISABLE_WIFI");
                     enableWifi(false);
                     break;
                 default:
                     break;
             }
         }
     };
 
     // Oneshot wifi on/off
     private OnClickListener mWifiButtonClickedListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             if (!mWm.isWifiEnabled()) {
                 enableWifi(true);
             } else {
                 enableWifi(false);
             }
         }
     };
 
     // Running start or stop button
     private OnClickListener mWifiRunningButtonClickedListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             if (v.getId() == R.id.wifi_button_running_start) {
                 startWifiRunning();
             } else if (v.getId() == R.id.wifi_button_running_stop) {
                 stopWifiRunning();
             }
         }
     };
 
     private void initialize() {
         mWifiButton = (Button)findViewById(R.id.wifi_button);
         mWifiButton.setOnClickListener(mWifiButtonClickedListener);
         updateWifiButtonState(false, false);
         mWifiRunningStartButton = (Button)findViewById(R.id.wifi_button_running_start);
         mWifiRunningStartButton.setOnClickListener(mWifiRunningButtonClickedListener);
         mWifiRunningStopButton = (Button)findViewById(R.id.wifi_button_running_stop);
         mWifiRunningStopButton.setOnClickListener(mWifiRunningButtonClickedListener);
         updateWifiRunningButtonState();
 
         mWifiState = (TextView)findViewById(R.id.wifi_state);
         mWifiState.setText(sWifiStateMap.get(mWm.getWifiState()));
 
         mWifiOnCountView = (CountView)findViewById(R.id.wifi_on_count);
         mWifiOffCountView = (CountView)findViewById(R.id.wifi_off_count);
         mWifiOnFailCountView = (CountView)findViewById(R.id.wifi_on_fail_count);
         mWifiOffFailCountView = (CountView)findViewById(R.id.wifi_off_fail_count);
 
         ((EditText)findViewById(R.id.wifi_edittext_wait_enabled)).setText(String.valueOf(mWaitWifiEnabled));
         ((EditText)findViewById(R.id.wifi_edittext_wait_disabled)).setText(String.valueOf(mWaitWifiDisabled));
     }
 
     private void enableWifi(boolean enable) {
         if (isRunningTest) {
             if (!mWm.setWifiEnabled(enable)) { // fail
                 if (enable) {
                     mWifiOnFailCount++;
                     mWifiOnFailCountView.updateCount(mWifiOnFailCount);
                 } else {
                     mWifiOffFailCount++;
                     mWifiOffFailCountView.updateCount(mWifiOffFailCount);
                 }
             }
         } else { // One shot
             updateWifiButtonState(true, false);
 
             boolean ret = mWm.setWifiEnabled(enable);
             if (!ret) {
                 updateWifiButtonState(false, false);
                 Log.e(TAG, "enableWifi(" + enable + ") : Could not wifi " + (enable ? "enable" : "disable"));
 
                 OkDialogFragment newFragment = OkDialogFragment.newInstance(
                         enable ? R.string.wifi_enabling_error : R.string.wifi_disabling_error);
                 newFragment.show(getSupportFragmentManager(), "dialog");
             }
         }
     }
 
     private void startWifiRunning() {
         Log.d(TAG, "startWifiRunning() IN");
         if (isRunningTest) {
             return;
         }
         isRunningTest = true;
         updateWifiRunningButtonState();
         updateWifiButtonState(true, false);
 
         // Remove result file.
         outputWifiOnOffCount(true, 0, false);
 
         mWifiOffCount = mWifiOnCount = mWifiOffFailCount = mWifiOnFailCount = 0;
         updateCount();
 
         mWaitWifiEnabled = Long.parseLong(((EditText)findViewById(R.id.wifi_edittext_wait_enabled)).getText().toString());
         mWaitWifiDisabled = Long.parseLong(((EditText)findViewById(R.id.wifi_edittext_wait_disabled)).getText().toString());
 
         if (mWm.isWifiEnabled()) {
             Log.d(TAG, "startWifiRunning() : POST EVENT_DISABLE_WIFI");
             mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_WIFI), 10);
         } else {
             Log.d(TAG, "startWifiRunning() : POST EVENT_ENABLE_WIFI");
             mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_WIFI), 10);
         }
     }
 
     private void stopWifiRunning() {
         Log.d(TAG, "stopWifiRunning() IN");
         isRunningTest = false;
         if (mHandler.hasMessages(EVENT_ENABLE_WIFI)) {
             mHandler.removeMessages(EVENT_ENABLE_WIFI);
         }
         if (mHandler.hasMessages(EVENT_DISABLE_WIFI)) {
             mHandler.removeMessages(EVENT_DISABLE_WIFI);
         }
         updateWifiRunningButtonState();
         updateWifiButtonState(false, false);
     }
 
     class WifiBroadcastReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             mWifiState.setText(sWifiStateMap.get(mWm.getWifiState()));
 
             int previousState = mPreviousState;
             int newState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
             // Work around: When device orientation changed,
             // WIFI_STATE_CHANGED_ACTION is notified this receiver.
             mPreviousState = newState;
 
             if (!isRunningTest) {
                 updateWifiButtonState(false, false);
                 return;
             }
 
             //
             // Running test started
             //
             if (previousState == newState) {
                 // If state has not changed, ignore this intent.
                 Log.d(TAG, "WifiBroadcastReceiver : previousState=" + previousState
                         + " newState=" + newState);
                 return;
             }
 
             switch (newState) {
             case WifiManager.WIFI_STATE_ENABLED:
                 mWifiOnCount++;
                 mWifiOnCountView.updateCount(mWifiOnCount);
                 outputWifiOnOffCount(true, mWifiOnCount, true); // Output file
                 Log.i(TAG, "Wifi ON count=" + mWifiOnCount);
                 Log.d(TAG, "WifiBroadcastReceiver : POST EVENT_DISABLE_WIFI after "
                         + mWaitWifiEnabled + "s");
                 mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_DISABLE_WIFI), mWaitWifiEnabled);
                 break;
             case WifiManager.WIFI_STATE_DISABLING:
                 // noting to do
                 break;
             case WifiManager.WIFI_STATE_DISABLED:
                 mWifiOffCount++;
                 mWifiOffCountView.updateCount(mWifiOffCount);
                 outputWifiOnOffCount(false, mWifiOffCount, true); // Output file
                 Log.i(TAG, "Wifi OFF count=" + mWifiOffCount);
                 Log.d(TAG, "WifiBroadcastReceiver : POST EVENT_ENABLE_WIFI after "
                         + mWaitWifiDisabled + "s");
                 mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_ENABLE_WIFI), mWaitWifiDisabled);
                 break;
             case WifiManager.WIFI_STATE_ENABLING:
                 // noting to do
                 break;
             default:
                 break;
             }
         }
     }
 
     private void outputWifiOnOffCount(boolean on, int count, boolean append) {
         String result = Utility.getDate() + "    " + (on ? "ON" : "OFF") + "(" + String.valueOf(count) + ")";
         Utility.writeResultToFile("/sdcard/wifi_loop.txt", result, append);
     }
 
     private void updateWifiButtonState(boolean force, boolean forceState) {
         mWifiButton.setText(mWm.isWifiEnabled() ? getString(R.string.wifi_off) : getString(R.string.wifi_on));
 
         // When running test started, toggle button is disabled.
         if (isRunningTest) {
             mWifiButton.setEnabled(false);
             return;
         }
         // Force update
         if (force) {
             mWifiButton.setEnabled(forceState);
             return;
         }
         int state = mWm.getWifiState();
         switch (state) {
         case WifiManager.WIFI_STATE_ENABLED:
         case WifiManager.WIFI_STATE_DISABLED:
             mWifiButton.setEnabled(true);
             break;
         case WifiManager.WIFI_STATE_ENABLING:
         case WifiManager.WIFI_STATE_DISABLING:
             mWifiButton.setEnabled(false);
             break;
         default:
             break;
         }
     }
 
     private void updateWifiRunningButtonState() {
         if (isRunningTest) {
             mWifiRunningStartButton.setEnabled(false);
             mWifiRunningStopButton.setEnabled(true);
         } else {
             mWifiRunningStartButton.setEnabled(true);
             mWifiRunningStopButton.setEnabled(false);
         }
     }
 
     private void updateCount() {
         mWifiOnCountView.updateCount(mWifiOnCount);
         mWifiOffCountView.updateCount(mWifiOffCount);
         mWifiOnFailCountView.updateCount(mWifiOnFailCount);
         mWifiOffFailCountView.updateCount(mWifiOffFailCount);
     }
 }
