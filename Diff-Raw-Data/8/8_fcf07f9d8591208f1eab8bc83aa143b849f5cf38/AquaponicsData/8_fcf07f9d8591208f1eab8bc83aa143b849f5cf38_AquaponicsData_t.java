 package edu.hawaii.ihale.housesimulator.aquaponics;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 import java.util.Random;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Provides data on the Aquaponics system, as well as an XML representation.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class AquaponicsData {
 
 //  /** Test number generation here. */
 //  public static void main(String[] args) {
 //    for (int i = 0; i < 10; i++) {
 //      System.out.println((randomGen.nextInt(2) * randomGen.nextInt(2)
 //          * randomGen.nextInt(2))
 //          - (randomGen.nextInt(2) * randomGen.nextInt(2) *
 //              randomGen.nextInt(2)));
 //    }
 //  }
   
   /** Random generator. */
   private static final Random randomGen = new Random();
 
   // Necessary conditions for fish to live.
   /** Number of fish alive in tank. */
   private static int alive_fish = 100;
   /** Minimum electrical conductivity for fish. */
   private static double minEC = 1.0;
   /** Maximum electrical conductivity for fish. */
   private static double maxEC = 2.0;
   /** Minimum temperature for fish. */
   private static int minTemp = 20;
   /** Maximum temperature for fish. */
   private static int maxTemp = 45;
   /** Minimum water level for fish. */
   private static int minWaterLevel = alive_fish;
   /** Minimum water PH for fish. */
   private static double minPH = 6.8;
   /** Maximum water PH for fish. */
   private static double maxPH = 8.0;
   /** Minimum oxygen level for fish. */
   private static double minOxygen = alive_fish;
   /** Minimum tank water circulation for fish. */
   private static double minCirc = minOxygen;
   
   /** The current water circulation. */
   private static double circulation = (randomGen.nextDouble() * 10) + alive_fish;
   /** The current number of dead fish. */
   private static int dead_fish = 0;
   /** The current electrical conductivity. */
   private static double ec = (randomGen.nextDouble()) + 1;
   /** The current temperature. */
   private static int temperature = randomGen.nextInt(3) + 28;
   /** The current turbidity. */
   private static double turbidity = ec + dead_fish + circulation;
   /** The current water level. */
   private static int water_level = (randomGen.nextInt(51)) + 200;
   /** The current pH. */
   private static double ph = (randomGen.nextDouble() * 1.2) + 6.8;
   /** The current dissolved oxygen. */
   private static double oxygen = circulation;
 
   /** The desired electrical conductivity. */
   private static double desiredEC = (randomGen.nextDouble()) + 1;
   /** The desired temperature. */
   private static int desiredTemperature = randomGen.nextInt(3) + 28;
   /** The desired water level. */
   private static int desiredWaterLevel = (randomGen.nextInt(51)) + 200;
   /** The desired pH. */
   private static double desiredPh = (randomGen.nextDouble() * 1.2) + 6.8;
   
   /**
    * Modifies the state of the system.
    */
   public static void modifySystemState() {
 
     // Print system state values.
     final String desired = " (Desired: "; // PMD pickiness
     final String required = " (Required: "; // PMD pickiness
     System.out.println("----------------------");
     System.out.println("System: Aquaponics");
     System.out.println("Alive fish: " + alive_fish);
     System.out.println("Dead fish: " + dead_fish);
     System.out.println("Circulation: " + roundSingleDecimal(circulation) + required + minCirc
         + ")");
     System.out.println("Electrical conductivity: " + roundSingleDecimal(ec)
         + desired + roundSingleDecimal(desiredEC) + ")" + required + minEC + "-"
         + maxEC + ")");
     System.out.println("Temperature: " + temperature + desired + desiredTemperature + ")"
         + required + minTemp + "-" + maxTemp + ")");
     System.out.println("Turbidity: " + roundSingleDecimal(turbidity));
     System.out.println("Water level: " + water_level + desired + desiredWaterLevel + ")" + required
         + minWaterLevel + ")");
     System.out.println("pH: " + roundSingleDecimal(ph) + desired + roundSingleDecimal(desiredPh)
         + ")" + required + minPH + "-" + maxPH + ")");
     System.out.println("Oxygen level: " + roundSingleDecimal(oxygen) + required + minOxygen + ")");
     
     // Changes water circulation by a random amount.
     if (circulation < (alive_fish * 0.8)) {
       // Fish need more circulation and oxygen!!!  Increment circulation by 0.0 to 1.0 units.
       circulation += randomGen.nextDouble();
     }
     else {
       // Increment or decrement circulation by 0.0 to 1.0 units.
       circulation += randomGen.nextDouble() - randomGen.nextDouble();
     }
     
     // Checks if fish are going to die or not.
     if ((ec < minEC) || (ec > maxEC) || (temperature < minTemp) || (temperature > maxTemp)
         || (water_level < minWaterLevel) || (ph < minPH) || (ph > maxPH) || (oxygen < minOxygen)) {
       // 50% chance of 1 fish dying.
       int fishDeath = randomGen.nextInt(2);
       dead_fish += fishDeath;
       alive_fish -= fishDeath;
       if (fishDeath == 1) {
         // 1 dead fish increases EC by 0.05
         ec += 0.05;
       }
     }
     
     // Increments electrical conductivity within range of desired value.
     if (ec < desiredEC) {
       // 25% chance of incrementing by 0.05
       ec += (randomGen.nextInt(2) * randomGen.nextInt(2)) * 0.05;
     }
     else if (ec > desiredEC) {
       // 50% chance of decrementing by 0.05.  EC declines as nutrients and food gets used up.
       ec -= (randomGen.nextInt(2)) * 0.05;
     }
    else {
       // 12.5% chance of incrementing or decrementing 0.05 from desired ec.
       ec += ((randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))
         - (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))) * 0.05;
     }
     
     // Increments temperature within range of the desired temperature.
     if (temperature < desiredTemperature) {
       // 25% chance of incrementing by 1 degree.
       temperature += randomGen.nextInt(2) * randomGen.nextInt(2);
     }
     else if (temperature > desiredTemperature) {
       // 25% chance of decrementing by 1 degree.
       temperature -= randomGen.nextInt(2) * randomGen.nextInt(2);
     }
     else if (temperature == desiredTemperature) {
       // 12.5% chance of incrementing or decrementing 1 degree from desired temperature.
       temperature += (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))
         - (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2));
     }
     
     // Changes water level by a random amount.
     if (water_level < (minWaterLevel)) {
       // Fish need more water!!!  50% chance to increment water_level by 1 unit.
       water_level += randomGen.nextInt(2);
     }
     else {
       // Increment or decrement water_level by 1 unit.
       water_level += randomGen.nextInt(2) - randomGen.nextInt(2);
     }
     // Update required water level in case fish have died.
     minWaterLevel = alive_fish;
     
     // Increments PH within range of the desired PH.
     if (ph < desiredPh) {
       // 25% chance of incrementing by 0.1
       ph += (randomGen.nextInt(2) * randomGen.nextInt(2)) * 0.1;
     }
     else if (ph > desiredPh) {
       // 25% chance of decrementing by 0.1
       ph -= (randomGen.nextInt(2) * randomGen.nextInt(2)) * 0.1;
     }
    else {
       // 12.5% chance of incrementing or decrementing 0.1 from desired PH.
       ph += ((randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))
         - (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))) * 0.1;
     }
     
     // Update oxygen.
     oxygen = circulation;
     // Update required circulation in case fish have died.
     minCirc = alive_fish;
     // Update required oxygen in case fish have died.
     minOxygen = alive_fish;
     // Updates turbidity.  More dead fish = more turbidity.  More circulation = less turbidity.
    turbidity = ((alive_fish * 1.0) / 2) + ec + (dead_fish * 2) - (circulation - minCirc);
   }
   
   /**
    * Modifies the desired system state values to within acceptable range.
    */
   public static void modifyDesiredState() {
     desiredEC = (randomGen.nextDouble()) + 1;
     desiredTemperature = randomGen.nextInt(3) + 28;
     desiredWaterLevel = (randomGen.nextInt(51)) + 200;
     desiredPh = (randomGen.nextDouble() * 1.2) + 6.8;
   }
 
   /**
    * Sets the desired temperature.
    * 
    * @param newDesiredTemperature the desired temperature
    */
   public static void setDesiredTemperature(int newDesiredTemperature) {
     desiredTemperature = newDesiredTemperature;
   }
 
   /**
    * Adds fish feed to the tank.
    * 
    * @param feedAmount the amount of fish feed to add.
    */
   public static void addFishFeed(double feedAmount) {
     ec += feedAmount * 0.01;
   }
 
   /**
    * Harvests fish from tank.
    * 
    * @param numFish the amount of fish to harvest.
    */
   public static void harvestFish(int numFish) {
     alive_fish -= numFish;
   }
 
   /**
    * Sets the desired nutrient amount which corresponds to EC.
    * 
    * @param newDesiredNutrients the nutrient level.
    */
   public static void setNutrients(double newDesiredNutrients) {
     desiredEC = newDesiredNutrients;
   }
 
   /**
    * Sets the desired pH.
    * 
    * @param newDesiredPh the ph.
    */
   public static void setDesiredPh(double newDesiredPh) {
     desiredPh = newDesiredPh;
   }
 
   /**
    * Sets the desired water level.
    * 
    * @param newDesiredWaterLevel the water level.
    */
   public static void setDesiredWaterLevel(int newDesiredWaterLevel) {
     desiredWaterLevel = newDesiredWaterLevel;
   }
 
   /**
    * Rounds double value to a single decimal place.
    * 
    * @param doubleValue A double value
    * @return Rounded value
    */
   static double roundSingleDecimal(double doubleValue) {
     DecimalFormat singleDecimal = new DecimalFormat("#.#");
     return Double.valueOf(singleDecimal.format(doubleValue));
   }
   
   
 
   /**
    * Returns the data as an XML Document instance.
    * 
    * @return The data as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml() throws Exception {
     final String state = "state"; // PMD pickiness
     final String key = "key"; // PMD pickiness
     final String value = "value"; // PMD pickiness
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", "AQUAPONICS");
     rootElement.setAttribute("device", "arduino-1");
     rootElement.setAttribute("timestamp", String.valueOf(new Date().getTime()));
     doc.appendChild(rootElement);
 
     // Create circulation state tag.
     Element circulationElement = doc.createElement(state);
     circulationElement.setAttribute(key, "CIRCULATION");
     circulationElement.setAttribute(value, String.valueOf(circulation));
     rootElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(dead_fish));
     rootElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value, String.valueOf(ec));
     rootElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(temperature));
     rootElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(turbidity));
     rootElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(water_level));
     rootElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(ph)));
     rootElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(oxygen));
     rootElement.appendChild(oxygenElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
   
   /**
    * Appends Aquaponics state data at a specific timestamp snap-shot to the Document object
    * passed to this method.
    *
    * @param doc Document object to append Aquaponics state data as child nodes.
    * @param timestamp The specific time snap-shot the state data interested to be appended.
    * @return Document object with appended Aquaponics state data.
    */
   public static Document toXmlByTimestamp(Document doc, Long timestamp) {
     final String state = "state"; // PMD pickiness
     final String key = "key"; // PMD pickiness
     final String value = "value"; // PMD pickiness
 
     // Change the values.
     modifyDesiredState();
     modifySystemState();
     
     // Get the root element, in this case would be <state-history> element.
     Element rootElement = doc.getDocumentElement();
     
     // Create state-data tag
     Element stateElement = doc.createElement("state-data");
     stateElement.setAttribute("system", "AQUAPONICS");
     stateElement.setAttribute("device", "arduino-1");
     stateElement.setAttribute("timestamp", timestamp.toString());
     rootElement.appendChild(stateElement);
 
     // Create circulation state tag.
     Element circulationElement = doc.createElement(state);
     circulationElement.setAttribute(key, "CIRCULATION");
     circulationElement.setAttribute(value, String.valueOf(circulation));
     stateElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(dead_fish));
     stateElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value, String.valueOf(ec));
     stateElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(temperature));
     stateElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(turbidity));
     stateElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(water_level));
     stateElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(ph)));
     stateElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(oxygen));
     stateElement.appendChild(oxygenElement);
 
     // Return the XML in DomRepresentation form.
     return doc;
   }
 
 }
