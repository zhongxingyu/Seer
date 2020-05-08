 package org.pillarone.riskanalytics.core.output;
 
 import org.joda.time.DateTime;
 
 /**
  * Data class which transports a single result from a collecting mode strategy to an output strategy.
  * This class is a 'copy' of SingleValueResult, but it performs better because it is not a domain class.
  *
  * Using a domain class instead would generate hibernate overhead.
  */
 public class SingleValueResultPOJO {
 
     private SimulationRun simulationRun;
     private int period;
     private int iteration;
     private PathMapping path;
     private CollectorMapping collector;
     private FieldMapping field;
     private int valueIndex;
     private double value;
     private DateTime date;
 
     @Override
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("Period: ");
         buffer.append(period);
         buffer.append("> ");
         buffer.append(value);
         buffer.append(" [");
         buffer.append(path.toString());
         buffer.append(":");
         buffer.append(field.toString());
         buffer.append("]");
         return buffer.toString();
     }
 
     public CollectorMapping getCollector() {
         return collector;
     }
 
     public void setCollector(CollectorMapping collector) {
         this.collector = collector;
     }
 
     public FieldMapping getField() {
         return field;
     }
 
     public void setField(FieldMapping field) {
         this.field = field;
     }
 
     public PathMapping getPath() {
         return path;
     }
 
     public void setPath(PathMapping path) {
         this.path = path;
     }
 
     public int getIteration() {
         return iteration;
     }
 
     public void setIteration(int iteration) {
         this.iteration = iteration;
     }
 
     public int getPeriod() {
         return period;
     }
 
     public void setPeriod(int period) {
         this.period = period;
     }
 
     public SimulationRun getSimulationRun() {
         return simulationRun;
     }
 
     public void setSimulationRun(SimulationRun simulationRun) {
         this.simulationRun = simulationRun;
     }
 
     public double getValue() {
         return value;
     }
 
     public void setValue(double value) {
         this.value = value;
     }
 
     public int getValueIndex() {
         return valueIndex;
     }
 
     public void setValueIndex(int valueIndex) {
         this.valueIndex = valueIndex;
     }
 
     public DateTime getDate() {
         return date;
     }
 
     public void setDate(DateTime date) {
         this.date = date;
     }
 }
