 package pl.edu.agh.nnsimulator.neurons;
 
 import pl.edu.agh.nnsimulator.activationFunctions.*;
 import pl.edu.agh.nnsimulator.exceptions.ConnectionNotExistsException;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Neuron {
     private Map<Neuron, Double> weights;
     private ActivationFunctionType activationFunctionType;
     private ActivationFunctionInterface activationFunction;
     private double bias;
     protected double output;
 
     public Neuron(ActivationFunctionType activationFunctionType, double bias, Map<Neuron, Double> weights){
         this.activationFunctionType = activationFunctionType;
         this.bias = bias;
         this.weights = new HashMap<Neuron, Double>();
         this.weights = weights;
 
         switch (activationFunctionType){
             case PURELIN:
                 activationFunction = new PurelinActivationFunction();
                 break;
             case HARDLIM:
                 activationFunction = new HardlimActivationFunction();
                 break;
             case TANSIG:
                 activationFunction = new TansigActivationFunciton();
                 break;
         }
     }
 
     public double calculate(){
         double result = 0;
         for(Neuron neuron : weights.keySet()){
             result +=neuron.getOutput()* weights.get(neuron);
 
         }
         result += bias;
         output = activationFunction.calculate(result);
 
         return output;
     }
 
     public double getOutput(){
         return output;
     }
 
     public Map<Neuron, Double> getWeights() {
         return new HashMap<Neuron, Double>(weights);
     }
 
     public void updateWeight(Neuron neuron, double weight) throws ConnectionNotExistsException {
         if(!weights.containsKey(neuron)){
             throw new ConnectionNotExistsException();
         }
     }
 
 }
