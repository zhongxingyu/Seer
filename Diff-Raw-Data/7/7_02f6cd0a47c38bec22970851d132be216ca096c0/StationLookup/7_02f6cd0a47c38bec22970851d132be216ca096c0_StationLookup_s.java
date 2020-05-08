 package gov.usgs.cida.watersmart.netcdf;
 
 import com.ctc.wstx.stax.WstxInputFactory;
 import gov.usgs.cida.netcdf.dsg.Station;
 import gov.usgs.cida.watersmart.config.DynamicReadOnlyProperties;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.xpath.*;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
import static javax.xml.stream.XMLStreamConstants.*;
import org.apache.commons.httpclient.URI;
 
 /**
  * Build a list of stations for a model run / netcdf file
  * Have a way to look up the index of stations
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class StationLookup {
     
     private static Logger LOG = LoggerFactory.getLogger(StationLookup.class);
     // Read wfs endpoint from JNDI
     private static DynamicReadOnlyProperties props = DynamicReadOnlyProperties.initProps();
     
     // Call to get the list of all stations, create 
     public static List<Station> getStationList() {
         List<Station> stations = new LinkedList<Station>();
 
         try {
             String wfsUrl = props.getProperty("watersmart.stations.url", "http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver/watersmart/ows");
             String typeName = props.getProperty("watersmart.stations.typeName", "watersmart:se_sites");
             String url = wfsUrl + "?service=WFS&version=1.1.0&request=GetFeature&typeName=" + typeName + "&outputFormat=text/xml; subtype=gml/3.2";
             String primaryAtt = props.getProperty("watersmart.stations.primaryAttribute", "site_no");
             
 //            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 //            dbf.setNamespaceAware(true);
 //            Document doc = dbf.newDocumentBuilder().parse(url);
             HttpClient client = new HttpClient();
             GetMethod get = new GetMethod();
             URI uri = new URI(url, false);
             get.setURI(uri);
             int statusCode = client.executeMethod(get);
             if (statusCode != HttpStatus.SC_OK) {
                 LOG.error("Bad request to wfs: " + wfsUrl);
             }
             InputStream response = get.getResponseBodyAsStream();
             
             XMLInputFactory factory = WstxInputFactory.newFactory();
             XMLStreamReader reader = factory.createXMLStreamReader(response);
 
             float lat = Float.NaN;
             float lon = Float.NaN;
             String stationId = null;
             
             // TODO not doing namespacing for now, might be important at some point
             while (reader.hasNext()) {
                 int code = reader.next();
                 if (code == START_ELEMENT && 
                         reader.getName().getLocalPart().equals("member")) {
                     lat = Float.NaN;
                     lon = Float.NaN;
                     stationId = null;
                 }
                 else if (code == END_ELEMENT &&
                          reader.getName().getLocalPart().equals("member")) {
                     stations.add(new Station(lat, lon, stationId));
                 }
                 else if (code == START_ELEMENT &&
                          reader.getName().getLocalPart().equals("pos")) {
                     String pos = reader.getElementText();
                     String[] split = pos.split(" ");
                     lat = Float.parseFloat(split[0]);
                     lon = Float.parseFloat(split[1]);
                 }
                 else if (code == START_ELEMENT && 
                          reader.getName().getLocalPart().equals(primaryAtt)) {
                     stationId = reader.getElementText();
                 }
             }
             
 //            XPathFactory xPathfactory = XPathFactory.newInstance();
 //            XPath xpath = xPathfactory.newXPath();
 //            xpath.setNamespaceContext(new GetFeatureNamespaceContext());
 //            XPathExpression expr = xpath.compile("/wfs:FeatureCollection/wfs:member/" + typeName + "/watersmart:" +
 //                                                 primaryAtt);
 //            NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 //
 //            for (int i=0; i<nodes.getLength(); i++) {
 //                Node item = nodes.item(i);
 //                stations.add(item.getTextContent());
 //            }
         }
         catch (XMLStreamException ex) {
             LOG.debug("StAX error occured", ex);
         }
         catch (IOException ex) {
             LOG.debug("IOException from somewhere", ex);
         }
         return stations;
     }
     
     public static int lookup(String station) {
         try {
             String wfsUrl = props.getProperty("watersmart.stations.url", "http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver/watersmart/ows");
             String typeName = props.getProperty("watersmart.stations.typeName", "watersmart:se_sites");
             String primaryAtt = props.getProperty("watersmart.stations.primaryAttribute", "site_no");
             // ugly!
             String url = wfsUrl + "?service=WFS&version=1.1.0&request=GetFeature&typeName=" + 
                          typeName + "&outputFormat=text/xml; subtype=gml/3.2&CQL_FILTER=" +
                          primaryAtt + "%20like%20'%25" + station + "'";
             
             // May want to switch to StAX
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             dbf.setNamespaceAware(true);
             Document doc = dbf.newDocumentBuilder().parse(url);
 
             XPathFactory xPathfactory = XPathFactory.newInstance();
             XPath xpath = xPathfactory.newXPath();
             xpath.setNamespaceContext(new GetFeatureNamespaceContext());
             XPathExpression expr = xpath.compile("/wfs:FeatureCollection/wfs:member/" + typeName + "/@gml:id");
             String attr = (String)expr.evaluate(doc, XPathConstants.STRING);
             String[] split = attr.split("\\.");
             if (split.length == 2) {
                 // gml is one-indexed
                 int index = Integer.parseInt(split[1]) - 1;
                 return index;
             }
         }
         catch (XPathExpressionException ex) {
             LOG.debug("XPath is broke", ex);
         }
         catch (SAXException ex) {
             LOG.debug("XML parsing failed", ex);
         }
         catch (IOException ex) {
             LOG.debug("I/O Exception reading xml", ex);
         }
         catch (ParserConfigurationException ex) {
             LOG.debug("parser unable to parse", ex);
         }
         return -1;
     }
     
     private static class GetFeatureNamespaceContext implements NamespaceContext {
 
         public final static Map<String, String> namespaceMap;
 
         static {
             namespaceMap = new HashMap<String, String>();
             namespaceMap.put("", "http://www.opengis.net/wfs/2.0");
             namespaceMap.put("wfs", "http://www.opengis.net/wfs/2.0");
             namespaceMap.put("ows", "http://www.opengis.net/ows");
             namespaceMap.put("gml", "http://www.opengis.net/gml/3.2");
             namespaceMap.put("ogc", "http://www.opengis.net/ogc");
             namespaceMap.put("xlink", "http://www.w3.org/1999/xlink");
             namespaceMap.put("watersmart", "gov.usgs.cida.watersmart");
         }
 
         @Override
         public String getNamespaceURI(String prefix) {
             if (prefix == null) {
                 throw new NullPointerException("prefix is null");
             }
             String namespaceURI = namespaceMap.get(prefix);
 
             return namespaceURI;
         }
 
         @Override
         public String getPrefix(String namespaceURI) {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public Iterator<String> getPrefixes(String namespaceURI) {
             throw new UnsupportedOperationException();
         }
     }
 }
