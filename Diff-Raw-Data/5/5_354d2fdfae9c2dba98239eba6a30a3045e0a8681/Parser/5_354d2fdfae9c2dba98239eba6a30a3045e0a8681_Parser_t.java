 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.XMLReader;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class Parser extends DefaultHandler {
     public LinkedList<Route> routes = new LinkedList<Route>();
 
     private String currentRouteTitle;
     private Route currentRoute;
 
     public Parser() {
         super();
     }
 
     public void startElement(java.lang.String uri, java.lang.String localName,
             java.lang.String qName, Attributes attributes) throws SAXException {
         String tag = localName;
         if (tag.equals("predictions")) {
             currentRouteTitle = attributes.getValue("routeTitle");
         } else if (tag.equals("direction")) {
             String direction = attributes.getValue("title");
            currentRoute = new Route(direction, currentRouteTitle);
         } else if (tag.equals("prediction")) {
             int seconds = Integer.parseInt(attributes.getValue("seconds"));
             currentRoute.addPrediction(seconds);
         }
     }
 
     public void endElement(java.lang.String uri, java.lang.String localName,
             java.lang.String qName) throws SAXException {
         if (localName.equals("predictions"))
             routes.add(currentRoute);
     }
 
     public static void main(String[] args) {
         try {
             XMLReader xr = XMLReaderFactory.createXMLReader();
             Parser parser = new Parser();
             xr.setContentHandler(parser);
             xr.setErrorHandler(parser);
             xr.parse("test.xml");
             for (Route r : parser.routes) {
                System.out.println(r.title + "->" + r.direction);
                 for (int seconds : r.predictions)
                     System.out.println(seconds);
             }
         } catch (IOException ex1) {
             ex1.printStackTrace();
         } catch (SAXException ex2) {
             ex2.printStackTrace();
         }
     }
 }
