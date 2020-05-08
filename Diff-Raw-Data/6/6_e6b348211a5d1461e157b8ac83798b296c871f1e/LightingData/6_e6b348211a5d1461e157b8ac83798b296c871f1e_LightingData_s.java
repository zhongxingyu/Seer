 package edu.hawaii.systemh.housesimulator.lighting;
 
 import java.util.Calendar;
 import java.util.Date;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import edu.hawaii.systemh.api.ApiDictionary.SystemHState;
 
 /**
  * Provides data on the Lighting system, as well as an XML representation.
  * 
  * @author Anthony Kinsey, Michael Cera, Kurt Teichman
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class LightingData {
 
   /** Initialize Srings so as not to throw pmd with too many literals. */
   private static String key = "key";
   private static String value = "value";
   private static String state = "state";
   /** The current date defaulted to a value when this class is first instantiated. **/
   private static Date currentTime = new Date();
   
   /** The living room lighting level. */
   private static int livingLevel = 70;
   /** The dining room lighting level. */
   private static int diningLevel = 80;
   /** The kitchen lighting level. */
   private static int kitchenLevel = 90;
   /** The bathroom lighting level. */
   private static int bathroomLevel = 100;
   
   /** The dining room lighting color. */
   private static String diningColor = "#FF01FF";
   /** The bathroom lighting color. */
   private static String bathroomColor = "#FF02FF";
   /** The living room lighting color. */
   private static String livingColor = "#FF03FF";
   /** The kitchen lighting color. */
   private static String kitchenColor = "#FF04FF";
   
   /** The living light switch. */
   private static boolean livingEnabled = false;  
   /** The kitchen light switch. */
   private static boolean kitchenEnabled = false;
   /** The bathroom lighting switch. */
   private static boolean bathroomEnabled = false;
   /** The dining light switch. */
   private static boolean diningEnabled = false;
 
   
   /** Flag for if the occupants are home or away. **/
   //private static boolean occupantsHome  = false;
 
   /**
    * Modifies the state of the system. Modeled to match data specified
    * by Engineers xcel sheet, "new Energy Consumption.xls".
    */
   public static void modifySystemState() {
     //final Random randomGenerator = new Random();
     int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
     //int intensity = Math.log(hour)
     switch (hour) {
     case (5) :
       diningLevel = 100;
       livingLevel = 100;
       kitchenLevel = 100;
       bathroomLevel = 100;
       break;
     case (6) :
       diningLevel = 100;
       livingLevel = 100;
       kitchenLevel = 100;
       bathroomLevel = 100;
       break;
     case (7) :
       diningLevel = 100;
       livingLevel = 100;
       kitchenLevel = 100;
       bathroomLevel = 100;
       break;
     case (18) :
       diningLevel = 50;
       livingLevel = 50;
       kitchenLevel = 50;
       bathroomLevel = 50;
       break;
     case (19) :
       diningLevel = 75;
       livingLevel = 75;
       kitchenLevel = 75;
       bathroomLevel = 75;
       break;
     case (20) :
       diningLevel = 100;
       livingLevel = 100;
       kitchenLevel = 100;
       bathroomLevel = 100;
       break;
     case (21) :
       diningLevel = 100;
       livingLevel = 100;
       kitchenLevel = 100;
       bathroomLevel = 100;
     break;
     case (22) :
       diningLevel = 75;
       livingLevel = 75;
       kitchenLevel = 75;
       bathroomLevel = 75;
     break;
     case (23) :
       diningLevel = 50;
       livingLevel = 50;
       kitchenLevel = 50;
       bathroomLevel = 50;
     break;
     case (24) :
       diningLevel = 25;
       livingLevel = 25;
       kitchenLevel = 25;
       bathroomLevel = 25;
     break;
     default: // all other hours, lights are off
       diningLevel = 0;
       livingLevel = 0;
       kitchenLevel = 0;
       bathroomLevel = 0;
   }
     /*
       //Calendar.get(Calendar.HOUR_OF_DAY); // get hour of the day
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
     */
   }
   
   /**
    * Prints the current state of the HVAC system.
    */
   public static void printLightingSystemState() {
     System.out.println("----------------------");
     System.out.println("System: Lighting");
     if (livingEnabled) {
       System.out.println("Living room level: " + livingLevel);
       System.out.println("Living room color: " + livingColor);
     }
     else {
       System.out.println("Living room lights are off.");
     }
     
     if (diningEnabled) {
       System.out.println("Dining room level: " + diningLevel);
       System.out.println("Dining room color: " + diningColor);
     }
     else {
       System.out.println("Dining room lights are off.");
     }
     
     if (kitchenEnabled) {
       System.out.println("Kitchen room level: " + kitchenLevel);
       System.out.println("Kitchen room color: " + kitchenColor);
     }
     else {
       System.out.println("Kitchen room lights are off.");
     }
     
     if (bathroomEnabled) {
       System.out.println("Bathroom level: " + bathroomLevel);
       System.out.println("Bathroom color: " + bathroomColor);
     }
     else {
       System.out.println("Bathroom lights are off.");
     }
   }
   
   /**
    * Return the current level intensity for the living room.
    *
    * @return Current level intensity.
    */
   public long getLivingLevel() {
     return livingLevel;
   }
   
   /**
    * Return the current level intensity for the dining room.
    *
    * @return Current level intensity.
    */
   public long getDiningLevel() {
     return diningLevel;
   }
   
   /**
    * Return the current level intensity for the kitchen room.
    *
    * @return Current level intensity.
    */
   public long getKitchenLevel() {
     return kitchenLevel;
   }
   
   /**
    * Return the current level intensity for the bathroom.
    *
    * @return Current level intensity.
    */
   public long getBathroomLevel() {
     return bathroomLevel;
   }
   
   /**
    * Return the current light color for the living room.
    *
    * @return Current light color.
    */
   public String getLivingColor() {
     return livingColor;
   }
   
   /**
    * Return the current light color for the dining room.
    *
    * @return Current light color.
    */
   public String getDiningColor() {
     return diningColor;
   }
   
   /**
    * Return the current light color for the kitchen room.
    *
    * @return Current light color.
    */
   public String getKitchenColor() {
     return kitchenColor;
   }
   
   /**
    * Return the current light color for the bathroom.
    *
    * @return Current light color.
    */
   public String getBathroomColor() {
     return bathroomColor;
   }
   
   /**
    * Returns true if the living lights are on, off otherwise.
    *
    * @return Lights on or off.
    */
   public boolean isLivingLightsEnabled() {
     return livingEnabled;
   }
   
   /**
    * Returns true if the dining lights are on, off otherwise.
    *
    * @return Lights on or off.
    */
   public boolean isDiningLightsEnabled() {
     return diningEnabled;
   }
   
   /**
    * Returns true if the kitchen lights are on, off otherwise.
    *
    * @return Lights on or off.
    */
   public boolean isKitchenEnabled() {
     return kitchenEnabled;
   }
   
   /**
    * Returns true if the bathroom lights are on, off otherwise.
    *
    * @return Lights on or off.
    */
   public boolean isBathroomEnabled() {
     return bathroomEnabled;
   }
   
   /**
    * Sets the living room lighting level.
    * 
    * @param newLivingLevel the level
    */
   public static void setLivingLevel(int newLivingLevel) {
     livingLevel = newLivingLevel;
   }
 
   /**
    * Sets the dining room lighting level.
    * 
    * @param newDiningLevel the level
    */
 
   public static void setDiningLevel(int newDiningLevel) {
     diningLevel = newDiningLevel;
   }
 
   /**
    * Sets the kitchen lighting level.
    * 
    * @param newKitchenLevel the level
    */
 
   public static void setKitchenLevel(int newKitchenLevel) {
     kitchenLevel = newKitchenLevel;
   }
 
   /**
    * Sets the bathroom lighting level.
    * 
    * @param newBathroomLevel the level
    */
   public static void setBathroomLevel(int newBathroomLevel) {
     bathroomLevel = newBathroomLevel;
   }
   
   /**
    * Sets the bathroom lighting colors.
    * 
    * @param newBathroomColor the colors
    */
   public static void setBathroomColor(String newBathroomColor) {
     bathroomColor = newBathroomColor;
   }
   
   /**
    * Sets the bathroom lighting to on or off.
    * 
    * @param newBathroomEnabled the colors
    */
   public static void setBathroomEnabled(Boolean newBathroomEnabled) {
     bathroomEnabled = newBathroomEnabled;
   }
   
 
   /**
    * Sets the living lighting colors.
    * 
    * @param newLivingColor the colors
    */
   public static void setLivingColor(String newLivingColor) {
     livingColor = newLivingColor;
   }
   
   /**
    * Sets the living room lighting to on or off.
    * 
    * @param newLivingEnabled the colors
    */
   public static void setLivingEnabled(Boolean newLivingEnabled) {
     livingEnabled = newLivingEnabled;
   }
   
   /**
    * Sets the living lighting colors.
    * 
    * @param newKitchenColor the colors
    */
   public static void setKitchenColor(String newKitchenColor) {
    kitchenColor = newKitchenColor;
   }
   
   /**
    * Sets the living room lighting to on or off.
    * 
    * @param newKitchenEnabled the colors
    */
   public static void setKitchenEnabled(Boolean newKitchenEnabled) {
     kitchenEnabled = newKitchenEnabled;
   }
 
   /**
    * Sets the living lighting colors.
    * 
    * @param newDiningColor the colors
    */
   public static void setDiningColor(String newDiningColor) {
    diningColor = newDiningColor;
   }
   
   /**
    * Sets the living room lighting to on or off.
    * 
    * @param newDiningEnabled the colors
    */
   public static void setDiningEnabled(Boolean newDiningEnabled) {
     diningEnabled = newDiningEnabled;
   }
   /**
    * Sets the current time to a new time. Used for reproducing historical or future temperature
    * records.
    * 
    * @param time The new date in milliseconds that have passed since January 1, 1970 00:00:00 GMT.
    */
   public static void setCurrentTime(long time) {
     currentTime = new Date(time);
    System.out.println(currentTime);
   }
 
   /**
    * Returns the data as an XML Document instance.
    * 
    * @param room the room.
    * @return The data as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml(String room) throws Exception {
     //initializing room values so as not to repeat literals. 
     String living = "living";
     String dining = "dining";
     String kitchen = "kitchen";
     String bathroom = "bathroom";
  
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
     //modifySystemState();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", "LIGHTING");
 
     //pmd pickiness.
     String device = "device";
 
     // Set attribute according to room.
     if (living.equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-5");
     }
     else if (dining.equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-6");
     }
     else if (kitchen.equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-7");
     }
     else if (bathroom.equalsIgnoreCase(room)) {
       rootElement.setAttribute(device, "arduino-8");
     }
 
     rootElement.setAttribute("timestamp", String.valueOf(new Date().getTime()));
     doc.appendChild(rootElement);
 
     String levelString = SystemHState.LIGHTING_LEVEL.toString();
     String enableString = SystemHState.LIGHTING_ENABLED.toString();
     String colorString = SystemHState.LIGHTING_COLOR.toString();
     
     // Create state tag.
     Element levelElement = doc.createElement(state);
     levelElement.setAttribute(key, levelString);
 
     Element enableElement = doc.createElement(state);
     enableElement.setAttribute(key, enableString);
     
     Element colorElement = doc.createElement(state);
     colorElement.setAttribute(key, colorString);
     
     // Retrieve lighting level according to room.
     if (living.equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(livingLevel));
       enableElement.setAttribute(value, String.valueOf(livingEnabled));
       colorElement.setAttribute(value, String.valueOf(livingColor));
     }
     else if (dining.equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(diningLevel));
       enableElement.setAttribute(value, String.valueOf(diningEnabled));
       colorElement.setAttribute(value, String.valueOf(diningColor));
     }
     else if (kitchen.equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(kitchenLevel));
       enableElement.setAttribute(value, String.valueOf(kitchenEnabled));
       colorElement.setAttribute(value, String.valueOf(kitchenColor));
     }
     else if (bathroom.equalsIgnoreCase(room)) {
       levelElement.setAttribute(value, String.valueOf(bathroomLevel));
       enableElement.setAttribute(value, String.valueOf(bathroomEnabled));
       colorElement.setAttribute(value, String.valueOf(bathroomColor));
     }
 
     rootElement.appendChild(levelElement);
     rootElement.appendChild(enableElement);
     rootElement.appendChild(colorElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
   /**
    * Appends Lighting state data at a specific timestamp snap-shot to the Document object passed
    * to this method.
    * 
    * @param room The room that is associated with the correct arduino board.
    * @param doc Document object to append Lighting state data as child nodes.
    * @param timestamp The specific time snap-shot the state data interested to be appended.
    * @return Document object with appended Lighting state data.
    */
   public static Document toXmlByTimestamp(Document doc, Long timestamp, String room) {
 
     //initializing room values so as not to repeat literals. 
     String living = "living";
     String dining = "dining";
     String kitchen = "kitchen";
     String bathroom = "bathroom";
     String levelString = SystemHState.LIGHTING_LEVEL.toString();
     String enableString = SystemHState.LIGHTING_ENABLED.toString();
     String colorString = SystemHState.LIGHTING_COLOR.toString();
     
     setCurrentTime(timestamp);
     modifySystemState();
 
     // Get the root element, in this case would be <state-history> element.
     Element rootElement = doc.getDocumentElement();
  
     // Create state-data tag
     Element stateDataElement = doc.createElement("state-data");
     stateDataElement.setAttribute("system", "LIGHTING");
     //to appease pmd.
     String device = "device";
     
     // Set attribute according to room.
     if (living.equalsIgnoreCase(room)) {
       stateDataElement.setAttribute(device, "arduino-5");
     }
     else if (dining.equalsIgnoreCase(room)) {
       stateDataElement.setAttribute(device, "arduino-6");
     }
     else if (kitchen.equalsIgnoreCase(room)) {
       stateDataElement.setAttribute(device, "arduino-7");
     }
     else if (bathroom.equalsIgnoreCase(room)) {
       stateDataElement.setAttribute(device, "arduino-8");
     }
     
     stateDataElement.setAttribute("timestamp", timestamp.toString());
     rootElement.appendChild(stateDataElement);
   
     // Retrieve lighting level according to room.
     if (living.equalsIgnoreCase(room)) {
       Element livingLevelElement = doc.createElement(state);
       livingLevelElement.setAttribute(key, levelString);
       livingLevelElement.setAttribute(value, String.valueOf(livingLevel));
       stateDataElement.appendChild(livingLevelElement);
       
       Element livingEnabledElement = doc.createElement(state);
       livingEnabledElement.setAttribute(key, enableString);
       livingEnabledElement.setAttribute(value, String.valueOf(livingEnabled));
       stateDataElement.appendChild(livingEnabledElement);
 
       Element livingColorElement = doc.createElement(state);
       livingColorElement.setAttribute(key, colorString);
       livingColorElement.setAttribute(value, String.valueOf(livingColor));
       stateDataElement.appendChild(livingColorElement);
       
     }
     else if (dining.equalsIgnoreCase(room)) {
       Element diningLevelElement = doc.createElement(state);
       diningLevelElement.setAttribute(key, levelString);
       diningLevelElement.setAttribute(value, String.valueOf(diningLevel));
       stateDataElement.appendChild(diningLevelElement);
 
       Element diningEnabledElement = doc.createElement(state);
       diningEnabledElement.setAttribute(key, enableString);
       diningEnabledElement.setAttribute(value, String.valueOf(diningEnabled));
       stateDataElement.appendChild(diningEnabledElement);
 
       Element diningColorElement = doc.createElement(state);
       diningColorElement.setAttribute(key, colorString);
       diningColorElement.setAttribute(value, String.valueOf(diningColor));
       stateDataElement.appendChild(diningColorElement);
     
     }
     else if (kitchen.equalsIgnoreCase(room)) {
       Element kitchenLevelElement = doc.createElement(state);
       kitchenLevelElement.setAttribute(key, levelString);
       kitchenLevelElement.setAttribute(value, String.valueOf(kitchenLevel));
       stateDataElement.appendChild(kitchenLevelElement);
 
       Element kitchenEnabledElement = doc.createElement(state);
       kitchenEnabledElement.setAttribute(key, enableString);
      kitchenLevelElement.setAttribute(value, String.valueOf(kitchenEnabled));
      stateDataElement.appendChild(kitchenLevelElement);
       
       Element kitchenColorElement = doc.createElement(state);
       kitchenColorElement.setAttribute(key, colorString);
       kitchenColorElement.setAttribute(value, String.valueOf(kitchenColor));
       stateDataElement.appendChild(kitchenColorElement);
       
     }
     else if (bathroom.equalsIgnoreCase(room)) {
       Element bathroomLevelElement = doc.createElement(state);
       bathroomLevelElement.setAttribute(key, levelString);
       bathroomLevelElement.setAttribute(value, String.valueOf(bathroomLevel));
       stateDataElement.appendChild(bathroomLevelElement);
       
       Element bathroomEnabledElement = doc.createElement(state);
       bathroomEnabledElement.setAttribute(key, enableString);
       bathroomEnabledElement.setAttribute(value, String.valueOf(bathroomEnabled));
       stateDataElement.appendChild(bathroomEnabledElement);
 
       Element bathroomColorElement = doc.createElement(state);
       bathroomColorElement.setAttribute(key, colorString);
       bathroomColorElement.setAttribute(value, String.valueOf(bathroomColor));
       stateDataElement.appendChild(bathroomColorElement);
       
     }
       return doc;
   }
 
 }
