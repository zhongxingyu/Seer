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
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.net.DhcpInfo;
 import android.net.LinkAddress;
 import android.net.LinkProperties;
 import android.net.NetworkInfo.DetailedState;
 import android.net.NetworkUtils;
 import android.net.Proxy;
 import android.net.ProxyProperties;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiConfiguration.IpAssignment;
 import android.net.wifi.WifiConfiguration.AuthAlgorithm;
 import android.net.wifi.WifiConfiguration.KeyMgmt;
 import android.net.wifi.WpsConfiguration;
 import android.net.wifi.WpsConfiguration.Setup;
 
 import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
 import android.net.wifi.WifiConfiguration.ProxySettings;
 import android.net.wifi.WifiInfo;
 import android.security.Credentials;
 import android.security.KeyStore;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.settings.ProxySelector;
 import com.android.settings.R;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 /**
  * The class for allowing UIs like {@link WifiDialog} and {@link WifiConfigPreference} to
  * share the logic for controlling buttons, text fields, etc.
  */
 public class WifiConfigController implements TextWatcher,
         View.OnClickListener, AdapterView.OnItemSelectedListener {
     private static final String KEYSTORE_SPACE = "keystore://";
 
     private final WifiConfigUiBase mConfigUi;
     private final View mView;
     private final AccessPoint mAccessPoint;
 
     private boolean mEdit;
 
     private TextView mSsidView;
 
     // e.g. AccessPoint.SECURITY_NONE
     private int mAccessPointSecurity;
     private TextView mPasswordView;
 
     private Spinner mSecuritySpinner;
     private Spinner mEapMethodSpinner;
     private Spinner mEapCaCertSpinner;
     private Spinner mPhase2Spinner;
     private Spinner mEapUserCertSpinner;
     private TextView mEapIdentityView;
     private TextView mEapAnonymousView;
 
     /* This value comes from "wifi_ip_settings" resource array */
     private static final int DHCP = 0;
     private static final int STATIC_IP = 1;
 
     /* These values come from "wifi_network_setup" resource array */
     public static final int MANUAL = 0;
     public static final int WPS_PBC = 1;
     public static final int WPS_PIN_FROM_ACCESS_POINT = 2;
     public static final int WPS_PIN_FROM_DEVICE = 3;
 
     /* These values come from "wifi_proxy_settings" resource array */
     public static final int PROXY_NONE = 0;
     public static final int PROXY_STATIC = 1;
 
     private static final String TAG = "WifiConfigController";
 
     private Spinner mNetworkSetupSpinner;
     private Spinner mIpSettingsSpinner;
     private TextView mIpAddressView;
     private TextView mGatewayView;
     private TextView mNetworkPrefixLengthView;
     private TextView mDns1View;
     private TextView mDns2View;
 
     private Spinner mProxySettingsSpinner;
     private TextView mProxyHostView;
     private TextView mProxyPortView;
     private TextView mProxyExclusionListView;
 
     private IpAssignment mIpAssignment;
     private ProxySettings mProxySettings;
     private LinkProperties mLinkProperties = new LinkProperties();
 
     // True when this instance is used in SetupWizard XL context.
     private final boolean mInXlSetupWizard;
 
     static boolean requireKeyStore(WifiConfiguration config) {
         if (config == null) {
             return false;
         }
         String values[] = {config.ca_cert.value(), config.client_cert.value(),
                 config.private_key.value()};
         for (String value : values) {
             if (value != null && value.startsWith(KEYSTORE_SPACE)) {
                 return true;
             }
         }
         return false;
     }
 
     public WifiConfigController(
             WifiConfigUiBase parent, View view, AccessPoint accessPoint, boolean edit) {
         mConfigUi = parent;
         mInXlSetupWizard = (parent instanceof WifiConfigUiForSetupWizardXL);
 
         mView = view;
         mAccessPoint = accessPoint;
         mAccessPointSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE :
                 accessPoint.security;
         mEdit = edit;
 
         final Context context = mConfigUi.getContext();
         final Resources resources = context.getResources();
 
         if (mAccessPoint == null) { // new network
             mConfigUi.setTitle(R.string.wifi_add_network);
             mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
             mSsidView = (TextView) mView.findViewById(R.id.ssid);
             mSsidView.addTextChangedListener(this);
             mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
             mSecuritySpinner.setOnItemSelectedListener(this);
             if (context instanceof WifiSettingsForSetupWizardXL) {
                 // We want custom layout. The content must be same as the other cases.
                 mSecuritySpinner.setAdapter(
                         new ArrayAdapter<String>(context, R.layout.wifi_setup_custom_list_item_1,
                                 android.R.id.text1,
                                 context.getResources().getStringArray(R.array.wifi_security)));
             }
             mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
         } else {
             mConfigUi.setTitle(mAccessPoint.ssid);
 
             mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
             mIpSettingsSpinner.setOnItemSelectedListener(this);
             mProxySettingsSpinner = (Spinner) mView.findViewById(R.id.proxy_settings);
            // disable proxy UI until we have better app support
            mProxySettingsSpinner.setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_settings_title).setVisibility(View.GONE);
             mProxySettingsSpinner.setOnItemSelectedListener(this);
 
             ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);
 
             DetailedState state = mAccessPoint.getState();
             if (state != null) {
                 addRow(group, R.string.wifi_status, Summary.get(mConfigUi.getContext(), state));
             }
 
             String[] type = resources.getStringArray(R.array.wifi_security);
             addRow(group, R.string.wifi_security, type[mAccessPoint.security]);
 
             int level = mAccessPoint.getLevel();
             if (level != -1) {
                 String[] signal = resources.getStringArray(R.array.wifi_signal);
                 addRow(group, R.string.wifi_signal, signal[level]);
             }
 
             WifiInfo info = mAccessPoint.getInfo();
             if (info != null) {
                 addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
                 // TODO: fix the ip address for IPv6.
                 int address = info.getIpAddress();
                 if (address != 0) {
                     addRow(group, R.string.wifi_ip_address, Formatter.formatIpAddress(address));
                 }
             }
 
             if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                 WifiConfiguration config = mAccessPoint.getConfig();
                 if (config.ipAssignment == IpAssignment.STATIC) {
                     mIpSettingsSpinner.setSelection(STATIC_IP);
                 } else {
                     mIpSettingsSpinner.setSelection(DHCP);
                 }
 
                 if (config.proxySettings == ProxySettings.STATIC) {
                     mProxySettingsSpinner.setSelection(PROXY_STATIC);
                 } else {
                     mProxySettingsSpinner.setSelection(PROXY_NONE);
                 }
             }
 
             /* Show network setup options only for a new network */
             if (mAccessPoint.networkId == INVALID_NETWORK_ID && mAccessPoint.wpsAvailable) {
                 showNetworkSetupFields();
             }
 
             if (mAccessPoint.networkId == INVALID_NETWORK_ID || mEdit) {
                 showSecurityFields();
                 showIpConfigFields();
                 showProxyFields();
             }
 
             if (mEdit) {
                 mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
             } else {
                 if (state == null && level != -1) {
                     mConfigUi.setSubmitButton(context.getString(R.string.wifi_connect));
                 } else {
                     mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
                 }
                 if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                     mConfigUi.setForgetButton(context.getString(R.string.wifi_forget));
                 }
             }
         }
 
 
         mConfigUi.setCancelButton(context.getString(R.string.wifi_cancel));
         if (mConfigUi.getSubmitButton() != null) {
             enableSubmitIfAppropriate();
         }
     }
 
     private void addRow(ViewGroup group, int name, String value) {
         View row = mConfigUi.getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
         ((TextView) row.findViewById(R.id.name)).setText(name);
         ((TextView) row.findViewById(R.id.value)).setText(value);
         group.addView(row);
     }
 
     /* show submit button if the password is valid */
     private void enableSubmitIfAppropriate() {
         if ((mSsidView != null && mSsidView.length() == 0) ||
             ((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
             ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mPasswordView.length() == 0) ||
             (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mPasswordView.length() < 8)))) {
             mConfigUi.getSubmitButton().setEnabled(false);
         } else {
             mConfigUi.getSubmitButton().setEnabled(true);
         }
     }
 
     /* package */ WifiConfiguration getConfig() {
         if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
             return null;
         }
 
         WifiConfiguration config = new WifiConfiguration();
 
         if (mAccessPoint == null) {
             config.SSID = AccessPoint.convertToQuotedString(
                     mSsidView.getText().toString());
             // If the user adds a network manually, assume that it is hidden.
             config.hiddenSSID = true;
         } else if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
             config.SSID = AccessPoint.convertToQuotedString(
                     mAccessPoint.ssid);
         } else {
             config.networkId = mAccessPoint.networkId;
         }
 
         switch (mAccessPointSecurity) {
             case AccessPoint.SECURITY_NONE:
                 config.allowedKeyManagement.set(KeyMgmt.NONE);
                 break;
 
             case AccessPoint.SECURITY_WEP:
                 config.allowedKeyManagement.set(KeyMgmt.NONE);
                 config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                 config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                 if (mPasswordView.length() != 0) {
                     int length = mPasswordView.length();
                     String password = mPasswordView.getText().toString();
                     // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                     if ((length == 10 || length == 26 || length == 58) &&
                             password.matches("[0-9A-Fa-f]*")) {
                         config.wepKeys[0] = password;
                     } else {
                         config.wepKeys[0] = '"' + password + '"';
                     }
                 }
                 break;
 
             case AccessPoint.SECURITY_PSK:
                 config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                 if (mPasswordView.length() != 0) {
                     String password = mPasswordView.getText().toString();
                     if (password.matches("[0-9A-Fa-f]{64}")) {
                         config.preSharedKey = password;
                     } else {
                         config.preSharedKey = '"' + password + '"';
                     }
                 }
                 break;
 
             case AccessPoint.SECURITY_EAP:
                 config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                 config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                 config.eap.setValue((String) mEapMethodSpinner.getSelectedItem());
 
                 config.phase2.setValue((mPhase2Spinner.getSelectedItemPosition() == 0) ? "" :
                         "auth=" + mPhase2Spinner.getSelectedItem());
                 config.ca_cert.setValue((mEapCaCertSpinner.getSelectedItemPosition() == 0) ? "" :
                         KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                         (String) mEapCaCertSpinner.getSelectedItem());
                 config.client_cert.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                         "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                         (String) mEapUserCertSpinner.getSelectedItem());
                 config.private_key.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?
                         "" : KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                         (String) mEapUserCertSpinner.getSelectedItem());
                 config.identity.setValue((mEapIdentityView.length() == 0) ? "" :
                         mEapIdentityView.getText().toString());
                 config.anonymous_identity.setValue((mEapAnonymousView.length() == 0) ? "" :
                         mEapAnonymousView.getText().toString());
                 if (mPasswordView.length() != 0) {
                     config.password.setValue(mPasswordView.getText().toString());
                 }
                 break;
 
             default:
                     return null;
         }
 
         validateAndFetchIpAndProxyFields();
 
         config.proxySettings = mProxySettings;
         config.ipAssignment = mIpAssignment;
         config.linkProperties = new LinkProperties(mLinkProperties);
 
         return config;
     }
 
     private void validateAndFetchIpAndProxyFields() {
         mLinkProperties.clear();
         mIpAssignment = (mIpSettingsSpinner != null &&
                 mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                 IpAssignment.STATIC : IpAssignment.DHCP;
 
         if (mIpAssignment == IpAssignment.STATIC) {
             //TODO: A better way to do this is to not dismiss the
             //dialog as long as one of the fields is invalid
             int result = validateIpConfigFields(mLinkProperties);
             if (result != 0) {
                 mLinkProperties.clear();
                 Toast.makeText(mConfigUi.getContext(), result, Toast.LENGTH_LONG).show();
                 mIpAssignment = IpAssignment.UNASSIGNED;
             }
         }
 
         mProxySettings = (mProxySettingsSpinner != null &&
                 mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                 ProxySettings.STATIC : ProxySettings.NONE;
 
         if (mProxySettings == ProxySettings.STATIC) {
             String host = mProxyHostView.getText().toString();
             String portStr = mProxyPortView.getText().toString();
             String exclusionList = mProxyExclusionListView.getText().toString();
             int port = 0;
             int result = 0;
             try {
                 port = Integer.parseInt(portStr);
                 result = ProxySelector.validate(host, portStr, exclusionList);
             } catch (NumberFormatException e) {
                 result = R.string.proxy_error_invalid_port;
             }
             if (result == 0) {
                 ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                 mLinkProperties.setHttpProxy(proxyProperties);
             } else {
                 Toast.makeText(mConfigUi.getContext(), result, Toast.LENGTH_LONG).show();
                 mProxySettings = ProxySettings.UNASSIGNED;
             }
         }
     }
 
     private int validateIpConfigFields(LinkProperties linkProperties) {
         try {
             String ipAddr = mIpAddressView.getText().toString();
             if (!InetAddress.isNumeric(ipAddr)) {
                 return R.string.wifi_ip_settings_invalid_ip_address;
             }
             InetAddress inetAddr = InetAddress.getByName(ipAddr);
 
             int networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText()
                     .toString());
             if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                 return R.string.wifi_ip_settings_invalid_network_prefix_length;
             }
 
             linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));
 
             String gateway = mGatewayView.getText().toString();
             if (!InetAddress.isNumeric(gateway)) {
                 return R.string.wifi_ip_settings_invalid_gateway;
             }
             linkProperties.setGateway(InetAddress.getByName(gateway));
 
             String dns = mDns1View.getText().toString();
             if (!InetAddress.isNumeric(dns)) {
                 return R.string.wifi_ip_settings_invalid_dns;
             }
             linkProperties.addDns(InetAddress.getByName(dns));
             if (mDns2View.length() > 0) {
                 dns = mDns2View.getText().toString();
                 if (!InetAddress.isNumeric(dns)) {
                     return R.string.wifi_ip_settings_invalid_dns;
                 }
                 linkProperties.addDns(InetAddress.getByName(dns));
             }
 
         } catch (NumberFormatException ignore) {
             return R.string.wifi_ip_settings_invalid_network_prefix_length;
         } catch (UnknownHostException e) {
             //Should not happen since we have already validated addresses
             Log.e(TAG, "Failure to validate IP configuration " + e);
             return R.string.wifi_ip_settings_invalid_ip_address;
         }
         return 0;
     }
 
     int chosenNetworkSetupMethod() {
         if (mNetworkSetupSpinner != null) {
             return mNetworkSetupSpinner.getSelectedItemPosition();
         }
         return MANUAL;
     }
 
     WpsConfiguration getWpsConfig() {
         WpsConfiguration config = new WpsConfiguration();
         switch (mNetworkSetupSpinner.getSelectedItemPosition()) {
             case WPS_PBC:
                 config.setup = Setup.PBC;
                 break;
             case WPS_PIN_FROM_ACCESS_POINT:
                 config.setup = Setup.PIN_FROM_ACCESS_POINT;
                 break;
             case WPS_PIN_FROM_DEVICE:
                 config.setup = Setup.PIN_FROM_DEVICE;
                 break;
             default:
                 config.setup = Setup.INVALID;
                 Log.e(TAG, "WPS not selected type");
                 return config;
         }
         config.pin = ((TextView) mView.findViewById(R.id.wps_pin)).getText().toString();
         config.BSSID = (mAccessPoint != null) ? mAccessPoint.bssid : null;
 
         validateAndFetchIpAndProxyFields();
 
         config.proxySettings = mProxySettings;
         config.ipAssignment = mIpAssignment;
         config.linkProperties = new LinkProperties(mLinkProperties);
         return config;
     }
 
     private void showSecurityFields() {
         if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
             mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
             return;
         } else if (mAccessPointSecurity == AccessPoint.SECURITY_EAP && mInXlSetupWizard) {
             // In SetupWizard for XLarge screen, we don't have enough space for showing
             // configurations needed for EAP. We instead disable the whole feature there and let
             // users configure those networks after the setup.
             mView.findViewById(R.id.eap_not_supported).setVisibility(View.VISIBLE);
             mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
             return;
         }
         mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
 
         if (mPasswordView == null) {
             mPasswordView = (TextView) mView.findViewById(R.id.password);
             mPasswordView.addTextChangedListener(this);
             ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);
 
             if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                 mPasswordView.setHint(R.string.wifi_unchanged);
             }
         }
 
         if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
             mView.findViewById(R.id.eap).setVisibility(View.GONE);
             return;
         }
         mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);
 
         if (mEapMethodSpinner == null) {
             mEapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
             mPhase2Spinner = (Spinner) mView.findViewById(R.id.phase2);
             mEapCaCertSpinner = (Spinner) mView.findViewById(R.id.ca_cert);
             mEapUserCertSpinner = (Spinner) mView.findViewById(R.id.user_cert);
             mEapIdentityView = (TextView) mView.findViewById(R.id.identity);
             mEapAnonymousView = (TextView) mView.findViewById(R.id.anonymous);
 
             loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);
             loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY);
 
             if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                 WifiConfiguration config = mAccessPoint.getConfig();
                 setSelection(mEapMethodSpinner, config.eap.value());
                 setSelection(mPhase2Spinner, config.phase2.value());
                 setCertificate(mEapCaCertSpinner, Credentials.CA_CERTIFICATE,
                         config.ca_cert.value());
                 setCertificate(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY,
                         config.private_key.value());
                 mEapIdentityView.setText(config.identity.value());
                 mEapAnonymousView.setText(config.anonymous_identity.value());
             }
         }
     }
 
     private void showNetworkSetupFields() {
         mView.findViewById(R.id.setup_fields).setVisibility(View.VISIBLE);
 
         if (mNetworkSetupSpinner == null) {
             mNetworkSetupSpinner = (Spinner) mView.findViewById(R.id.network_setup);
             mNetworkSetupSpinner.setOnItemSelectedListener(this);
         }
 
         int pos = mNetworkSetupSpinner.getSelectedItemPosition();
 
         /* Show pin text input if needed */
         if (pos == WPS_PIN_FROM_ACCESS_POINT) {
             mView.findViewById(R.id.wps_fields).setVisibility(View.VISIBLE);
         } else {
             mView.findViewById(R.id.wps_fields).setVisibility(View.GONE);
         }
 
         /* show/hide manual security fields appropriately */
         if ((pos == WPS_PIN_FROM_ACCESS_POINT) || (pos == WPS_PIN_FROM_DEVICE)
                 || (pos == WPS_PBC)) {
             mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
         } else {
             mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
         }
 
     }
 
     private void showIpConfigFields() {
         WifiConfiguration config = null;
 
         mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);
 
         if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
             config = mAccessPoint.getConfig();
         }
 
         if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
             mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
             if (mIpAddressView == null) {
                 mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                 mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                 mNetworkPrefixLengthView = (TextView) mView.findViewById(
                         R.id.network_prefix_length);
                 mDns1View = (TextView) mView.findViewById(R.id.dns1);
                 mDns2View = (TextView) mView.findViewById(R.id.dns2);
             }
             if (config != null) {
                 LinkProperties linkProperties = config.linkProperties;
                 Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                 if (iterator.hasNext()) {
                     LinkAddress linkAddress = iterator.next();
                     mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
                     mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                             .getNetworkPrefixLength()));
                 }
                 InetAddress gateway = linkProperties.getGateway();
                 if (gateway != null) {
                     mGatewayView.setText(linkProperties.getGateway().getHostAddress());
                 }
                 Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                 if (dnsIterator.hasNext()) {
                     mDns1View.setText(dnsIterator.next().getHostAddress());
                 }
                 if (dnsIterator.hasNext()) {
                     mDns2View.setText(dnsIterator.next().getHostAddress());
                 }
             }
         } else {
             mView.findViewById(R.id.staticip).setVisibility(View.GONE);
         }
     }
 
     private void showProxyFields() {
         WifiConfiguration config = null;
 
         mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);
 
         if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
             config = mAccessPoint.getConfig();
         }
 
         if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
             mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
             if (mProxyHostView == null) {
                 mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                 mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                 mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
             }
             if (config != null) {
                 ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                 if (proxyProperties != null) {
                     mProxyHostView.setText(proxyProperties.getHost());
                     mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                     mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                 }
             }
         } else {
             mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
         }
     }
 
 
 
     private void loadCertificates(Spinner spinner, String prefix) {
         final Context context = mConfigUi.getContext();
         final String unspecified = context.getString(R.string.wifi_unspecified);
 
         String[] certs = KeyStore.getInstance().saw(prefix);
         if (certs == null || certs.length == 0) {
             certs = new String[] {unspecified};
         } else {
             final String[] array = new String[certs.length + 1];
             array[0] = unspecified;
             System.arraycopy(certs, 0, array, 1, certs.length);
             certs = array;
         }
 
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                 context, android.R.layout.simple_spinner_item, certs);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
     }
 
     private void setCertificate(Spinner spinner, String prefix, String cert) {
         prefix = KEYSTORE_SPACE + prefix;
         if (cert != null && cert.startsWith(prefix)) {
             setSelection(spinner, cert.substring(prefix.length()));
         }
     }
 
     private void setSelection(Spinner spinner, String value) {
         if (value != null) {
             ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
             for (int i = adapter.getCount() - 1; i >= 0; --i) {
                 if (value.equals(adapter.getItem(i))) {
                     spinner.setSelection(i);
                     break;
                 }
             }
         }
     }
 
     public boolean isEdit() {
         return mEdit;
     }
 
     @Override
     public void afterTextChanged(Editable s) {
         enableSubmitIfAppropriate();
     }
 
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
     }
 
     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
     }
 
     @Override
     public void onClick(View view) {
         mPasswordView.setInputType(
                 InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                 InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                 InputType.TYPE_TEXT_VARIATION_PASSWORD));
     }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if (parent == mSecuritySpinner) {
             mAccessPointSecurity = position;
             showSecurityFields();
             enableSubmitIfAppropriate();
         } else if (parent == mNetworkSetupSpinner) {
             showNetworkSetupFields();
         } else if (parent == mProxySettingsSpinner) {
             showProxyFields();
         } else {
             showIpConfigFields();
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
     }
 }
