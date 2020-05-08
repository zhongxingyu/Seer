package edu.hawaii.ihale.backend.restserver;
 
 import org.restlet.resource.Get;
 import org.restlet.resource.Put;
 import org.restlet.resource.ServerResource;
 import edu.hawaii.ihale.api.SystemStateEntry;
 import edu.hawaii.ihale.backend.restserver.IHaleDAO;
 
 /**
  * A server resource that will handle requests for the aquaponics system.
  * Supported operations: GET and PUT.
  * 
  * @author Leonardo Nguyen, David Lin, Nathan Dorman
  * @version Java 1.6.0_21
  */
 public class AquaponicsResource extends ServerResource {
   
   /**
    * Returns the current state of the aquaponics system. 
    * @return Current state of the aquaponics system. 
    */
   @Get
   public String getAquaponicsState() {
     
     /** Direction 1: Unsure if when a GET method is sent from front-end to the REST server,
      *               if the REST server is to send a request to the arduino device for state
      *               information or if front-end request wants the information from the back
      *               end DB. Why not just have the front-end query the device directly and
      *               have a XML send back if so?
      */
     // The aquaponics arduino for state information URL
     //String arduinoURL = "http://arduino-1.halepilihonua.hawaii.edu/aquaponics/state";
     //ClientResource client = new ClientResource(Method.GET, arduinoURL);
 
     
     /** Direction 2: Just assume all GET methods is requesting information stored on the DB
      * 
      */
     
     IHaleDAO db = new IHaleDAO();
     /**
     try {
       // Retrieve a list of all aquaponics entries and find the most current entry stored 
       // in the DB.
       List<SystemStateEntry> list = db.getEntries("Aquaponics", "Arduino-1", 0, 
           new Date().getTime());
       
       SystemStateEntry recentEntry;
       long timestamp = 0;
       for (SystemStateEntry entry : list) {
         if (entry.getTimestamp() > timestamp) {
           recentEntry = entry;
           timestamp = entry.getTimestamp();
         }
       }
     */  
       /** TO-DO: Return the entry in XML format to the front-end */
     /*
     }
     catch (SystemStateEntryDBException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     */
     
     SystemStateEntry testEntry = db.getEntry("T", "E", 222222);
     return "This is the aquaponics resource!\n " + testEntry.getSystemName();
   }
   
   /**
    * Commands an aquaponics device to set the temperature to a new value.
    * @param temperature The temperature.
    */
   @Put
   public void setTemp(Long temperature) {
     // Empty method
   }
 }
