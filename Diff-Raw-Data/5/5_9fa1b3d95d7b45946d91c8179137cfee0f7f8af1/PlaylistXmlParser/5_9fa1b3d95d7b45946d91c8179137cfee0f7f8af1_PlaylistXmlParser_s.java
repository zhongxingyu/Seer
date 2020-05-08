 package fi.cie.chiru.servicefusionar.groovesharkService;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Xml;
 
 import util.Log;
 
 
 public class PlaylistXmlParser
 {
 	private static final String LOG_TAG = "PlaylistXmlParser";	
 	private static final String ns = null;
 
     public List<String> parse(String input){
     	List<String> songs = null;
     	try {
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(new StringReader(input));
             parser.nextTag();
             songs = readFeed(parser);
             
         } catch (XmlPullParserException e) {
         	Log.e(LOG_TAG, e.toString());
         } catch (IOException e) {
         	Log.e(LOG_TAG, e.toString());
 		}
         return songs;
     }
     
     private List<String> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
         List<String> entries = new ArrayList<String>();
         
         parser.require(XmlPullParser.START_TAG, ns, "rss");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
            // Starts by looking for the show tag
             if (name.equals("channel")) {
                 entries = readItems(parser);
                 //Log.i(LOG_TAG, name);
             } else {
                 skip(parser);
             }
         }
         return entries;
     }
     
     private List<String> readItems(XmlPullParser parser) throws IOException, XmlPullParserException 
     {
     	List<String> items = new ArrayList<String>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
 	    	if (name.equals("item")) {
 	            items.add(readSong(parser));
 	        } 
             else
             {
                 skip(parser);
             }
         }
     	return items;    	
     }
     
     private String readSong(XmlPullParser parser) throws IOException, XmlPullParserException 
     {
         String description = null;
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
 	    	if (name.equals("description")) {
 	    		description = readDescription(parser);
 	        } 
             else
             {
                 skip(parser);
             }
         }
     	return new String(description);    	
     }
     
     // Processes description tags in the feed.
     private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, "description");
         String description = readText(parser);
         Log.i(LOG_TAG,  description);
         parser.require(XmlPullParser.END_TAG, ns, "description");
         return description;
     }
 
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String result = "";
         if (parser.next() == XmlPullParser.TEXT) {
             result = parser.getText();
             
            //TODO Create some smarted method for parsing since rss feed seems to change from time to time.
             //Remove starting time from received text
             String[] temp = result.split("[:]");
             if (temp.length>2) // String format: "11:35:18 Beck - Girl"
             	result = temp[2].substring(3);
             
             else if (temp.length > 1) // String format: "Next 2: Pink - Get The Party Started"
             	result = temp[1].substring(1);
             
             parser.nextTag();
         }
         return result;
     }
     
     // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
     // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
     // finds the matching END_TAG (as indicated by the value of "depth" being 0).
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
             switch (parser.next()) {
             case XmlPullParser.END_TAG:
                     depth--;
                     break;
             case XmlPullParser.START_TAG:
                     depth++;
                     break;
             }
         }
     }
 }
