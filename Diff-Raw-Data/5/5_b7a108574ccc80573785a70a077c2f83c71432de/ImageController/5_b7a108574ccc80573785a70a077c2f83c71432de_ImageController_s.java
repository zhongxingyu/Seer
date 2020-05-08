 package src.nonui;
 
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import src.ui.ImagePanelView;
 import src.utils.Point;
 import src.utils.Polygon;
 
 public class ImageController {
     // How far a user can click from a point and still select it (in pixels) 
     private static final double EDITING_THRESHOLD_DISTANCE = 5.0;
     
     private final AppController appController;
     private JFrame appFrame;
     private ImagePanelView imagePanel;
 
     // Used when adding/editing points.
     private Polygon polygonInCreation = new Polygon(); 
     private Polygon polygonInEditing = new Polygon();
     private Point currentPoint = null;
 
     public ImageController(AppController appController, JFrame appFrame) {
         this.appController = appController;
         this.appFrame = appFrame;
     }
 
     /**
      * Sets the panel that this controller is for.
      * 
      * @param imagePanel the panel this controller is for
      */
     public void setPanel(ImagePanelView imagePanel) {
         this.imagePanel = imagePanel;
     }
 
     /**
      * Returns a list of the points of the completed polygons.
      */
     public List<List<Point>> getCompletedPolygonsPoints() {
         Map<String, Polygon> completedPolygons = appController.getCompletedPolygons();        
         List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
         for (Polygon polygon : completedPolygons.values()) {
             points.add(new ArrayList<Point>(polygon.getPoints()));
         }
         return points;
     }
 
     /**
      * Returns a list of the points of the currently selected polygons.
      */
     public List<List<Point>> getSelectedPolygonsPoints() {
         List<Polygon> selectedPolygons = getSelectedPolygons();
         List<List<Point>> points = new ArrayList<List<Point>>(selectedPolygons.size());
         for (Polygon selectedPolygon : selectedPolygons) {
             points.add(selectedPolygon.getPoints());
         }
         return points;
     }
 
     /**
      * Returns a list of the points of the polygon that is currently being created.
      */
     public List<Point> getCurrentPolygonPoints() {
     	if (polygonInCreation != null)
     		return polygonInCreation.getPoints();
     	return null;
     }
 
     /**
      * Returns a list of the points of the polygon that is currently being edited,
      * or null if no polygon is being edited.
      */
     public List<Point> getEditedPolygonPoints() {
         if (appController.getApplicationState() == ApplicationState.EDITING_POLYGON) {
             return polygonInEditing.getPoints();
         }
 
         return null;
     }
 
     /**
      * Called when the image is clicked on.
      * 
      * @param x the x-coordinate of the mouse click
      * @param y the y-coordinate of the mouse click
      * @param doubleClick whether or not the user is double clicking
      */
     public void imageMouseClick(int x, int y, boolean doubleClick) {
         switch (appController.getApplicationState()) {
             
         	case DEFAULT:
                 if (doubleClick) {
                     // Double clicking does nothing in the default state.
                     return;
                 }
                 
             	// If an existing point is recognised as being clicked, 
             	// select it and change the state to EDITING
                 selectClosestPoint(x, y);
                 break;
                 
             case ADDING_POLYGON:
                 if (doubleClick) {
                     finishedAddingPolygon();
                 } else {
                 	if (nearFirstPoint(x,y)) {
                 		finishedAddingPolygon();
                 	} else {
                 		polygonInCreation.addPoint(new Point(x, y));
                         imagePanel.repaint();	
                 	}                 
                 }
                 break;
                 
             case EDITING_POLYGON:
             	
                 // If a point is being clicked, select this polygon.              
                 if (!selectClosestPoint(x, y)) {
                 	addPointToCompletedPolygon(x,y);
                 } else {
                 	
                 }
             	imagePanel.repaint();
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the mouse is pressed over the image (but not released
      * immediately).
      * 
      * @param x the x coordinate where the mouse was pressed
      * @param y the y coordinate where the mouse was pressed
      */
     public void imageMousePress(int x, int y) {
         switch (appController.getApplicationState()) {
         	case DEFAULT:
             	// If the user clicked on an existing point, select it.
                 selectClosestPoint(x, y);
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
             	// The user is either selecting a point on a polygon, or trying to add
                 // a new point to a polygon.
                 if (!selectClosestPoint(x, y)) {
                 	addPointToCompletedPolygon(x,y);
                 	imagePanel.repaint();                	
                 }
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the mouse is released (from a press/drag, not a click) over
      * the image.
      */
     public void imageMouseReleased() {
         switch (appController.getApplicationState()) {
             case DEFAULT:
                 currentPoint = null;
                 polygonInCreation = new Polygon();
                 imagePanel.repaint();
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
                 currentPoint = null;
                 polygonInCreation = new Polygon();
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the user drags their mouse over the image. 
      * 
      * @param x the x coordinate they have dragged to
      * @param y the y coordinate they have dragged to
      */
     public void imageMouseDrag(int x, int y) {
         switch (appController.getApplicationState()) {
             case DEFAULT:
                 // Do nothing
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
                 if (currentPoint != null && polygonInEditing != null) {
                     // Move the point.
                     Point newPoint = new Point(x, y);
                     if (polygonInEditing.replacePoint(currentPoint, newPoint)) {
                         currentPoint = newPoint;
                     }
                     imagePanel.repaint();
                 }
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the user is finished adding the current polygon, either by
      * clicking on the starting point, double-clicking, or clicking the "Done"
      * button on the toolbox.
      */
     public void finishedAddingPolygon() {
         if (appController.getApplicationState() != ApplicationState.ADDING_POLYGON) {
             return;
         }
 
         if (polygonInCreation.getPoints().size() < 3) {
             JOptionPane.showMessageDialog(appFrame,
                     "A label must have 3 or more vertices.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         String name = "";
         boolean hasName = false;
         while (!hasName) {
             String message = "Label Name";
             name = JOptionPane.showInputDialog(appFrame, message, name);
 
             // TODO: Should this totally cancel, or only cancel the "done"?
             // Occurs if the user hits the cancel option.
             if (name == null) {
                polygonInCreation = new Polygon();
                imagePanel.repaint();
                 return;
             }
 
             name = name.trim();
             if (appController.getCompletedPolygons().containsKey(name)) {
                 JOptionPane.showMessageDialog(appFrame, "That name is already in use.", "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (name.isEmpty()) {
                 JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                         JOptionPane.ERROR_MESSAGE);
            } else if (!name.matches("[a-zA-Z0-9]+")) {
                 JOptionPane.showMessageDialog(appFrame,
                         "Only alphanumeric characters are allowed in label names.", "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else {
                 hasName = true;
             }
         }
 
         polygonInCreation.setName(name);
         appController.getCompletedPolygons().put(name, polygonInCreation);
         polygonInCreation = new Polygon();
 
         appController.finishedAddingPolygon(name);
 
         imagePanel.repaint();
         
         if (appController.areTipsOn()) { 
         	appController.showNewLabelTip();
         }
         
     }
 
     /**
      * Undoes the last added vertex on the current polygon.
      */
     public void undo() {
         polygonInCreation.removeLastPoint();
         imagePanel.repaint();
     }
 
     /**
      * Redoes the last undone vertex on the current polygon.
      */
     public void redo() {
         polygonInCreation.redoPoint();
         imagePanel.repaint();
     }
 
     /**
      * Gets the polygon that is currently being edited.
      */
     public Polygon getEditedPolygon() {
         return polygonInEditing;
     }
 
     /**
      * Cancels the adding of the current polygon.
      */
     public void cancel() {
         polygonInCreation = new Polygon();
         imagePanel.repaint();
     }
 
     /**
      * Sets the image from a file.
      * 
      * @param bufferedImage the file to open the image from
      */
     public void setImage(BufferedImage image) {
         imagePanel.setImage(image);
     }
 
     /**
      * Sets the default text for the image panel.
      * 
      * @param text the text to display
      */
     public void setDefaultText(String text) {
         imagePanel.setDefaultText(text);
     }
 
     /**
      * Gets a list of the currently selected polygons.
      */
     private List<Polygon> getSelectedPolygons() {
         Map<String, Polygon> completedPolygons = appController.getCompletedPolygons();
         List<String> selectedNames = appController.getSelectedNames();
         List<Polygon> selectedPolygons = new ArrayList<Polygon>(selectedNames.size());
 
         for (String name : selectedNames) {
             selectedPolygons.add(completedPolygons.get(name));
         }
         return selectedPolygons;
     }
 
     /**
      * Selects the closest point to a given target point.
      * 
      * @param x the x coordinate of the target
      * @param y the y coordinate of the target
      */
     private boolean selectClosestPoint(int x, int y) {
         Point targetPoint = new Point(x, y);
         Point closestPoint = null;
         Polygon closestPolygon = null;
 
         double smallestDistance = -1;
 
         for (Polygon polygon : appController.getCompletedPolygons().values()) {
             for (Point point : polygon.getPoints()) {
                 double distanceToTarget = targetPoint.distanceFrom(point);
                 if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                     if (isSelected(polygon)) {
                         smallestDistance = distanceToTarget;
                         closestPoint = point;
                         closestPolygon = polygon;
                     }
                 }
 
             }
         }
 
         if (smallestDistance >= 0 && smallestDistance < EDITING_THRESHOLD_DISTANCE) {
             appController.setApplicationState(ApplicationState.EDITING_POLYGON);
             currentPoint = closestPoint;
             polygonInEditing = closestPolygon;
             return true;
         } else {
             appController.setApplicationState(ApplicationState.DEFAULT);
         }
         
         return false;
     }
 
     /**
      * Adds a point to the nearest completed polygon.
      * 
      * @param x the x coordinate of the point
      * @param y the y coordinate of the point
      */
     private void addPointToCompletedPolygon(int x, int y) {
     	Point targetPoint = new Point(x, y);
     	
     	polygonInCreation = null;
     	
     	for (Polygon polygon : getSelectedPolygons()) {
 	    	List<Point> polygonPoints = polygon.getPoints();
 	    	
 	    	for (int i = 0; i < polygonPoints.size(); i++) {
 	    		Point point1 = polygonPoints.get(i);
 	    		Point point2 = polygonPoints.get((i + 1) % polygonPoints.size());
 	    		
 	    		// y = mx + c
 	    		double m = (double) (point1.getY() - point2.getY()) / (double) (point1.getX() - point2.getX());
 	    		double c = point1.getY() - (double)(m * point1.getX());
 	    		
 	    		// Plug the new point into the line equation to see what the coordinates should be.
 	    		double expectedY = (m * (double) x) + c; 
 	    		double expectedX = ((double) y - c) / m;
 	    			    		
 	    		if (Math.abs(expectedY - (double) y) < EDITING_THRESHOLD_DISTANCE || 
 	    				Math.abs(expectedX - (double) x) < EDITING_THRESHOLD_DISTANCE) {
 
                     if (withinBoundingBox(targetPoint, point1, point2)) {
 	    				polygon.addPointAt(targetPoint, ((i+1) % polygonPoints.size()));
 	    				appController.setApplicationState(ApplicationState.EDITING_POLYGON);
 	    				polygonInEditing = polygon;
 	    				return;
 	    			}
 	    		}
 	    		
 	        }
     	}
     	
     }
     
     /**
      * Checks if a target point is within the bounding box created by two other points.
      * 
      * @param targetPoint the target point to check
      * @param point1 one of the points that define the bounding box
      * @param point2 the other point that defines the bounding box
      */
     private boolean withinBoundingBox(Point targetPoint, Point point1, Point point2) {
         int left = Math.min(point1.getX(), point2.getX());
         int right = Math.max(point1.getX(), point2.getX());
         int top = Math.min(point1.getY(), point2.getY());
         int bottom = Math.max(point1.getY(), point2.getY());
         
         int x = targetPoint.getX();
         int y = targetPoint.getY();
         
         return x >= left && x <= right && y >= top && y <= bottom;
 	}
 
     
     /**
      * Checks if a set of coordinates is near the starting point of the in-progress
      * polygon.
      * 
      * @param x the x coordinate to check
      * @param y the y coordinate to check
      */
 	private boolean nearFirstPoint(int x, int y) {
         if (polygonInCreation.getPoints().isEmpty()) {
         	return false;
         }
 
         Point targetPoint = new Point(x, y);        
         double distanceToTarget = targetPoint.distanceFrom(polygonInCreation.getPoints().get(0));
 
         return distanceToTarget < EDITING_THRESHOLD_DISTANCE;
     }
 
     /**
      * Returns true if a given polygon is currently selected.
      * 
      * @param polygon the polygon that may be selected
      */
     private boolean isSelected(Polygon polygon) {
         return getSelectedPolygons().contains(polygon);
     }
 }
