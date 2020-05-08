 package edu.hawaii.systemh.housesimulator.hvac;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 import java.io.IOException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.resource.ClientResource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import edu.hawaii.systemh.api.ApiDictionary.SystemHCommandType;
 import edu.hawaii.systemh.api.ApiDictionary.SystemHState;
 import edu.hawaii.systemh.api.ApiDictionary.SystemHSystem;
 import edu.hawaii.systemh.housesimulator.SimulatorServer;
 
 /**
  * Tests the HTTP operations of the system.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class TestHVAC {
 
   private static final String valueAttributeString = "value";
   private static final int hvacDevicePort = 7102;
   private String hvacUrl = "http://localhost:" + hvacDevicePort + "/hvac/";
   private String putUrl;
   private DomRepresentation xmlRep;
   private ClientResource putClient;
 
   /**
    * Start up a test server before testing any of the operations on this resource.
    * 
    * @throws Exception If problems occur starting up the server.
    */
   @BeforeClass
   public static void startServer() throws Exception {
     SimulatorServer.runServer();
   }
 
   /**
    * Tests that the GET and PUT operations are compliant with milestone 2's HTTP API 2.0.
    * 
    * @throws Exception If GET or PUT request fails.
    */
   @Test
   public void testGetAndPut() throws Exception {
 
     String putTemp = "25";
     putUrl = hvacUrl + "temperature";
     putClient = new ClientResource(putUrl);
 
     // Issue a PUT request to the HVAC system to set the home temperature to 25 C.
     xmlRep = createPutXmlRepresentation(putTemp);
     putClient.put(xmlRep);
     getValue(hvacDevicePort);
 
     /**
      * Case 2: Provide wrong URI to the device system, should return a Not Acceptable reply from the
      * server.
      **/
 
     String wrongCommand = "wrongcommand";
     putUrl = hvacUrl + wrongCommand;
     putClient = new ClientResource(putUrl);
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("command");
     rootElement.setAttribute("name", SystemHCommandType.SET_TEMPERATURE.toString());
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("arg");
     temperatureElement.setAttribute(valueAttributeString, putTemp);
     rootElement.appendChild(temperatureElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     try {
       putClient.put(result);
     }
     catch (Exception e) {
       String errorName = Status.CLIENT_ERROR_NOT_ACCEPTABLE.getName();
       assertEquals("Not Acceptable should be server response.", errorName, e.getMessage());
     }
   }
 
   /**
    * Tests if the mean temperature is calculated correctly when a TemperatureRecord is instantiated
    * with a given average high and average low temperature.
    */
   @Test
   public void testTemperatureRecord() {
     TemperatureRecord record = new TemperatureRecord(50, 30);
     int meanTemperature = record.getMeanTemp();
     assertEquals("Asserting the mean temperature", (50 + 30) / 2, meanTemperature);
   }
 
   /**
    * Tests for a variety of situations that are simulated by the HVAC system and determine the
    * resulting temperatures and boolean flags (occupants are home, desired temperature has been set,
    * etc.) are set appropriately.
    * 
    * @throws Exception When errors occur.
    */
   @Test
   public void testHVACModifyState() throws Exception {
 
     HVACData.resetHVACSystem();
     HVACData hvac = new HVACData();
 
     int currentHomeTemp = getValue(7102);
     // Arbitrarily decide to heat the home by 2C.
     int desiredTemp = currentHomeTemp + 2;
     putUrl = hvacUrl + "temperature";
     putClient = new ClientResource(putUrl);
     xmlRep = createPutXmlRepresentation(Integer.toString(desiredTemp));
     putClient.put(xmlRep);
 
     // When the PUT request to change the home temperature was issued.
     long timestampWhenPutIssued = hvac.getWhenDesiredTempCommandIssued();
     // The current temperature should change 1 degree C after approximately 3 minutes has
     // elapsed since the PUT request has been successfully sent.
     long timestampTempChangeOccur = timestampWhenPutIssued + (1000 * 60 * 3) + 30000;
 
     // Set the HVAC system to simulate home temperatures 3.5 minutes into the future.
     HVACData.setCurrentTime(timestampTempChangeOccur);
 
     // Retrieve the home temperature of the future state.
     DomRepresentation representation = HVACData.toXml();
     Document doc = representation.getDocument();
 
     // Retrieve a child node from the Document object. Represents state data.
     NodeList stateNodes = doc.getElementsByTagName("state");
     // HVAC XML only has one <state> node so we know the first one is the one we want.
     int newCurrentHomeTemp =
         Integer.parseInt(((Element) stateNodes.item(0)).getAttribute(valueAttributeString));
 
    assertTrue("The home temperature should change.", currentHomeTemp != newCurrentHomeTemp);
 
     HVACData.resetHVACSystem();
 
     currentHomeTemp = getValue(7102);
     // Arbitrarily decide to cool the home by 4C.
     desiredTemp = currentHomeTemp - 4;
     xmlRep = createPutXmlRepresentation(Integer.toString(desiredTemp));
     putClient.put(xmlRep);
 
     // System.out.println();
     // System.out.println("Current HVAC state after setting a desired temperature.");
     // HVACData.printHVACSystemState();
     // System.out.println();
 
     // When the PUT request to change the home temperature was issued.
     timestampWhenPutIssued = hvac.getWhenDesiredTempCommandIssued();
     // The current temperature should change 2 degree C after approximately 6 minutes has
     // elapsed since the PUT request has been successfully sent.
     timestampTempChangeOccur = timestampWhenPutIssued + (1000 * 60 * 6) + 30000;
 
     // Set the HVAC system to simulate home temperatures 6.5 minutes into the future.
     HVACData.setCurrentTime(timestampTempChangeOccur);
 
     representation = HVACData.toXml();
     doc = representation.getDocument();
     stateNodes = doc.getElementsByTagName("state");
     newCurrentHomeTemp =
         Integer.parseInt(((Element) stateNodes.item(0)).getAttribute(valueAttributeString));
 
    assertTrue("The home temperature should change", currentHomeTemp != newCurrentHomeTemp);
 
     // System.out.println("Current HVAC state 6.5 mintues after setting a desired temperature.");
     // HVACData.printHVACSystemState();
     // System.out.println();
   }
 
   /**
    * Helper function that queries the HVAC system with a GET request to retrieve the current home
    * temperature. Also asserts that the returned XML DomRepresentation from the GET request complies
    * with the REST/HTTP 2.0 API such as system name, device name, and key name.
    * 
    * @param port The port of the HVAC control device that handles GET request for current state
    * information.
    * @throws IOException If there is a problem getting the document
    * @return The current home temperature.
    */
   public static int getValue(int port) throws IOException {
 
     // Set up the GET client
     String getUrl = "http://localhost:" + port + "/hvac/state";
     ClientResource getClient = new ClientResource(getUrl);
 
     // Get the XML representation.
     DomRepresentation domRep = new DomRepresentation(getClient.get());
     Document domDoc = domRep.getDocument();
 
     String systemNameAttributeName = "system";
     String deviceNameAttributeName = "device";
 
     // Get the root element, in this case would be state-data element.
     Element stateData = domDoc.getDocumentElement();
     // Retrieve the attributes from state-data element, the system and device name.
     String systemName = stateData.getAttribute(systemNameAttributeName);
     String deviceName = stateData.getAttribute(deviceNameAttributeName);
 
     assertEquals("Checking for system name", SystemHSystem.HVAC.toString(), systemName);
     assertEquals("Checking for device name", "arduino-3", deviceName);
 
     // Retrieve a child node from the Document object. Represents state data.
     NodeList xmlList = domDoc.getElementsByTagName("state");
 
     // Retrieve an attributes key.
     String key = ((Element) xmlList.item(0)).getAttribute("key");
     String value = ((Element) xmlList.item(0)).getAttribute(valueAttributeString);
 
     String temperatureString = SystemHState.TEMPERATURE.toString();
 
     assertEquals("Checking that key is TEMPERATURE", key, temperatureString);
 
     return Integer.parseInt(value);
   }
 
   /**
    * Creates a XML representation of a PUT request to the HVAC system to set the home at a certain
    * temperature.
    * 
    * @param value The desired temperature we want the HVAC system to maintain the home at.
    * @return The XML representation for a PUT request to the HVAC system.
    * @throws ParserConfigurationException If there is a problem with the parser.
    * @throws IOException If there is a problem building the document.
    */
   public static DomRepresentation createPutXmlRepresentation(String value)
       throws ParserConfigurationException, IOException {
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("command");
     rootElement.setAttribute("name", SystemHCommandType.SET_TEMPERATURE.toString());
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("arg");
     temperatureElement.setAttribute(valueAttributeString, value);
     rootElement.appendChild(temperatureElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation putXmlRepresentation = new DomRepresentation();
     putXmlRepresentation.setDocument(doc);
 
     return putXmlRepresentation;
   }
 }
