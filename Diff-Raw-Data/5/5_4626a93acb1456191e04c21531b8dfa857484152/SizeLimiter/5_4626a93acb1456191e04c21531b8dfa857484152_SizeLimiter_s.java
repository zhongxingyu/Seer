 package uk.co.acuminous.julez.scenario.source;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import uk.co.acuminous.julez.scenario.Scenario;
 
 public class SizeLimiter implements ScenarioSource {
 
     private final ScenarioSource scenarios;
     private final AtomicInteger counter;
 
     public SizeLimiter(ScenarioSource scenarios, int sizeLimit) {
         this.scenarios = scenarios;        
         this.counter = new AtomicInteger(sizeLimit);
     }
 
     @Override
     public Scenario next() {
         Scenario scenario = null;
         if (counter.getAndDecrement() > 0) {
             return scenario = scenarios.next();
         }
         return scenario;
     }
 
     @Override
     public int available() {
        int limittedAvailability = Math.max(0, counter.get());
         int underlyingAvailability = scenarios.available();
        return Math.min(limittedAvailability, underlyingAvailability);
     }
 
 }
