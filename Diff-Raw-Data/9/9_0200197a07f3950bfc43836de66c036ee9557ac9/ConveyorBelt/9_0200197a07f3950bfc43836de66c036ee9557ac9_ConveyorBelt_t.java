 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.frontend.graphics.Line;
 import edu.mines.csci598.recycler.frontend.graphics.Path;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 
 import java.util.ArrayList;
 
 public class ConveyorBelt {
     private Path p;
     private ArrayList<Recyclable> recyclables;
     private double bottomLineStartTime = GameConstants.BOTTOM_PATH_START_TIME;
     private double bottomLineEndTime = GameConstants.BOTTOM_PATH_END_TIME;
     private double verticalLineStartTime = GameConstants.VERTICAL_PATH_START_TIME;
     private double verticalLineEndTime = GameConstants.VERTICAL_PATH_END_TIME;
     private double topLineStartTime = GameConstants.TOP_PATH_START_TIME;
     private double topLineEndTime = GameConstants.TOP_PATH_END_TIME;
 
     private Line bottomLine = new Line(GameConstants.BOTTOM_PATH_START_X, GameConstants.BOTTOM_PATH_START_Y,
             GameConstants.BOTTOM_PATH_END_X, GameConstants.BOTTOM_PATH_END_Y, bottomLineStartTime);
     private Line verticalLine = new Line(GameConstants.VERTICAL_PATH_START_X, GameConstants.VERTICAL_PATH_START_Y,
             GameConstants.VERTICAL_PATH_END_X, GameConstants.VERTICAL_PATH_END_Y, verticalLineStartTime);
     private Line topLine = new Line(GameConstants.TOP_PATH_START_X, GameConstants.TOP_PATH_START_Y,
             GameConstants.TOP_PATH_END_X, GameConstants.TOP_PATH_END_Y, topLineStartTime);
 
     public ConveyorBelt() {
         recyclables = new ArrayList<Recyclable>();
 
         p = new Path();
         p.addLine(bottomLine);
         p.addLine(verticalLine);
         p.addLine(topLine);
     }
 
     public synchronized ArrayList<Recyclable> getRecyclables() {
         return recyclables;
     }
 
     public synchronized void addRecyclable(Recyclable r) {
         recyclables.add(r);
         r.getSprite().setPath(p);
     }
 
     public synchronized void removeRecyclable(Recyclable r) {
         recyclables.remove(r);
     }
 
     public synchronized void setSpeed(double pctOfFullSpeed) {
         bottomLine.setTotalTime(bottomLineStartTime + pctOfFullSpeed * (bottomLineEndTime - bottomLineStartTime));
         verticalLine.setTotalTime(verticalLineStartTime + pctOfFullSpeed * (verticalLineEndTime - verticalLineStartTime));
         topLine.setTotalTime(topLineStartTime + pctOfFullSpeed * (topLineEndTime - topLineStartTime));
     }
 }
 
 
 
