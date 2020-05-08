 package dk.itu.kf04.g4tw.controller;
 
 import dk.itu.kf04.g4tw.model.*;
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
      * @param model  The model to perform searches on
      * @param input  The input string received from the client
      * @return  A series of bytes as a response
      * @throws IllegalArgumentException  If the input is malformed
      * @throws UnsupportedEncodingException If the input cannot be understood under utf-8 encoding
      * @throws TransformerException  If we fail to transform the xml-document to actual output
      */
     public static byte[] parseToInputStream(MapModel model, String input) throws IllegalArgumentException, UnsupportedEncodingException, TransformerException {
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
         DynamicArray<Road> search = model.search(x1, y1, x2, y2, filter);
 
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
 
         // Transform the xml
         transformer.transform(source, result);
         
 		// calculates and prints the time taken.
 		long endTime = System.currentTimeMillis() - startTime;
 		Log.info("Found and wrote " + search.length() + " roads in : " + endTime + "ms");
 
         // Return the result-stream as a byte-array
         return os.toByteArray();
     }
 
     /**
      * Handles input from the server through the input parameter and returns an appropriate
      * message as an array of bytes, ready to dispatch to the sender.
      * @param model  The model to perform the search on
      * @param input  The input string received from the client
      * @return  A series of bytes as a response
      * @throws IllegalArgumentException  If the input is malformed
      * @throws TransformerException  If we fail to transform the xml-document to actual output
      */
     public static byte[] parsePathToInputStream(MapModel model, String input) throws IllegalArgumentException, TransformerException {
         String[] inputs = input.split("&");
 
         // if there ain't exactly 2 arguments in the request, throw an error!
         if(!(inputs.length == 2 || inputs.length == 4))
             throw new IllegalArgumentException("Must have the format \"adr1=first+address&adr2=second+address\" OR \"adr1=first+address&adr2=second+address&id1=Xid2=Y\"");
 
         // The two addresses from the client
         String adr1 = inputs[0].substring(5);
         String adr2 = inputs[1].substring(5);
         int id1 = 0, id2 = 0;
 
         // Array over all the roads that match the address.
         DynamicArray<Road> hits1 = AddressParser.getRoad(adr1);
         DynamicArray<Road> hits2 = AddressParser.getRoad(adr2);
 
         if(inputs.length == 4) {
             id1 = Integer.parseInt(inputs[2].substring(4));
             id2 = Integer.parseInt(inputs[3].substring(4));
 
             if(hits1.length() > 1) {
                 outerloop:
                 for(int i = 0; i < hits1.length(); i++)
                     if(hits1.get(i).getId() == id1) {
                         Road hit = hits1.get(i);
                         hits1 = new DynamicArray<Road>();
                         hits1.add(hit);
                         break outerloop;
                     }
             }
             if(hits2.length() > 1) {
                 outerloop:
                 for(int i = 0; i < hits2.length(); i++)
                     if(hits2.get(i).getId() == id2) {
                         Road hit = hits2.get(i);
                         hits2 = new DynamicArray<Road>();
                         hits2.add(hit);
                         break outerloop;
                     }
             }
         }
 
 
         // Instantiate the parser
         XMLDocumentParser xmlParser = new XMLDocumentParser();
 
         System.out.println(xmlParser.createDocument().getXmlEncoding());
 
         // Creates an XML document
         Document docXML = xmlParser.createDocument();
 
         // Creates a roadCollection element inside the root.
         Element roads = null;
 
         if(hits1.length() == 0 || hits2.length() == 0) { // One or both of the addresses gave zero hits. User have to give a new address.
             // Oh crap, couldn't find at least one of the addresses!
             roads = docXML.createElement("error");
             roads.setAttribute("type", "1");
             docXML.appendChild(roads);
 
             if(hits1.length() == 0) {
                 Element element = docXML.createElement("address");
                 element.appendChild(docXML.createTextNode(adr1));
                 roads.appendChild(element);
                 Log.info("Could not find \"" + adr1 + "\" in the system");
             }
 
             if(hits2.length() == 0) {
                 Element element = docXML.createElement("address");
                 element.appendChild(docXML.createTextNode(adr2));
                 roads.appendChild(element);
                 Log.info("Could not find \"" + adr2 + "\" in the system");
             }
 
         } else if(hits1.length() == 1 && hits2.length() == 1) { // The addresses both gave only one hit. We can find a path.
             // You've found a path. Now go make some cool XML stuff!!!
             // TODO: Find a way to see if there are any connection between the two roads. Maybe there are no reason for doing that?
             Log.info("Trying to find path");
             Road[] result = DijkstraSP.shortestPath(model, hits1.get(0), hits2.get(0));
 
             // Initialize the roadCollection element and add namespaces
             roads = docXML.createElement("roadCollection");
             roads.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
             roads.setAttribute("xsi:noNamespaceSchemaLocation", "kraX.xsd");
             docXML.appendChild(roads);
 
             // Iterates through the result array, appending the XML element of the current
             // road to the roadCollection element. This is creating the XML document.
             int prev = hits2.get(0).getId();
             roads.appendChild(hits2.get(0).toXML(docXML));
            while(result[prev] != null) {
                 roads.appendChild(result[prev].toXML(docXML));
                 prev = result[prev].getId();
            };
 
             System.out.println("Start ID: " + hits1.get(0).getId());
             System.out.println("End ID: " + hits2.get(0).getId());
         } else { // One or both of the addresses gave more than one hit. Make the user decide.
             // Alright, we have a problem. Put we can fix this. Right?
             roads = docXML.createElement("error");
             roads.setAttribute("type", "2");
             docXML.appendChild(roads);
 
             if(hits1.length() > 1) {
                 Element collection = docXML.createElement("collection");
                 roads.appendChild(collection);
                 Element element = docXML.createElement("address");
                 element.appendChild(docXML.createTextNode(adr1));
                 collection.appendChild(element);
 
                 for (int i = 0; i < hits1.length(); i++)
                 {
                     collection.appendChild(hits1.get(i).toErrorXML(docXML));
                 }
                 Log.info("Found more than one road. \"" + adr1 + "\" in the system");
             }
 
             if(hits2.length() > 1) {
                 Element collection = docXML.createElement("collection");
                 roads.appendChild(collection);
                 Element element = docXML.createElement("address");
                 element.appendChild(docXML.createTextNode(adr2));
                 collection.appendChild(element);
 
                 for (int i = 0; i < hits2.length(); i++)
                 {
                     collection.appendChild(hits2.get(i).toErrorXML(docXML));
                 }
                 Log.info("Found more than one road. \"" + adr2 + "\" in the system");
             }
 
         }
 
         // Create the source
         Source source = new DOMSource(docXML);
 
         // Instantiate output-sources
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         Result result            = new StreamResult(os);
 
         // Instantiate xml-transformers
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer    = factory.newTransformer();
 
         // Transform the xml
         transformer.transform(source, result);
 
         // Return the result-stream as a byte-array
         return os.toByteArray();
     }
 }
