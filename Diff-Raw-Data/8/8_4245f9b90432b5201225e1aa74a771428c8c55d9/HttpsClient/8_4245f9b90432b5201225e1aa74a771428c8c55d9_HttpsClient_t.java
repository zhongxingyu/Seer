 package de.dominicscheurer.secmail.net;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.AbstractHttpEntity;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 /**
  * Offers means to interact with a HTTPS web site.
  * 
  * @author Dominic Scheurer
  */
 public class HttpsClient {
 
     private static final String TRUSTSTORE_PWD = "Master4pres!";
    private static final String TRUSTSTORE_FILE = "/dscheurer-truststore.jks";
     
     private HttpsClient() {        
     }
     
     private static void sendHttpsRequest(URI uri, AbstractHttpEntity entity) 
             throws KeyStoreException,
             KeyManagementException,
             UnrecoverableKeyException,
             NoSuchAlgorithmException,
             CertificateException, IOException {
         DefaultHttpClient httpclient = new DefaultHttpClient();
         
         try {
             KeyStore trustStore = KeyStore.getInstance(KeyStore
                     .getDefaultType());
             
            InputStream instream = new HttpsClient().getClass().getResourceAsStream(TRUSTSTORE_FILE);
             
             try {
                 trustStore.load(instream, TRUSTSTORE_PWD.toCharArray());
             } finally {
                 // ESCA-JAVA0008:
                 // ESCA-JAVA0166:
                 try {
                     instream.close();
                 } catch (Exception ignore) {
                     // Nothing done here, wouldn't change anything
                 }
             }
 
             SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
             
             Scheme sch = new Scheme("https", 443, socketFactory);
             httpclient.getConnectionManager().getSchemeRegistry().register(sch);
             
             HttpPost httpPost = new HttpPost(uri);
             httpPost.setEntity(entity);
             
             HttpResponse response = httpclient.execute(httpPost);
             HttpEntity responseEntity = response.getEntity();
             
             EntityUtils.consume(responseEntity);
             
             //TODO analyse responseEntity
 
         } finally {
             // When HttpClient instance is no longer needed,
             // shut down the connection manager to ensure
             // immediate deallocation of all system resources
             httpclient.getConnectionManager().shutdown();
         }
     }
     
     public static void uploadFile(String url, File file)
             throws KeyStoreException,
             KeyManagementException,
             UnrecoverableKeyException,
             NoSuchAlgorithmException,
             CertificateException, IOException, URISyntaxException {        
         uploadFile(url, new HashMap<String, String>(), file);
     }
 
     public static void uploadFile(String url, Map<String, String> parameters, File file)
             throws KeyStoreException,
             KeyManagementException,
             UnrecoverableKeyException,
             NoSuchAlgorithmException,
             CertificateException, IOException, URISyntaxException {        
         FileEntity fileEntity = new FileEntity(file, ContentType.create("text/plain", "UTF-8"));
         
         URIBuilder builder = new URIBuilder(url);
         for (String key : parameters.keySet()) {
             builder.addParameter(key, parameters.get(key));
         }
         
         URI uri = builder.build();
         
         sendHttpsRequest(uri, fileEntity);
     }
 
 }
