 package quizlet;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 class Utils {
 
 
     /**
      * Performs an HTTP GET request at the url.
      * @param urlString
      * @return Returns an InputStream containing the response from the request
      */
    public static InputStream get(String urlString) {
         InputStream response = null;
         try {
             URL url = new URL(urlString);
             URLConnection connection = url.openConnection();
             response = connection.getInputStream();
 
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         return response;
     }
 
 }
