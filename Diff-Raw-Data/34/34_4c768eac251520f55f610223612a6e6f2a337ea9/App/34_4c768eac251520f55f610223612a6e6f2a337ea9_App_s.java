 package org.uci.luci.interchange;
 
 public class App 
 {
     public static void main( String[] args ) throws Exception
     {
         OsmFileReader osmFileReader = new OsmFileReader();
         osmFileReader.parseStructure();
         OpenStreetMap openStreetMap = osmFileReader.osmHandler.openStreetMap;
         
         Global.openStreetMap = openStreetMap;
         Global.openStreetMap.purgeUnconnectedNodes();
         IntersectionRegistry.generateIntersections();
         
         AppWindow appWindow = new AppWindow();
         
         Simulator simulator = new Simulator();
         Global.simulator = simulator;
         simulator.start();
     }
 }
