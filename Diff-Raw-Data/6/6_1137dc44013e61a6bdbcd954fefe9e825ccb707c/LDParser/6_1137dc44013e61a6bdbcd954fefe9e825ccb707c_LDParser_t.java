 package fu.berlin.de.webdatabrowser.webdataparser;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.util.Log;
 import fu.berlin.de.webdatabrowser.util.Debug;
 
 public class LDParser {
     static ArrayList<String>    tags    = new ArrayList<String>(
                                                 // befuellt mit ein paar Tags
                                                 // die man
                                                 // nehmen koennte
                                                 Arrays.asList(
                                                         // Einwohnerzahl
                                                         "dbpedia-owl:populationTotal",
                                                         // Buergermeister
                                                         "dbpedia-owl:leader",
                                                         // Koordinaten
                                                         // POINT(13.3989,
                                                         // 52.5006)
                                                         "geo:geometry",
                                                         // Gesamtflaeche
                                                         // (xsd:double)
                                                         "dbpedia-owl:areaTotal",
                                                         // Geburtsort von...
                                                         "dbpedia-owl:birthPlace",
                                                         // Hauptquartier von...
                                                         "dbpedia-owl:headquarter",
                                                         // Heimatstadt von...
                                                         "dbpedia-owl:hometown"
                                                         ));
     private static final String LOG_TAG = "LDParser";
     private final WebDataParser resultHandler;
 
     public LDParser(WebDataParser resultHandler) {
         this.resultHandler = resultHandler;
     }
 
     // Gibt dom document als string zurueck
     private String getStringFromDoc(org.w3c.dom.Document doc) {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(32);
 
         try {
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
         }
         catch(TransformerConfigurationException e) {
             Log.w(LOG_TAG, Log.getStackTraceString(e));
         }
         catch(TransformerFactoryConfigurationError e) {
             Log.w(LOG_TAG, Log.getStackTraceString(e));
         }
         catch(TransformerException e) {
             Log.w(LOG_TAG, Log.getStackTraceString(e));
         }
 
         return outputStream.toString();
     }
 
     public void parseLD(String webContent) {
 
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = null;
         Document doc = null;
 
         try {
             db = dbf.newDocumentBuilder();
             doc = db.parse(new ByteArrayInputStream(webContent.getBytes()));
         }
         catch(ParserConfigurationException e) {
             Log.e(LOG_TAG, Log.getStackTraceString(e));
         }
         catch(SAXException e) {
             Log.e(LOG_TAG, Log.getStackTraceString(e));
         }
         catch(IOException e) {
             Log.e(LOG_TAG, Log.getStackTraceString(e));
         }
 
         NodeList rdfEntries = doc.getElementsByTagName("rdf:Description");
         Document targetDoc = db.newDocument();
         Element root = targetDoc.createElement("rdf:RDF");
         root.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
         root.setAttribute("xmlns:dbpedia-owl", "http://dbpedia.org/ontology/"); // TODO
                                                                                 // n�tige
                                                                                 // namespaces
                                                                                 // noch
                                                                                 // unvollst�ndig
         targetDoc.appendChild(root);
 
         for(int i = 0; i < rdfEntries.getLength(); i++) {
             Node subject = rdfEntries.item(i);
             if(subject.getNodeType() == Node.ELEMENT_NODE) {
 
                 NodeList predicates = subject.getChildNodes();
 
                 boolean foundTag;
                 for(int j = predicates.getLength() - 1; j >= 0; j--) {
 
                     Node predicate = predicates.item(j);
                     foundTag = false;
 
                     for(String element : tags) {
                        if(predicate.getNodeName().equals(element)) {
                             foundTag = true;
                         }
                     }
 
                     if(!foundTag)
                        subject.removeChild(predicate); // l�scht predicate
                                                        // wenn
                                                         // es nicht in der
                                                         // Tagliste gefunden
                                                         // wurde
                 }
                 if(subject.hasChildNodes())
                     targetDoc.getDocumentElement().appendChild(targetDoc.adoptNode(subject)); // Subjekt
                                                                                               // wird
                                                                                               // nur
                                                                                               // ins
                                                                                               // neue
                                                                                               // Doc
                                                                                               // eingeh�ngt
                                                                                               // wenn
                                                                                               // es
                                                                                               // Pr�dikate
                                                                                               // hat
             }
         }
 
         Debug.writeFileToExternalStorage(webContent, "ldXMLPreLDXSLT.xml");
         Debug.logLongString(webContent);
         String result = getStringFromDoc(targetDoc);
         Debug.writeFileToExternalStorage(result, "ldXMLPreRDFXSLT.xml");
         Debug.logLongString(result);
         resultHandler.onParsingResultAvailable(result);
     }
 
 }
