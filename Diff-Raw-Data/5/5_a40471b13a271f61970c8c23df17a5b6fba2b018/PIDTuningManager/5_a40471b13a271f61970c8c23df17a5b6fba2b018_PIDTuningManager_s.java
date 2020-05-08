 package com.edinarobotics.utils.pid;
 
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import java.util.Vector;
 
 /**
  * Allows tuning of PID systems through the dashboard using a
  * {@link NetworkTable}.
  * This class handles exchanging data with the dashboard and passing that data
  * to the relevant PIDConfig instance. This class <em>does not</em> save
  * tuning data, it will be lost as soon as the robot is power-cycled.
  */
 public class PIDTuningManager {
     private static PIDTuningManager instance;
     private NetworkTable pidTable;
     private Vector pidConfigNames;
     private Vector pidConfigInstances;
     
     private PIDTuningManager(){
         pidTable = NetworkTable.getTable("pid");
         pidConfigNames = new Vector();
         pidConfigInstances = new Vector();
     }
     
     /**
      * Returns the instance of the PIDTuningManager singleton.
      * @return The PIDTuningManager instance.
      */
     public static PIDTuningManager getInstance(){
         if(instance == null){
             instance = new PIDTuningManager();
         }
         return instance;
     }
     
     /**
      * Returns the PIDConfig with the given name. If no PIDConfig with
      * {@code name} exists, a new PIDConfig is created. {@code name} <em>must not</em>
      * contain a comma (",") as this will cause bugs with the dashboard.
      * @param name The name of the requested PIDConfig.
      * @return The PIDConfig instance with the requested {@code name}.
      */
     public PIDConfig getPIDConfig(String name){
         int index = pidConfigNames.indexOf(name);
         if(index >= 0){
             return (PIDConfig) pidConfigInstances.elementAt(index);
         }
         pidConfigNames.addElement(name);
         PIDConfig newConfig = new PIDConfig(name);
         pidConfigInstances.addElement(newConfig);
         return newConfig;
     }
     
     /**
      * Returns a comma-separated String of the names of all existing
      * PIDConfig instances. This is used internally by PIDTuningManager.
      * @return A comma-separated String of the names of all existing PIDConfig
      * instances.
      */
     private String getConfigNames(){
         String names = "";
        for(int i = 0; i < pidConfigNames.size(); i++){
             names += (","+pidConfigNames.elementAt(i));
         }
         return names;
     }
     
     /**
      * Runs a single iteration of the PID tuning loop. This method should be
      * called periodically during the PID tuning process (for example
      * in the testPeriodic() method of IterativeRobot).
      * This method will handle the NetworkTable data exchange with the dashboard.
      */
     public void runTuning(){
         if(pidTable.getBoolean("tunepid", false)){
             //PID Tuning enabled
             pidTable.putString("subsystems", getConfigNames());
             PIDConfig pidSystem = getPIDConfig(pidTable.getString("system", "default"));
             pidSystem.setPID(pidTable.getNumber("p", 0), pidTable.getNumber("i", 0),
                     pidTable.getNumber("d", 0));
             pidTable.putNumber("value", pidSystem.getValue());
             pidTable.putNumber("setpoint", pidSystem.getSetpoint());
         }
     }
     
     /**
      * Resets all PIDConfig objects to default values. This will undo
      * any tuning performed by the dashboard.
      */
     public void resetAll(){
         for(int i = 0; i < pidConfigInstances.size(); i++){
             ((PIDConfig)pidConfigInstances.elementAt(i)).reset();
         }
     }
 }
