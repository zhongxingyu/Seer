 package app.Parser;
 
 import android.util.Xml;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 //import org.apache.http.HttpResponse;
 import org.apache.http.*;
 import android.util.Log;
 import java.io.UnsupportedEncodingException;
 
 /**
  * AppXmlParser
  *
  * Parset XML-output van webservice waar deze applicatie zijn
  * data vandaan haalt.
  */
 public class AppXmlParser {
 	
 	// We don't use namespaces
     private static final String ns = null;
     
     /**
      * 
      */
     public List parse(InputStream in) throws XmlPullParserException, IOException
     {
         try
         {
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readFeed(parser);
         }
         finally
         {
             in.close();
         }
     }
 
     /**
      *
      * @param parser
      * @return
      * @throws XmlPullParserException
      * @throws IOException
      */
     private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
         List entries = new ArrayList();
 
         parser.require(XmlPullParser.START_TAG, ns, "feed");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("entry")) {
                //entries.add(this.readEntry(parser));
             } else {
                //this.skip(parser);
             }
         }
         return entries;
     }
 
     /**
      * 
      */
     public String getXmlFromUrl(String url) {
 
         String xml = null;
  
         try
         {
             // defaultHttpClient
             DefaultHttpClient httpClient = new DefaultHttpClient();
             HttpGet httpGet = new HttpGet(url);
  
             HttpResponse httpResponse = httpClient.execute(httpGet);
             HttpEntity httpEntity = httpResponse.getEntity();
             xml = EntityUtils.toString(httpEntity);
         }
         catch (UnsupportedEncodingException e)
         {
             e.printStackTrace();
         }
         catch (ClientProtocolException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         // return XML
         return xml;
     }
 
 }
