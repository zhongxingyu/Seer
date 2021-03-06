 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.settings;
 
 import android.app.Activity;
 import android.app.admin.DevicePolicyManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Switch;
 
 import com.android.internal.telephony.TelephonyIntents;
 import com.android.internal.telephony.TelephonyProperties;
 import com.android.settings.nfc.NfcEnabler;
 
 public class WirelessSettings extends SettingsPreferenceFragment {
 
     private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
     private static final String KEY_TOGGLE_NFC = "toggle_nfc";
     private static final String KEY_ZEROCLICK_SETTINGS = "zeroclick_settings";
     private static final String KEY_VPN_SETTINGS = "vpn_settings";
     private static final String KEY_WIFI_P2P_SETTINGS = "wifi_p2p_settings";
     private static final String KEY_TETHER_SETTINGS = "tether_settings";
     private static final String KEY_PROXY_SETTINGS = "proxy_settings";
     private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
 
    private static final Boolean WIFI_P2P_DEBUG = false;

     public static final String EXIT_ECM_RESULT = "exit_ecm_result";
     public static final int REQUEST_CODE_EXIT_ECM = 1;
 
     private AirplaneModeEnabler mAirplaneModeEnabler;
     private CheckBoxPreference mAirplaneModePreference;
     private NfcEnabler mNfcEnabler;
     private NfcAdapter mNfcAdapter;
 
     /**
      * Invoked on each preference click in this hierarchy, overrides
      * PreferenceActivity's implementation.  Used to make sure we track the
      * preference click events.
      */
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                 SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
             // In ECM mode launch ECM app dialog
             startActivityForResult(
                 new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                 REQUEST_CODE_EXIT_ECM);
             return true;
         }
         // Let the intents be launched by the Preference manager
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     public static boolean isRadioAllowed(Context context, String type) {
         if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
             return true;
         }
         // Here we use the same logic in onCreate().
         String toggleable = Settings.System.getString(context.getContentResolver(),
                 Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
         return toggleable != null && toggleable.contains(type);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.wireless_settings);
 
         final Activity activity = getActivity();
         mAirplaneModePreference = (CheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);
         CheckBoxPreference nfc = (CheckBoxPreference) findPreference(KEY_TOGGLE_NFC);
         PreferenceScreen zeroclick = (PreferenceScreen) findPreference(KEY_ZEROCLICK_SETTINGS);
 
         mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);
         mNfcEnabler = new NfcEnabler(activity, nfc, zeroclick);
 
         String toggleable = Settings.System.getString(activity.getContentResolver(),
                 Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
 
         // Manually set dependencies for Wifi when not toggleable.
         if (toggleable == null || !toggleable.contains(Settings.System.RADIO_WIFI)) {
             findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
         }
 
         // Manually set dependencies for Bluetooth when not toggleable.
         if (toggleable == null || !toggleable.contains(Settings.System.RADIO_BLUETOOTH)) {
             // No bluetooth-dependent items in the list. Code kept in case one is added later.
         }
 
         // Manually set dependencies for NFC when not toggleable.
         if (toggleable == null || !toggleable.contains(Settings.System.RADIO_NFC)) {
             findPreference(KEY_TOGGLE_NFC).setDependency(KEY_TOGGLE_AIRPLANE);
             findPreference(KEY_ZEROCLICK_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
         }
 
         // Remove NFC if its not available
         mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
         if (mNfcAdapter == null) {
             getPreferenceScreen().removePreference(nfc);
             getPreferenceScreen().removePreference(zeroclick);
             mNfcEnabler = null;
         }
 
         // Remove Mobile Network Settings if it's a wifi-only device.
         if (Utils.isWifiOnly()) {
             getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTINGS));
         }
 
        if (!WIFI_P2P_DEBUG) {
             getPreferenceScreen().removePreference(findPreference(KEY_WIFI_P2P_SETTINGS));
         }
 
         // Enable Proxy selector settings if allowed.
         Preference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
         DevicePolicyManager mDPM = (DevicePolicyManager)
                 activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
         // proxy UI disabled until we have better app support
         getPreferenceScreen().removePreference(mGlobalProxy);
         mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);
 
         // Disable Tethering if it's not allowed or if it's a wifi-only device
         ConnectivityManager cm =
                 (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
         if (!cm.isTetheringSupported()) {
             getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
         } else {
             String[] usbRegexs = cm.getTetherableUsbRegexs();
             String[] wifiRegexs = cm.getTetherableWifiRegexs();
             String[] bluetoothRegexs = cm.getTetherableBluetoothRegexs();
 
             boolean usbAvailable = usbRegexs.length != 0;
             boolean wifiAvailable = wifiRegexs.length != 0;
             boolean bluetoothAvailable = bluetoothRegexs.length != 0;
 
             Preference p = findPreference(KEY_TETHER_SETTINGS);
             if (wifiAvailable && usbAvailable && bluetoothAvailable) {
                 p.setTitle(R.string.tether_settings_title_all);
             } else if (wifiAvailable && usbAvailable) {
                 p.setTitle(R.string.tether_settings_title_all);
             } else if (wifiAvailable && bluetoothAvailable) {
                 p.setTitle(R.string.tether_settings_title_all);
             } else if (wifiAvailable) {
                 p.setTitle(R.string.tether_settings_title_wifi);
             } else if (usbAvailable && bluetoothAvailable) {
                 p.setTitle(R.string.tether_settings_title_usb_bluetooth);
             } else if (usbAvailable) {
                 p.setTitle(R.string.tether_settings_title_usb);
             } else {
                 p.setTitle(R.string.tether_settings_title_bluetooth);
             }
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         mAirplaneModeEnabler.resume();
         if (mNfcEnabler != null) {
             mNfcEnabler.resume();
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         mAirplaneModeEnabler.pause();
         if (mNfcEnabler != null) {
             mNfcEnabler.pause();
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_EXIT_ECM) {
             Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
             // Set Airplane mode based on the return value and checkbox state
             mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                     mAirplaneModePreference.isChecked());
         }
     }
 }
