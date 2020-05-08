 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.awt.Point;
 
 /**
  *
  * @author Antoine
  */
public class City {
     
     public String name;
     public int codeINSEE;
     public Point pxPosition;
     public Point gpsPosition;
 
     public City(String name, int codeINSEE, Point gpsPosition) {
         this.name = name;
         this.codeINSEE = codeINSEE;
         this.gpsPosition = gpsPosition;
     }
 }
