 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package eu.cassandra.training.entities;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 
 import org.jfree.chart.ChartPanel;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 import eu.cassandra.training.consumption.PowerConsumptionModel;
 import eu.cassandra.training.consumption.ReactiveConsumptionModel;
 import eu.cassandra.training.consumption.TripletPower;
 import eu.cassandra.training.consumption.TripletReactive;
 import eu.cassandra.training.utils.ChartUtils;
 
 public class Appliance
 {
 
   private String name = "";
   private String installation = "";
   private String type = "Generic";
   private String applianceID = "";
   private String energyClass = "";
   private boolean controllable = false;
   private boolean shiftable = false;
   private double standbyConsumption = 0.0;
   private String powerConsumptionModelString = "";
   private String reactiveConsumptionModelString = "";
   private PowerConsumptionModel powerConsumptionModel =
     new PowerConsumptionModel();
   private ReactiveConsumptionModel reactiveConsumptionModel =
     new ReactiveConsumptionModel();
   private String eventsFile = "";
   private double[] activePower;
   private double[] reactivePower;
 
   public Appliance ()
   {
     activePower = new double[0];
     reactivePower = new double[0];
   }
 
   public Appliance (String name, String installation, String powerModel,
                     String reactiveModel, String eventFile, double[] active)
   {
     this.name = name;
     this.installation = installation;
     this.eventsFile = eventFile;
     powerConsumptionModelString = powerModel;
     reactiveConsumptionModelString = reactiveModel;
     DBObject dbo = (DBObject) JSON.parse(powerConsumptionModelString);
     dbo = (DBObject) JSON.parse(reactiveConsumptionModelString);
     powerConsumptionModel.init(dbo);
 
     activePower = active;
   }
 
   public Appliance (String name, String installation, String powerModel,
                     String reactiveModel, String eventFile, double[] active,
                     double[] reactive)
   {
 
     this.name = name;
     this.installation = installation;
     this.eventsFile = eventFile;
     powerConsumptionModelString = powerModel;
     reactiveConsumptionModelString = reactiveModel;
     DBObject dbo = (DBObject) JSON.parse(powerConsumptionModelString);
     powerConsumptionModel.init(dbo);
     dbo = (DBObject) JSON.parse(reactiveConsumptionModelString);
     reactiveConsumptionModel.init(dbo);
     activePower = active;
     reactivePower = reactive;
 
   }
 
   public Appliance (String name, String powerModelFile,
                     String reactiveModelFile, String eventFile,
                     Installation installation, boolean power)
     throws IOException
   {
 
     this.name = name;
     this.installation = installation.getName();
     parsePowerConsumptionModel(powerModelFile);
     parseReactiveConsumptionModel(reactiveModelFile);
     this.eventsFile = eventFile;
     activePower = installation.getActivePower();
     if (!power)
       reactivePower = installation.getReactivePower();
 
   }
 
   public String getName ()
   {
     return name;
   }
 
   public String getType ()
   {
     return type;
   }
 
   public double getStandbyConsumption ()
   {
     return standbyConsumption;
   }
 
   public boolean getControllable ()
   {
     return controllable;
   }
 
   public boolean getShiftable ()
   {
     return shiftable;
   }
 
   public String getApplianceID ()
   {
     return applianceID;
   }
 
   public String getEnergyClass ()
   {
     return energyClass;
   }
 
   public String getInstallation ()
   {
     return installation;
   }
 
   public String getEventsFile ()
   {
     return eventsFile;
   }
 
   public String getPowerConsumptionModelString ()
   {
     return powerConsumptionModelString;
   }
 
   public String getReactiveConsumptionModelString ()
   {
     return reactiveConsumptionModelString;
   }
 
   public void setApplianceID (String id)
   {
     applianceID = id;
   }
 
   public Double[] getPowerConsumptionModel ()
   {
 
     ArrayList<Double> temp = new ArrayList<Double>();
     int times = powerConsumptionModel.getOuterN();
     if (times == 0)
       times = 2;
     // Number of repeats
     for (int i = 0; i < times; i++) {
       // System.out.println("Time: " + i);
       // Number of patterns in each repeat
       for (int j = 0; j < powerConsumptionModel.getPatternN(); j++) {
         // System.out.println("Pattern: " + j);
         int internalTimes = powerConsumptionModel.getN(j);
         if (internalTimes == 0)
           internalTimes = 2;
         // System.out.println("Internal Times: " + k);
         for (int k = 0; k < internalTimes; k++) {
           ArrayList<TripletPower> tripplets =
             powerConsumptionModel.getPattern(j);
           for (int l = 0; l < tripplets.size(); l++) {
             // System.out.println("TripletPower: " + l);
             for (int m = 0; m < tripplets.get(l).d; m++) {
               temp.add(tripplets.get(l).p);
             }
           }
         }
       }
     }
     Double[] result = new Double[temp.size()];
     temp.toArray(result);
     return result;
 
   }
 
   public Double[] getReactiveConsumptionModel ()
   {
 
     ArrayList<Double> temp = new ArrayList<Double>();
     int times = reactiveConsumptionModel.getOuterN();
     if (times == 0)
       times = 2;
     // Number of repeats
     for (int i = 0; i < times; i++) {
       // System.out.println("Time: " + i);
       // Number of patterns in each repeat
       for (int j = 0; j < reactiveConsumptionModel.getPatternN(); j++) {
         // System.out.println("Pattern: " + j);
         int internalTimes = reactiveConsumptionModel.getN(j);
         if (internalTimes == 0)
           internalTimes = 2;
         // System.out.println("Internal Times: " + k);
         for (int k = 0; k < internalTimes; k++) {
           ArrayList<TripletReactive> tripplets =
             reactiveConsumptionModel.getPattern(j);
           for (int l = 0; l < tripplets.size(); l++) {
             // System.out.println("TripletPower: " + l);
             for (int m = 0; m < tripplets.get(l).d; m++) {
               temp.add(tripplets.get(l).q);
             }
           }
         }
       }
     }
     Double[] result = new Double[temp.size()];
     temp.toArray(result);
     return result;
 
   }
 
   public double[] getActivePower ()
   {
     return activePower;
   }
 
   public double getActivePower (int index)
   {
     return activePower[index];
   }
 
   public double[] getReactivePower ()
   {
     if (reactivePower.length == 0)
       System.out
               .println("No Reactive Power measurements available for this appliance");
 
     return reactivePower;
   }
 
   public double getReactivePower (int index)
   {
     if (reactivePower.length == 0) {
       System.out
               .println("No Reactive Power measurements available for this appliance");
       return 0;
     }
     return reactivePower[index];
   }
 
   public void parsePowerConsumptionModel (String filename) throws IOException
   {
 
     File file = new File(filename);
 
     String model = "";
 
     String extension =
       filename.substring(filename.length() - 3, filename.length());
 
     Scanner scanner = new Scanner(file);
     switch (extension) {
 
     case "son":
 
       while (scanner.hasNext())
         model = model + scanner.nextLine();
       break;
     default:
 
       while (scanner.hasNext())
         model = model + scanner.nextLine();
 
       model.replace(" ", "");
     }
     scanner.close();
 
     powerConsumptionModelString = model;
     DBObject dbo = (DBObject) JSON.parse(powerConsumptionModelString);
     powerConsumptionModel.init(dbo);
   }
 
   public void parseReactiveConsumptionModel (String filename)
     throws IOException
   {
 
     File file = new File(filename);
 
     String model = "";
 
     String extension =
       filename.substring(filename.length() - 3, filename.length());
 
     Scanner scanner = new Scanner(file);
     switch (extension) {
 
     case "son":
 
       while (scanner.hasNext())
         model = model + scanner.nextLine();
       break;
     default:
 
       while (scanner.hasNext())
         model = model + scanner.nextLine();
 
       model.replace(" ", "");
     }
     scanner.close();
 
     reactiveConsumptionModelString = model;
     DBObject dbo = (DBObject) JSON.parse(reactiveConsumptionModelString);
     reactiveConsumptionModel.init(dbo);
   }
 
   public String toString ()
   {
     return name;
   }
 
   public DBObject toJSON (String installationID)
   {
 
     DBObject temp = new BasicDBObject();
 
     temp.put("name", name);
     temp.put("type", type);
     temp.put("description", name + " " + type);
     temp.put("controllable", controllable);
     temp.put("shiftable", shiftable);
     temp.put("energy_class", "Class A");
     temp.put("standy_consumption", standbyConsumption);
     temp.put("inst_id", installationID);
 
     return temp;
 
   }
 
   public DBObject powerConsumptionModelToJSON ()
   {
 
     DBObject temp = new BasicDBObject();
 
     temp.put("name", name + " Consumption Model");
     temp.put("type", type);
     temp.put("description", "P and Q Consumption Model");
     temp.put("app_id", applianceID);
     temp.put("pmodel", JSON.parse(powerConsumptionModelString));
     temp.put("qmodel", JSON.parse(reactiveConsumptionModelString));
     temp.put("pvalues", new double[1]);
     temp.put("qvalues", new double[1]);
     return temp;
 
   }
 
   public ChartPanel consumptionGraph ()
   {
 
     return ChartUtils.createArea(name + " Consumption Model", "Time Step",
                                  "Power", getPowerConsumptionModel(),
                                  getReactiveConsumptionModel());
   }
 
   public void status ()
   {
     System.out.println("Name: " + name);
     System.out.println("Type: " + type);
     System.out.println("Controllable: " + controllable);
     System.out.println("Shiftable: " + shiftable);
     System.out.println("Energy Class: " + energyClass);
     System.out.println("StandBy Consumption: " + standbyConsumption);
     System.out.println("Appliance Of Installation: " + installation);
     System.out.println("Events File: " + eventsFile);
     System.out.println("Power Consumption Model:"
                        + powerConsumptionModel.toString());
     System.out.println("Reactive Power Consumption Model:"
                        + reactiveConsumptionModel.toString());
     System.out.println("Active Power:" + Arrays.toString(activePower));
     System.out.println("Reactive Power:" + Arrays.toString(reactivePower));
   }
 }
