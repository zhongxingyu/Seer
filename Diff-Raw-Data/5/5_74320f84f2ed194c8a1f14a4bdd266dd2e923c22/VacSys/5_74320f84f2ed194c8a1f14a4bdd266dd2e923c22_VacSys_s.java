 package vacsys;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * System to regulate treatment to patients based on a number of criteria
  */
 public class VacSys {
 
     VacSysHeap priorityQueue;
     HashMap<String, Integer> zPops;
     int tPop;
 
     /**
      * Create a system with an empty priority queue
      */
     public VacSys() {
         priorityQueue = new VacSysHeap();
     }
 
     /**
      * Create a system loaded with requests from a batch file
      * @param filename batch file
      * @throws java.io.FileNotFoundException file does not exist
      * @throws IOException Could not read file successfully
      */
     public VacSys(String filename) throws FileNotFoundException, IOException {
         BufferedReader reader;
         ArrayList<String[]> lines = new ArrayList<String[]>();
         String line;
 
         // toFile
         // build lines for each patient where [0] = name, [1] = age, [2] = zip
         reader = new BufferedReader(new FileReader(filename));
         while ((line = reader.readLine()) != null) {
             lines.add(line.split(","));
         }
 
         this.buildZPops(lines);
         this.buildTPop(lines);
     }
 
     /**
      * Sets the tPop field to the total population of the file
      * @param lines
      */
     private void buildTPop(ArrayList<String[]> lines) {
         this.tPop = lines.size();
     }
 
     /**
      * Sets the zPops hashmap for easy searchability
      * @param lines
      */
     private void buildZPops(ArrayList<String[]> lines) {
         String key;
         // iterate through lines
         for (int i = 0; i < lines.size(); i++) {
             key = lines.get(i)[2];
             // Set the key's value to zero if it does not exist
             if (!this.zPops.containsKey(key)) {
                 this.zPops.put(key, 0);
             }
             // Increment the key
             this.zPops.put(key, this.zPops.get(key) + 1);
         }
     }
 
     /**
      * Add a new request to the system
      *
      * @param name of the new patient
      * @param age  of the new patient
      * @param zip  of the new patient
      * @return successful?
      */
     public boolean insert(String name, int age, String zip) {
         return priorityQueue.add(new Patient(name, age, zip, this.getPopConstant(zip)));
     }
 
     /**
      * Gets the population constant for a particular zip code
      *
      * @param zip code in question
      * @return population constant
      */
     private int getPopConstant(String zip) {
         float zpop = this.zPops.get(zip);
         float tpop = this.tPop;
         return (int) (zpop / tpop * 10);
     }
 
     /**
      * Remove the next request from the system
      * @return Comma-Delimited String on Patient Fields name, age, zip
      */
     public String remove() {
         return priorityQueue.poll().toString();
     }
 
     /**
      * Remove num requests and store them in a CSV format
      *
      * @param num      of records to save
      * @param filename to save in
      * @return success
      */
     public boolean remove(int num, String filename) {
         ArrayBlockingQueue<String> results = new ArrayBlockingQueue<String>(num) {
         };
 
         while (num > 0) {
             results.add(priorityQueue.poll().toString());
             num--;
         }
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
             while (!results.isEmpty()) {
                 writer.write(results.poll());
             }
         } catch (IOException x) {
             return false;
         } catch (ArrayIndexOutOfBoundsException x) {
             return false;
         }
         return true;
     }
}
