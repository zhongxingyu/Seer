 package edu.hawaii.systemh.housemodel.hvac;
 
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
 public class TestHvacSystem {
   
   /**
   * Tests that the values put into the Hvac Consumption Model are working
    * as intended.
    */
   
   @Test
   public void testHvacValues() throws Exception {
     
     EnergyConsumptionModel energyConsumptionModel = new EnergyConsumptionModel();
     java.util.Date todayDate = new java.util.Date();
     
     long startTime = todayDate.getTime();
     long endTime = startTime + 60*60*24*7; // 1 week from our startTime
 
     List<TimestampDoublePair> hvacSystemData =
       energyConsumptionModel.getSystemLoadDuringInterval(
           EnergyConsumptionSystem.HVAC, startTime, endTime);
     
     List<TimestampDoublePair> humidifierValues = 
       energyConsumptionModel.getDeviceLoadDuringInterval(
           EnergyConsumptionDevice.HUMIDIFIER, startTime, endTime);
     
     List<TimestampDoublePair> heatingandcoolingValues =
       energyConsumptionModel.getDeviceLoadDuringInterval(
           EnergyConsumptionDevice.HEATING_COOLING, startTime, endTime);
     
     double systemTotal = 0, humidifierTotal = 0, heatingandcoolingTotal = 0;
 
     for (int i = 0; i < hvacSystemData.size(); i++) {
       systemTotal += hvacSystemData.get(i).getValue();
       humidifierTotal += humidifierValues.get(i).getValue();
       heatingandcoolingTotal += heatingandcoolingValues.get(i).getValue();
     }
     
     assertEquals(systemTotal, (humidifierTotal + heatingandcoolingTotal), 0.0);
   }
 }
