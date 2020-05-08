 package edu.hawaii.systemh.housemodel.lighting;
 
 import static org.junit.Assert.assertEquals;
 import java.util.List;
 import org.junit.Test;
 import edu.hawaii.systemh.api.repository.TimestampDoublePair;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionModel;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionDevice;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionSystem;
 
 /**
  * Tests the HTTP operations of the system.
  * 
  * @author Kurt Teichman
  */
 public class TestLightingSystem {
   
   /**
   * Tests that the values put into the Lighting Consumption Model are working
    * as intended.
    */
   
   @Test
   public void testLightingValues() throws Exception {
     
     EnergyConsumptionModel energyConsumptionModel = new EnergyConsumptionModel();
     java.util.Date todayDate = new java.util.Date();
     
     long startTime = todayDate.getTime();
     long endTime = startTime + 60*60*24*7; // 1 week from our startTime
 
     List<TimestampDoublePair> lightingSystemData =
       energyConsumptionModel.getSystemLoadDuringInterval(
           EnergyConsumptionSystem.LIGHTING, startTime, endTime);
     
     List<TimestampDoublePair> lightingValues = 
       energyConsumptionModel.getDeviceLoadDuringInterval(
           EnergyConsumptionDevice.LIGHTING, startTime, endTime);
     
     double systemTotal = 0, lightingTotal = 0;
 
     for (int i = 0; i < lightingSystemData.size(); i++) {
       systemTotal += lightingSystemData.get(i).getValue();
       lightingTotal += lightingValues.get(i).getValue();
     }
     
     assertEquals(systemTotal, lightingTotal, 0.0);
   }
 }
