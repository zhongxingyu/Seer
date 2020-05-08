 package cz.cvut.indepmod.uc.workspace;
 
 import com.jgoodies.binding.value.ValueModel;
 import cz.cvut.indepmod.uc.frames.toolChooser.ToolChooserModel;
 import cz.cvut.indepmod.uc.modelFactory.ucGraphItemModels.UseCaseModel;
 import org.jgraph.graph.BasicMarqueeHandler;
 import org.jgraph.graph.Port;
 import org.jgraph.graph.PortView;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 /**
  * UseCase plugin - SI2/3 school project
  * User: Alena Varkockova
  * User: Viktor Bohuslav Bohdal
  * 
  * Implementation of MarqueeHandler for UC notation.
  */
 public class UCWorkspaceMarqueeHandler extends BasicMarqueeHandler {
 
     private PortView currentPort = null;
     private PortView startingPort = null;
 
     private Point2D startingPoint;
     private Point2D point;
 
     private Point2D previewDrawPosition;    // where was painted?
     private String previewDrawName;         // was preview component painted?
     private Point2D lastMovePoint;
     private Point2D newMovePoint;
     final private String NO_COMPONENT = "NO COMPONENT";
 
     private final UCGraph graph;
 
     final ValueModel selectedToolModel;
 
     final JPopupMenu popupMenu;
 
     public UCWorkspaceMarqueeHandler(final UCGraph graph,
                                       final ValueModel selectedToolModel,
                                       final JPopupMenu popupMenu
     ){
         this.graph = graph;
         this.selectedToolModel = selectedToolModel;
         this.popupMenu = popupMenu;
         previewDrawName = NO_COMPONENT;
     }
 
     /**
      * Decides whether this handler is supposed to be preferred before others.
      *
      * @param e is an instance of MouseEvent that has occurred
      * @return true if this handler is supposed to be preferred before others
      */
     @Override
     public boolean isForceMarqueeEvent(MouseEvent e) {
         //graph.getGraphics().drawImage(actorIcon.getImage(), (int) e.getPoint().getX(), (int) e.getPoint().getY(), null);
 
         //Cursor c = graph.getToolkit().createCustomCursor(actorIcon.getImage(), new Point(0,0), "Actor");
         //graph.setCursor(c);
 
         if(e.isShiftDown()){
             return false; // when one pressed shift and right button, there will be added a new point on the edge
         }
 
         if (SwingUtilities.isRightMouseButton(e) || addingVertex(e)  ){
             return true; //don't use any other handler
         }
 
         currentPort = graph.getSourcePortAt(e.getPoint());
         return (currentPort != null && graph.isPortsVisible()) || super.isForceMarqueeEvent(e);
     }
 
     /**
      * Performs action when mouse button has been pressed.
      *
      * @param e is an instance of MouseEvent that has occurred
      */
     public void mousePressed(final MouseEvent e) {
        previewDrawName = NO_COMPONENT;
        
         if(SwingUtilities.isRightMouseButton(e)){
             // show the popup menu
             popupMenu.show(graph, e.getX(), e.getY());
 
         }  else if(addingVertex(e)){
             // insert new cell
             graph.insert(e.getPoint());
 
         } else if((currentPort != null) && (graph.isPortsVisible())){
             // prepare for edge painting
             startingPort = currentPort;
             startingPoint = graph.toScreen(currentPort.getLocation());
 
         } else {
             super.mousePressed(e);
         }
     }
 
     /**
      * Deals with mouse drag movements.
      *
      * @param e is an instance of MouseEvent that has occurred
      */
     public void mouseDragged(MouseEvent e) {
         if (startingPoint != null) { // starting point has been set <=> painting edge            
             final PortView newPort = graph.getTargetPortAt(e.getPoint());
 
             if (newPort == null || newPort != currentPort){
                 paintConnector(Color.black, graph.getBackground());
                 currentPort = newPort;
 
                 if (currentPort != null){
                     point = graph.toScreen(currentPort.getLocation()); // target port found
                 } else {
                     point = graph.snap(e.getPoint()); // no target port defined, find current point of mouse on graph
                 }
 
                 paintConnector(graph.getBackground(), Color.black);
             }
         }
 
         super.mouseDragged(e);
     }
 
     /**
      * Paint the edge.
      *
      * @param foreground is the line color
      * @param background is the graph background color
      */
     protected void paintConnector(final Color foreground, final Color background){
         final Graphics graphics = graph.getGraphics();
 
         if (graph.isXorEnabled()) {
             graphics.setColor(foreground);
             graphics.setXORMode(background);
             drawConnectorLine(graphics);
 
         } else {
             Rectangle dirty = new Rectangle((int) startPoint.getX(), (int) startPoint.getY(), 1, 1);
 
             if (point != null) {
                 dirty.add(point);
             }
 
             dirty.grow(1, 1);
 
             graph.repaint(dirty);
         }
     }
 
     protected void drawSystemBorder(Graphics g, Point2D point)
     {
         g.drawRect((int) point.getX(), (int) point.getY(), 400, 800);
     }
     protected void drawUseCase(Graphics g, Point2D point)
     {
         g.drawArc((int) point.getX(), (int) point.getY(), UseCaseModel.USE_CASE_WIDTH, UseCaseModel.USE_CASE_HEIGHT, 0, 360);
     }
 
     protected void drawActor(Graphics g, Point2D point)
     {
         int actorH = 120, actorW = 40;
         int borderH = 0;
         int borderW = 0;
         
         int x = (int) point.getX();
         int y = (int) point.getY();
 
         g.drawLine(x + borderW / 2, y + actorH - 20, x + borderW / 2 + actorW / 2, y + actorH - actorH / 3);
         g.drawLine(x + borderW / 2 + actorW, y + actorH - 20, x + borderW / 2 + actorW / 2, y + actorH - actorH / 3);
 
         g.drawLine(x + borderW / 2 + actorW / 2, y + actorH - actorH / 3, x + borderW / 2 + actorW / 2, y + actorH / 4);
         g.drawLine(x + borderW / 2, y + actorH / 3, x + borderW / 2 + actorW, y + actorH / 3);
         g.drawArc(x + borderW / 2 + actorW / 4, y + 0 , actorW / 2, actorH / 4, 0, 360);
     }
     protected void paintDiagramComponent(String tool, final Color foreground, final Color background, Point2D point)
     {
         final Graphics graphics = graph.getGraphics();
 
         if (graph.isXorEnabled()) {
             graphics.setColor(foreground);
             graphics.setXORMode(background);
             //String tool = selectedToolModel.getValue().toString();
             if(tool.compareTo("ADD_ACTOR") == 0) {
                 drawActor(graphics, point);
             } else if(tool.compareTo("ADD_USE_CASE") == 0) {
                 drawUseCase(graphics, point);
             } else if(tool.compareTo("ADD_SYSTEM_BORDER") == 0) {
                 drawSystemBorder(graphics, point);
             }
         }
     }
 
     /**
      * Draws the temporary line from source port to the current location of mouse.
      *
      * @param g is the Graphics object
      */
     protected void drawConnectorLine(Graphics g) {
         if (startingPort != null && startingPoint != null && point != null) {
             g.drawLine((int) startingPoint.getX(), (int) startingPoint.getY(), (int) point.getX(), (int) point.getY());
         }
     }
 
     /**
      * Defines actions for mouse release event. 
      *
      * @param e is an instance of MouseEvent that has occurred
      */
     public void mouseReleased(MouseEvent e) {
         
         if (e != null && currentPort != null && startingPort != null /* startingPort != currentPort allow self-loops */ ) {
             // connect source and target vertexes
             graph.connectVertexes((Port) startingPort.getCell(), (Port) currentPort.getCell());
             e.consume();
         }
 
         graph.repaint();
 
         startingPort = null;
         currentPort = null;
         startingPoint = null;
         point = null;
 
         super.mouseReleased(e);
                       
     }
 
     /**
      * Show CROSSHAIR_CURSOR cursor when the mouse is over any portview.
      * @param event is the MouseEvent that has occurred
      */
     public void mouseMoved(MouseEvent event) {
 
         if ((event != null) && (graph.isPortsVisible()) && (graph.getSourcePortAt(event.getPoint()) != null)){
             // isPortsVisible(), ports are visible only when the AddEdge tool is selected
             
             graph.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
             event.consume(); // do not let BasicGraphUI make any other changes
 
         } else {
             super.mouseMoved(event);
         }
 
         newMovePoint = event.getPoint();
         if(lastMovePoint != null && newMovePoint != lastMovePoint)
         {
             if(previewDrawName.compareTo(NO_COMPONENT) != 0)
             {
                 paintDiagramComponent(previewDrawName, Color.black, graph.getBackground(), previewDrawPosition);
                 previewDrawName = NO_COMPONENT;
             }
             String toolString = selectedToolModel.getValue().toString();
             if(toolString.compareTo("ADD_ACTOR") == 0 ||
                toolString.compareTo("ADD_USE_CASE") == 0 ||
                toolString.compareTo("ADD_SYSTEM_BORDER") == 0)
             {
                 paintDiagramComponent(toolString, graph.getBackground(), Color.black, event.getPoint());
                 previewDrawName = toolString;
                 previewDrawPosition = event.getPoint();
             }
         }
         lastMovePoint = newMovePoint;
     }
 
     /**
      * Decides whether the user is just adding new vertex or no.
      *
      * @param e is the current mouse event
      * @return true if user is adding new vertex, false otherwise
      */
     private boolean addingVertex(final MouseEvent e){
         final ToolChooserModel.Tool tool;
         try{
             tool = (ToolChooserModel.Tool) selectedToolModel.getValue();
         } catch (ClassCastException exception){
             return false;
         }
 
         final boolean addingTool;
         switch (tool){
             case ADD_ACTOR:
             case ADD_USE_CASE:
             case ADD_STEP:
             case ADD_SCENARIO:
             case ADD_SYSTEM_BORDER:
                 addingTool = true;
                 break;
 
             default:
                 addingTool = false;
         }
 
         return (SwingUtilities.isLeftMouseButton(e) && addingTool);
     }
 }
