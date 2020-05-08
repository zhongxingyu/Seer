 package edu.msoe.se2800.h4.jplot;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import edu.msoe.se2800.h4.FileIO;
 import edu.msoe.se2800.h4.administrationFeatures.DatabaseConnection;
 import edu.msoe.se2800.h4.administrationFeatures.LoginUI;
 import lejos.robotics.navigation.Waypoint;
 import lejos.robotics.pathfinding.Path;
 import edu.msoe.se2800.h4.UserListController;
 import edu.msoe.se2800.h4.jplot.Constants.GridMode;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 import edu.msoe.se2800.h4.jplot.grid.GridInterface;
 
 public class JPlotController {
 
     private static JPlotController instance = null;
 
     private int gridDensity = Constants.DEFAULT_GRID_DENSITY;
 
     private JPlot jplot;
     private GridInterface grid;
     private Path path;
     private List<Waypoint> oldList;
     private Waypoint highlightedPoint;
     private boolean closingForModeChange = false;
 
     public static JPlotController getInstance() {
         if (instance == null) {
             synchronized (JPlotController.class) {
                 if (instance == null) {
                     instance = new JPlotController();
                 }
             }
         }
         return instance;
     }
 
     private JPlotController() {
         path = new Path();
         oldList = new ArrayList<Waypoint>();
     }
 
     public void init() {
         grid = new Grid();
         jplot = new JPlot(GridMode.OBSERVER_MODE, grid);
         jplot.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         jplot.addWindowListener(new JPlotWindowListener());
     }
 
     public GridInterface getGrid() {
         return grid;
     }
 
     public void changeMode(GridMode mode) {
         grid = new Grid();
         if (Constants.CURRENT_MODE == GridMode.IMMEDIATE_MODE) {
             path.clear();
             for (Waypoint p : oldList) {
                 path.add(p);
             }
         }
         Constants.CURRENT_MODE = mode;
         if (mode == GridMode.IMMEDIATE_MODE) {
             copyPoints();
             path.clear();
         }
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 closingForModeChange = true;
                 jplot.dispose();
                 jplot = new JPlot(Constants.CURRENT_MODE, grid);
                 closingForModeChange = false;
                 jplot.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                 jplot.addWindowListener(new JPlotWindowListener());
             }
         });
     }
 
     public void changeMode(DatabaseConnection.UserTypes accessLevel) {
         if (accessLevel == DatabaseConnection.UserTypes.ADMIN || accessLevel == DatabaseConnection.UserTypes.PROGRAMMER) {
             changeMode(GridMode.ADMINISTRATOR_MODE);
         } else {
             changeMode(GridMode.OBSERVER_MODE);
         }
     }
 
     public Path getPath() {
         return path;
     }
 
     public Waypoint[] getPathPoints() {
         Waypoint[] points = new Waypoint[path.size()];
         path.toArray(points);
         return points;
     }
 
     public void addPoint(Waypoint point) {
         path.add(point);
         if (jplot != null) {
             jplot.repaint();
         }
     }
 
     public void removePoint(int indexOfPoint) {
         path.remove(indexOfPoint);
         jplot.repaint();
     }
 
     public void copyPoints() {
         oldList.clear();
         for (Waypoint j : path) {
             oldList.add(j);
         }
         path.clear();
         grid.redraw();
     }
 
     public void zoomIn() {
         setGridDensity(getGridDensity() - 1);
         grid.redraw();
     }
 
     public void zoomOut() {
         setGridDensity(getGridDensity() + 1);
         grid.redraw();
     }
 
     public int getGridDensity() {
         return gridDensity;
     }
 
     public void setGridDensity(int density) {
         if (density > 1) {
             gridDensity = Math.min(density, 100);
             grid.redraw();
         } else {
             gridDensity = 1;
             grid.redraw();
         }
     }
 
     public Waypoint getHighlightedPoint() {
         return this.highlightedPoint;
     }
 
     public void setHighlightedPoint(int indexInPointsArray) {
         if (indexInPointsArray == -5) {
             this.highlightedPoint = null;
         } else {
             this.highlightedPoint = JPlotController.getInstance().getPath().get(indexInPointsArray);
         }
     }
 
     public void createUser() {
         JOptionPane.showMessageDialog(null, "Someone implement creating a user.  This is in the createUser() in JPlotController.java", "TEAM .SCRUMBOT", JOptionPane.ERROR_MESSAGE);
     }
 
     public void listUsers() {
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             @SuppressWarnings("unused")
             public void run() {
                 new UserListController();
             }
         });
     }
 
 
     private class JPlotWindowListener extends WindowAdapter {
         @Override
         public void windowClosing(WindowEvent e) {
             //if closing because of mode change, do not want to log out
             if (!closingForModeChange) {
                 JPlotController.this.logOut();
             } else {
                 jplot.dispose();
             }
         }
     }
 
     public void start() {
         JFrame dummyFrame = new JFrame();
         dummyFrame.setUndecorated(true);
         dummyFrame.setVisible(true);
         LoginUI login = new LoginUI(dummyFrame);
         dummyFrame.dispose();
         if (login.wasLoginSuccessful()) {
             //TODO log to logger
             this.init();
             try {
                 this.changeMode(DatabaseConnection.getInstance().getUserRole(login.getUsername()));
             } catch (IOException e) {
                 System.out.println("Unable to retrieve user role and set grid mode");
             }
         }
     }
 
     public void logOut() {
         if (jplot == null) {
             throw new NullPointerException("Tried to log out when jplot was null");
         }
         //TODO check to make sure the robot is not running
         //if(!robot.isRunning()){
         //TODO log to logger
 
         //TODO Check to make sure current user is not observer (because they can't save the file anyways)
        if(FileIO.getCurrentPathFile() != null || !JPlotController.this.getPath().isEmpty()){
             int result = JOptionPane
                     .showConfirmDialog(null, "Do you wish to save your current Path?", "Save...?",
                             JOptionPane.YES_NO_OPTION);
             if (result == JOptionPane.YES_OPTION) {
                 FileIO.save();
             }
 
         }
 
 
         this.jplot.dispose();
     }
 
 }
