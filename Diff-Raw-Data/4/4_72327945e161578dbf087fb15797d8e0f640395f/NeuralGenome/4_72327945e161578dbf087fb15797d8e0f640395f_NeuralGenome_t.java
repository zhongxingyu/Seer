 package ecprac.era270;
 
 import java.util.Arrays;
 //import ecprac.torcs.genome.IGenome;
 
 public class NeuralGenome extends GenericGenome {
     public double fitness;
 
     // 19 because of the number of sensors
     public FeedForward network = new FeedForward(new double[19], 1);
 
     public String toString() {  
         return "";
     }
 
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof NeuralGenome)) return false;
             
         NeuralGenome other = (NeuralGenome)o;
 
        return Arrays.equals(network.getWeights(), other.network.getWeights());
     }
 }
