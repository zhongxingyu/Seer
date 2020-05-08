 package ux.display;
 
 import ux.painters.OutlinePainter;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.NoninvertibleTransformException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 import publishersubscriber.SimulatorPublisher;
 import publishersubscriber.SimulatorSubscriber;
 import simulation.geometry.Environment;
 import simulation.geometry.Entity;
 import simulation.geometry.RigidBody;
 import simulation.geometry.Terrain;
 import simulation.geometry.XPoint;
 import simulation.entities.Cup;
 import simulation.entities.Robot;
 import ux.painters.EnvironmentPainter;
 import ux.painters.EnvironmentPainterImp;
 import ux.painters.ItemPainter;
 import ux.painters.ItemPainterImp;
 import ux.painters.RobotPainter;
 import ux.painters.RobotPainterProgressImp2;
 import ux.painters.SelectedPainter;
 import ux.painters.SelectedPainterImp;
 import ux.painters.SensorPainter;
 import ux.painters.SensorPainterImp;
 import ux.painters.VelocityVectorPainter;
 import ux.painters.VelocityVectorPainterImp;
 import ux.painters.TerrainPainter;
 import ux.painters.TerrainPainterImp2;
 import ux.usercontrol.SimulatorControlGui;
 import ux.usercontrol.SimulatorController;
 import ux.usercontrol.SimulatorController.Mode;
 
 /**
  * {@code GraphicalDisplay} is a {@code JPanel} that displays a graphical
  * representation of of a robot simulator. It is a subscriber of a
  * {@link publishersubscriber.SimulatorPublisher}, and thus, has its
  * {@link publishersubscriber.SimulatorSubscriber#update(SimulatorPublisher)}
  * method called automatically by a simulator publisher. The graphical display
  * is also a {@code SimulatorControlGui}- a user interface that is able to
  * change the state of the simulation. Being a {@code SelectionPublisher}, the
  * graphical display informs any subscribers of any simulation item, such as a
  * robot or terrain, that has been selected with the mouse pointer in the
  * graphical display. This allows any {@code SelectionSubscribers} to monitor
  * what simulation items the user clicks on.
  *
  */
 public class GraphicalDisplay extends JPanel implements SimulatorSubscriber,
         SelectionPublisher, SimulatorControlGui {
     // The controller to use to control the simulator.
 
     private SimulatorController controller;
     // The publisher representing the underlying simulator.
     private SimulatorPublisher simulatorPublisher;
     // Subscribers to update when a simulation item is selected.
     private Collection<SelectionSubscriber> subscribers = new HashSet<SelectionSubscriber>();
     // Simulation Items.
     private Collection<? extends Robot> robots;
     private Collection<? extends Cup> inanimates;
     private Collection<? extends Terrain> terrain;
     // Swing components.
     private JLabel timeElapsedLabel = new JLabel();
     private String mousePositionDisplay;
     private Point2D.Double mousePosition;
     private JLabel mousePositionLabel = new JLabel();
     private Environment environment;
     // The displays painters: these are delegated a role in painting on the
     // panel's canvas.
     private EnvironmentPainter environmentPainter;
     private ItemPainter itemPainter;
     private RobotPainter robotPainter;
     private SelectedPainter selectedPainter;
     private TerrainPainter terrainPainter;
     private SensorPainter sensorPainter;
     private VelocityVectorPainter velocityVectorPainter;
     private OutlinePainter outlinePainter;
     private RigidBody selectedItem;
     private RigidBody draggedItem;
     private RigidBody draggingOutline;
     private double draggingOffsetX,
             draggingOffsetY;
     // Painter switches- used to enable and disable individual painters.
     private boolean isPaintEnvironmentOn = true;
     private boolean isPaintTerrainOn = true;
     private boolean isPaintItemOn = true;
     private boolean isPaintRobotOn = true;
     private boolean isPaintSensorOn = true;
     private boolean isPaintVelocityVectorOn = true;
     private boolean isPaintSelectedOn = true;
 
     /**
      * Constructs a GraphicalDisplay and subscribes the display to
      * the given SimulatorPublisher It also informs the given controller that
      * it will use the controller to control the simulator and wishes to be
      * informed of any changes of state of the simulator.
      *
      * @param   simulatorPublisher  the SimulatorPublisher to subscribe to.
      * @param   controller          the controller to use to control the
      *                              simulator.
      */
     public GraphicalDisplay(SimulatorPublisher simulatorPublisher, SimulatorController controller) {
         this.controller = controller;
         this.simulatorPublisher = simulatorPublisher;
         simulatorPublisher.addSubscriber(this);
 
         // Retrieve the simulation items from the simulator.
         robots = simulatorPublisher.getRobots();
         inanimates = simulatorPublisher.getThings();
         environment = simulatorPublisher.getEnvironment();
         terrain = environment.getTerrain();
 
         // Give the panel a layout. This is needed to position components such
         // as the timeElapsedLabel.
         setLayout(new MigLayout());
         add(timeElapsedLabel);
         mousePositionDisplay = "";
         add(mousePositionLabel);
 
         setPreferredSize(new Dimension(500, 500));
 
         // Initialise the painters.
         setDefaultPainters();
 
         // Add mouse listeners to the panel to enable the selecting and dragging
         // functionality.
         addMouseListener(new HighlightedObjectListener());
         addMouseListener(new DraggingObjectListener());
         addMouseMotionListener(new DraggingObjectListener());
         addMouseMotionListener(new MousePositionListener());
     }
 
     /**
      * Initialises all the painters to a default painter.
      */
     protected void setDefaultPainters() {
         environmentPainter = new EnvironmentPainterImp();
         robotPainter = new RobotPainterProgressImp2();
         sensorPainter = new SensorPainterImp();
         terrainPainter = new TerrainPainterImp2();
         itemPainter = new ItemPainterImp();
         velocityVectorPainter = new VelocityVectorPainterImp();
         selectedPainter = new SelectedPainterImp();
         outlinePainter = new OutlinePainter();
     }
 
     /**
      * Draws the graphical representation of the simulation.
      * @param graphics The graphic to paint.
      */
     @Override
     public void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         // Convert the legacy graphics to Graphics2D.
         Graphics2D graphics2D = (Graphics2D) graphics;
 
         // Increase the rendering quality of the display.
         graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 
         // Depending on whether a painter is enabled, use the painters to paint
         // on the canvas.
 
 
 
         if (isPaintEnvironmentOn) {
             if (environment != null) {
                 environmentPainter.paint(environment, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintTerrainOn) {
             for (Terrain aTerrain : terrain) {
                 terrainPainter.paint(aTerrain, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintItemOn) {
             for (Cup thing : inanimates) {
                 itemPainter.paint(thing, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintRobotOn) {
             for (Robot robot : robots) {
                 robotPainter.paint(robot, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintSensorOn && simulatorPublisher.getScheduler().isRunning()) {
             for (Robot robot : robots) {
                 sensorPainter.paint(robot, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintVelocityVectorOn) {
             for (Robot robot : robots) {
                 velocityVectorPainter.paint(robot, getShapeTransform(), graphics2D);
             }
         }
 
         if (isPaintSelectedOn) {
             if (selectedItem != null) {
                 selectedPainter.paint(selectedItem, getShapeTransform(), graphics2D);
             }
         }
 
         if (draggingOutline != null) {
             outlinePainter.paint(draggingOutline, getShapeTransform(), graphics2D);
         }
 
         if (mousePosition != null) {
             graphics2D.setColor(Color.GRAY); // Gray works well as it can be seen against black terrain
             graphics2D.drawString(mousePositionDisplay, (int) mousePosition.x, (int) mousePosition.y);
         }
     }
 
     /**
      * This is called by a SimulatorPublisher that the GraphicalDisplay is
      * subscribed to. The display will be updated to reflect the changes to the
      * simulator represented by the SimulatorPublisher.
      *
      * @param simulatorPublisher    the SimulatorPublisher which is calling the
      *                              update method.
      */
     public void update(SimulatorPublisher simulatorPublisher) {
         // Update all the collections of simulator objects.
         robots = simulatorPublisher.getRobots();
         inanimates = simulatorPublisher.getThings();
         environment = simulatorPublisher.getEnvironment();
         terrain = environment.getTerrain();
 
         // Update the time-elapsed.
         double timeElapsed = simulatorPublisher.getTimeElapsed();
         String timeElapsedString = new DecimalFormat("#.##").format(timeElapsed);
         timeElapsedLabel.setText("Time elapsed: " + timeElapsedString);
 
         if (simulatorPublisher.isInSimulator(selectedItem)) {
             this.setPaintSelectedOn(true);
         } else {
             this.setPaintSelectedOn(false);
         }
 
         // Cause the panel to refresh.
         revalidate();
         repaint();
     }
 
     /**
      * Returns an AffineTransform to scale all shapes in the drawing area before
      * they are drawn or filled. The transform transforms the shape into GUI
      * coordinates ( (0,0) starts in the top left) and scales the shapes so that
      * they fit on the panel while maintaining their aspect ratio.
      */
     private AffineTransform getShapeTransform() {
 
         // Account for any insets, such as border insets so that any shapes
         // drawn do not overlap borders or other components. The -5 is
         // needed to move drawing area a little further inside the panel,
         // otherwise part of the outline is missing.
         Insets insets = getInsets();
         int panelHeight = getHeight() - insets.top - insets.bottom;
         int panelWidth = getWidth() - insets.left - insets.right;
 
         // Choose a scaling factor that scales the shapes to fit in the drawing
         // area both horizontally and vertically. The most stringent scaling
         // factor is chosen and applied to both shape coordinates to maintain
         // the shapes' size ratios while fitting in the drawing area.
         // The max x and y coordinates are used instead of width and height, as
         // the environment may not be centered at the origin.
         double environmentWidth = environment.getBounds().getMaxX();
         double environmentHeight = environment.getBounds().getMaxY();
         double widthScalingFactor = panelWidth / environmentWidth;
         double heightScalingFactor = panelHeight / environmentHeight;
         double scalingFactor = ((widthScalingFactor < heightScalingFactor)
                 ? widthScalingFactor : heightScalingFactor);
 
         // Create the transform.
         AffineTransform transform = new AffineTransform();
 
         // Translate the coordinate system so it does not overlap any borders.
         transform.translate(insets.left, insets.top);
 
         // Flip the coordinate system such that it appears to be a top-right
         // quadrant coordinate system.
         final double inset = 0.99;            //scales the image so it fits inside the display with a small buffer
         final double halfInset = (1 - inset) / 2; //used in translation to move the image away from the bottom and left sides
 
         transform.scale(inset, -inset);
         transform.translate(halfInset * panelWidth, -((1 + halfInset) * panelHeight));
 
         // Scale the shapes to fit the size of the drawing panel.
         transform.scale(scalingFactor, scalingFactor);
 
         return transform;
     }
 
     /**
      * Inform any SelectionSubscribers subscribed to the graphical display that
      * the given geometric shape has been selected.
      *
      * @param selectedPaintable the geometric shape that has been selected.
      */
     private void updateSubscribers(Entity selectedPaintable) {
         for (SelectionSubscriber subscriber : subscribers) {
             subscriber.setSelected(selectedPaintable);
         }
     }
 
     @Override
     public boolean addSubscriber(SelectionSubscriber subscriber) {
         return subscribers.add(subscriber);
     }
 
     @Override
     public boolean removeSubscriber(SelectionSubscriber subscriber) {
         return subscribers.add(subscriber);
     }
 
     /**
      * Resets the selected item
      * @param publisher The publisher to reset
      */
     @Override
     public void simulatorResetted(SimulatorPublisher publisher) {
         selectedItem = null;
     }
 
     /**
      * HighlightedObjectListener is used to detect when an item is selected.
      *
      */
     private class HighlightedObjectListener extends MouseAdapter {
 
         /**
          * Detects whether there is an item at the same location as where the
          * mouse was pressed. All SelectionSubscribers subscribed to the
          * graphical display are notified of a selection, if one occurs.
          */
         @Override
         public void mousePressed(MouseEvent event) {
             int mouseX = event.getX();
             int mouseY = event.getY();
 
             selectedItem = null;
 
             // Create a new collection with enough space for all objects
             // (faster than allocating size when objects are added).
             Collection<RigidBody> simulationObjects = new ArrayList<RigidBody>();
 
             simulationObjects.addAll(robots);
             simulationObjects.addAll(inanimates);
             simulationObjects.addAll(environment.getTerrain());
 
             // Check every simulation object to see if the mouse was pressed
             // inside it.
             for (RigidBody simulationObject : simulationObjects) {
                 // The shape must first be converted to the GUI coordinates.
                 Shape shapeInGuiCoords =
                         getShapeTransform().createTransformedShape(simulationObject);
 
                 if (shapeInGuiCoords.contains(mouseX, mouseY)) {
                     selectedItem = simulationObject;
                     break;
                 }
             }
             updateSubscribers(selectedItem);
         }
     }
     private boolean pausedBySelection = false;
 
     /**
      * Detects an object being dragged and moves it appropriately.
      *
      * @author kad78
      *
      */
     private class DraggingObjectListener extends MouseAdapter {
 
         private XPoint transformMouse(MouseEvent e) {
             AffineTransform af = getShapeTransform();
             try {
                 af.invert();
             } catch (NoninvertibleTransformException ex) {
             }
             XPoint env = new XPoint(e.getX(), e.getY());
             selectedItem.getCom();
 
             af.transform(env, env);
             return env;
         }
 
         /**
          * Detects that the mouse button has been pressed and, if it has been
          * pressed on an object, pauses the simulator.
          *
          * @param e
          */
         @Override
         public void mousePressed(MouseEvent e) {
             selectObjectAt(e.getX(), e.getY());
             if (selectedItem != null) {
                 if (controller.pauseSimulator()) {
                     pausedBySelection = true;
                 }
                 XPoint env = transformMouse(e);
 
                 draggingOffsetX = selectedItem.getX() - env.getX();
                 draggingOffsetY = selectedItem.getY() - env.getY();
 
                 draggingOutline = new RigidBody(selectedItem);
             }
         }
 
         /**
          * Tracks the coordinate path of the dragged mouse and moves the
          * selected object along the path.
          *
          * @param e
          */
         @Override
         public void mouseDragged(MouseEvent e) {
             if (selectedItem != null) {
                 // New coordinates
                 double envX;
                 double envY;
                 draggedItem = selectedItem;
                 // Find how the environment is modified to the canvas
                 XPoint env = transformMouse(e);
 
                 envX = env.getX() > environment.getWidth() ? environment.getWidth() : env.getX() < 0 ? 0 : env.getX();
                 envY = env.getY() > environment.getHeight() ? environment.getHeight() : env.getY() < 0 ? 0 : env.getY();
 
                 draggingOutline.place(envX + draggingOffsetX, envY + draggingOffsetY);
             }
         }
 
         /**
          * Restarts the simulator once the mouse is released.
          *
          * @param e
          */
         @Override
         public void mouseReleased(MouseEvent e) {
             if (draggingOutline != null) {
                 if (simulatorPublisher.canPlace(selectedItem, draggingOutline)) {
                    XPoint env = transformMouse(e);
                    double envX = env.getX() > environment.getWidth() ? environment.getWidth() : env.getX() < 0 ? 0 : env.getX();
                    double envY = env.getY() > environment.getHeight() ? environment.getHeight() : env.getY() < 0 ? 0 : env.getY();
                    selectedItem.place(envX + draggingOffsetX, envY + draggingOffsetY);
                 }
                 draggingOutline = null;
             }
             if (pausedBySelection) {
                 controller.startSimulator();
                 pausedBySelection = false;
             }
 
         }
 
         private void selectObjectAt(double x, double y) {
             selectedItem = null;
 
             // Create a new collection with enough space for all objects.
             Collection<RigidBody> simulationObjects = new ArrayList<RigidBody>();
 
             simulationObjects.addAll(robots);
             simulationObjects.addAll(inanimates);
             simulationObjects.addAll(environment.getTerrain());
 
             for (RigidBody simulationObject : simulationObjects) {
                 Shape shapeInGuiCoords = getShapeTransform().createTransformedShape(simulationObject);
 
                 if (shapeInGuiCoords.contains(x, y)) {
                     selectedItem = simulationObject;
                     break;
                 }
             }
         }
     }
 
     /**
      * This listener tracks the position of the mouse on the environment
      *
      * @param e
      */
     private class MousePositionListener extends MouseAdapter {
 
         @Override
         public void mouseMoved(MouseEvent e) {
 
             AffineTransform af = new AffineTransform();
             try {
                 af = getShapeTransform().createInverse();
             } catch (NoninvertibleTransformException et) {
             }
             // Point to transform
             mousePosition = new Point2D.Double(e.getX(), e.getY());
 
             af.transform(mousePosition, mousePosition);
 
             String xPointStr = Integer.toString((int) mousePosition.x);
             String yPointStr = Integer.toString((int) mousePosition.y);
 
             mousePositionDisplay = "(" + xPointStr + "," + yPointStr + ")";
 
             mousePosition.setLocation(e.getX(), e.getY());
         }
     }
 
     @Override
     public void setResetted() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void setStarted() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void setPaused() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void setMode(Mode mode) {
     }
 
     /**
      * Sets the graphical display's environment painter to the given
      * EnvironmentPainter.
      *
      * @param environmentPainter    the painter to set as the display's
      *                              environment painter.
      */
     public void setEnvironmentPainter(EnvironmentPainter environmentPainter) {
         this.environmentPainter = environmentPainter;
     }
 
     /**
      * Sets the graphical display's 'selected' painter to the given
      * SelectedPainter.
      *
      * @param selectedPainter   the painter to set as the display's
      *                          'selected' painter.
      */
     public void setSelectedPainter(SelectedPainter selectedPainter) {
         this.selectedPainter = selectedPainter;
     }
 
     /**
      * Sets the graphical display's robot painter to the given
      * RobotPainter.
      *
      * @param robotPainter  the painter to set as the display's
      *                      robot painter.
      */
     public void setRobotPainter(RobotPainter robotPainter) {
         this.robotPainter = robotPainter;
     }
 
     /**
      * Sets the graphical display's sensor painter to the given
      * SensorPainter.
      *
      * @param sensorPainter     the painter to set as the display's
      *                          sensor painter.
      */
     public void setSensorPainter(SensorPainter sensorPainter) {
         this.sensorPainter = sensorPainter;
     }
 
     /**
      * Sets the graphical display's terrain painter to the given
      * TerrainPainter.
      *
      * @param terrainPainter    the painter to set as the display's
      *                          terrain painter.
      */
     public void setTerrainPainter(TerrainPainter terrainPainter) {
         this.terrainPainter = terrainPainter;
     }
 
     /**
      * Sets the graphical display's item painter to the given
      * ItemPainter.
      *
      * @param itemPainter   the painter to set as the display's
      *                      item painter.
      */
     public void setItemPainter(ItemPainter itemPainter) {
         this.itemPainter = itemPainter;
     }
 
     /**
      * Returns whether the environment painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintEnvironmentOn() {
         return isPaintEnvironmentOn;
     }
 
     /**
      * Enables or disables the environment painter.
      *
      * @param isPaintEnvironmentOn  whether to enable or disable the painter.
      */
     public void setPaintEnvironmentOn(boolean isPaintEnvironmentOn) {
         this.isPaintEnvironmentOn = isPaintEnvironmentOn;
     }
 
     /**
      * Returns whether the terrain painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintTerrainOn() {
         return isPaintTerrainOn;
     }
 
     /**
      * Enables or disables the terrain painter.
      *
      * @param isPaintTerrainOn  whether to enable or disable the painter.
      */
     public void setPaintTerrainOn(boolean isPaintTerrainOn) {
         this.isPaintTerrainOn = isPaintTerrainOn;
     }
 
     /**
      * Returns whether the item painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintItemOn() {
         return isPaintItemOn;
     }
 
     /**
      * Enables or disables the item painter.
      *
      * @param isPaintItemOn  whether to enable or disable the painter.
      */
     public void setPaintItemOn(boolean isPaintItemOn) {
         this.isPaintItemOn = isPaintItemOn;
     }
 
     /**
      * Returns whether the robot painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintRobotOn() {
         return isPaintRobotOn;
     }
 
     /**
      * Enables or disables the robot painter.
      *
      * @param isPaintRobotOn  whether to enable or disable the painter.
      */
     public void setPaintRobotOn(boolean isPaintRobotOn) {
         this.isPaintRobotOn = isPaintRobotOn;
     }
 
     /**
      * Returns whether the sensor painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintSensorOn() {
         return this.isPaintSensorOn;
     }
 
     /**
      * Enables or disables the sensor painter.
      *
      * @param isPaintSensorOn  whether to enable or disable the painter.
      */
     public void setPaintSensorOn(boolean isPaintSensorOn) {
         this.isPaintSensorOn = isPaintSensorOn;
     }
 
     /**
      * Returns whether the velocity painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintVelocityVectorOn() {
         return this.isPaintVelocityVectorOn;
     }
 
     /**
      * Enables or disables the sensor painter.
      *
      * @param isPaintVelocityVectorOn whether to enable or disable the painter.
      */
     public void setPaintVelocityVectorOn(boolean isPaintVelocityVectorOn) {
         this.isPaintVelocityVectorOn = isPaintVelocityVectorOn;
     }
 
     /**
     VelocityVector* Returns whether the 'selected' painter is currently enabled.
      *
      * @return
      */
     public boolean isPaintSelectedOn() {
         return isPaintSelectedOn;
     }
 
     /**
      * Enables or disables the 'selected' painter.
      *
      * @param isPaintSelectedOn whether to enable or disable the painter.
      */
     public void setPaintSelectedOn(boolean isPaintSelectedOn) {
         this.isPaintSelectedOn = isPaintSelectedOn;
     }
 }
