 package edu.hawaii.ihale.housesimulator.lighting;
 
 import java.util.Date;
 import java.util.Random;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Provides data on the Lighting system, as well as an XML representation.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class LightingData {
 
   /** The living room lighting level. */
   private static long livingLevel = 100;
   /** The dining room lighting level. */
   private static long diningLevel = 80;
   /** The kitchen lighting level. */
   private static long kitchenLevel = 0;
   /** The bathroom lighting level. */
   private static long bathroomLevel = 0;
 
   /**
    * Modifies the state of the system.
    */
   public static void modifySystemState() {
     final Random randomGenerator = new Random();
 
     // Living Room lights will be on ~60% of the time
     // When they are turned on they will be set to a
     // random value, just to show fluctuation in data
     if (randomGenerator.nextDouble() > 0.4) {
       if (randomGenerator.nextBoolean()) {
         livingLevel = randomGenerator.nextInt(101);
       }
     }
     else {
       livingLevel = 0;
     }
 
     // Dining Room lights will be on ~20% of the time
     // When they are turned on they will be set to a
     // random value, just to show fluctuation in data
     if (randomGenerator.nextDouble() > 0.8) {
       if (randomGenerator.nextBoolean()) {
         diningLevel = randomGenerator.nextInt(101);
       }
     }
     else {
       diningLevel = 0;
     }
 
     // Kitchen lights will be on ~15% of the time
     // When they are turned on they will be set to a
     // random value, just to show fluctuation in data
     if (randomGenerator.nextDouble() > 0.85) {
       if (randomGenerator.nextBoolean()) {
         kitchenLevel = randomGenerator.nextInt(101);
       }
     }
     else {
       kitchenLevel = 0;
     }
 
     // Bathroom lights will be on ~15% of the time
     // When they are turned on they will be set to a
     // random value, just to show fluctuation in data
     if (randomGenerator.nextDouble() > 0.85) {
       if (randomGenerator.nextBoolean()) {
         bathroomLevel = randomGenerator.nextInt(101);
       }
     }
     else {
       bathroomLevel = 0;
     }
 
     System.out.println("----------------------");
     System.out.println("System: Lighting");
     System.out.println("Living Room Level: " + livingLevel);
     System.out.println("Dining Room Level: " + diningLevel);
     System.out.println("Kitchen Room Level: " + kitchenLevel);
     System.out.println("Bathroom Level: " + bathroomLevel);
   }
 
   /**
    * Sets the living room lighting level.
    * 
    * @param newLivingLevel the level
    */
   public static void setLivingLevel(long newLivingLevel) {
     livingLevel = newLivingLevel;
   }
 
   /**
    * Sets the dining room lighting level.
    * 
    * @param newDiningLevel the level
    */
 
   public static void setDiningLevel(long newDiningLevel) {
     diningLevel = newDiningLevel;
   }
 
   /**
    * Sets the kitchen lighting level.
    * 
    * @param newKitchenLevel the level
    */
 
   public static void setKitchenLevel(long newKitchenLevel) {
     kitchenLevel = newKitchenLevel;
   }
 
   /**
    * Sets the bathroom lighting level.
    * 
    * @param newBathroomLevel the level
    */
   public static void setBathroomLevel(long newBathroomLevel) {
     bathroomLevel = newBathroomLevel;
   }
 
   /**
    * Returns the data as an XML Document instance.
    * 
    * @param room the room.
    * @return The data as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml(String room) throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", "lighting");
 
     String device = "device";
 
     // Set attribute according to room.
     if ("living".equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-5");
     }
     else if ("dining".equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-6");
     }
     else if ("kitchen".equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-7");
     }
     else if ("bathroom".equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-8");
     }
 
     rootElement.setAttribute("timestamp", String.valueOf(new Date().getTime()));
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element levelElement = doc.createElement("state");
     levelElement.setAttribute("key", "level");
 
     String value = "value";
     // Retrieve lighting level according to room.
     if ("living".equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(livingLevel));
     }
     else if ("dining".equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(diningLevel));
     }
     else if ("kitchen".equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(kitchenLevel));
     }
     else if ("bathroom".equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(bathroomLevel));
     }
 
     rootElement.appendChild(levelElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
 }
