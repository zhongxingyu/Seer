 package pl.agh.enrollme.controller.preferencesmanagement;
 
 import java.io.Serializable;
 
 /**
  * This class is used as a data model for the ring component from prefereces-management view.
  * Author: Piotr Turek
  */
 public class ProgressToken implements Serializable {
 
     private static final long serialVersionUID = 8604434732732542332L;
 
     private static final String DEFAULT_COLOR = "#ff0000";
 
     private String name;
     private String color;
     private int id;
     private int maxPoints;
     private int minPoints;
     private int pointsUsed;
     private int progress;
 
 
     public ProgressToken(int id, String name, int maxPoints, int minPoints, int pointsUsed) {
         this(id, name, maxPoints, minPoints, pointsUsed, DEFAULT_COLOR);
     }
 
     public ProgressToken(int id, String name, int maxPoints, int minPoints, int pointsUsed, String color) {
         this.id = id;
         this.name = name;
         this.maxPoints = maxPoints;
         this.minPoints = minPoints;
         this.pointsUsed = pointsUsed;
         this.color = color;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getMaxPoints() {
         return maxPoints;
     }
 
     public void setMaxPoints(int maxPoints) {
         this.maxPoints = maxPoints;
     }
 
     public int getMinPoints() {
         return minPoints;
     }
 
     public void setMinPoints(int minPoints) {
         this.minPoints = minPoints;
     }
 
     public int getPointsUsed() {
         return pointsUsed;
     }
 
     public void setPointsUsed(int pointsUsed) {
         this.pointsUsed = pointsUsed;
     }
 
     public String getColor() {
         return color;
     }
 
     public void setColor(String color) {
         this.color = color;
     }
 
     public int getProgress() {
         progress = (int)(((double)pointsUsed / (double)maxPoints) * 100);
         return progress;
     }
 
     public void setProgress(int progress) {
         this.progress = progress;
     }
 }
