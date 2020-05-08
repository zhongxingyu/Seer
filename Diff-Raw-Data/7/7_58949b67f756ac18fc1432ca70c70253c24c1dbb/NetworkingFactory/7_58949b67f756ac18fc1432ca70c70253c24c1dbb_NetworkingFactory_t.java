 package com.augmentari.roadworks.sensorlogger.net.ssl;
 
 import android.content.Context;
 import com.augmentari.roadworks.sensorlogger.R;
 import org.apache.http.conn.ssl.StrictHostnameVerifier;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.cert.CertificateException;
 
 /**
  * Factory providing secure HTTPS connections with the certificate chain/ssl verifier understanding our own certificates.
  */
 public class NetworkingFactory {
 
    private static final String TRUSTED_KEYSTORE_PASSWORD = "tomcat";
 
     private static SSLContext sslContext = null;
 
     /**
      * Using STRICT hostname verifier, which means it will not accept SSL connections from indirect sub-domains.
      */
     private static StrictHostnameVerifier HOSTNAME_VERIFIER = new StrictHostnameVerifier();
 
     /**
      * This method is both suitable for HTTP and HTTPS connections. It Returns them in OPEN state, which means that
      * they may be used straight away by calling getInputStream() or connect(). Technically, there should be no need for
      * upcasting HTTP to HTTPSUrlConnection in client code.
      *
      * @param url     url to open
      * @param context context where we live (only used for loading resources).
      * @return open url connection. Don't forget to disconnect() it!
      * @throws NetworkingException in case of any exception (crypto, ...). Look into the cause for the details.
      */
     public static HttpURLConnection openConnection(String url, Context context) throws NetworkingException {
         try {
             ensureSslContextReady(context);
 
             URL urlObject = new URL(url);
             HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
 
             // for regular HTTP connections, just skip that.
             // for HTTPS, put in our own factory/host name verifier.
             if (connection instanceof HttpsURLConnection) {
                 HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                 httpsConnection.setHostnameVerifier(HOSTNAME_VERIFIER);
                 httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
             }
 
             return connection;
         } catch (Exception e) {
             e.printStackTrace();
             throw new NetworkingException(e);
         }
 
     }
 
     private synchronized static void ensureSslContextReady(Context context) throws Exception {
         if (sslContext == null) {
             KeyStore keystore = loadKeystore(context);
             TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
             tmf.init(keystore);
 
             sslContext = SSLContext.getInstance("TLS");
             sslContext.init(null, tmf.getTrustManagers(), null);
         }
     }
 
     private static KeyStore loadKeystore(Context context) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException {
         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 
         // get user password and file input stream
         char[] password = TRUSTED_KEYSTORE_PASSWORD.toCharArray();
 
         InputStream fis = null;
         try {
            fis = context.getResources().openRawResource(R.raw.interserver_public_keystore);
             ks.load(fis, password);
             return ks;
         } finally {
             if (fis != null) {
                 fis.close();
             }
         }
     }
 
 
 }
 
