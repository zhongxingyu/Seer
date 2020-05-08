 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Yuri
  */
 public abstract class HMMAbstract {
 
     public double[][] transition;
     public double[][] emission;
     public double[] initial;
     public int[] sequence;
 
     public void parseTransition(String b) {
 
         // Parse the transition matrix
         String[] tmp = b.split(" ");
 
         // Create the transistion matrix
         transition = new double[Integer.parseInt(tmp[0])][Integer.parseInt(tmp[1])];
 
         // Add each value to the transition matrix
         int rows = Integer.parseInt(tmp[0]);
         int columns = Integer.parseInt(tmp[1]);
 
         // Remove the unnecesary lines
         String[] transNumbers = new String[tmp.length - 2];
 
         for (int i = 2; i < tmp.length; i++) {
             transNumbers[i - 2] = tmp[i];
         }
 
         // add the numbers to the transition matrix
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 transition[i][j] = Double.parseDouble(transNumbers[(i * columns) + j]);
             }
         }
 
     }
 
     public void parseEmission(String b) {
 
         // Parse the transition matrix
         String[] tmp = b.split(" ");
 
         // Create the transistion matrix
         emission = new double[Integer.parseInt(tmp[0])][Integer.parseInt(tmp[1])];
 
         // Add each value to the transition matrix
         int rows = Integer.parseInt(tmp[0]);
         int columns = Integer.parseInt(tmp[1]);
 
         // Remove the unnecesary lines
         String[] emissionNumbers = new String[tmp.length - 2];
 
         for (int i = 2; i < tmp.length; i++) {
             emissionNumbers[i - 2] = tmp[i];
         }
 
         // add the numbers to the transition matrix
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 emission[i][j] = Double.parseDouble(emissionNumbers[(i * columns) + j]);
             }
         }
 
     }
 
     public void parseInitial(String b) {
         // Parse the transition matrix
         String[] tmp = b.split(" ");
 
         // Create the transistion matrix
         initial = new double[Integer.parseInt(tmp[1])];
 
         // Add each value to the transition matrix
         int columns = Integer.parseInt(tmp[1]);
 
         // Remove the unnecesary lines
         String[] initialNumbers = new String[tmp.length - 2];
 
         for (int i = 2; i < tmp.length; i++) {
             initialNumbers[i - 2] = tmp[i];
         }
 
         // add the numbers to the transition matrix
         for (int i = 0; i < columns; i++) {
             initial[i] = Double.parseDouble(initialNumbers[i]);
         }
     }
 
     public void parseSequence(String b) {
         // Parse the transition matrix
         String[] tmp = b.split(" ");
 
         // Create the transistion matrix
         sequence = new int[Integer.parseInt(tmp[0])];
 
         // Add each value to the transition matrix
         int columns = Integer.parseInt(tmp[0]);
 
         // Remove the unnecesary lines
         String[] initialNumbers = new String[tmp.length - 1];
 
         for (int i = 1; i < tmp.length; i++) {
             initialNumbers[i - 1] = tmp[i];
         }
 
         // add the numbers to the transition matrix
         for (int i = 0; i < columns; i++) {
             sequence[i] = Integer.parseInt(initialNumbers[i]);
         }
     }
 
     public String printTransition() {
         StringBuilder transitionout = new StringBuilder();
 
         // Add the dimensions
         transitionout.append(transition.length);
         transitionout.append(" ");
         transitionout.append(transition[0].length);
         transitionout.append(" ");
 
         for (double[] row : transition) {
             for (double column : row) {
                 transitionout.append(column);
                 transitionout.append(" ");
             }
         }
 
         // Remove the last space
         transitionout.deleteCharAt(transitionout.length() - 1);
 
         return transitionout.toString();
     }
     
     public String printEmission() {
         StringBuilder transitionout = new StringBuilder();
 
         // Add the dimensions
         transitionout.append(emission.length);
         transitionout.append(" ");
         transitionout.append(emission[0].length);
         transitionout.append(" ");
 
         for (double[] row : emission) {
             for (double column : row) {
                 transitionout.append(column);
                 transitionout.append(" ");
             }
         }
 
         // Remove the last space
         transitionout.deleteCharAt(transitionout.length() - 1);
 
         return transitionout.toString();
     }
     
     public double logplus(double plog, double qlog) {
         double max, diff;
         if (plog > qlog) {
             if (qlog == Double.NEGATIVE_INFINITY) {
                 return plog;
             } else {
                 max = plog;
                 diff = qlog - plog;
             }
         } else {
             if (plog == Double.NEGATIVE_INFINITY) {
                 return qlog;
             } else {
                 max = qlog;
                 diff = plog - qlog;
             }
         }
         // Now diff <= 0 so Math.exp(diff) will not overflow
         //return max + (diff < -37 ? 0 : Math.log(1 + Math.exp(diff)));
         return max + Math.log(1+Math.exp(diff));
     }
 }
