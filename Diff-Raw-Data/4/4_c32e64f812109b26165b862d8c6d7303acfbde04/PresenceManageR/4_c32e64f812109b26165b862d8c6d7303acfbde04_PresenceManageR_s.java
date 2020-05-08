 package eu.uberdust.application.foi.manager;
 
 
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 
 
 public class PresenceManageR extends Observable implements Observer {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(PresenceManageR.class);
     /**
      * Contains all States for all known sensors.
      */
     private Map<String, Long> timeStamps;
     /**
      * Our Instance.
      */
     private static PresenceManageR instance = null;
     /**
      * Delay for transition between States.
      */
     private long pirDelay;
     /**
      * EMPTY State Indicator.
      */
     public static final int EMPTY = 1;
     /**
      * NEW_ENTRY State Indicator.
      */
     public static final int NEW_ENTRY = 2;
     /**
      * OCCUPIED State Indicator.
      */
     public static final int OCCUPIED = 3;
     /**
      * LEFT State Indicator.
      */
     public static final int LEFT = 4;
     /**
      * Current State of the Presence Monitor.
      */
     private int currentState;
     /**
      * Timestamp of the first pir Event.
      */
     private long firstTimestamp;
 
     /**
      * Timestamp of Latest pir Event.
      */
     private long LatestPirTimestamp;
 
     /**
      * Timer used for monitoring absence
      */
     private final Timer timer;
 
     /**
      * Returns the current state of the operation.
      *
      * @return {EMPTY , NEW_ENTRY ,OCCUPIED ,LEFT}
      */
     public int getCurrentState() {
         return currentState;
     }
 
     /**
      * Returns the timestamp of latest pir event.
      *
      * @return timestamp of latest pir event
      */
     public long getLastPirReading(){
         return LatestPirTimestamp;
     }
 
     /**
      * Default Constructor.
      */
     public PresenceManageR() {
         reset();
         timer = new Timer();
         LOGGER.info("PirDelay:" + pirDelay);
     }
 
     /**
      * Singleton Get Instance.
      *
      * @return the single instance.
      */
     public static PresenceManageR getInstance() {
         synchronized (PresenceManageR.class) {
             if (instance == null) {
                 instance = new PresenceManageR();
             }
             return instance;
         }
     }
 
     /**
      * Adds a new reading from Uberdust.
      *
      * @param reading the new reading.
      */
     public void addReading(Message.NodeReadings.Reading reading) {
         //interested only in pir events
         if ("urn:wisebed:node:capability:pir".equals(reading.getCapability())) {
             //interested only in Presence not absence
             if (reading.getDoubleReading() > 0) {
                 //update timestamps hashMap
                 timeStamps.put(reading.getNode(), reading.getTimestamp());
                 //LatestPirTimestamp = Collections.min(timeStamps.values());
                 LatestPirTimestamp = reading.getTimestamp();
                 PresenceHandler();
                 //getPirDelay();
             }
         }
     }
 
     /**
      * Converts int state value to string.
      *
      * @param state the new state.
      */
     public String stateToString(int state){
 
         String s = "";
 
         switch (state){
 
             case 1:
                 s = "EMPTY";
                 break;
 
             case 2:
                 s = "NEW_ENTRY";
                 break;
 
             case 3:
                 s = "OCCUPIED";
                 break;
 
             case 4:
                 s = "LEFT";
                 break;
         }
 
         return s;
 
     }
 
     /**
      * Sets the new state and notifies Observers fro state change.
      *
      * @param newState the new state.
      */
     public void setCurrentState(int newState) {
 
         LOGGER.info("Setting next state: "+stateToString(currentState)+" -------->> "+stateToString(newState));
 
         currentState = newState;
 
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Monitors Presence and updates currentSate accordingly
      *
      *
      */
     public synchronized void PresenceHandler() {
 
         if (!ZoneManageR.getInstance().getFirstStatus()) {
 
                setCurrentState(NEW_ENTRY);
                firstTimestamp = LatestPirTimestamp;
 
                timer.schedule(new AbsenceHandler(timer), pirDelay);
 
            } else if (!ZoneManageR.getInstance().getLastStatus()) {

                ZoneManageR.getInstance().switchOnFirst();
 
                 if ( isLongPresence() ) {
 
                     setCurrentState(OCCUPIED);
 
                 }
             }
     }
 
     /**
      * Inner class responsible for updating the currentState
      * according to time of absence and light level status
      *
      */
 
     public class AbsenceHandler extends TimerTask {
 
         private final Timer timer;
 
         public AbsenceHandler(final Timer thatTimer) {
             super();
             this.timer = thatTimer;
         }
 
         @Override
         public final void run() {
 
             LOGGER.info("AbsenceHandler initialized");
 
             if (ZoneManageR.getInstance().getLastStatus()) {
 
                 if (isLongAbsence(20)) {
                     //turn off zone 2
                     LOGGER.info("Turn off last light level");
 
                     setCurrentState(LEFT);
 
                     //Re-schedule this timer to run in 30000ms to turn off
                     this.timer.schedule(new AbsenceHandler(timer), pirDelay);
                 } else {
                     //Re-schedule this timer to run in 5000ms to turn off
                     this.timer.schedule(new AbsenceHandler(timer), 5000);
                 }
             } else if (ZoneManageR.getInstance().getFirstStatus()) {
 
                 if (isLongAbsence()) {
 
                     LOGGER.info("Turn off all lights");
 
                     setCurrentState(EMPTY);
 
                 } else {
                     //Re-schedule this timer to run in 5000ms to turn off
                     LOGGER.info("Timer reschedule for turning last light level off");
                     this.timer.schedule(new AbsenceHandler(timer), 5000);
                 }
             }
         }
     }
 
 
 
     /**
      * Checks for extended presence.
      *
      * @return true/false
      */
     private boolean isLongPresence() {
 
         for (String host : timeStamps.keySet()) {
 
             LOGGER.info(host + "@" + timeStamps.get(host));
             LOGGER.info("Check for Long Presence @"+host+" : "+(timeStamps.get(host) - firstTimestamp)+" > "+pirDelay / 2);
 
             if ((timeStamps.get(host) - firstTimestamp) > pirDelay / 2) { // the first (that is the older one) hashMap timestamp
                 return true;                                              // that evaluates the preceding expression to true, signifies long presence.
             }
         }
         return false;
     }
 
 
 
     /**
      * Checks for extended absence
      *
      * @return true/false
      */
     private boolean isLongAbsence() {
 
         for (String host : timeStamps.keySet()) {
 
             LOGGER.info(host + "@" + timeStamps.get(host));
         }
 
         LOGGER.info("Check for Long Absence : "+(System.currentTimeMillis() - LatestPirTimestamp)+" > "+pirDelay);
 
        return (System.currentTimeMillis() - LatestPirTimestamp > pirDelay);  // in this case we need the most recent (hashMap) timestamp
                                                                             // that evaluates the preceding expression to true to signify
                                                                            // long absence.
     }
 
     /**
      * Checks for extended absence with custom time interval
      *
      * @return true/false
      */
     private boolean isLongAbsence(int addDelay) {
 
         for (String host : timeStamps.keySet()) {
 
             LOGGER.info(host + "@" + timeStamps.get(host));
         }
 
         LOGGER.info("Check for Long Absence : "+(System.currentTimeMillis() - LatestPirTimestamp)+" > "+(pirDelay + addDelay*1000));
 
         return (System.currentTimeMillis() - LatestPirTimestamp > (pirDelay + addDelay*1000));  // in this case we need the most recent (hashMap) timestamp
                                                                                                 // that evaluates the preceding expression to true to signify
                                                                                                 // long absence.
     }
 
 
     /**
      * Update and Get the Pir Delay used.
      *
      * @return the pirDelay in milliseconds.
      */
     public Long getPirDelay() {
         try {
             pirDelay = Long.parseLong(ProfileManager.getInstance().getElement("pir_delay")) * 1000;
         } catch (NumberFormatException nfe) {
             pirDelay = 1000;
         }
         return pirDelay;
     }
 
     @Override
     public void update(Observable o, Object arg) {
         if (!(o instanceof WSReadingsClient)) {
             return;
         }
 
         if (!(arg instanceof Message.NodeReadings)) {
             return;
         }
 
         final Message.NodeReadings readings = (Message.NodeReadings) arg;
         for (final Message.NodeReadings.Reading reading : readings.getReadingList()) {
             addReading(reading);
         }
     }
 
 
     /**
      * Reset the internal state.
      */
     public void reset() {
         this.timeStamps = new HashMap<String, Long>();
         setCurrentState(EMPTY);
         firstTimestamp = System.currentTimeMillis();
         getPirDelay();
     }
 }
