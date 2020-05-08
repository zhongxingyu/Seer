 package com.explodingsheep.tetherapp;
 
 import java.lang.reflect.Method;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.explodingsheep.tetherapp.AuthAdapter.SecurityOption;
 
 public class MainActivity extends Activity {
     private CheckBox mEnableHotspotCheckbox;
     private TextView mSsidTextView;
     private Spinner mAuthTypeSpinner;
     private TextView mPasswordTextView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mEnableHotspotCheckbox = (CheckBox) findViewById(R.id.enable_hotspot);
         mSsidTextView = (TextView) findViewById(R.id.setup_ssid_name);
         mAuthTypeSpinner = (Spinner) findViewById(R.id.setup_ssid_authtype);
         mPasswordTextView = (TextView) findViewById(R.id.setup_ssid_password);
 
         initializeAuthType();
         initializeFields();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return false;
     }
 
     public void onToggleHotspot(View view) {
         final boolean isChecked = ((CheckBox) view).isChecked();
         final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
         try {
             if (isChecked) {
                 final boolean disconnected = disableWifi(wifiManager);
                 if (!disconnected) {
                     final Toast toast = Toast.makeText(this, R.string.wifi_disconnect_error,
                             Toast.LENGTH_SHORT);
                     toast.show();
                     return;
                 }
 
                 enableWifiAp(wifiManager);
             } else {
                 disableWifiAp(wifiManager);
             }
         } catch (Exception e) {
             final Resources resources = MainActivity.this.getResources();
             final String errorMessage = String.format(
                     resources.getString(R.string.reflection_error), e.toString());
             final Toast toast = Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG);
             toast.show();
         }
     }
 
     private void initializeAuthType() {
         final AuthAdapter authAdapter = new AuthAdapter(this);
         authAdapter.initializeOptions();
         mAuthTypeSpinner.setAdapter(authAdapter);
     }
 
     private void initializeFields() {
         final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
         try {
             final Method wifiConfigMethod = wifiManager.getClass().getDeclaredMethod(
                     "getWifiApConfiguration");
             final WifiConfiguration wifiConfig = (WifiConfiguration) wifiConfigMethod
                     .invoke(wifiManager);
             final Method wifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod(
                     "isWifiApEnabled");
             final boolean isEnabled = Boolean.valueOf(wifiApEnabledMethod.invoke(wifiManager)
                     .toString());
 
             mSsidTextView.setText(wifiConfig.SSID);
             mPasswordTextView.setText(wifiConfig.preSharedKey);
             mEnableHotspotCheckbox.setChecked(isEnabled);
         } catch (Exception e) {
             final Resources resources = MainActivity.this.getResources();
             final String errorMessage = String.format(
                     resources.getString(R.string.reflection_error), e.toString());
             final Toast toast = Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG);
             toast.show();
         }
     }
 
     private boolean disableWifi(WifiManager wifiManager) {
         if (wifiManager.isWifiEnabled()) {
             return wifiManager.setWifiEnabled(false);
         }
 
         // wifi already disabled
         return true;
     }
 
     private void enableWifiAp(WifiManager wifiManager) throws Exception {
         final WifiConfiguration wifiConfig = new WifiConfiguration();
         final SecurityOption securityOption = (SecurityOption) mAuthTypeSpinner.getSelectedItem();
 
         wifiConfig.allowedGroupCiphers.set(securityOption.getGroupCipher());
         wifiConfig.allowedKeyManagement.set(securityOption.getKeyMgmt());
         wifiConfig.allowedPairwiseCiphers.set(securityOption.getPairCipher());
         wifiConfig.allowedProtocols.set(securityOption.getProtocol());
 
         wifiConfig.SSID = mSsidTextView.getText().toString();
         wifiConfig.preSharedKey = mPasswordTextView.getText().toString();
 
         final Method hotspotMethod = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled",
                 WifiConfiguration.class, boolean.class);
         hotspotMethod.invoke(wifiManager, wifiConfig, true);
 
         final Toast toast = Toast.makeText(this, R.string.enabling_hotspot, Toast.LENGTH_SHORT);
         toast.show();
     }
 
     private void disableWifiAp(WifiManager wifiManager) throws Exception {
         final WifiConfiguration wifiConfig = new WifiConfiguration();
         final Method hotspotMethod = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled",
                 WifiConfiguration.class, boolean.class);
 
         hotspotMethod.invoke(wifiManager, wifiConfig, false);
 
         final Toast toast = Toast.makeText(MainActivity.this, R.string.disabling_hotspot,
                 Toast.LENGTH_SHORT);
         toast.show();
     }
 
 }
