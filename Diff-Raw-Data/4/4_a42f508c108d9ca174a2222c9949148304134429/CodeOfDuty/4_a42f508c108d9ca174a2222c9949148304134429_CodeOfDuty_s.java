 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Soldier @jie:
  * this is my Code of Duty!
  */
 public class CodeOfDuty {
 
   //============================================================================
   // The algorithm
 
   /**
    * Equilibrate the vector and return a List of iterations, an iteration being a list of integer x_i:
    * x_i = 5 means a transfer from 5 to 6
    * x_i = -5 means a transfer from 5 to 4
    * This sparse representation is more space efficient than copying the whole vector for each iteration.
    * Indeed, if there is only 3 transfers, it costs only 3 integers instead of a vector of length N
    */
   private static List<List<Integer>> equilibrate(int[] vector) {
 
     // Detect if <vector> is equilibrable, that is: sum(vector) must be a multiple of vector.length
     int sum = 0;
     for (int i = 0; i < vector.length; i++)
       sum += vector[i];
     int mean = sum / vector.length;
    if (sum % mean != 0)
      return null;
 
     // Compute sums[i] = sum_{0<=j<=i} vector[i]
     sum = 0;
     int[] sums = new int[vector.length];
     for (int i = 0; i < vector.length; i++) {
       sum += vector[i];
       sums[i] = sum;
     }
     
     List<List<Integer>> iterations = new ArrayList<List<Integer>>();
     List<Integer> currentIteration;
     int i = 0;
 
     // Assume that vector[0, i - 1] is already equilibrated,
     // we use the following algorithm for each iteration:
     // for j = i to vector.length - 1
     //   if (j > i && vector[j] > 0 && sums[j - 1] < j * mean) then transfer from j to j - 1
     //   if (sums[j] > (j + 1) * mean) then transfer from j to j + 1
     while (i < vector.length) {
       if (vector[i] == mean) {
         i++;
       } else {
         currentIteration = new ArrayList<Integer>();
         for (int j = i; j < vector.length; j++) {
           if (j > i && vector[j] > 0 && sums[j - 1] < j * mean) {
             vector[j]--;
             vector[j - 1]++;
             sums[j - 1]++;
             currentIteration.add(-j);
           } else if (sums[j] > (j + 1) * mean) {
             vector[j]--;
             vector[j + 1]++;
             sums[j]--;
             currentIteration.add(j);
             if (vector[j + 1] == 1) // if vector[j + 1] = 1, no transfer is possible for j + 1
               j++;
           }
         }
         iterations.add(currentIteration);
       }      
     }
     
     return iterations;
 
   }
 
   //============================================================================
   // To test if the algorithm works
   
   /**
    * generate a test vector of length <n> with a mean value <mean> and
    * randomly do <nTransfer> transfers (transfer from i to i + 1 or i -1)
    */
   private static int[] generateVector(int mean, int n, int nTransfer) {
     Random random = new Random();
     int[] vector = new int[n];
     int transferIndex;
     Arrays.fill(vector, mean);
     for (int i = 0; i < nTransfer; i++) {
       if (random.nextBoolean()) {
         transferIndex = random.nextInt(n - 1);
         vector[transferIndex]--;
         vector[transferIndex + 1]++;
       } else {
         transferIndex = random.nextInt(n - 1) + 1;
         vector[transferIndex]--;
         vector[transferIndex - 1]++;
       }
     }
     return vector;
   }
 
   //============================================================================
   // Read data from file, write data to file
   private static List<int[]> readData(String fileName) {
     List<int[]> data = new ArrayList<int[]>();
     try {
       Scanner scanner = new Scanner(new File(fileName));
       int[] vector;
       int n;
       while ((n = scanner.nextInt()) > 0) {
         vector = new int[n];
         for (int i = 0; i < n; i++) {
           vector[i] = scanner.nextInt();
         }
         data.add(vector);
       }
     } catch (FileNotFoundException ex) {
       Logger.getLogger(CodeOfDuty.class.getName()).log(Level.SEVERE, null, ex);
     }
     return data;
   }
 
   private static void writeData(PrintWriter writer, int[] vector, List<List<Integer>> iterations) {
     if (iterations == null) {
       writer.println("-1\n");
       return;
     }
     writer.println(iterations.size());
     writer.println("0 : " + arrayToString(vector));
     for (int i = 0; i < iterations.size(); i++) {
       for (Integer transfer : iterations.get(i)) {
         if (transfer < 0) {
           vector[-transfer]--;
           vector[-transfer - 1]++;
         } else {
           vector[transfer]--;
           vector[transfer + 1]++;
         }
       }
       writer.println((i + 1) + " : " + arrayToString(vector));
     }
     writer.println();
 
   }
 
   private static String arrayToString(int[] array) {
     String s = "";
     for (int i = 0; i < array.length; i++) {
       s += i == 0 ? "(" : ", ";
       s += array[i];
       s += i == array.length - 1 ? ")" : "";
     }
     return s;
   }
 
   //============================================================================
   public static void main(String[] args) {
 
     PrintWriter writer = null;
 
     try {
     
       String input;
       String output;
 
       if (args.length == 0 || args.length == 2) {
         
         if (args.length == 0) {
           input = "input.txt";
           output = "output.txt";
         } else {
           input = args[0];
           output = args[1];
         }
         List<int[]> vectors = readData(input);
         writer = new PrintWriter(output);
         for (int[] vector : vectors)
           writeData(writer, Arrays.copyOf(vector, vector.length), equilibrate(vector));
 
       } else if (args[0].equals("-rand")) {
         writer = new PrintWriter(args[3]);
         int[] vector = generateVector(Integer.parseInt(args[1]),
                 Integer.parseInt(args[2]), 1000);
         writeData(writer, Arrays.copyOf(vector, vector.length), equilibrate(vector));
       }
       
     } catch (FileNotFoundException ex) {
       Logger.getLogger(CodeOfDuty.class.getName()).log(Level.SEVERE, null, ex);
     } finally {
       writer.close();
     }
   }
 
 }
