 package de.tuberlin.dima.ml.pact.logreg.batchgd;
 
 import org.apache.mahout.math.Vector;
 import org.apache.mahout.math.function.Functions;
 
 import de.tuberlin.dima.ml.pact.types.PactVector;
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.common.stubs.CrossStub;
 import eu.stratosphere.pact.common.type.PactRecord;
 
 public class ApplyGradient extends CrossStub {
   
   public static final String CONF_KEY_LEARNING_RATE = "parameter.LEARNING_RATE";
   
   private double learningRate = 0;
   
   @Override
   public void open(Configuration parameters) throws Exception {
     super.open(parameters);
     this.learningRate = Double.parseDouble(parameters.getString(CONF_KEY_LEARNING_RATE, "1"));
   }
 
   @Override
   public void cross(PactRecord modelRecord, PactRecord gradientRecord,
       Collector<PactRecord> out) throws Exception {
     System.out.println("--------\nAPPLY GRADIENT\n--------");
     
     Vector w = modelRecord.getField(0, PactVector.class).getValue();
     Vector gradient = gradientRecord.getField(0, PactVector.class).getValue();
     System.out.println("- Old model: D=" + w.size() + " non-zeros=" + w.getNumNonZeroElements());
     System.out.println("- Gradient: D=" + gradient.size() + " non-zeros=" + gradient.getNumNonZeroElements());
 
     gradient.assign(Functions.MULT, learningRate);
     w.assign(gradient, Functions.MINUS);
     
     System.out.println("- New model: D=" + w.size() + " non-zeros=" + w.getNumNonZeroElements());
    
    out.collect(new PactRecord(new PactVector(w)));
   }
 
 }
