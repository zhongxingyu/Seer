 
 package edu.msoe.se2800.h4;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 public enum Path {
 
     INSTANCE;
 
     private static final String TAG = "Path";
 
     private List<Point> points = new ArrayList<Point>();
     Logger mLogger = Logger.INSTANCE;
 
     /**
      * Writes the current path to a specified file
      * 
      * @param outputFile File to which the Path should be written
      * @return true if the path was written succesfully
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
             e.printStackTrace();
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
 
     public boolean add(Point point) {
         return points.add(point);
     }
 
     public int size() {
         return points.size();
     }
 
     /**
      * Parses the given file for points and assigns those points to the points
      * variable. If there is an error while parsing or the file or the file
      * could not be found, an exception is thrown and the points variable
      * remains as it was before entering this method.
      * 
      * @param input The file containing the coordinates
      * @throws UnsupportedEncodingException Thrown if there was an error while
      *             parsing because of bad file format
      * @throws FileNotFoundException Thrown if the file does not exist
      */
     public void readFromFile(File input) throws UnsupportedEncodingException, FileNotFoundException {
         // check to make sure the input argument is valid
         if (input == null) {
             throw new NullPointerException();
         } else if (!input.exists()) {
             throw new FileNotFoundException();
         }
 
         // create a temporary list to hold the points in the file
         List<Point> tempPoints = new ArrayList<Point>();
 
         try {
             // set up the reader variables
             FileInputStream fs = new FileInputStream(input);
             DataInputStream in = new DataInputStream(fs);
 
             // TODO Andrew: this resource isn't closed... We might leak
             // resources
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
             String line;
             int lineCount = 1;
             boolean error = false;
             // while there are still lines to parse
             while ((line = br.readLine()) != null) {
                 // verify that the first line is the predefined header
                 if (lineCount == 1) {
                    if (!line.trim().equals("## The points in this file are represented as <x-value, y-value>. ##")) {
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
                             tempPoints.add(new Point(x, y));
                         } catch (Exception e) {
                             error = true;
                         }
 
                     }
 
                 }
 
                 if (error) {
                     throw new UnsupportedEncodingException("File has corrupt data. Line "
                             + lineCount + " was not in the correct format");
                 }
 
                 lineCount++;
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // if at this point, no error has ocurred. Set this.points to the
         // tempPoints
         points = tempPoints;
     }
 
 }
