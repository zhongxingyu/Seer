 
 package edu.msoe.se2800.h4;
 
 import com.google.common.io.Closeables;
 import edu.msoe.se2800.h4.jplot.JPoint;
 
 import java.awt.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class Path {
 
     private static final String TAG = "Path";
     private List<JPoint> points;
     Logger mLogger = Logger.INSTANCE;
 
     /**
      * Constructor - Initializes points list
      */
    public Path() {
         points = new ArrayList<JPoint>();
     }

     /**
      * Writes the current path to a specified file
      * 
      * @param outputFile File to which the Path should be written
     * @return true if the path was written successfully
      */
     public boolean writeToFile(File outputFile) {
         checkNotNull(outputFile, "The File instance passed was null");
         boolean threwException = true;
         PrintWriter writer = null;
         try {
             writer = new PrintWriter(outputFile);
             writer.println("## The points in this file are represented as <x-value, y-value>. ##");
             for (Point point : points) {
                 writer.print(point.getX());
                 writer.print(", ");
                 writer.println(point.getY());
             }
             threwException = false;
             mLogger.log(TAG, "Successfully wrote Path to file at :(" + outputFile.getAbsolutePath()
                     + ")");
         } catch (IOException e) {
             mLogger.log(TAG, "Problem writing Path to file at :(" + outputFile.getAbsolutePath()
                     + ")");
             System.out.println("Threw IO Exception");
         } finally {
             if (writer != null) {
                 writer.close();
             }
         }
         return !threwException;
     }
 
     public void reset() {
         points.clear();
     }
 
     public boolean add(JPoint point) {
         return points.add(point);
     }
 
     public List<JPoint> getPoints() {
         return points;
     }
 
     public JPoint get(int index) {
         return points.get(index);
     }
 
     public int size() {
         return points.size();
     }
 
     /**
      * Parses the given file for points and assigns those points to the points variable. If there is
      * an error while parsing or the file or the file could not be found, an exception is thrown and
      * the points variable remains as it was before entering this method.
      * 
      * @param input The file containing the coordinates
      * @throws UnsupportedEncodingException Thrown if there was an error while parsing because of
      *             bad file format
      * @throws FileNotFoundException Thrown if the file does not exist
      */
     public void readFromFile(File input) throws BadFormatException, FileNotFoundException {
         // check to make sure the input argument is valid
         if (input == null) {
             throw new NullPointerException();
         } else if (!input.exists()) {
             throw new FileNotFoundException();
         }
 
         // create a temporary list to hold the points in the file
         List<JPoint> tempPoints = new ArrayList<JPoint>();
 
         try {
             // set up the reader variables
             FileInputStream fs = new FileInputStream(input);
             DataInputStream in = new DataInputStream(fs);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
             String line;
             int lineCount = 1;
             boolean error = false;
             // while there are still lines to parse
             while ((line = br.readLine()) != null) {
                 // verify that the first line is the predefined header
                 if (lineCount == 1) {
                     if (!line.trim().equals(
                             "## The points in this file are represented as <x-value, y-value>. ##")) {
                         error = true;
                     }
 
                 } else {
                     // not on the first line so parse the coordinates. x and y
                     // separated by a ,
                     String[] coordinatesAsString = line.split(",");
                     if (coordinatesAsString.length != 2) {
                         error = true;
                     } else {
                         try {
                             // parse the points as integers and add to the
                             // temporary points list
                             int x = Integer.parseInt(coordinatesAsString[0].trim());
                             int y = Integer.parseInt(coordinatesAsString[1].trim());
                             tempPoints.add(new JPoint(x, y));
                         } catch (Exception e) {
                             error = true;
                         }
 
                     }
 
                 }
 
                 if (error) {
                     Closeables.close(br, true);
                     throw new BadFormatException("File has corrupt data. Line "
                             + lineCount + " was not in the correct format");
                 }
 
                 lineCount++;
             }
             Closeables.close(br, true);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
        // if at this point, no error has occurred. Set this.points to the
         // tempPoints
         points = tempPoints;
     }
 
     @SuppressWarnings("serial")
     public class BadFormatException extends Exception {
 
         public BadFormatException(String message) {
             super(message);
         }
 
     }
 
 }
