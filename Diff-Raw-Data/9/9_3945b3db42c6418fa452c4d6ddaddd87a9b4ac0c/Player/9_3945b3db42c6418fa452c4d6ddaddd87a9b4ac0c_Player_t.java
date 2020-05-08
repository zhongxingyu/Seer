package fruit.g2_new;
 
 import java.util.*;
 import java.io.*;
 
 public class Player extends fruit.sim.Player
 {
     private Stat stat;
     private int nplayers;
     private int[] pref;
     // number of times that a bowl passed to the player
     // at the end of first round npassed+getindex() = nplayers
     private int npassed;
     private int round;
     // Housekeeping, useless in functionality
     private boolean picked;
     private double[] coefficients = new double[100];
 
     public void init(int nplayers, int[] pref) {
         stat = new Stat(nplayers, pref);
         this.nplayers = nplayers;
         this.pref = pref.clone();
         npassed = 0;
         round = 0;
         picked  = false;
 	
 	try{
 		BufferedReader buffer = new BufferedReader(new FileReader("fruit/g2/uniform_10000.txt"));
 		for(int i = 1; i < 100; i++){
 			String sCoeff = buffer.readLine();
 			float coeff = Float.parseFloat(sCoeff);
 			coefficients[i] = coeff;
 		}
 	}
 	catch (Exception e){
 		e.printStackTrace();
 	}
 		
     }
 
     public boolean pass(int[] bowl, int bowlId, int round,
                         boolean canPick,
                         boolean mustTake) {
         npassed++;
         stat.add(bowl);
 	System.out.println("choice left: " + choiceLeft() + " can pick " + canPick);
         if (choiceLeft() == 0 && this.round == 0) {
             // beginning of second round
             assert(picked);
             picked = false;
             this.round = 1;
         }
         assert(round == this.round);
         assert(picked != canPick);
         // already picked
         if (picked)
             return false;
         // must take
         if (choiceLeft() == 1) {
             if (!picked) {
                 assert(mustTake);
                 picked = true;
                 return true;
             }
         }
         // otherwise look for avg + std
         else {
 	    double coeff = getCoeff(choiceLeft());
 	    System.out.println("Score to take: " + (stat.average() + coeff*adjustedStdDev(stat.stdev())));
             if (stat.score(npassed-1) < stat.average() + coeff*adjustedStdDev(stat.stdev()))
                 return false;
             else {
                 picked = true;
                 return true;
             }
         }
         return false;
     }
     
     public double getCoeff(int choiceLeft){
 
 	assert(choiceLeft>0);
 	if (choiceLeft>=100)
 		return coefficients[99];
 	else
 		return coefficients[choiceLeft];
     }
 
     public double adjustedStdDev(double stdev){
 
      int numfruits = stat.getNFruits();
      double influence = Math.pow(.5, (double)npassed-1);
      return influence*numfruits+(1-influence)*stdev;
 
     }
 
     private int choiceLeft() {
         if (round == 0)
             return nplayers - getIndex()- npassed+1;
         else
             return nplayers - npassed+2;
     }
 }
