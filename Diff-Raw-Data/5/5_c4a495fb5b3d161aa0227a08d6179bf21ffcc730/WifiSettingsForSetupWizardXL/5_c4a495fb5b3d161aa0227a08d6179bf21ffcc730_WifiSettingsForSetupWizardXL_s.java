 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.settings.wifi;
 
 import com.android.settings.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.NetworkInfo.DetailedState;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceCategory;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.android.internal.util.AsyncChannel;
 
 import java.util.Collection;
 import java.util.EnumMap;
 import java.util.List;
 
 /**
  * WifiSetings Activity specific for SetupWizard with X-Large screen size.
  */
 public class WifiSettingsForSetupWizardXL extends Activity implements OnClickListener {
     private static final String TAG = "SetupWizard";
     private static final boolean DEBUG = true;
 
     private static final EnumMap<DetailedState, DetailedState> sNetworkStateMap =
             new EnumMap<DetailedState, DetailedState>(DetailedState.class);
 
     static {
         sNetworkStateMap.put(DetailedState.IDLE, DetailedState.DISCONNECTED);
         sNetworkStateMap.put(DetailedState.SCANNING, DetailedState.SCANNING);
         sNetworkStateMap.put(DetailedState.CONNECTING, DetailedState.CONNECTING);
         sNetworkStateMap.put(DetailedState.AUTHENTICATING, DetailedState.CONNECTING);
         sNetworkStateMap.put(DetailedState.OBTAINING_IPADDR, DetailedState.CONNECTING);
         sNetworkStateMap.put(DetailedState.CONNECTED, DetailedState.CONNECTED);
         sNetworkStateMap.put(DetailedState.SUSPENDED, DetailedState.SUSPENDED);  // ?
         sNetworkStateMap.put(DetailedState.DISCONNECTING, DetailedState.DISCONNECTED);
         sNetworkStateMap.put(DetailedState.DISCONNECTED, DetailedState.DISCONNECTED);
         sNetworkStateMap.put(DetailedState.FAILED, DetailedState.FAILED);
     }
 
     /**
      * Used with {@link Button#setTag(Object)} to remember "Connect" button is pressed in
      * with "add network" flow.
      */
     private static final int CONNECT_BUTTON_TAG_ADD_NETWORK = 1;
 
     private WifiSettings mWifiSettings;
     private WifiManager mWifiManager;
 
     /** Used for resizing a padding above title. Hiden when software keyboard is shown. */
     private View mTopPadding;
 
     /** Used for resizing a padding inside Config UI. Hiden when software keyboard is shown. */
     private View mWifiConfigPadding;
 
     private TextView mTitleView;
     /**
      * The name of a network currently connecting, or trying to connect.
      * This may be empty ("") at first, and updated when configuration is changed.
      */
     private CharSequence mNetworkName = "";
     private CharSequence mEditingTitle;
 
     private ProgressBar mProgressBar;
     private View mTopDividerNoProgress;
     /**
      * Used for resizing a padding between WifiSettings preference and bottom bar when
      * ProgressBar is visible as a top divider.
      */
     private View mBottomPadding;
 
     private Button mAddNetworkButton;
     private Button mRefreshButton;
     private Button mSkipOrNextButton;
     private Button mBackButton;
 
     private Button mConnectButton;
 
     /**
      * View enclosing {@link WifiSettings}.
      */
     private View mWifiSettingsFragmentLayout;
     private View mConnectingStatusLayout;
     private TextView mConnectingStatusView;
 
     /*
      * States of current screen, which should be saved and restored when Activity is relaunched
      * with orientation change, etc.
      */
     private static final int SCREEN_STATE_DISCONNECTED = 0;
     private static final int SCREEN_STATE_EDITING = 1;
     private static final int SCREEN_STATE_CONNECTING = 2;
     private static final int SCREEN_STATE_CONNECTED = 3;
 
     /** Current screen state. */
     private int mScreenState = SCREEN_STATE_DISCONNECTED;
 
     private WifiConfigUiForSetupWizardXL mWifiConfig;
 
     private InputMethodManager mInputMethodManager;
 
     /**
      * Used to store View visibility status.
      *
      * We store the status when we show "connecting" screen and use the stored data when the
      * device failed to connect to the network.
      */
     private int mPreviousWpsFieldsVisibility = View.GONE;
     private int mPreviousSecurityFieldsVisibility = View.GONE;
     private int mPreviousTypeVisibility = View.GONE;
 
     /**
      * Previous network connection state reported by main Wifi module.
      *
      * Note that we don't use original {@link DetailedState} object but simplified one translated
      * using sNetworkStateMap.
      */
     private DetailedState mPreviousNetworkState = DetailedState.DISCONNECTED;
 
     private int mBackgroundId = R.drawable.setups_bg_default;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.wifi_settings_for_setup_wizard_xl);
 
         mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
         // There's no button here enabling wifi network, so we need to enable it without
         // users' request.
         mWifiManager.setWifiEnabled(true);
         mWifiManager.asyncConnect(this, new WifiServiceHandler());
 
         mWifiSettings =
                 (WifiSettings)getFragmentManager().findFragmentById(R.id.wifi_setup_fragment);
         mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 
         initViews();
 
         // At first, Wifi module doesn't return SCANNING state (it's too early), so we manually
         // show it.
         showScanningStatus();
     }
 
     private void initViews() {
         if (getIntent().getBooleanExtra("firstRun", false)) {
             final View layoutRoot = findViewById(R.id.layout_root);
             layoutRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_BACK);
         }
 
         mTitleView = (TextView)findViewById(R.id.wifi_setup_title);
         mProgressBar = (ProgressBar)findViewById(R.id.scanning_progress_bar);
         mProgressBar.setMax(2);
         mTopDividerNoProgress = findViewById(R.id.top_divider_no_progress);
         mBottomPadding = findViewById(R.id.bottom_padding);
 
         mProgressBar.setVisibility(View.VISIBLE);
         mProgressBar.setIndeterminate(true);
         mTopDividerNoProgress.setVisibility(View.GONE);
 
         mAddNetworkButton = (Button)findViewById(R.id.wifi_setup_add_network);
         mAddNetworkButton.setOnClickListener(this);
         mRefreshButton = (Button)findViewById(R.id.wifi_setup_refresh_list);
         mRefreshButton.setOnClickListener(this);
         mSkipOrNextButton = (Button)findViewById(R.id.wifi_setup_skip_or_next);
         mSkipOrNextButton.setOnClickListener(this);
         mConnectButton = (Button)findViewById(R.id.wifi_setup_connect);
         mConnectButton.setOnClickListener(this);
         mBackButton = (Button)findViewById(R.id.wifi_setup_cancel);
         mBackButton.setOnClickListener(this);
 
         mTopPadding = findViewById(R.id.top_padding);
         mWifiConfigPadding = findViewById(R.id.wifi_config_padding);
 
         mWifiSettingsFragmentLayout = findViewById(R.id.wifi_settings_fragment_layout);
         mConnectingStatusLayout = findViewById(R.id.connecting_status_layout);
         mConnectingStatusView = (TextView) findViewById(R.id.connecting_status);
     }
 
     private class WifiServiceHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                     if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                         //AsyncChannel in msg.obj
                     } else {
                         //AsyncChannel set up failure, ignore
                         Log.e(TAG, "Failed to establish AsyncChannel connection");
                     }
                     break;
                 default:
                     //Ignore
                     break;
             }
         }
     }
 
     private void restoreFirstButtonVisibilityState() {
         showDefaultTitle();
         mAddNetworkButton.setVisibility(View.VISIBLE);
         mRefreshButton.setVisibility(View.VISIBLE);
         mSkipOrNextButton.setVisibility(View.VISIBLE);
         mConnectButton.setVisibility(View.GONE);
         mBackButton.setVisibility(View.GONE);
         setPaddingVisibility(View.VISIBLE, View.GONE);
     }
 
     @Override
     public void onClick(View view) {
         hideSoftwareKeyboard();
         if (view == mAddNetworkButton) {
             if (DEBUG) Log.d(TAG, "AddNetwork button pressed");
             onAddNetworkButtonPressed();
         } else if (view == mRefreshButton) {
             if (DEBUG) Log.d(TAG, "Refresh button pressed");
             refreshAccessPoints(true);
         } else if (view == mSkipOrNextButton) {
             if (DEBUG) Log.d(TAG, "Skip/Next button pressed");
             if (TextUtils.equals(getString(R.string.wifi_setup_skip), ((Button)view).getText())) {
                 // We don't want to let Wifi enabled when a user press skip without choosing
                 // any access point.
                 mWifiManager.setWifiEnabled(false);
             }
             setResult(RESULT_OK);
             finish();
         } else if (view == mConnectButton) {
             if (DEBUG) Log.d(TAG, "Connect button pressed");
             onConnectButtonPressed();
         } else if (view == mBackButton) {
             if (DEBUG) Log.d(TAG, "Back button pressed");
             onBackButtonPressed();
         }
     }
 
     private void hideSoftwareKeyboard() {
         if (DEBUG) Log.i(TAG, "Hiding software keyboard.");
         final View focusedView = getCurrentFocus();
         if (focusedView != null) {
             mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
         }
     }
 
     // Called from WifiSettings
     /* package */ void updateConnectionState(DetailedState originalState) {
         final DetailedState state = sNetworkStateMap.get(originalState);
 
         if (originalState == DetailedState.FAILED) {
             // We clean up the current connectivity status and let users select another network
             // if they want.
             refreshAccessPoints(true);
         }
 
         switch (state) {
         case SCANNING: {
             // Let users know the device is working correctly though currently there's
             // no visible network on the list.
             if (mWifiSettings.getAccessPointsCount() == 0) {
                 showScanningProgressBar();
             } else {
                 // Users already see available networks.
                 showDisconnectedProgressBar();
             }
             break;
         }
         case CONNECTING: {
            if (mScreenState != SCREEN_STATE_DISCONNECTED &&
                    mScreenState != SCREEN_STATE_CONNECTED) {
                 showConnectingState();
             }
             break;
         }
         case CONNECTED: {
             showConnectedState();
             break;
         }
         default:  // DISCONNECTED, FAILED
             if (mScreenState != SCREEN_STATE_CONNECTED) {
                 showDisconnectedState(Summary.get(this, state));
             }
             break;
         }
         mPreviousNetworkState = state;
     }
 
     private void showDisconnectedState(String stateString) {
         showDisconnectedProgressBar();
         mAddNetworkButton.setEnabled(true);
         mRefreshButton.setEnabled(true);
     }
 
     private void showConnectingState() {
         mScreenState = SCREEN_STATE_CONNECTING;
 
         mBackButton.setVisibility(View.VISIBLE);
         // We save this title and show it when authentication failed.
         mEditingTitle = mTitleView.getText();
         showConnectingTitle();
         showConnectingProgressBar();
 
         setPaddingVisibility(View.VISIBLE);
     }
 
     private void showConnectedState() {
         // Once we show "connected" screen, we won't change it even when the device becomes
         // disconnected afterwards. We keep the state unless a user explicitly cancel it
         // (by pressing "back" button).
         mScreenState = SCREEN_STATE_CONNECTED;
 
         hideSoftwareKeyboard();
         setPaddingVisibility(View.VISIBLE);
 
         trySetBackground(R.drawable.setups_bg_complete);
         showConnectedTitle();
         showConnectedProgressBar();
 
         mWifiSettingsFragmentLayout.setVisibility(View.GONE);
         mConnectingStatusLayout.setVisibility(View.VISIBLE);
 
         mConnectingStatusView.setText(R.string.wifi_setup_description_connected);
         mConnectButton.setVisibility(View.GONE);
         mAddNetworkButton.setVisibility(View.GONE);
         mRefreshButton.setVisibility(View.GONE);
         mBackButton.setVisibility(View.VISIBLE);
         mBackButton.setText(R.string.wifi_setup_back);
         mSkipOrNextButton.setVisibility(View.VISIBLE);
         mSkipOrNextButton.setEnabled(true);
     }
 
     private void showDefaultTitle() {
         mTitleView.setText(getString(R.string.wifi_setup_title));
     }
 
     private void showAddNetworkTitle() {
         mNetworkName = "";
         mTitleView.setText(R.string.wifi_setup_title_add_network);
     }
 
     private void showEditingTitle() {
         if (TextUtils.isEmpty(mNetworkName) && mWifiConfig != null) {
             if (mWifiConfig.getController() != null &&
                 mWifiConfig.getController().getConfig() != null) {
                 mNetworkName = mWifiConfig.getController().getConfig().SSID;
             } else {
                 Log.w(TAG, "Unexpected null found (WifiController or WifiConfig is null). " +
                         "Ignore them.");
             }
         }
         mTitleView.setText(getString(R.string.wifi_setup_title_editing_network, mNetworkName));
     }
 
     private void showConnectingTitle() {
         if (TextUtils.isEmpty(mNetworkName) && mWifiConfig != null) {
             if (mWifiConfig.getController() != null &&
                     mWifiConfig.getController().getConfig() != null) {
                 mNetworkName = mWifiConfig.getController().getConfig().SSID;
             } else {
                 Log.w(TAG, "Unexpected null found (WifiController or WifiConfig is null). " +
                         "Ignore them.");
             }
         }
         mTitleView.setText(getString(R.string.wifi_setup_title_connecting_network, mNetworkName));
     }
 
     private void showConnectedTitle() {
         if (TextUtils.isEmpty(mNetworkName) && mWifiConfig != null) {
             if (mWifiConfig.getController() != null &&
                     mWifiConfig.getController().getConfig() != null) {
                 mNetworkName = mWifiConfig.getController().getConfig().SSID;
             } else {
                 Log.w(TAG, "Unexpected null found (WifiController or WifiConfig is null). " +
                         "Ignore them.");
             }
         }
         mTitleView.setText(getString(R.string.wifi_setup_title_connected_network, mNetworkName));
     }
 
     /**
      * Shows top divider with ProgressBar without defining the state of the ProgressBar.
      *
      * @see #showScanningProgressBar()
      * @see #showConnectedProgressBar()
      * @see #showConnectingProgressBar()
      */
     private void showTopDividerWithProgressBar() {
         mProgressBar.setVisibility(View.VISIBLE);
         mTopDividerNoProgress.setVisibility(View.GONE);
         mBottomPadding.setVisibility(View.GONE);
     }
 
     private void showScanningStatus() {
         showScanningProgressBar();
     }
 
     private void onAddNetworkButtonPressed() {
         mWifiSettings.onAddNetworkPressed();
     }
 
     /**
      * Called when the screen enters wifi configuration UI. UI widget for configuring network
      * (a.k.a. ConfigPreference) should be taken care of by caller side.
      * This method should handle buttons' visibility/enabled.
      * @param selectedAccessPoint AccessPoint object being selected. null when a user pressed
      * "Add network" button, meaning there's no selected access point.
      */
     /* package */ void showConfigUi(AccessPoint selectedAccessPoint, boolean edit) {
         if (selectedAccessPoint != null &&
                 (selectedAccessPoint.security == AccessPoint.SECURITY_WEP ||
                         selectedAccessPoint.security == AccessPoint.SECURITY_PSK)) {
             // We forcibly set edit as true so that users can modify every field if they want,
             // while config UI doesn't allow them to edit some of them when edit is false
             // (e.g. password field is hiden when edit==false).
             edit = true;
         }
 
         trySetBackground(R.drawable.setups_bg_default);
 
         // We don't want to keep scanning Wifi networks during users' configuring a network.
         mWifiSettings.pauseWifiScan();
 
         mWifiSettingsFragmentLayout.setVisibility(View.GONE);
         mConnectingStatusLayout.setVisibility(View.GONE);
         final ViewGroup parent = (ViewGroup)findViewById(R.id.wifi_config_ui);
         parent.setVisibility(View.VISIBLE);
         parent.removeAllViews();
         mWifiConfig = new WifiConfigUiForSetupWizardXL(this, parent, selectedAccessPoint, edit);
 
         // Tag will be updated in this method when needed.
         mConnectButton.setTag(null);
         if (selectedAccessPoint == null) {  // "Add network" flow
             showAddNetworkTitle();
             mConnectButton.setVisibility(View.VISIBLE);
             mConnectButton.setTag(CONNECT_BUTTON_TAG_ADD_NETWORK);
 
             showEditingButtonState();
         } else if (selectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
             mNetworkName = selectedAccessPoint.getTitle().toString();
 
             // onConnectButtonPressed() will change visibility status.
             mConnectButton.performClick();
         } else {
             mNetworkName = selectedAccessPoint.getTitle().toString();
             showEditingTitle();
             showEditingButtonState();
             if (selectedAccessPoint.security == AccessPoint.SECURITY_EAP) {
                 onEapNetworkSelected();
             } else {
                 mConnectButton.setVisibility(View.VISIBLE);
 
                 // WifiConfigController shows Connect button as "Save" when edit==true and a user
                 // tried to connect the network.
                 // In SetupWizard, we just show the button as "Connect" instead.
                 mConnectButton.setText(R.string.wifi_connect);
                 mBackButton.setText(R.string.wifi_setup_cancel);
             }
         }
     }
 
     /**
      * Called before security fields are correctly set by {@link WifiConfigController}.
      *
      * @param view security field view
      * @param accessPointSecurity type of security. e.g. AccessPoint.SECURITY_NONE
      * @return true when it is ok for the caller to init security fields. false when
      * all security fields are managed by this method, and thus the caller shouldn't touch them.
      */
     /* package */ boolean initSecurityFields(View view, int accessPointSecurity) {
         // Reset all states tweaked below.
         view.findViewById(R.id.eap_not_supported).setVisibility(View.GONE);
         view.findViewById(R.id.eap_not_supported_for_add_network).setVisibility(View.GONE);
         view.findViewById(R.id.ssid_text).setVisibility(View.VISIBLE);
         view.findViewById(R.id.ssid_layout).setVisibility(View.VISIBLE);
 
         if (accessPointSecurity == AccessPoint.SECURITY_EAP) {
             hideSoftwareKeyboard();
 
             // In SetupWizard for XLarge screen, we don't have enough space for showing
             // configurations needed for EAP. We instead disable the whole feature there and let
             // users configure those networks after the setup.
             if (view.findViewById(R.id.type).getVisibility() == View.VISIBLE) {
                 view.findViewById(R.id.eap_not_supported_for_add_network)
                         .setVisibility(View.VISIBLE);
             } else {
                 view.findViewById(R.id.eap_not_supported).setVisibility(View.VISIBLE);
             }
             view.findViewById(R.id.security_fields).setVisibility(View.GONE);
             view.findViewById(R.id.ssid_text).setVisibility(View.GONE);
             view.findViewById(R.id.ssid_layout).setVisibility(View.GONE);
             onEapNetworkSelected();
 
             // This method did init security fields by itself. The caller must not do it.
             return false;
         }
 
         // Let the caller init security fields.
         return true;
     }
 
     /* package */ void onEapNetworkSelected() {
         mConnectButton.setVisibility(View.GONE);
         mBackButton.setText(R.string.wifi_setup_back);
     }
 
     private void showEditingButtonState() {
         mSkipOrNextButton.setVisibility(View.GONE);
         mAddNetworkButton.setVisibility(View.GONE);
         mRefreshButton.setVisibility(View.GONE);
         mBackButton.setVisibility(View.VISIBLE);
     }
 
     // May be called when user press "connect" button in WifiDialog
     /* package */ void onConnectButtonPressed() {
         mScreenState = SCREEN_STATE_CONNECTING;
 
         trySetBackground(R.drawable.setups_bg_wifi);
 
         mWifiSettings.submit(mWifiConfig.getController());
 
         // updateConnectionState() isn't called soon by the main Wifi module after the user's
         // "connect" request, and the user still sees "not connected" message for a while, which
         // looks strange for users though legitimate from the view of the module.
         //
         // We instead manually show "connecting" message before the system gets actual
         // "connecting" message from Wifi module.
         showConnectingState();
 
         // Might be better to delay showing this button.
         mBackButton.setVisibility(View.VISIBLE);
         mBackButton.setText(R.string.wifi_setup_back);
 
         // We need to restore visibility status when the device failed to connect the network.
         final View wpsFieldView = findViewById(R.id.wps_fields);
         if (wpsFieldView != null) {
             mPreviousWpsFieldsVisibility = wpsFieldView.getVisibility();
             wpsFieldView.setVisibility(View.GONE);
         }
         final View securityFieldsView = findViewById(R.id.security_fields);
         if (securityFieldsView != null) {
             mPreviousSecurityFieldsVisibility = securityFieldsView.getVisibility();
             securityFieldsView.setVisibility(View.GONE);
         }
         final View typeView = findViewById(R.id.type);
         if (typeView != null) {
             mPreviousTypeVisibility = typeView.getVisibility();
             typeView.setVisibility(View.GONE);
         }
 
         // TODO: investigate whether visibility handling above is needed. Now that we hide
         // them completely when connecting, so we may not need to do so, though we probably
         // need to show software keyboard conditionaly.
         final ViewGroup parent = (ViewGroup)findViewById(R.id.wifi_config_ui);
         parent.setVisibility(View.GONE);
         mConnectingStatusLayout.setVisibility(View.VISIBLE);
         mConnectingStatusView.setText(R.string.wifi_setup_description_connecting);
 
         mSkipOrNextButton.setVisibility(View.VISIBLE);
         mSkipOrNextButton.setEnabled(false);
         mConnectButton.setVisibility(View.GONE);
         mAddNetworkButton.setVisibility(View.GONE);
         mRefreshButton.setVisibility(View.GONE);
     }
 
     private void onBackButtonPressed() {
         trySetBackground(R.drawable.setups_bg_default);
 
         if (mScreenState == SCREEN_STATE_CONNECTING || mScreenState == SCREEN_STATE_CONNECTED) {
             if (DEBUG) Log.d(TAG, "Back button pressed after connect action.");
             mScreenState = SCREEN_STATE_DISCONNECTED;
 
             // When a user press "Back" button after pressing "Connect" button, we want to cancel
             // the "Connect" request and refresh the whole Wifi status.
             restoreFirstButtonVisibilityState();
 
             mSkipOrNextButton.setEnabled(true);
             changeNextButtonState(false);  // Skip
 
             // Wifi list becomes empty for a moment. We show "scanning" effect to a user so that
             // he/she won't be astonished there. This stops once the scan finishes.
             showScanningProgressBar();
 
             // Remembered networks may be re-used during SetupWizard, which confuse users.
             // We force the module to forget them to reduce UX complexity
             final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
             for (WifiConfiguration config : configs) {
                 if (DEBUG) {
                     Log.d(TAG, String.format("forgeting Wi-Fi network \"%s\" (id: %d)",
                             config.SSID, config.networkId));
                 }
                 mWifiManager.forgetNetwork(config.networkId);
             }
 
             refreshAccessPoints(true);
         } else { // During user's Wifi configuration.
             mWifiSettings.resumeWifiScan();
 
             restoreFirstButtonVisibilityState();
 
             mAddNetworkButton.setEnabled(true);
             mRefreshButton.setEnabled(true);
             mSkipOrNextButton.setEnabled(true);
         }
 
         mWifiSettingsFragmentLayout.setVisibility(View.VISIBLE);
         mConnectingStatusLayout.setVisibility(View.GONE);
         final ViewGroup parent = (ViewGroup)findViewById(R.id.wifi_config_ui);
         parent.removeAllViews();
         parent.setVisibility(View.GONE);
         mWifiConfig = null;
     }
 
     /**
      * @param connected true when the device is connected to a specific network.
      */
     /* package */ void changeNextButtonState(boolean connected) {
         if (connected) {
             mSkipOrNextButton.setText(R.string.wifi_setup_next);
         } else {
             mSkipOrNextButton.setText(R.string.wifi_setup_skip);
         }
     }
 
     /**
      * Called when the list of AccessPoints are modified and this Activity needs to refresh
      * the list.
      */
     /* package */ void onAccessPointsUpdated(
             PreferenceCategory holder, Collection<AccessPoint> accessPoints) {
         // If we already show some of access points but the bar still shows "scanning" state, it
         // should be stopped.
         if (mProgressBar.isIndeterminate() && accessPoints.size() > 0) {
             showTopDividerWithProgressBar();
             mAddNetworkButton.setEnabled(true);
             mRefreshButton.setEnabled(true);
         }
 
         for (AccessPoint accessPoint : accessPoints) {
             accessPoint.setLayoutResource(R.layout.custom_preference);
             holder.addPreference(accessPoint);
         }
     }
 
     private void refreshAccessPoints(boolean disconnectNetwork) {
         final Object tag = mConnectButton.getTag();
         if (tag != null && (tag instanceof Integer) &&
                 ((Integer)tag == CONNECT_BUTTON_TAG_ADD_NETWORK)) {
             // In "Add network" flow, we won't get DetaledState available for changing ProgressBar
             // state. Instead we manually show previous status here.
             showDisconnectedState(Summary.get(this, mPreviousNetworkState));
         } else {
             showScanningStatus();
         }
 
         if (disconnectNetwork) {
             mWifiManager.disconnect();
         }
 
         mWifiSettings.refreshAccessPoints();
     }
 
     /**
      * Called when {@link WifiSettings} received
      * {@link WifiManager#SUPPLICANT_STATE_CHANGED_ACTION}.
      */
     /* package */ void onSupplicantStateChanged(Intent intent) {
         final int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
         if (errorCode == WifiManager.ERROR_AUTHENTICATING) {
             Log.i(TAG, "Received authentication error event.");
             onAuthenticationFailure();
         }
     }
 
     /**
      * Called once when Authentication failed.
      */
     private void onAuthenticationFailure() {
         mScreenState = SCREEN_STATE_EDITING;
 
         mSkipOrNextButton.setVisibility(View.GONE);
         mConnectButton.setVisibility(View.VISIBLE);
         mConnectButton.setEnabled(true);
 
         trySetBackground(R.drawable.setups_bg_default);
 
         if (!TextUtils.isEmpty(mEditingTitle)) {
             mTitleView.setText(mEditingTitle);
         } else {
             Log.w(TAG, "Title during editing/adding a network was empty.");
             showEditingTitle();
         }
 
         final ViewGroup parent = (ViewGroup)findViewById(R.id.wifi_config_ui);
         parent.setVisibility(View.VISIBLE);
         mConnectingStatusLayout.setVisibility(View.GONE);
 
         // Restore View status which was tweaked on connection.
         final View wpsFieldView = findViewById(R.id.wps_fields);
         if (wpsFieldView != null) {
             wpsFieldView.setVisibility(mPreviousWpsFieldsVisibility);
         }
         final View securityFieldsView = findViewById(R.id.security_fields);
         if (securityFieldsView != null) {
             securityFieldsView.setVisibility(mPreviousSecurityFieldsVisibility);
             if (mPreviousSecurityFieldsVisibility == View.VISIBLE && mWifiConfig != null) {
                 final View passwordView = findViewById(R.id.password);
                 if (passwordView != null) {
                     if (passwordView.isFocused()) {
                         setPaddingVisibility(View.GONE);
                     }
                     mWifiConfig.requestFocusAndShowKeyboard(R.id.password);
                 }
             }
         }
         final View typeView = findViewById(R.id.type);
         if (typeView != null) {
             typeView.setVisibility(mPreviousTypeVisibility);
             if (mPreviousTypeVisibility == View.VISIBLE && mWifiConfig != null) {
                 final View ssidView = findViewById(R.id.ssid);
                 if (ssidView != null) {
                     if (ssidView.isFocused()) {
                         setPaddingVisibility(View.GONE);
                     }
                     mWifiConfig.requestFocusAndShowKeyboard(R.id.ssid);
                 }
             }
         }
     }
 
     /** Note: doesn't affect bottom padding */
     // Used by WifiConfigUiForSetupWizardXL
     /* package */ void setPaddingVisibility(int visibility) {
         setPaddingVisibility(visibility, visibility);
     }
 
     /** Note: doesn't affect bottom padding */
     private void setPaddingVisibility(int topPaddingVisibility, int configVisibility) {
         mTopPadding.setVisibility(topPaddingVisibility);
         mWifiConfigPadding.setVisibility(configVisibility);
     }
 
     private void showDisconnectedProgressBar() {
         mProgressBar.setVisibility(View.GONE);
         mProgressBar.setIndeterminate(false);
         mTopDividerNoProgress.setVisibility(View.VISIBLE);
         mBottomPadding.setVisibility(View.VISIBLE);
     }
 
     /**
      * Shows top divider with ProgressBar, whose state is intermediate.
      */
     private void showScanningProgressBar() {
         showTopDividerWithProgressBar();
         mProgressBar.setIndeterminate(true);
     }
 
     /**
      * Shows top divider with ProgressBar, showing "connecting" state.
      */
     private void showConnectingProgressBar() {
         showTopDividerWithProgressBar();
         mProgressBar.setIndeterminate(false);
         mProgressBar.setProgress(1);
     }
 
     private void showConnectedProgressBar() {
         showTopDividerWithProgressBar();
         mProgressBar.setIndeterminate(false);
         mProgressBar.setProgress(2);
     }
 
     /**
      * Called when WifiManager is requested to save a network.
      */
     /* package */ void onSaveNetwork(WifiConfiguration config) {
         // We want to both save and connect a network. connectNetwork() does both.
         mWifiManager.connectNetwork(config);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, view, menuInfo);
     }
 
     /**
      * Replace the current background with a new background whose id is resId if needed.
      */
     private void trySetBackground(int resId) {
         if (mBackgroundId != resId) {
             getWindow().setBackgroundDrawable(getResources().getDrawable(resId));
             mBackgroundId = resId;
         }
     }
 }
