 package ohm.roth.lbs;
 
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.StringWriter;
 import java.text.DecimalFormat;
 import java.util.UUID;
 
 /**
  * User: mischarohlederer
  * Date: 25.10.11
  * Time: 20:39
  */
 public class GPXBuilder {
     public static String build(String filename, Path path) {
         try {
             /////////////////////////////
             //Creating an empty XML Document
 
             //We need a Document
             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
             Document doc = docBuilder.newDocument();
             doc.setXmlStandalone(true);
             doc.setXmlVersion("1.0");
 
             ////////////////////////
             //Creating the XML tree
 
             //create the root element and add it to the document
             Element root = doc.createElement("gpx");
             root.setAttribute("version", "1.1");
             root.setAttribute("creator", "test2");
             root.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
             root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
             root.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
             doc.appendChild(root);
 
             //create child element, add an attribute, and add to root
             Element meta = doc.createElement("metadata");
             root.appendChild(meta);
 
             Element name = doc.createElement("name");
             name.appendChild(doc.createTextNode("FOO"));
             meta.appendChild(name);
 
 
             Element track = doc.createElement("trk");
             root.appendChild(track);
             Element trackname = doc.createElement("name");
             trackname.appendChild(doc.createTextNode(path.getName()));
             track.appendChild(trackname);
 
             for (PathSegment segment : path.getSegments()) {
                 Element trackSegment = doc.createElement("trkseg");
                 track.appendChild(trackSegment);
                 PathNode currNode = segment.getFirstNode();
                DecimalFormat f = new DecimalFormat("#0.000000000");
                 while (currNode != null) {
                     Element trackPoint = doc.createElement("trkpt");
                     trackname = doc.createElement("name");
                     trackname.appendChild(doc.createTextNode(UUID.randomUUID().toString()));
                     trackPoint.appendChild(trackname);
                    trackPoint.setAttribute("lat", f.format(currNode.getNode().getPosition().getLat()));
                    trackPoint.setAttribute("lon", f.format(-currNode.getNode().getPosition().getLon()));
                     trackSegment.appendChild(trackPoint);
 
                     currNode = currNode.getNextNode();
                 }
             }
 
 
             /////////////////
             //Output the XML
 
             //set up a transformer
             TransformerFactory transfac = TransformerFactory.newInstance();
             Transformer trans = transfac.newTransformer();
             trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
             //create string from xml tree
             StringWriter sw = new StringWriter();
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(doc);
             trans.transform(source, result);
             String xmlString = sw.toString();
 
             //print xml
             return xmlString;
 
         } catch (Exception e) {
             System.out.println(e);
             return null;
         }
     }
 }
