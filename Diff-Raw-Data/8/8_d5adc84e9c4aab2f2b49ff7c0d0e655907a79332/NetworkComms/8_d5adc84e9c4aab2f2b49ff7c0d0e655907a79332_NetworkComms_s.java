 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import edu.wpi.first.wpilibj.networktables.NetworkTableKeyNotDefined;
 
 /**
  *
  * @author ROBOTics
  */
 public class NetworkComms extends Component {
 
     private Component[] components;
     private int UPDATE_PERIOD = 0;
     private int counter = 0;
     private boolean enabled = false;
     private boolean endGame = false;
     private static NetworkComms instance = null;
     
     public static NetworkComms getInstance(Component[] components){
         if(instance == null){
             instance = new NetworkComms(components);
         }
         return instance;
     }
     
     public static NetworkComms getInstance(){
         return instance;
     }
 
     private NetworkComms(Component[] components) {
         this.components = components;
     }
 
     public void update() {
         if (counter >= UPDATE_PERIOD && enabled) {
             NetworkTable.getTable("status").putBoolean("pidEnabled", Drive.getInstance().isPIDEnabled());
             NetworkTable.getTable("status").putBoolean("transmissionLow", Transmission.getInstance().isTranmissionLow());
             try {
                 endGame = NetworkTable.getTable("status").getBoolean("endgame");
             } catch (NetworkTableKeyNotDefined ex) {
                 endGame = false;
             }
             for (int i = 0; i < components.length; ++i) {
                 try {
                     components[i].setEnabled(NetworkTable.getTable("components").getBoolean(components[i].getRMRName()));
                 } catch (Exception e) {
                     e.printStackTrace();
                     //No entry in the tables, default enabled
                     components[i].setEnabled(true);
                 }
             }
             counter = 0;
         }
         counter++;
     }
     
     public boolean isEndGame(){
         return endGame;
     }
 
     public void reset() {
        NetworkTable.initialize();
     }
 
     public String getRMRName() {
         return "Network Comms";
     }
 }
