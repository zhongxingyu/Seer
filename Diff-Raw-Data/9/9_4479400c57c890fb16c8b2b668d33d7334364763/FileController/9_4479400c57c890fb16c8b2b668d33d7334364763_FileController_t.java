 /*
  * The MIT License
  *
  * Copyright 2013 Pieter Van Eeckhout.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.pietervaneeckhout.waypointcoverter.controller.file;
 
 import com.pietervaneeckhout.waypointcoverter.model.Waypoint;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * GPSCoordinateControler.java (UTF-8)
  *
  * <p>Controller for manipulating GPS coordinates.</p>
  *
  * 2013/06/08
  *
  * @author Pieter Van Eeckhout <vaneeckhout.pieter@gmail.com>
  * @since 1.0.0
  * @version 1.0.2
  */
 public class FileController {
 
     public FileController(){
     }
 
     public List<Waypoint> readGarminWaypointsFromFile(String filePath) {
         BufferedReader br = null;
         List<Waypoint> waypointList = new ArrayList<>();
 
         try {
             FileReader fr = new FileReader(filePath);
             br = new BufferedReader(fr);
             String line;
             while ((line = br.readLine()) != null) {
                 if (line.startsWith("Waypoint")) {
                     waypointList.add(parseGarminWaypoint(line));
                 }
             }
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FileController.class.getName()).
                     log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(FileController.class.getName()).
                     log(Level.SEVERE, null, ex);
         } finally {
             if (br != null) {
                 try {
                     br.close();
                 } catch (IOException ex) {
                     Logger.getLogger(FileController.class.getName()).
                             log(Level.SEVERE, null, ex);
                 }
             }
         }
         
         return waypointList;
     }
 
     private Waypoint parseGarminWaypoint(String line) {
         boolean north, east;
         double longitude, latitude;
         String name;
 
         String[] parts = line.split("\\t");
         String[] position = parts[4].split(" ");
 
         name = parts[1];
 
         north = position[0].startsWith("N");
         east = position[1].startsWith("E");
 
         latitude = Double.parseDouble(position[0].substring(1));
         longitude = Double.parseDouble(position[1].substring(1));
 
         Waypoint waypoint = new Waypoint(name, longitude, east, latitude, north);
 
         System.out.println(waypoint.toVolkswagenWaypoint());
 
         return waypoint;
     }
 
     public void writeVolkswagenWaypointsToFile(String filePath, List<Waypoint> waypointList) {
         PrintWriter pw = null;
        
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("filePath cannot be null or empty.");
        }
        
        //auto add file extention
        if (!filePath.endsWith(".txt")){
            filePath +=  ".txt";
        }
 
         try {
             File file = new File(filePath);
             FileWriter fw = new FileWriter(file);
             pw = new PrintWriter(fw);
             for (Waypoint waypoint : waypointList) {
                 pw.println(waypoint.toVolkswagenWaypoint());
             }
         } catch (IOException ex) {
             Logger.getLogger(FileController.class.getName()).
                     log(Level.SEVERE, null, ex);
         } finally {
             if (pw != null) {
                 pw.close();
             }
         }
     }
 }
