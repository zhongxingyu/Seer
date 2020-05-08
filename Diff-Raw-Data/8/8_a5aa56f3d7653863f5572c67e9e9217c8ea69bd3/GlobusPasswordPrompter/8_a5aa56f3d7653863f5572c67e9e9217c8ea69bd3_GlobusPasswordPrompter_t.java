 package edu.cshl.schatz.jnomics.manager.client;
 
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 import org.apache.commons.codec.binary.Base64;
 
 import javax.net.ssl.*;
 import java.io.*;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 import java.util.Properties;
 
 /**
  * User: james
  */
 public class GlobusPasswordPrompter {
 
     public static String GLOBUS_API_URL = "https://nexus.api.globusonline.org/goauth/token?grant_type=client_credentials";
 
     private static TrustManager[] trustAllCerts = new TrustManager[] {
             new X509TrustManager() {
                 public X509Certificate[] getAcceptedIssuers() {
                     return new X509Certificate[0];
                 }
                 public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                 public void checkServerTrusted(X509Certificate[] certs, String authType) {}
             }};
 
     // Ignore differences between given hostname and certificate hostname
     private static HostnameVerifier hostVerifier = new HostnameVerifier() {
         public boolean verify(String hostname, SSLSession session) { return true; }
     };
 
     private static String readData(InputStream stream) throws IOException {
         byte []data = new byte[10240];
         StringBuilder builder = new StringBuilder();
         while(-1 != stream.read(data)){
             builder.append(new String(data));
         }
         return builder.toString();
     }
 
     public static void getPasswordFromUser() throws Exception {
         Console console = System.console();
         System.out.println();
         System.out.println("Please authenticate with Globus Online:");
         String username = console.readLine("[%s]","Username:");
         String passwd = new String(console.readPassword("[%s]", "Password:"));
         
         getPasswordFromUser(username,passwd);
     }
     
     public static void getPasswordFromUser(String user, String passwd) throws Exception{
 
         URL url = new URL(GLOBUS_API_URL);
 
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, new SecureRandom());
 
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         HttpsURLConnection.setDefaultHostnameVerifier(hostVerifier);
 
         String up_str = user + ":" + passwd;
         String up_encoded = new String(Base64.encodeBase64(up_str.getBytes()));
 
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
         conn.setRequestProperty("Authorization","Basic " + up_encoded );
 
        InputStream inStream = null;
        try{
            inStream = conn.getInputStream();
        }catch(Exception e){
            System.out.println("Bad username/password");
            System.exit(1);
        }
         String data = readData(inStream);
         DBObject jobject = (DBObject) JSON.parse(data);
                 
         String token = (String)jobject.get("access_token");
 
         File jconf_dir = JnomicsClientEnvironment.USER_CONF_DIR;
         if(!jconf_dir.exists()){
             jconf_dir.mkdir();
         }
 
         File auth_file = JnomicsClientEnvironment.USER_AUTH_FILE;
         Properties properties = new Properties();
 
         if(!auth_file.exists()){
             System.out.println(auth_file);
             auth_file.createNewFile();
         }else{
             properties.load(new FileInputStream(auth_file));            
         }
         properties.setProperty("token",token);
         properties.store(new FileOutputStream(auth_file),"Globus Online Authentication Token");
     }
 }
