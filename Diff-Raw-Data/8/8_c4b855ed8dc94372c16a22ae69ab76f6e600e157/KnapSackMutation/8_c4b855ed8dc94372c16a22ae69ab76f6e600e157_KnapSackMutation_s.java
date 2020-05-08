 package knapsack;
 
 import java.util.BitSet;
 import java.util.Random;
 
 import model.Genome;
 import model.Mutation;
 
 public class KnapSackMutation implements Mutation {
 	private Random rand = new Random();
 	
 	public Genome mutate(Genome victim) {
 		BitSet bits = victim.getBits();
         int length = bits.length();
        if (length == -1) {
	    	bits.flip(rand.nextInt(0));
		}
        else {
            bits.flip(length);
        }
         return new Genome(bits);
 	}
 }
