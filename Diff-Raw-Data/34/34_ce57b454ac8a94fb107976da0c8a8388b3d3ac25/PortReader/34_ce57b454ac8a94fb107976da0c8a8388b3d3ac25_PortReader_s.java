 package org.omships.omships;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
import android.util.Log;

 /**
  * Class reads port data.
  * @author jimtahu
  */
 public class PortReader implements Reader<FeedItem> {
     private String url;
 
     public PortReader(String url) {
         this.url = url;
     }
     
     public List<FeedItem> getItems() throws ParserConfigurationException, SAXException, IOException{
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser saxParser = factory.newSAXParser();
         PortParseHandler handler = new PortParseHandler();
         saxParser.parse(url, handler);
         // The result of the parsing process is being stored in the handler object
         return handler.getItems();
     }
 }
