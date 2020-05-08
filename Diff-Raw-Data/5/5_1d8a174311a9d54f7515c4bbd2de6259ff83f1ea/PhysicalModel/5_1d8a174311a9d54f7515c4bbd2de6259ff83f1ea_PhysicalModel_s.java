 package eel.seprphase4.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonIdentityInfo;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.ObjectIdGenerators;
 import eel.seprphase4.GameOverException;
 import eel.seprphase4.Utilities.Energy;
 import eel.seprphase4.Utilities.Mass;
 import eel.seprphase4.Utilities.Percentage;
 import eel.seprphase4.Utilities.Pressure;
 import eel.seprphase4.Utilities.Temperature;
 import static eel.seprphase4.Utilities.Units.*;
 import com.fasterxml.jackson.annotation.JsonAutoDetect;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  *
  * @author Marius
  */
 @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
 @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
 public class PhysicalModel implements PlantController, PlantStatus {
 
     @JsonProperty
     private Reactor reactor = new Reactor();
     @JsonProperty
     private Turbine turbine = new Turbine();
     @JsonProperty
     private Condenser condenser = new Condenser();
     @JsonProperty
     private Energy energyGenerated = joules(0);
     @JsonProperty
     private Connection reactorToTurbine;
     @JsonProperty
     private Connection turbineToCondenser;
     @JsonProperty
     private Pump condenserToReactor;
     @JsonProperty
     private Pump heatsinkToCondenser;
     @JsonProperty
     private String username;
     @JsonProperty
     private HeatSink heatSink;
     
 
     /**
      *
      */
     public PhysicalModel() {
 
         heatSink = new HeatSink();
 
         reactorToTurbine = new Connection(reactor.outputPort(), turbine.inputPort(), 0.05);
         turbineToCondenser = new Connection(turbine.outputPort(), condenser.inputPort(), 0.05);
 
 
         condenserToReactor = new Pump(condenser.outputPort(), reactor.inputPort());
         heatsinkToCondenser = new Pump(heatSink.outputPort(), condenser.coolantInputPort());
 
     }
 
     
     /**
      *
      * @param steps
      */
     @Override
     public void step(int steps) throws GameOverException {
         for (int i = 0; i < steps; i++) {
             reactor.step();
             turbine.step();
             condenser.step();
             energyGenerated = joules(energyGenerated.inJoules() + turbine.outputPower());
             reactorToTurbine.step();
             turbineToCondenser.step();
             condenserToReactor.step();
             heatsinkToCondenser.step();
         }
     }
 
     /**
      *
      * @param percent
      */
     @Override
     public void moveControlRods(Percentage percent) {
         reactor.moveControlRods(percent);
     }
 
     /**
      *
      * @return
      */
     @Override
     public Temperature reactorTemperature() {
         return reactor.temperature();
     }
 
     public Mass reactorMinimumWaterMass() {
         return reactor.minimumWaterMass();
     }
 
     public Mass reactorMaximumWaterMass() {
         return reactor.maximumWaterMass();
     }
 
     @Override
     public Percentage reactorMinimumWaterLevel() {
         return reactor.minimumWaterLevel();
     }
 
     @Override
     public boolean quencherUsed() {
         return reactor.quencherUsed();
     }
 
     @Override
     public void failReactor() {
         reactor.fail();
     }
 
     @Override
     public void failCondenser() {
         condenser.fail();
     }
 
     /**
      *
      * @return
      */
     @Override
     public Energy energyGenerated() {
         return energyGenerated;
     }
 
     /**
      *
      * @return
      */
     @Override
     public Percentage controlRodPosition() {
         return reactor.controlRodPosition();
     }
 
     /**
      *
      * @return
      */
     @Override
     public Pressure reactorPressure() {
         return reactor.pressure();
     }
 
     /**
      *
      * @return
      */
     @Override
     public Percentage reactorWaterLevel() {
         return reactor.waterLevel();
     }
 
 
     
     @Override
     public ArrayList<FailableComponent> components() {
         ArrayList<FailableComponent> c = new ArrayList<FailableComponent>();
         c.add(0, turbine);
         c.add(1, reactor);
         c.add(2, condenser);
         c.add(3, condenserToReactor);
         c.add(4, heatsinkToCondenser);
         return c;
     }
     
     @Override
     public void changeValveState(int valveNumber, boolean isOpen) {
         if (valveNumber == 1) {
             reactorToTurbine.setOpen(isOpen);
             return;
         }
         
         if (valveNumber == 2) {
             turbineToCondenser.setOpen(isOpen);
             return;
         }
         
         throw new IllegalArgumentException("Valve number out of range");
     }
 
     @Override
     public boolean valveIsOn(int valveNumber) {
         if (valveNumber == 1) {
             return reactorToTurbine.getOpen();
         }
         
         if (valveNumber == 2) {
             return turbineToCondenser.getOpen();
         }
         
         throw new IllegalArgumentException("Valve number out of range");
     }
 
     @Override
     public void changePumpState(int pumpNumber, boolean isPumping) {
         
         if (pumpNumber == 1) {
             condenserToReactor.setOnState(isPumping);
             return;
         }
         
         if (pumpNumber == 2) {
             heatsinkToCondenser.setOnState(isPumping);
             return;
         }
         
         throw new IllegalArgumentException("Pump number out of range");
     }
 
     @Override
     public void quenchReactor() {
         reactor.quench();
     }
 
     @Override
     public void repairPump(int pumpNumber) {
         if (pumpNumber == 1) {
            condenserToReactor.fail();
             return;
         }
         
         if (pumpNumber == 2) {
            heatsinkToCondenser.fail();
             return;
         }
         
         throw new IllegalArgumentException("Pump number out of range");
     }
 
     @Override
     public void repairCondenser() {
         condenser.repair();
     }
 
     @Override
     public void repairTurbine() {
         turbine.repair();
     }
 
     @Override
     public Temperature condenserTemperature() {
         return condenser.getTemperature();
     }
 
     @Override
     public Pressure condenserPressure() {
         return condenser.getPressure();
     }
 
     @Override
     public Percentage condenserWaterLevel() {
         return condenser.getWaterLevel();
     }
 
     @Override
     public boolean condenserHasFailed() {
         return condenser.hasFailed();
     }
     
     @Override
     public boolean turbineHasFailed() {
         return turbine.hasFailed();
     }
 
 
     @Override
     public void failSoftware() {
         throw new UnsupportedOperationException("Not supported from this class");
     }
 
     @Override
     public void failTurbine() {
         turbine.fail();
     }
 
     @Override
     public void failPump(int pump) {
         if (pump == 1) {
             condenserToReactor.fail();
             return;
         }
         
         if (pump == 2) {
             heatsinkToCondenser.fail();
             return;
         }
         
         throw new IllegalArgumentException("Pump number out of range");
 
     }
 
     @Override
     public void allowRandomFailures(boolean yes) {
         throw new UnsupportedOperationException("Not supported by this object.");
     }
 
     @Override
     public boolean allowsRandomFailures() {
         throw new UnsupportedOperationException("Not supported by this object.");
     }
 
     @Override
     public boolean pumpIsOn(int pumpNumber) {
         if (pumpNumber == 1) {
             return condenserToReactor.isOn();
         }
         if (pumpNumber == 2) {
             return heatsinkToCondenser.isOn();
         }
         
         throw new IllegalArgumentException("Pump number out of range");
     }
 
     @Override
     public boolean pumpHasFailed(int pumpNumber) {
         if (pumpNumber == 1) {
             return condenserToReactor.hasFailed();
         }
         if (pumpNumber == 2) {
             return heatsinkToCondenser.hasFailed();
         }
         
         throw new IllegalArgumentException("Pump number out of range");
     }
 }
