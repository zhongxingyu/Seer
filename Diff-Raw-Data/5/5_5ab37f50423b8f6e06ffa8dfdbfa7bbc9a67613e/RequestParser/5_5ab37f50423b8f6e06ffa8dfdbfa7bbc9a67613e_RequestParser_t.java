 package dk.itu.kf04.g4tw.controller;
 
 import dk.itu.kf04.g4tw.model.MapModel;
 import dk.itu.kf04.g4tw.model.Road;
 import dk.itu.kf04.g4tw.util.DynamicArray;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.logging.Logger;
 
 /**
  * Handles request for the web-server via the paseToInputStream method.
  */
 public class RequestParser {
 
     public static Logger Log = Logger.getLogger(RequestParser.class.getName());
     
     /**
      * Handles input from the server through the input parameter, decodes it and returns an appropriate
      * message as an array of bytes, ready to dispatch to the sender.
      * @param input  The input string received from the client
      * @return  A series of bytes as a response
      * @throws IllegalArgumentException  If the input is malformed
      * @throws UnsupportedEncodingException If the input cannot be understood under utf-8 encoding
      * @throws TransformerException  If we fail to transform the xml-document to actual output
      */
     public static byte[] parseToInputStream(String input) throws IllegalArgumentException, UnsupportedEncodingException, TransformerException {
     	// Variables for the request
     	double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
     	int filter = 0;
 
         // Decode the input and split it up
         String[] queries = URLDecoder.decode(input, "UTF-8").split("&");
 
         if (queries.length != 5) {
             throw new IllegalArgumentException("Must have exactly 5 queries.");
         }
 
         // Iterate through the queries
         for(String query : queries) {
             String[] arr = query.split("=");
             
             // Something is wrong if there are not exactly 2 values
             if (arr.length != 2) {
                 throw new IllegalArgumentException("Must have format x1=lowValue&y1=lowValue&x2=highValue&y2=highValue&filter=[1-128]");
             } else {
             	// Assign input to local variables
                 for(int i = 0; i < arr.length; i++) {
 					String name = arr[i];
 					String value = arr[++i];
 					if(name.equals("x1")) x1 = Double.parseDouble(value);
 					if(name.equals("x2")) x2 = Double.parseDouble(value);
 					if(name.equals("y1")) y1 = Double.parseDouble(value);
 					if(name.equals("y2")) y2 = Double.parseDouble(value);
 					if(name.equals("filter")) filter = Integer.parseInt(value);
 				}
             }
         }
 
 		// Time when the search starts
         long startTime = System.currentTimeMillis();
 
         // Instantiate the parse
         XMLDocumentParser xmlParser = new XMLDocumentParser();
 
         // Search the model and concatenate the results with the previous
         DynamicArray<Road> search = MapModel.search(x1, y1, x2, y2, filter);
 
 		// Creates an XML document
 		Document docXML = xmlParser.createDocument();
         
         // Creates a roadCollection element inside the root and add namespaces
         Element roads = docXML.createElement("roadCollection");
         roads.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         roads.setAttribute("xsi:noNamespaceSchemaLocation", "kraX.xsd");
         docXML.appendChild(roads);
 
 		// Iterates through the search array, appending the XML element of the current
 		// road to the roadCollection element. This is creating the XML document.
 		for(int i = 0; i < search.length(); i++) {
             roads.appendChild(search.get(i).toXML(docXML));
         }
 
         // Create the source
         Source source = new DOMSource(docXML);
         
         // Instantiate output-sources
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         Result result            = new StreamResult(os);
         
         // Instantiate xml-transformers
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer    = factory.newTransformer();
 
         // Transform the xml to the byte array format
         transformer.transform(source, result);
         
 		// calculates and prints the time taken.
 		long endTime = System.currentTimeMillis() - startTime;
 		Log.info("Found and wrote " + search.length() + " roads in : " + endTime + "ms");
 
         // Return the byte-array
         return os.toByteArray();
     }
 }
