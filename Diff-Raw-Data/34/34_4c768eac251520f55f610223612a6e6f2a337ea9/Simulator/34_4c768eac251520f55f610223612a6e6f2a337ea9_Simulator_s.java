 package org.uci.luci.interchange;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.swing.Timer;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.text.DecimalFormat;
 
 public class Simulator extends Thread {
   private boolean debug = false;
   private boolean paused;
 	ArrayList<String> spawnPoints = new ArrayList<String>();
   int lastSimulatorStepTotalVehicles;
   long lastSimulatorStepTotalTime;
   int delay;
   
   public Simulator() throws InterruptedException {
     delay = 10;
     lastSimulatorStepTotalVehicles = 0;
     lastSimulatorStepTotalTime = 0;
     paused = false;
     
     ActionListener taskPerformer = new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         System.out.println("Simulator performance");
         
         System.out.println("\tFree memory: " + humanReadableByteCount(Runtime.getRuntime().freeMemory(), false));
         System.out.println("\tMaximum memory: " + (Runtime.getRuntime().maxMemory() == Long.MAX_VALUE ? "no limit" : humanReadableByteCount(Runtime.getRuntime().maxMemory(), false)));
         System.out.println("\t" + lastSimulatorStepTotalVehicles + " vehicles in simulator.");
         System.out.println("\t" + lastSimulatorStepTotalTime + " ns per simulator step.");
         
         
         double nsPerVehicle = 0;
         if (lastSimulatorStepTotalVehicles != 0)
           nsPerVehicle = (lastSimulatorStepTotalTime/lastSimulatorStepTotalVehicles);
         double vps = 1.0/(nsPerVehicle/1000000000);
         
         DecimalFormat df = new DecimalFormat();
         // DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         // dfs.setGroupingSeparator('.');
         // df.setDecimalFormatSymbols(dfs);
         // log(df.format((int)num));
         System.out.println("\taround " + df.format(vps) + " vehicle ticks per sec.");
       }
     };
     new Timer(2000, taskPerformer).start();
   }
   
   public void run() {
     try {
       int tick = 0;
     
       while (true) {
         if (paused) {
           Thread.sleep(500);
           continue;
         }
       
         tick++;
         
         long startTime = System.nanoTime();
         long endTime;
         
         log("// simulator tick begin");
       
         if (tick%2 == 1) {
           log("\t// generating vehicle");
           // Vehicle v = VehicleFactory.createVehicleAtNode(Global.openStreetMap.getNode("1575123787"));
           Vehicle v = VehicleFactory.createVehicleAtRandomPoint();
           VehicleDriver d = VehicleDriverFactory.createVehicleDriver(v);
           try {
             // d.setDestinationAndGo("122838448");
             d.pickRandomDestinationAndGo();
           }
           catch (NoPathToDestinationException e) {
             VehicleDriverFactory.destroyVehicleDriver(d);
             VehicleFactory.destroyVehicle(v);
             log("removing vehicle " + v.vin);
           }
         
           // Vehicle v = VehicleFactory.createVehicleAtNode(Global.openStreetMap.getNode("122633613"));
           // // Vehicle v = VehicleFactory.createVehicleAtRandomPoint();
           // VehicleDriver d = VehicleDriverFactory.createVehicleDriver(v);
           // d.pickRandomDestinationAndGo();
         }
 
       
         log("\t// drivers.tick()");
         for (VehicleDriver d : VehicleDriverRegistry.allLicensedDrivers()) {
           if (d.vehicle.paused()) continue;
           
           try {
             d.tick(tick);
           }
           catch (Exception e) {
             log("\tCrash for v = " + d.vehicle.vin + " license = " + d.licence);
             for (VehicleDriver dd : VehicleDriverRegistry.allLicensedDrivers()) {
               log("\t\ttick v = " + dd.vehicle.vin + " license = " + dd.licence);
             }
             e.printStackTrace();
             System.exit(1);
           }
         }
         
         log("\t// moving vehicles");
         for (Vehicle v : VehicleRegistry.allRegisteredVehicles()) {
           // each vehicle's velocity vector has been determined by now
           // we simply calculate exactly where the vehicle should be for
           // the next timestep
         
           if (v.velocity == null || v.paused()) {
             continue;
           }
         
           Node lastNode = v.getOriginNode();
           Node nextNode = v.getDestinationNode();
         
           double newLat = v.lat + v.velocity.x;
           double newLon = v.lon + v.velocity.y;
         
           v.lat = (double)newLat;
           v.lon = (double)newLon;
         }
       
         log("\t// intersections.tick()");
         for (Intersection i : IntersectionRegistry.allRegisteredIntersections()) {
           i.tick(tick);
         }
         
         // log("\t// collision test");
         // List<Vehicle> collisions = VehicleCollisionChecker.checkCollisions(VehicleRegistry.allRegisteredVehicles());
         // for (Vehicle v : collisions)
         //   v.pause();
         
         log("\t// removing flagged vehicles");
         for (VehicleDriver d : VehicleDriverRegistry.allLicensedDrivers()) {
           if (d.vehicle.flagForRemoval) {
             log("removing vehicle " + d.vehicle.vin);
             Vehicle vv = d.vehicle;
             
             VehicleDriverFactory.destroyVehicleDriver(d);
             VehicleFactory.destroyVehicle(d.vehicle);
             
             if (VehicleRegistry.allRegisteredVehicles().contains(vv)) {
               log("clearly this doesn't work");
             }
             
             for (VehicleDriver dx : VehicleDriverRegistry.allLicensedDrivers()) {
             // if (VehicleDriverRegistry.allLicensedDrivers().contains(d)) {
               if (d.licence == dx.licence)
                 log("clearly this doesn't work (d)");
             }
             
             log("removed vehicle " + vv.vin);
           }
         }
         
         endTime = System.nanoTime();
         long duration = endTime - startTime;
         lastSimulatorStepTotalVehicles = VehicleRegistry.allRegisteredVehicles().size();
         lastSimulatorStepTotalTime = duration;
       
         if (delay >= 1)
           Thread.sleep(delay);
       }
     }
     catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
   
   public static String humanReadableByteCount(long bytes, boolean si) {
       int unit = si ? 1000 : 1024;
       if (bytes < unit) return bytes + " B";
       int exp = (int) (Math.log(bytes) / Math.log(unit));
       String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
       return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
   }
   
   public void pause() {
     paused = true;
   }
   
   public void unpause() {
     paused = false;
   }
   
   public void changeSpeed(int delta) {
     if (delay+delta < 0)
       delay = 0;
     else
       delay += delta;
   }
   
   private void log(String str) {
     if (!debug)
       return;
     System.out.println(str);
   }
 }
