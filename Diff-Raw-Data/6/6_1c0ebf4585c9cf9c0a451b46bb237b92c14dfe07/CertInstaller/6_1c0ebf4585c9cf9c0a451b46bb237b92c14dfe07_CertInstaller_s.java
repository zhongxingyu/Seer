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
 
 package com.android.certinstaller;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.FileObserver;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.security.Credentials;
 import android.security.KeyStore;
 import android.text.Html;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DEROctetString;
 import org.bouncycastle.asn1.x509.BasicConstraints;
 import org.bouncycastle.openssl.PEMWriter;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Serializable;
 import java.security.KeyStore.PasswordProtection;
 import java.security.KeyStore.PrivateKeyEntry;
 import java.security.KeyFactory;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Installs certificates to the system keystore. It reacts to the
  * {@link Credentials#INSTALL_ACTION} intent.
  */
 public class CertInstaller extends PreferenceActivity
             implements Preference.OnPreferenceClickListener, FileFilter,
             DialogInterface.OnCancelListener {
     private static final String TAG = "CertInstaller";
 
     private static final int NAME_CREDENTIAL_DIALOG = 1;
     private static final int PKCS12_PASSWORD_DIALOG = 2;
     private static final int PROGRESS_BAR_DIALOG = 3;
     private static final int REQUEST_SYSTEM_INSTALL_CODE = 1;
 
     private static final String DOWNLOAD = "download";
 
     // dialog result
     private static final int REOPEN = 1; // re-open the dialog
     private static final int DONE = 2;
     private static final int CANCELLED = 3;
 
     private static final byte[] PKEY_MAP_KEY = "PKEY_MAP".getBytes();
 
     private KeyStore mKeyStore = KeyStore.getInstance();
     private View mView;
     private int mDialogResult = REOPEN;
 
     private boolean mIsBrowsingSdCard;
     private SdCardMonitor mSdCardMonitor;
     private File mCertFile;
 
     private CredentialHelper mCredentials;
     private Runnable mBottomHalf;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         handleIntents(getIntent());
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (mBottomHalf != null) {
             Runnable r = mBottomHalf;
             mBottomHalf = null;
             r.run();
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         stopSdCardMonitor();
     }
 
     @Override
     protected Dialog onCreateDialog (int id) {
         switch (id) {
             case PKCS12_PASSWORD_DIALOG:
                 return createPkcs12PasswordDialog();
 
             case NAME_CREDENTIAL_DIALOG:
                 return createNameCredentialDialog();
 
             case PROGRESS_BAR_DIALOG:
                 ProgressDialog dialog = new ProgressDialog(this);
                 dialog.setMessage(getString(R.string.extracting_pkcs12));
                 dialog.setIndeterminate(true);
                 dialog.setCancelable(false);
                 return dialog;
 
             default:
                 return null;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == REQUEST_SYSTEM_INSTALL_CODE) {
             if (resultCode == RESULT_OK) {
                 Log.d(TAG, "credential is added: " + mCredentials.getName());
                 Toast.makeText(this,
                         getString(R.string.cert_is_added,
                                 mCredentials.getName()),
                         Toast.LENGTH_LONG).show();
                 deleteCert(mCertFile);
                 if (!mIsBrowsingSdCard) finish();
             } else {
                 Log.d(TAG, "credential not saved, err: " + resultCode);
                 toastErrorAndFinish(R.string.cert_not_saved);
             }
         } else {
             Log.w(TAG, "unknown request code: " + requestCode);
             return;
         }
     }
 
     private void handleIntents(Intent intent) {
         if (intent == null) return;
         String action = intent.getAction();
 
         if (Credentials.INSTALL_ACTION.equals(action)) {
             mCredentials = new CredentialHelper(intent);
 
             if (!mCredentials.containsAny()) {
                 mIsBrowsingSdCard = true;
                 addPreferencesFromResource(R.xml.pick_file_pref);
                 startSdCardMonitor();
                 createFileList();
             } else if (mCredentials.isPkcs12KeyStore()) {
                 showDialog(PKCS12_PASSWORD_DIALOG);
             } else {
                 installAskingKeyStoreAccess();
             }
         }
     }
 
     private void installAskingKeyStoreAccess() {
         if (mCredentials.isKeyPair()) {
             if (isKeyStoreLocked()) {
                 unlockKeyStoreWithBottomHalf();
                 return;
             }
             saveKeyPair();
             finish();
         } else {
             X509Certificate crt = mCredentials.getUserCertificate();
             if (crt != null) {
                 // find matched private key
                 String key = toMd5(crt.getPublicKey().getEncoded());
                 Map<String, byte[]> map = getPkeyMap();
                 byte[] privatekey = map.get(key);
                 if (privatekey != null) {
                     if (isKeyStoreLocked()) {
                         unlockKeyStoreWithBottomHalf();
                         return;
                     }
                     Log.d(TAG, "found matched key: " + privatekey);
                     map.remove(key);
                     setPkeyMap(map);
 
                     mCredentials.setPrivateKey(privatekey);
                 } else {
                     Log.d(TAG, "didn't find matched private key: " + key);
                 }
             }
             nameCredential();
         }
     }
 
     private void unlockKeyStoreWithBottomHalf() {
         mBottomHalf = new Runnable() {
             public void run() {
                 if (!isKeyStoreLocked()) {
                     installAskingKeyStoreAccess();
                 } else {
                     toastErrorAndFinish(R.string.cert_not_saved);
                 }
             }
         };
         Credentials.getInstance().unlock(this);
     }
 
     private void nameCredential() {
         if (!mCredentials.readyForSystemInstall()) {
             toastErrorAndFinish(R.string.no_cert_to_saved);
         } else {
             showDialog(NAME_CREDENTIAL_DIALOG);
         }
     }
 
     private void saveKeyPair() {
         byte[] privatekey = mCredentials.getData(Credentials.PRIVATE_KEY);
         String key = toMd5(mCredentials.getData(Credentials.PUBLIC_KEY));
         Map<String, byte[]> map = getPkeyMap();
         map.put(key, privatekey);
         setPkeyMap(map);
         Log.d(TAG, "privatekey key: " + key + " --> #keys:" + map.size());
     }
 
     private void setPkeyMap(Map<String, byte[]> map) {
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(baos);
             os.writeObject(map);
             os.close();
             if (!mKeyStore.put(PKEY_MAP_KEY, baos.toByteArray())) {
                 Log.w(TAG, "setPkeyMap(): failed to write pkey map");
             }
         } catch (Exception e) {
             // if anything wrong, we lost the private key
             Log.w(TAG, "setPkeyMap(): " + e);
         }
     }
 
     private Map<String, byte[]> getPkeyMap() {
         byte[] bytes = mKeyStore.get(PKEY_MAP_KEY);
         if (bytes != null) {
             try {
                 ObjectInputStream is =
                         new ObjectInputStream(new ByteArrayInputStream(bytes));
                 Map<String, byte[]> map = (Map<String, byte[]>) is.readObject();
                 if (map != null) return map;
             } catch (Exception e) {
                 // if anything wrong, create a new map
                 Log.w(TAG, "getPkeyMap(): " + e);
             }
         }
 
         return new MyMap();
     }
 
     public void onCancel(DialogInterface dialog) {
         mDialogResult = CANCELLED;
     }
 
     private Dialog createPkcs12PasswordDialog() {
         mView = View.inflate(this, R.layout.password_dialog, null);
         mDialogResult = REOPEN;
 
         DialogInterface.OnClickListener onClickHandler =
                 new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 if (which == DialogInterface.BUTTON_NEGATIVE) {
                     onCancel(dialog);
                     return;
                 }
 
                 String passwd = getViewText(R.id.credential_password);
 
                 hideError();
                 if (TextUtils.isEmpty(passwd)) {
                     showError(R.string.password_empty_error);
                 } else {
                     mDialogResult = DONE;
                 }
             }
         };
 
         DialogInterface.OnDismissListener onDismissHandler =
                 new DialogInterface.OnDismissListener() {
             public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "onDismiss() called");
                 // Original code without progress bar:
                 //if (mDialogResult == DONE) {
                 //    String passwd = getViewText(R.id.credential_password);
                 //    if (!mCredentials.extractFromPkcs12(passwd)) {
                 //        mDialogResult = REOPEN;
                 //        showError(R.string.password_error);
                 //    }
                 //}
                 //enterPasswdDialogBottomHalf();
 
                 // show progress bar and extract certs in a background thread
                 if (mDialogResult == DONE) {
                     showDialog(PROGRESS_BAR_DIALOG);
 
                     final String passwd = getViewText(R.id.credential_password);
                     new Thread(new Runnable() {
                         public void run() {
                             if (!mCredentials.extractFromPkcs12(passwd)) {
                                 mDialogResult = REOPEN;
                                 showError(R.string.password_error);
                             }
                             removeDialog(PROGRESS_BAR_DIALOG);
                             runOnUiThread(new Runnable() {
                                 public void run() {
                                     enterPasswdDialogBottomHalf();
                                 }
                             });
                         }
                     }).start();
                 } else {
                     enterPasswdDialogBottomHalf();
                 }
             }
         };
 
         String title = (mCertFile == null)
             ? getString(R.string.pkcs12_password_dialog_title)
             : getString(R.string.pkcs12_file_password_dialog_title,
                     mCertFile.getName());
         Dialog d = new AlertDialog.Builder(this)
                 .setView(mView)
                 .setTitle(title)
                 .setPositiveButton(android.R.string.ok, onClickHandler)
                 .setNegativeButton(android.R.string.cancel, onClickHandler)
                 .setOnCancelListener(this)
                 .create();
         d.setOnDismissListener(onDismissHandler);
         return d;
     }
 
     private void enterPasswdDialogBottomHalf() {
         if (mDialogResult == DONE) {
             nameCredential();
         } else if (mDialogResult == REOPEN) {
             showDialog(PKCS12_PASSWORD_DIALOG);
             return;
         } else {
             toastErrorAndFinish(R.string.cert_not_saved);
         }
         removeDialog(PKCS12_PASSWORD_DIALOG);
     }
 
     private Dialog createNameCredentialDialog() {
         mView = View.inflate(this, R.layout.name_credential_dialog, null);
         mDialogResult = REOPEN;
 
         setViewText(R.id.credential_name_title, R.string.credential_name);
         setViewText(R.id.credential_info_title, R.string.credential_info);
         setViewText(R.id.credential_info,
                 mCredentials.getDescription().toString());
 
         DialogInterface.OnClickListener onClickHandler =
                 new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 if (which == DialogInterface.BUTTON_NEGATIVE) {
                     onCancel(dialog);
                     return;
                 }
 
                 hideError();
 
                 String name = getViewText(R.id.credential_name);
                 if (TextUtils.isEmpty(name)) {
                     showError(R.string.name_empty_error);
                 } else {
                     mCredentials.setName(name);
                     mDialogResult = DONE;
                 }
             }
         };
 
         DialogInterface.OnDismissListener onDismissHandler =
                 new DialogInterface.OnDismissListener() {
             public void onDismiss(DialogInterface dialog) {
                 if (mDialogResult == DONE) {
                     // install everything to system keystore
                     try {
                         startActivityForResult(
                                 mCredentials.createSystemInstallIntent(),
                                 REQUEST_SYSTEM_INSTALL_CODE);
                     } catch (ActivityNotFoundException e) {
                         Log.w(TAG, "systemInstall(): " + e);
                         toastErrorAndFinish(R.string.cert_not_saved);
                     }
                 } else if (mDialogResult == REOPEN) {
                     showDialog(NAME_CREDENTIAL_DIALOG);
                     return;
                 } else {
                     toastErrorAndFinish(R.string.cert_not_saved);
                 }
                 removeDialog(NAME_CREDENTIAL_DIALOG);
             }
         };
 
         Dialog d = new AlertDialog.Builder(this)
                 .setView(mView)
                 .setTitle(R.string.name_credential_dialog_title)
                 .setPositiveButton(android.R.string.ok, onClickHandler)
                 .setNegativeButton(android.R.string.cancel, onClickHandler)
                 .setOnCancelListener(this)
                 .create();
         d.setOnDismissListener(onDismissHandler);
         return d;
     }
 
     private void setAllFilesEnabled(boolean enabled) {
         PreferenceScreen root = getPreferenceScreen();
         for (int i = 0, n = root.getPreferenceCount(); i < n; i++) {
             root.getPreference(i).setEnabled(enabled);
         }
     }
 
     public boolean onPreferenceClick(Preference pref) {
         File file = new File(Environment.getExternalStorageDirectory(),
                 pref.getTitle().toString());
         if (file.isDirectory()) {
             Log.w(TAG, "impossible to pick a directory! " + file);
         } else {
             setAllFilesEnabled(false);
             installFromSdCard(file);
         }
         return true;
     }
 
     private void createFileList() {
         if (isFinishing()) {
             Log.d(TAG, "finishing, exit createFileList()");
             return;
         }
         try {
             PreferenceScreen root = getPreferenceScreen();
             root.removeAll();
             File dir = Environment.getExternalStorageDirectory();
             createPreferencesFor(new File(dir, DOWNLOAD));
             createPreferencesFor(dir);
             if (root.getPreferenceCount() == 0) {
                 Toast.makeText(this, R.string.no_pkcs12_found,
                         Toast.LENGTH_SHORT).show();
             }
         } catch (IOException e) {
             // should not occur
             Log.w(TAG, "createFileList(): " + e);
             throw new RuntimeException(e);
         }
     }
 
     private void createPreferencesFor(File dir) throws IOException {
         if ((dir == null) || !dir.isDirectory()) return;
 
         PreferenceScreen root = getPreferenceScreen();
         int prefixEnd = Environment.getExternalStorageDirectory()
                 .getCanonicalPath().length() + 1;
         for (File f : dir.listFiles(this)) {
             Preference p = new Preference(this);
             p.setTitle(f.getCanonicalPath().substring(prefixEnd));
             root.addPreference(p);
             p.setOnPreferenceClickListener(this);
         }
     }
 
     public boolean accept(File file) {
         if (!file.isDirectory()) {
             return file.getPath().endsWith(".p12");
         } else {
             return false;
         }
     }
 
     private void toastErrorAndFinish(int msgId) {
         if (msgId == R.string.cert_not_saved) {
             toastErrorAndFinish(msgId, Toast.LENGTH_SHORT);
         } else {
             toastErrorAndFinish(msgId, Toast.LENGTH_LONG);
         }
     }
 
     private void toastErrorAndFinish(int msgId, int duration) {
         toastErrorAndFinish(getString(msgId), duration);
     }
 
     private void toastErrorAndFinish(String msg, int duration) {
         Toast.makeText(this, msg, duration).show();
 
         if (mIsBrowsingSdCard) {
             setAllFilesEnabled(true);
         } else {
             finish();
         }
     }
 
     private void installFromSdCard(File file) {
         Log.d(TAG, "install cert from " + file);
 
         mCertFile = file;
         if (file.exists()) {
             long length = file.length();
             if (length < 1000000) {
                 byte[] data = readCert(file);
                 if (data == null) {
                     toastErrorAndFinish(R.string.cert_read_error);
                     return;
                 }
                 mCredentials.putData(Credentials.PKCS12, data);
                 showDialog(PKCS12_PASSWORD_DIALOG);
             } else {
                 Log.w(TAG, "cert file is too large: " + length);
                 toastErrorAndFinish(R.string.cert_too_large_error);
             }
         } else {
             Log.w(TAG, "cert file does not exist");
             toastErrorAndFinish(R.string.cert_missing_error);
         }
     }
 
     private byte[] readCert(File file) {
         try {
             byte[] data = new byte[(int) file.length()];
             FileInputStream fis = new FileInputStream(file);
             fis.read(data);
             fis.close();
             return data;
         } catch (Exception e) {
             Log.w(TAG, "cert file read error: " + e);
             return null;
         }
     }
 
     private void deleteCert(File file) {
         if ((file != null) && !file.delete()) {
             Log.w(TAG, "cannot delete cert: " + file);
         }
     }
 
     private boolean isKeyStoreLocked() {
         return (mKeyStore.test() != KeyStore.NO_ERROR);
     }
 
     private TextView showError(int msgId) {
         TextView v = (TextView) mView.findViewById(R.id.error);
         v.setText(msgId);
         if (v != null) v.setVisibility(View.VISIBLE);
         return v;
     }
 
     private void hide(int viewId) {
         View v = mView.findViewById(viewId);
         if (v != null) v.setVisibility(View.GONE);
     }
 
     private void hideError() {
         hide(R.id.error);
     }
 
     private String getViewText(int viewId) {
         return ((TextView) mView.findViewById(viewId)).getText().toString();
     }
 
     private void setViewText(int viewId, String text) {
         TextView v = (TextView) mView.findViewById(viewId);
         if (v != null) v.setText(text);
     }
 
     private void setViewText(int viewId, int textId) {
         TextView v = (TextView) mView.findViewById(viewId);
         if (v != null) v.setText(textId);
     }
 
     private void startSdCardMonitor() {
         if (mSdCardMonitor == null) mSdCardMonitor = new SdCardMonitor();
         mSdCardMonitor.startWatching();
     }
 
     private void stopSdCardMonitor() {
         if (mSdCardMonitor != null) mSdCardMonitor.stopWatching();
     }
 
     private static String toMd5(byte[] bytes) {
         try {
             MessageDigest algorithm = MessageDigest.getInstance("MD5");
             algorithm.reset();
             algorithm.update(bytes);
             return toHexString(algorithm.digest(), "");
         } catch(NoSuchAlgorithmException e){
             // should not occur
             Log.w(TAG, "toMd5(): " + e);
             throw new RuntimeException(e);
         }
     }
 
     private static String toHexString(byte[] bytes, String separator) {
         StringBuilder hexString = new StringBuilder();
         for (byte b : bytes) {
             hexString.append(Integer.toHexString(0xFF & b)).append(separator);
         }
         return hexString.toString();
     }
 
     private class CredentialHelper {
         private Bundle mBundle;
         private String mName;
 
         private PrivateKey mUserKey;
         private X509Certificate mUserCert;
         private List<X509Certificate> mCaCerts =
                 new ArrayList<X509Certificate>();
 
         CredentialHelper(Intent intent) {
             mBundle = intent.getExtras();
             if (mBundle == null) {
                 mBundle = new Bundle();
                 return;
             }
 
             // debug
             Log.d(TAG, "# extras: " + mBundle.size());
             for (String key : mBundle.keySet()) {
                 Log.d(TAG, "   " + key + ": " + mBundle.getByteArray(key));
             }
             parseCert(getData(Credentials.CERTIFICATE));
         }
 
         X509Certificate getUserCertificate() {
             return mUserCert;
         }
 
         PrivateKey getUserKey() {
             return mUserKey;
         }
 
         private void parseCert(byte[] bytes) {
             if (bytes == null) return;
             try {
                 CertificateFactory crtFactory =
                         CertificateFactory.getInstance("X.509");
                 X509Certificate crt = (X509Certificate)
                         crtFactory.generateCertificate(
                                 new ByteArrayInputStream(bytes));
                 if (isCa(crt)) {
                     Log.d(TAG, "got a CA cert");
                     mCaCerts.add(crt);
                 } else {
                     Log.d(TAG, "got a user cert");
                     mUserCert = crt;
                 }
             } catch (Exception e) {
                 Log.w(TAG, "parseCert(): " + e);
                 toastErrorAndFinish(
                         getString(R.string.intent_parse_error, e.toString()),
                         Toast.LENGTH_LONG);
             }
         }
 
         private boolean isCa(X509Certificate crt) {
             try {
                 // TODO: add a test about this
                 byte[] basicConstraints = crt.getExtensionValue("2.5.29.19");
                 Object obj = new ASN1InputStream(basicConstraints).readObject();
                 basicConstraints = ((DEROctetString) obj).getOctets();
                 obj = new ASN1InputStream(basicConstraints).readObject();
                 return new BasicConstraints((ASN1Sequence) obj).isCA();
             } catch (Exception e) {
                 return false;
             }
         }
 
         boolean isPkcs12KeyStore() {
             return mBundle.containsKey(Credentials.PKCS12);
         }
 
         boolean isKeyPair() {
             return mBundle.containsKey(Credentials.PUBLIC_KEY)
                     && mBundle.containsKey(Credentials.PRIVATE_KEY);
         }
 
         boolean readyForSystemInstall() {
             return ((mUserKey != null) || (mUserCert != null)
                     || !mCaCerts.isEmpty());
         }
 
         private void setPrivateKey(byte[] bytes) {
             try {
                 KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                 mUserKey = keyFactory.generatePrivate(
                         new PKCS8EncodedKeySpec(bytes));
             } catch (Exception e) {
                 // should not occur
                 Log.w(TAG, "setPrivateKey(): " + e);
                 throw new RuntimeException(e);
             }
         }
 
         // notes: only this method checks if mBundle is null
         boolean containsAny() {
             if (mBundle == null) return false;
             return !mBundle.isEmpty();
         }
 
         byte[] getData(String key) {
             return mBundle.getByteArray(key);
         }
 
         void putData(String key, byte[] data) {
             mBundle.putByteArray(key, data);
         }
 
         CharSequence getDescription() {
             // TODO: create more descriptive string
             StringBuilder sb = new StringBuilder();
             String newline = "<br>";
             if (mUserKey != null) {
                 sb.append(getString(R.string.one_userkey)).append(newline);
             }
             if (mUserCert != null) {
                 sb.append(getString(R.string.one_usercrt)).append(newline);
             }
             int n = mCaCerts.size();
             if (n > 0) {
                 if (n == 1) {
                     sb.append(getString(R.string.one_cacrt));
                 } else {
                     sb.append(getString(R.string.n_cacrts, n));
                 }
             }
             return Html.fromHtml(sb.toString());
         }
 
         void setName(String name) {
             mName = name;
         }
 
         String getName() {
             return mName;
         }
 
         Intent createSystemInstallIntent() {
             Intent intent = new Intent(Credentials.SYSTEM_INSTALL_ACTION);
             if (mUserKey != null) {
                 intent.putExtra(Credentials.USER_PRIVATE_KEY + mName,
                         convertToPem(mUserKey));
             }
             if (mUserCert != null) {
                 intent.putExtra(Credentials.USER_CERTIFICATE + mName,
                         convertToPem(mUserCert));
             }
             if (!mCaCerts.isEmpty()) {
                 Object[] cacrts = (Object[])
                         mCaCerts.toArray(new X509Certificate[mCaCerts.size()]);
                 intent.putExtra(Credentials.CA_CERTIFICATE + mName,
                         convertToPem(cacrts));
             }
             return intent;
         }
 
         boolean extractFromPkcs12(String passwd) {
             try {
                 return extractFromPkcs12Internal(passwd);
             } catch (Exception e) {
                 Log.w(TAG, "extractFromPkcs12(): " + e);
                 return false;
             }
         }
 
         private boolean extractFromPkcs12Internal(String passwd)
                 throws Exception {
             // TODO: add test about this
             java.security.KeyStore keystore =
                     java.security.KeyStore.getInstance("PKCS12");
             PasswordProtection passwdProtection =
                     new PasswordProtection(passwd.toCharArray());
             keystore.load(new ByteArrayInputStream(getData(Credentials.PKCS12)),
                     passwdProtection.getPassword());
 
             Enumeration<String> aliases = keystore.aliases();
             if (!aliases.hasMoreElements()) return false;
 
             String alias = aliases.nextElement();
             Log.d(TAG, "extracted alias = " + alias);
             PrivateKeyEntry entry = (PrivateKeyEntry)
                     keystore.getEntry(alias, passwdProtection);
             mUserKey = entry.getPrivateKey();
             mUserCert = (X509Certificate) entry.getCertificate();
 
             Certificate[] crts = entry.getCertificateChain();
             Log.d(TAG, "# certs extracted = " + crts.length);
             List<X509Certificate> caCerts = mCaCerts =
                     new ArrayList<X509Certificate>(crts.length);
             for (Certificate c : crts) {
                 X509Certificate crt = (X509Certificate) c;
                 if (isCa(crt)) caCerts.add(crt);
             }
             Log.d(TAG, "# ca certs extracted = " + mCaCerts.size());
             return true;
         }
 
         private byte[] convertToPem(Object... objects) {
             try {
                 ByteArrayOutputStream bao = new ByteArrayOutputStream();
                 OutputStreamWriter osw = new OutputStreamWriter(bao);
                 PEMWriter pw = new PEMWriter(osw);
                 for (Object o : objects) pw.writeObject(o);
                 pw.close();
                 return bao.toByteArray();
             } catch (IOException e) {
                 // should not occur
                 Log.w(TAG, "convertToPem(): " + e);
                 throw new RuntimeException(e);
             }
         }
     }
 
     private static class MyMap extends LinkedHashMap<String, byte[]>
             implements Serializable {
         private static final long serialVersionUID = 1L;
 
         protected boolean removeEldestEntry(Map.Entry eldest) {
             // Note: one key takes about 1300 bytes in the keystore, so be
             // cautious about allowing more outstanding keys in the map that
             // may go beyond keystore's max space for one entry.
             return (size() > 3);
         }
     }
 
     private class SdCardMonitor {
         FileObserver mRootMonitor;
         FileObserver mDownloadMonitor;
 
         SdCardMonitor() {
             File root = Environment.getExternalStorageDirectory();
             mRootMonitor = new FileObserver(root.getPath()) {
                 @Override
                 public void onEvent(int evt, String path) {
                     commonHandler(evt, path);
                 }
             };
 
             File download = new File(root, DOWNLOAD);
             mDownloadMonitor = new FileObserver(download.getPath()) {
                 @Override
                 public void onEvent(int evt, String path) {
                     commonHandler(evt, path);
                 }
             };
         }
 
         private void commonHandler(int evt, String path) {
             switch (evt) {
                 case FileObserver.CREATE:
                 case FileObserver.DELETE:
                     if (path.endsWith(".p12")) {
                         runOnUiThread(new Runnable() {
                             public void run() {
                                 createFileList();
                             }
                         });
                     }
                     break;
             }
         };
 
         void startWatching() {
             mRootMonitor.startWatching();
             mDownloadMonitor.startWatching();
         }
 
         void stopWatching() {
             mRootMonitor.stopWatching();
             mDownloadMonitor.stopWatching();
         }
     }
 }
