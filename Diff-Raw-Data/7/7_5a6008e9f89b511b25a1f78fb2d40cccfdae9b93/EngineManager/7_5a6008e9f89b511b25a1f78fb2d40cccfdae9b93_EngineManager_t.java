 package de.tubs.cs.ibr.finn.engine;
 
 import eu.funinnumbers.station.rmi.StationInterface;
 
 /**
  * Created by IntelliJ IDEA.
  * User: akribopo
  * Date: 9/8/11
  * Time: 5:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public final class EngineManager {
 
     /**
      * Single instance of manager.
      */
     private static EngineManager thisInstance;
 
 
     private AbstractTestbedEngineApp thisEngine = null;
 
 
     private EngineManager() {
     }
 
     /**
      * Provides access to the single instance of the manager.
      *
      * @return reference to the unique instance of the EngineManager.
      */
     public static EngineManager getInstance() {
         synchronized (EngineManager.class) {
             if (thisInstance == null) {
                 thisInstance = new EngineManager();
             }
         }
 
         return thisInstance;
     }
 
     public void setEngine(final AbstractTestbedEngineApp halEngineApp) {
         thisEngine = halEngineApp;
     }
 
     public AbstractTestbedEngineApp getEngine() {
         return thisEngine;
     }
 
     /**
      * TODO: Find the correct IF.
      */
    public StationInterface getStationInterface() {
         for (StationInterface stationInterface : thisEngine.getStationInterface()) {
            return (StationInterface) stationInterface;
         }
         return null;
     }
 
 
 }
 
 
