import java.util.ArrayList;

 public class hiddenNeuron extends neuron {
   
   //Output nodes will modify this to sum the error deltas
   public double errorDelta;
   
   double[] latestInput;
   
   //Amount of data going into this neuron
   static final int INPUT_SIZE = 20*20;//20x20 pixels are in each png numeral character file
   
   public hiddenNeuron(){
     setWeights(INPUT_SIZE);
   }
   
   public void train() {
     double error = errorDelta * latestActivation * (1-latestActivation);
     for(int i = 0; i < weight.length; i++){
       weight[i] += LEARN_RATE * error * latestInput[i];
     }
   }
 
   /**
    * Sum the weights * inputs, pass through activation function, and return value
    * 
    * @param input
    * @return
    */
   public double activation(double[] input) {
     latestInput = input;
     double sum = 0;
     for (int i = 0; i < input.length; i++)
       sum += weight[i] * input[i];
     
     return activationFunction(sum);
   }
 }
