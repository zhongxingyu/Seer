 package be.jaspervdj.googleimage;
 
 import ch9k.chat.Conversation;
 import ch9k.eventpool.WarningEvent;
 import ch9k.plugins.ImageProvider;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Class that gives us the ability to look up images on Google image search.
  * @author Jasper Van der Jeugt
  */
 public class GoogleImageProvider extends ImageProvider {
     /**
      * Logger, well, to log.
      */
     private static final Logger logger =
             Logger.getLogger(GoogleImageProvider.class);
 
     /**
      * The google API needs a referer.
      */
     private final String HTTP_REFERER = "http://zeus.ugent.be/";
 
     /**
      * Our Google API key.
      */
     private final String API_KEY =
             "ABQIAAAAs8RaJYu0ZebpBO6jB93ADhTsxjHa7uN5E720o7nIY50" +
             "-3t3KCxQI3dDybveQylpIWU1JS9e16BZIiQ";
 
     /**
      * Constructor.
      * @param conversation The relevant conversation.
      */
     public GoogleImageProvider(Conversation conversation) {
         super(conversation);
     }
 
     @Override
     public String[] getImageUrls(String text, int maxResults) {
         try {
             /* Log some information as well. */
             logger.info("Searching for: " + text);
 
             /* Create an URL and open the connection. */
             URL queryURL = makeURLFromText(text);
             URLConnection connection = queryURL.openConnection();
             connection.addRequestProperty("Referer", HTTP_REFERER);
 
             /* Get the results list from the object. */
             JSONObject json = getJSONResponse(connection);
             JSONArray results =
                     json.getJSONObject("responseData").getJSONArray("results");
 
             /* Put the results in a list of URL's, and log it as well. */
             int numberOfResults = results.length() < maxResults ?
                     results.length() : maxResults;
             String[] urls = new String[numberOfResults];
             for(int i = 0; i < numberOfResults; i++) {
                 JSONObject object = results.getJSONObject(i);
                 String url = object.getString("url");
                 urls[i] = url;
 
                 /* Log some information as well. */
                 logger.info("Photo result: " +
                         object.getString("titleNoFormatting") + " - " + url);
             }
 
             return urls;
         } catch (IOException exception) {
             WarningEvent.raise(this, "Could not contact Google: " + exception);
             return null;
         } catch (JSONException exception) {
             WarningEvent.raise(this, "Could not parse JSON: " + exception);
             return null;
         }
     }
 
     /**
      * Auxiliary function to create a query URL from a given piece of text.
      * @param text Search query.
      * @return A query URL.
      */
     protected URL makeURLFromText(String text) {
         /* Convert spaces to '+' etc. */
         String encoded;
         try {
             encoded = URLEncoder.encode(text, "UTF-8");
         } catch (UnsupportedEncodingException exception) {
             encoded = text;
         }
 
         /* Construct the url. */
         URL url;
         try {
             url = new URL("http://ajax.googleapis.com/ajax/services/search/" +
                "images?start=0&rsz=large&v=1.0&q=" + encoded + "&key=" +
                API_KEY + "&safe=off");
         } catch (MalformedURLException exception) {
             WarningEvent.raise(this, "Malformed URL: " + exception);
             url = null;
         }
 
         return url;
     }
 
     /**
      * Auxiliary function to read a JSONObject from a connection.
      * @param connection Connection to read from.
      * @return A JSONObject.
      */
     protected JSONObject getJSONResponse(URLConnection connection)
             throws IOException, JSONException {
         /* Use a StringBuilder for slightly more efficient appending. */
         String line;
         StringBuilder builder = new StringBuilder();
 
         /* Read everything from the stream. */
         BufferedReader reader = new BufferedReader(
         new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
             builder.append(line);
         }
 
         /* Close the stream and return the read object. */
         reader.close();
         String response = builder.toString();
         return new JSONObject(response);
     }
 }
