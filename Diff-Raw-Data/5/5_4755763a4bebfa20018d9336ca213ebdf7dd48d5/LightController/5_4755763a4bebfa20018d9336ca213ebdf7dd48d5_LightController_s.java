 package eu.uberdust.lights;
 
 import eu.uberdust.communication.UberdustClient;
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import eu.uberdust.lights.tasks.LightTask;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Timer;
 
 /**
  * Created by IntelliJ IDEA.
  * User: akribopo
  * Date: 11/14/11
  * Time: 2:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public final class LightController implements Observer {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(LightController.class);
 
     private boolean zone1;
 
     private boolean zone2;
 
     private long lastReading;
 
     private long zone1TurnedOnTimestamp;
 
     private long zone2TurnedOnTimestamp;
 
     /**
      * Pir timer.
      */
     private final Timer timer;
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static LightController ourInstance = null;
 
 
     /**
      * LightController is loaded on the first execution of LightController.getInstance()
      * or the first access to LightController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static LightController getInstance() {
         synchronized (LightController.class) {
             if (ourInstance == null) {
                 ourInstance = new LightController();
             }
         }
         return ourInstance;
     }
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private LightController() {
         PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
         LOGGER.info("Light Controller initialized");
         zone1 = false;
         zone2 = false;
         timer = new Timer();
         WSReadingsClient.getInstance().setServerUrl("ws://uberdust.cti.gr:80/readings.ws");
         // WSReadingsClient.getInstance().subscribe("urn:wisebed:ctitestbed:0x1ccd", "urn:wisebed:node:capability:pir");
         WSReadingsClient.getInstance().subscribe("urn:wisebed:ctitestbed:virtual:room:0.I.11", "urn:wisebed:node:capability:pir");
         WSReadingsClient.getInstance().addObserver(this);
     }
 
     public long getLastReading() {
         return lastReading;
     }
 
     public void setLastReading(final long thatReading) {
         this.lastReading = thatReading;
         if (!zone1) {
             controlLight(true, 2);
             controlLight(true, 1);
             zone1TurnedOnTimestamp = thatReading;
             timer.schedule(new LightTask(timer), LightTask.DELAY);
         } else if (!zone2) {
             controlLight(true, 2);
             controlLight(true, 1);
             if (thatReading - zone1TurnedOnTimestamp > 15000) {
                 controlLight(true, 3);
                 zone2TurnedOnTimestamp = thatReading;
             }
         } else {
             controlLight(true, 3);
         }
     }
 
 
     public void controlLight(final boolean value, final int zone) {
        if (zone == 3) {
             zone1 = value;
        } else if (zone == 1) {
             zone2 = value;
         }
 
         UberdustClient.getInstance().sendCoapPost("494", "lz" + zone, value ? "1" : "0");
 
     }
 
     public boolean isZone1() {
         return zone1;
     }
 
     public boolean isZone2() {
         return zone2;
     }
 
     public static void main(final String[] args) {
         LightController.getInstance();
     }
 
     @Override
     public void update(Observable o, Object arg) {
         System.out.println("update");
         if (!(o instanceof WSReadingsClient)) {
             return;
         }
         if (!(arg instanceof Message.NodeReadings)) {
             return;
         }
 
         Message.NodeReadings readings = (Message.NodeReadings) arg;
         for (Message.NodeReadings.Reading reading : readings.getReadingList()) {
             if (reading.hasDoubleReading() && reading.getDoubleReading() > 0) {
                 LightController.getInstance().setLastReading(reading.getTimestamp());
             }
         }
     }
 }
 
