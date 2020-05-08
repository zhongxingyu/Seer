 package fruit.g2_new;
 
 import java.util.*;
 import java.io.*;
 
 public class Stat {
     private LinkedList<int[]> history;
     private LinkedList<Integer> scoreHistory;
     private int nplayers;
     private int nkindfruits;
     private int nfruits;
     private int[] pref;
     private int nround;
 	
    //new stuff
    private double[] fruitAverages = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
    private double[] fruitStdevs = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
     
     public Stat(int nplayers, int[] pref) {
         history = new LinkedList<int[]>();
         scoreHistory = new LinkedList<Integer>();
         this.nplayers = nplayers;
         this.pref = pref.clone();
         nkindfruits = pref.length;
         nfruits = 0;
         nround = 0;
     }
 
     public int getNFruits(){
         return nfruits;
     }
 
     public void add(int[] bowl) {
         assert(nkindfruits == bowl.length);
         if (nfruits == 0)
             nfruits = sum(bowl);
         assert(nfruits == sum(bowl));
         nround++;
         history.add(bowl);
         scoreHistory.add(dot(bowl, pref));
         assert(nround == history.size());
 			
     }
 
     public int score(int round) {
         assert(round < nround);
         assert(round >= 0);
         return scoreHistory.get(round);
     }
 
     public double average() {
         // no data - guess an average
         if (nround == 0)
             return nfruits * sum(pref)/nkindfruits;
         double sum = 0;
         for (int i = 0; i < nround; i++)
             sum += score(i);
         return sum/nround;
     }
 	
 	//new method to compute the average values of all fruit in bowls
 	//new method. returns array of all fruit averages.
 	public double[] computeFruitAverages(){
 		if (nround == 0)
 			return fruitAverages; //TODO: not the best guessing method
 		for (int i = 0; i < 12;i++){
 			double sum = 0;
 			for (int j = 0; j < history.size(); j++){
 				int[] currentBowl = history.get(j);
 				sum += currentBowl[i];
 			}
 			fruitAverages[i] = sum/(history.size()*1.0);
 		}
 		return fruitAverages;
 	}
 	
 	//new method. returns array of all fruit stdevs.
 	public double[] computeFruitStdevs(){
 		if (nround == 0)
 			return fruitAverages; 	//TODO: again, bad guessing method.
 		for (int i = 0; i < 12; i++){
 			double sum = 0;
 			double[] fruitAvgs = computeFruitAverages();
 			double currentFruitAverage = fruitAvgs[i];
 			for (int j = 0; j < history.size(); j++){
 				int[] currentBowl = history.get(j);
 				double difference = currentBowl[i] - currentFruitAverage;
 				sum += Math.pow(difference,2);
 			}
 			fruitStdevs[i] = Math.sqrt(sum/(history.size()-1));
 		}
 		return fruitStdevs;
 	}
 	
 	//new method. returns average number of points we should expect to receive based on average numbers of fruits
 	public double averageScoreBasedOnFruits(){
 		double score = 0;
 		double[] fruitAvgs = computeFruitAverages();
 		for (int i = 0; i < 12; i++){
			score += (fruitAvgs[i]*(1.0*pref[i]));
 		}
 		return score;
 	}
 	
 	//new method. returns standard deviation of points based on stdev of all fruits
 	public double stdevBasedOnFruits(){
 		double stdev = 0;
 		double[] fruitSDs = computeFruitStdevs();
 		for (int i = 0; i < 12; i++){
			stdev += (fruitSDs[i]*(pref[i]*1.0));
 		}
 		return stdev;
 	}
 
     public double stdev() {
         if (nround == 0)
             return nfruits;
         double avg = average();
         int sum = 0;
         for (int i = 0; i < nround; i++) {
             double diff = avg - score(i);
             sum += Math.pow(diff, 2.0);
         }
         return Math.pow((sum/nround), 0.5);
 
     }
 
     public void dump(String filename) {
         LinkedList<Integer> scores = new LinkedList<Integer>(scoreHistory);
         int size = scores.size();
         double avg = average();
         double std = stdev();
         Collections.sort(scores, Collections.reverseOrder());
         try {
             PrintWriter writer = new PrintWriter(filename+".txt");
             for (int i = 1; i < 100; i++) {
                 // rank top 1/i
                 int rank = (int)(size / i - 1);
                 if (rank < 0)
                     rank = 0;
                 double score = scores.get(rank);
                 double coeff = (score - avg) / std;
                 writer.println(coeff);
             }
             writer.flush();
             writer.close();
             writer = new PrintWriter(filename+"_raw.txt");
             for (int i = 1; i < nround; i++) {
                 writer.println(scores.get(i));
             }
             writer.flush();
             writer.close();
         }
         catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
     private int sum(int[] a) {
         int sum = 0;
         for (int i = 0; i < a.length; i++) {
             sum += a[i];
         }
         return sum;
     }
 
     private int dot(int[] a, int[] b) {
         assert(a.length == b.length);
         int sum = 0;
         for (int i = 0; i < a.length; i++) {
             sum += a[i] * b[i];
         }
         return sum;
     }
    
 }
