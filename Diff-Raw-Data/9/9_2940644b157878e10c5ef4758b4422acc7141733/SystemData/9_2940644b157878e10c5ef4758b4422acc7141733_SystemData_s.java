 package edu.hawaii.systemh.android.systemdata;
 
 import java.text.DecimalFormat;
 import java.util.Locale;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import edu.hawaii.systemh.android.menu.Menu;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 /**
  * Retrieves and Change data from the backend.
  * 
  * @author Group H
  * 
  */
 public class SystemData {
   
   private static final String HTTP = "http://";
 
   // Initialize
   String ipAddress = "168.105.242.83";
 
   String system = "";
   String device = "";
   long timestamp = 0;
   String key = "";
   String value = "";
 
   long temperature = 0;
   double circulation = 0;
   double ec = 0;
   double turbidity = 0;
   int waterLevel = 0;
   double ph = 0;
   double oxygen = 0;
   int deadFish = 0;
 
   long power = 0;
   long energy = 0;
 
   int lightingLevel = 0;
   boolean enabled = false;
   String color = "";
 
   /**
    * Getter of values.
    * 
    * @param systemName The name of the system you want to get data from
    */
   public SystemData(String systemName) {
 
     final String TAG = SystemData.class.getSimpleName();
 
     SharedPreferences preferences = Menu.getPreferences();
 
     String ip = preferences.getString("ip_address", null);
     if (ip == null) {
       ipAddress = "127.0.0.1";
     }
     else {
       ipAddress = ip;
     }
     Log.d(TAG, "IP address: " + ipAddress);
     
     String getUrl = null;
 
     try {
 
       if ("Aquaponics".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/AQUAPONICS/state";
 
       }
       else if ("hvac".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/HVAC/state";
 
       }
       else if ("electric".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/ELECTRIC/state";
 
       }
       else if ("photovoltaics".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/PHOTOVOLTAIC/state";
 
       }
       else if ("lighting-bathroom".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/LIGHTING/state?room=BATHROOM";
 
       }
       else if ("lighting-livingroom".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/LIGHTING/state?room=LIVING";
 
       }
       else if ("lighting-diningroom".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/LIGHTING/state?room=DINING";
 
       }
       else if ("lighting-kitchen".equalsIgnoreCase(systemName)) {
         getUrl = HTTP + ipAddress + ":8111/LIGHTING/state?room=KITCHEN";
 
       }
       else {
         System.out.println("Unknown System Name!");
       }
 
       ClientResource getClient = new ClientResource(getUrl);
 
       DomRepresentation domRep = new DomRepresentation(getClient.get());
 
       // Get the XML representation.
       Document doc = domRep.getDocument();
       doc.getDocumentElement().normalize();
 
       // get root element of the xml
       Node root = doc.getDocumentElement();
 
       // Get attributes of root element
       NamedNodeMap rootAttributes = root.getAttributes();
       for (int i = 0; i < rootAttributes.getLength(); i++) {
         Node rootAttribute = rootAttributes.item(i);
 
         String label = rootAttribute.getNodeName();
         String data = rootAttribute.getNodeValue();
 
         // Checks the labels and store it
         if ("system".equals(label)) {
           system = data;
         }
 
         if ("device".equals(label)) {
           device = data;
         }
 
         if ("timestamp".equals(label)) {
           timestamp = Long.parseLong(data.trim());
         }
       }
 
       // Get attributes of child element
       NodeList children = root.getChildNodes();
 
       for (int i = 0; i < children.getLength(); i++) {
 
         Node child = children.item(i);
 
         if (child.getNodeType() == Node.ELEMENT_NODE) {
 
           // Get attributes of root element
           NamedNodeMap childAttributes = child.getAttributes();
           for (int x = 0; x < childAttributes.getLength(); x++) {
             Node childAttribute = childAttributes.item(x);
 
             String name = childAttribute.getNodeName();
             String val = childAttribute.getNodeValue();
 
             // Store the key and value (e.g. Temperature 75)
             if ("key".equals(name)) {
               key = val;
             }
             else {
               value = val;
             }
           }
 
           if ("Temperature".equalsIgnoreCase(key)) {
             temperature = Long.parseLong(value);
           }
           else if ("Electrical_Conductivity".equalsIgnoreCase(key)) {
             ec = Double.valueOf(value);
           }
           else if ("Ph".equalsIgnoreCase(key)) {
             ph = Double.valueOf(value);
           }
           else if ("Circulation".equalsIgnoreCase(key)) {
             circulation = Double.valueOf(value);
           }
           else if ("Turbidity".equalsIgnoreCase(key)) {
             turbidity = Double.valueOf(value);
           }
           else if ("Oxygen".equalsIgnoreCase(key)) {
             oxygen = Double.valueOf(value);
           }
           else if ("Water_Level".equalsIgnoreCase(key)) {
             waterLevel = Integer.parseInt(value);
           }
           else if ("Dead_Fish".equalsIgnoreCase(key)) {
             deadFish = Integer.parseInt(value);
           }
           else if ("Energy".equalsIgnoreCase(key)) {
             energy = Long.parseLong(value);
           }
           else if ("Power".equalsIgnoreCase(key)) {
             power = Long.parseLong(value);
           }
           else if ("Lighting_Level".equalsIgnoreCase(key)) {
             lightingLevel = Integer.parseInt(value);
           }
           else if ("Lighting_Enabled".equalsIgnoreCase(key)) {
             enabled = Boolean.parseBoolean(value);
           }
           else if ("Lighting_Color".equalsIgnoreCase(key)) {
             color = value;
           }
           else {
             System.out.println("Unknown Key Name!");
           }
         }
       }
       getClient.release();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
 
   }
 
   /** GETTERS **/
 
   /**
    * Get the current aquaponics water or HVAC temperature.
    * 
    * @return temperature
    */
   public long getTemp() {
     return temperature;
   }
 
   /**
    * Get the current water circulation value.
    * 
    * @return circulation
    */
   public double getCirculation() {
     return roundTwoDecimals(circulation);
   }
 
   /**
    * Get the current water turbidity value.
    * 
    * @return turbidity
    */
   public double getTurbidity() {
     return roundTwoDecimals(turbidity);
   }
 
   /**
    * Get the current water PH level.
    * 
    * @return ph
    */
   public double getPh() {
     return roundTwoDecimals(ph);
   }
 
   /**
    * Get the current water level.
    * 
    * @return waterLevel
    */
   public int getWaterLevel() {
     return waterLevel;
   }
 
   /**
    * Get the current water electric conductivity.
    * 
    * @return ec
    */
   public double getElectricalConductivity() {
 
     return roundTwoDecimals(ec);
   }
 
   /**
    * Get water oxygen level.
    * 
    * @return oxygen
    */
   public double getOxygen() {
     return roundTwoDecimals(oxygen);
   }
 
   /**
    * Get the number of dead fish.
    * 
    * @return deadFish
    */
   public int getDeadFish() {
     return deadFish;
   }
 
   /**
    * Get the current power value.
    * 
    * @return power
    */
   public long getPower() {
     return power;
   }
 
   /**
    * Get the current energy value.
    * 
    * @return energy
    */
   public long getEnergy() {
     return energy;
   }
 
   /**
    * Get the lights brightness level.
    * 
    * @return lightingLevel
    */
   public int getLevel() {
     return lightingLevel;
   }
 
   /**
    * Get the status of the lights on/off.
    * 
    * @return enabled
    */
   public boolean getEnabled() {
     return enabled;
   }
 
   /**
    * Get the color setting of the lights.
    * 
    * @return color
    */
   public String getColor() {
     return color;
   }
 
   /** SETTERS **/
 
   /**
    * Set aquaponics' water temperature.
    * 
    * @param value Desired temperature
    */
   public void setAquaponicsTemp(int value) {
 
     try {
       String putUrl =
           HTTP + ipAddress + ":8111/AQUAPONICS/command/SET_TEMPERATURE?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Set aquaponics' PH level.
    * 
    * @param value Desired PH level
    */
   public void setPh(double value) {
 
     try {
       String putUrl = HTTP + ipAddress + ":8111/AQUAPONICS/command/SET_PH?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Set aquaponics' water level.
    * 
    * @param value Desired water level
    */
  public void setWaterLevel(double value) {
 
     try {
       String putUrl =
           HTTP + ipAddress + ":8111/AQUAPONICS/command/SET_WATER_LEVEL?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Set aquaponics' water nutrients.
    * 
    * @param value Desired Nutrients level
    */
   public void setNutrients(double value) {
 
     try {
       String putUrl = HTTP + ipAddress + ":8111/AQUAPONICS/command/SET_NUTRIENTS?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Feed the fish.
    * 
    * @param value Amount of feed
    */
   public void feedFish(double value) {
 
     try {
       String putUrl = HTTP + ipAddress + ":8111/AQUAPONICS/command/FEED_FISH?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Harvest the fish.
    * 
    * @param value Number of fish to harvest
    */
   public void harvestFish(int value) {
 
     try {
       String putUrl = HTTP + ipAddress + ":8111/AQUAPONICS/command/HARVEST_FISH?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Set HVAC to desired temperature.
    * 
    * @param value Desired home temperature
    */
   public void setHvacTemp(int value) {
 
     try {
       String putUrl = HTTP + ipAddress + ":8111/HVAC/command/SET_TEMPERATURE?arg=" + value;
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Set the brightness of lights.
    * 
    * @param value Brightness level
    * @param room Room
    */
   public void setLightingLevel(int value, String room) {
 
     try {
 
       String putUrl =
           HTTP + ipAddress + ":8111/LIGHTING/command/SET_LIGHTING_LEVEL?arg=" + value
               + "&room=" + room.toUpperCase(Locale.US);
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Turn lights on/off.
    * 
    * @param value On or Off
    * @param room Room
    */
   public void setLightingEnabled(boolean value, String room) {
 
     try {
       String putUrl =
           HTTP + ipAddress + ":8111/LIGHTING/command/SET_LIGHTING_ENABLED?arg=" + value
               + "&room=" + room.toUpperCase(Locale.US);
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Change the color of the lights.
    * 
    * @param value The color
    * @param room Room
    */
   public void setLightingColor(String value, String room) {
 
     try {
       String putUrl =
           HTTP + ipAddress + ":8111/LIGHTING/command/SET_LIGHTING_COLOR?arg=" + value
               + "&room=" + room.toUpperCase(Locale.US);
       ClientResource putClient = new ClientResource(putUrl);
       putClient.put(putUrl);
       putClient.release();
     }
     catch (ResourceException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * 
    * @param d The number to convert.
    * 
    * @return double in two decimal places.
    */
   public double roundTwoDecimals(double d) {
     DecimalFormat twoDForm = new DecimalFormat("#.##");
     return Double.valueOf(twoDForm.format(d));
   }
 
 }
