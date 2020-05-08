 package faceclicker;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 /**
  * Contains a collection of SalientPoint objects matching the desired points 
  * specified in 'config-points.txt'. This collection corresponds to
  * the coordinate data for one image. The collection is iterated through as a 
  * user inputs points for an image. 
  *
  * @author Alex McKay
  */
 public class SalientPointCollection {
     
     /* Contains the salient points */
     ArrayList<SalientPoint> spArray;
     
     /* Contains the index value of the current salient point, to iterate through */
     int currentPoint; 
 
     
     /**
     * Constructor Method. Loads required point names from config-points.txt
     * then creates objects for every required salient point.
      *
      */
     public SalientPointCollection() {
         currentPoint = 0;
         spArray = new ArrayList<>();
         
         //load the required salient point names
         ArrayList<String> spNames = loadPointNames();
         
 
         //loops through the array of names and creates a salient point for each one
         for (String name : spNames)
             spArray.add(new SalientPoint(name));
         
     }
 
     /**
      * Retrieve the current salient point. This is the point which the user has
      * to click next.
      *
      * @return
      */
         public SalientPoint getCurrent() {
         if (currentPoint < spArray.size()) {
             return spArray.get(currentPoint);
         } else {
             return null;
         }
     }
     
     /**
      * Retrieve a salient point from the specified position in the list.
      *
      * @param i The position in the list to get the point from.
      * @return The SalientPoint object stored at that position in the list.
      */
     public SalientPoint get(int i){
         if (i < spArray.size()){
             return spArray.get(i);
         } else {
             return null;
         }
     }
 
     /**
      * Check if the collection has any more salient points yet to be set.
      *
      * @return True if there are still more salient points in the collection. 
      */
         public boolean hasNext() {
         return currentPoint != (spArray.size() - 1);
     }
 
     /**
      * Move to the next salient point in the collection.
      */
         public void iterate() {
         currentPoint++;
     }
 
     /**
      * Roll back to the previously clicked salient point, to allow it to be 
      * re-entered.
      */
     public void undo() {
         if (currentPoint > 0)
             currentPoint--;
     }
 
     /**
      * Print x/y values for every point to standard output.
      *
      */
     public void printPoints() {
         for (SalientPoint sp : spArray) {
             sp.printValues();
         }
     }
         
     /**
      * Write x/y values for every point to a specified file.
      *
      * @param filename The file to write to. 
      * @throws FileNotFoundException
      * @throws UnsupportedEncodingException
      */
     public void writePoints(String filename) throws FileNotFoundException, UnsupportedEncodingException {
         PrintWriter writer = new PrintWriter(filename, "UTF-8");
         
         for (SalientPoint sp : spArray) 
             writer.println(sp.toString());
         
         writer.close();
     }
 
     /**
      * Load names of the required points from config-points.txt. One line of the
      * file is assumed to correspond to the name of one point. 
      *
      * @return A list of point names. 
      */
     public ArrayList<String> loadPointNames() {
         ArrayList<String> names = new ArrayList<>();
 
         try {
             FileInputStream fstream = new FileInputStream("config-points.txt");
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
 
             //read in new lines, adding each to the list until no more exist
             while ((strLine = br.readLine()) != null)
                 names.add(strLine);
             
             in.close();
 
         } catch (IOException e) {
             System.err.println("Error: " + e.getMessage());
            return null;
         }
         
         return names;
     }
 
 }
