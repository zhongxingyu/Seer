 package com.omgren.apps.smsgcm.client;
 
 import static com.omgren.apps.smsgcm.client.CommonUtilities.displayMessage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.FileChannel;
 import java.security.KeyStore;
 import java.security.SecureRandom;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public final class CertUtilities
 {
 
   private static final String TAG = "CertUtilities";
 
   /**
    * look for the users key in /sdcard/Downloads and copy it into the
    * apps internal storage.
    */
   public static void copyKeystoreFile(final Context context)
     throws CertException, IOException
   {
     // check we can read and write to storage
     String state = Environment.getExternalStorageState();
     if(!Environment.MEDIA_MOUNTED.equals(state)){
       displayMessage(context, context.getString(R.string.cert_storage_unavailable));
       throw new CertException("device mounted");
     }
 
     // look for the keystore in Download
     File path = Environment.getExternalStoragePublicDirectory(
                     Environment.DIRECTORY_DOWNLOADS);
     File unsecuredKeystore = new File(path, context.getString(R.string.cert_name));
     if( !unsecuredKeystore.exists() ){
       displayMessage(context, context.getString(R.string.cert_not_installed));
       throw new CertException("keystore does not exist in " + unsecuredKeystore.getAbsolutePath());
     }
 
     // load location of internal spot
     FileOutputStream securedKeystore =
         context.openFileOutput(context.getString(R.string.cert_name)
                               , Context.MODE_PRIVATE);
 
     // copy to internal spot
     FileChannel unsecureFile = new FileInputStream(unsecuredKeystore).getChannel();
     FileChannel secureFile = securedKeystore.getChannel();
     try {
       unsecureFile.transferTo(0, unsecureFile.size(), secureFile);
     } catch (Exception e) {
       displayMessage(context, context.getString(R.string.cert_transfer_error));
       throw new CertException("failed to copy credentials: " + e);
     } finally {
       if( unsecureFile != null ) unsecureFile.close();
       if( secureFile != null ) secureFile.close();
     }
 
     // delete the unsecured file
     try {
       unsecuredKeystore.delete();
     } catch (Exception e) {
       throw new CertException("could not delete unsecured credentials: " + e);
     }
   }
 
   /**
    * open stream to the user's pkcs12 bundle
    */
   private static InputStream getKeystoreFile(final Context context)
     throws CertException
   {
     try {
       return context.openFileInput(context.getString(R.string.cert_name));
     } catch(Exception e) {
       displayMessage(context, context.getString(R.string.cert_not_loaded_warning));
       throw new CertException("could not load client key file: " + e);
     }
   }
 
   /**
    * load client certificates
    */
   private static KeyManager[] getKeyManagers(final Context context)
     throws CertException
   {
     KeyManagerFactory kmf;
 
     try {
       KeyStore keystore = loadClientCredentials(context);
       kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
       kmf.init(keystore, "".toCharArray());
       return kmf.getKeyManagers();
     } catch (Exception e) {
       displayMessage(context, context.getString(R.string.cert_password_warning));
       throw new CertException("could not get client key");
     }
   }
 
   /**
    * load CA trust certificates. 
    *
    * TODO: clean this up so it is not so redundant with
    * getKeyManagers. YUCK!
    */
   private static TrustManager[] getTrustManagers(final Context context)
     throws CertException
   {
     TrustManagerFactory tmf;
 
     try {
       KeyStore truststore = loadClientCredentials(context);
       tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
       tmf.init(truststore);
       return tmf.getTrustManagers();
     } catch (Exception e) {
       throw new CertException("could not load CA cert: " + e);
     } 
   }
 
   private static KeyStore credentials = null;
 
   /**
    * load and unlock the users pkcs12 credentials file
    */
  private static KeyStore loadClientCredentials(fint Context context)
     throws CertException
   {
     if( credentials != null ){
       Log.i(TAG, "credentials already loaded!");
       return credentials;
     }
 
     // load credentials
     InputStream credLocation = getKeystoreFile(context);
     try {
       // pkcs12 bundle password
       SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
       String credPassword = sharedPref.getString(SettingsActivity.PREF_CERT_PASSWORD, "");
 
       credentials = KeyStore.getInstance("PKCS12");
      credentials.load(keystoreLocation, keystorePassword.toCharArray());
       return credentials;
 
     } catch (Exception e) {
       displayMessage(context, context.getString(R.string.cert_password_warning));
       throw new CertException("could not open client's credentials");
     }

    return null;
   }
 
   private static SSLContext sslContext = null;
   /**
    * this gives us the sslcontext from our pkcs12 key.
    *
    * TODO: cache the context so we don't need to keep looking for and
    * loading these two keys (from the same file no less).
    */
   public static SSLContext getSSLContext(final Context cx)
     throws CertException
   {
     if( sslContext == null ){
       try {
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(getKeyManagers(cx), getTrustManagers(cx), new SecureRandom());
       } catch (Exception e){
         throw new CertException("bad ssl stuff: " + e);
       }
     }
 
     return sslContext;
   }
 
 }
