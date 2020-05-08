 package de.engineapp.controls;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 import de.engine.environment.Scene;
 import de.engine.math.*;
 import de.engine.objects.*;
 import de.engineapp.*;
 import de.engineapp.PresentationModel.*;
 import de.engineapp.util.*;
 import de.engineapp.visual.*;
 import de.engineapp.visual.Circle;
 import de.engineapp.visual.Ground;
 import de.engineapp.visual.Square;
 import de.engineapp.visual.decor.*;
 
 import static de.engineapp.Constants.*;
 
 
 public class Canvas extends JComponent implements MouseListener, MouseMotionListener, SceneListener, KeyListener, MouseWheelListener, StorageListener
 {
     private static final long serialVersionUID = -5320479580417617983L;
     
     
     private PresentationModel pModel;
     
     // secondary buffer, which will copied to the front buffer
     // after all draw operations are finished
     private BufferedImage frontBuffer = null;
     private BufferedImage backBuffer  = null;
     
     // stores the mouse offset while dragging
     private Point mouseOffset;
     
     // this objects causes a little delay till an object can be dragged
     // this should avoid drags, if an object should only be selected
     Task dragDelay = null;
     
     /**
      * Create a new Canvas to represent objects.
      * 
      * @param model - Global PresentationModel for the application
      */
     public Canvas(PresentationModel model)
     {
         pModel = model;
         model.setCanvas(this);
         
         this.addComponentListener(new ComponentAdapter()
         {
             // resize back buffer
             @Override
             public void componentResized(ComponentEvent e)
             {
                 frontBuffer = new BufferedImage(Canvas.this.getWidth(), Canvas.this.getHeight(), BufferedImage.TYPE_INT_RGB);
                 backBuffer  = new BufferedImage(Canvas.this.getWidth(), Canvas.this.getHeight(), BufferedImage.TYPE_INT_RGB);
                 
                 // fire resize
                 pModel.resizeCanvas(Canvas.this.getWidth(), Canvas.this.getHeight());
                 
                 // fire repaint
                 pModel.fireRepaint();
             }
         });
         
         // implement mouse (motion) listener to make navigating throw the scene
         // and manipulating objects possible
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
         this.addKeyListener(this);
         this.addMouseWheelListener(this);
         pModel.addSceneListener(this);
         pModel.addStorageListener(this);
     }
     
     
     /**
      * Cleares the buffer related to the Graphics object.
      * 
      * @param g - Graphics object
      */
     public void clearBuffer(Graphics2D g)
     {
         g.setBackground(this.getBackground());
         
         g.clearRect(0, 0, this.getWidth(), this.getHeight());
     }
     
     
     /**
      * Get the Graphics object, to render on the backbuffer.
      * The backbuffer will automatically cleared.
      */
     public Graphics2D getGraphics()
     {
         Graphics2D g = (Graphics2D) backBuffer.getGraphics();
         
         clearBuffer(g);
         
         return g;
     }
     
     /**
      * Swaps back- and frontbuffer.
      * 
      * After each rerendering, the buffers should be swapped 
      * to make the new buffer valid.
      */
     public void switchBuffers()
     {
         BufferedImage tmp = frontBuffer;
         frontBuffer = backBuffer;
         backBuffer = tmp;
     }
     
     
     // if a repaint event is fired, just paint the frontbuffer
     @Override
     public void paint(Graphics g)
     {
         g.drawImage(frontBuffer, 0, 0, null);
     }
     
     
     @Override
     public void mouseClicked(MouseEvent e) { }
     
     
     @Override
     public void mouseEntered(MouseEvent e) { }
     
     
     @Override
     public void mouseExited(MouseEvent e) { }
     
     
     @Override
     public void mousePressed(MouseEvent e)
     {
         
         // get in-scene cursor position
         Vector cursor = pModel.toTransformedVector(e.getPoint());
         boolean hasSelection = pModel.getSelectedObject() != null;
         
         if (SwingUtilities.isLeftMouseButton(e))
         {
             // start a timer, to make moving objects possible after a short delay
             dragDelay = new Task(200);
             dragDelay.start();
         }
         // right mouse button initializes the move the view offset
         else if (SwingUtilities.isRightMouseButton(e))
         {
             mouseOffset = new Point(e.getPoint());
         }
         
         // show radius if shift is pressed
         if (hasSelection && GuiUtil.isLeftButton(e, false, true, false))
         {
             Range range = new Range(pModel.getSelectedObject(), "radius");
             ((IDecorable) pModel.getSelectedObject()).putDecor(DECOR_RANGE, range);
         }
         // show angle if alt is pressed
         else if (hasSelection && (GuiUtil.isLeftButton(e, false, false, true) || 
                 GuiUtil.isLeftButton(e, true, true, false)))
         {
             AngleViewer angleViewer = new AngleViewer(pModel.getSelectedObject());
             ((IDecorable) pModel.getSelectedObject()).putDecor(DECOR_ANGLE_VIEWER, angleViewer);
         }
         // if ctrl is pressed modify the velocity vector
         else if (GuiUtil.isLeftButton(e, true, false, false))
         {
             // set new velocity
             pModel.getSelectedObject().velocity = Util.minus(cursor, pModel.getSelectedObject().getPosition());
             
             pModel.fireObjectUpdated(pModel.getSelectedObject());
         }
         // if no modifier key is pressed either selected an object or create new objects
         else if (GuiUtil.isLeftButton(e, false, false, false))
         {
             ObjectProperties object = pModel.getScene().getObjectFromPoint(cursor.getX(), cursor.getY());
             
             // create new objects if
             // 1. there is no object under the cursor
             // 2. the engine is not running
             // 3. the mode for placing objects is enabled
             // 4. currently the application is not in playback mode
             if (object == null && !pModel.getPhysicsState().isRunning() && pModel.getProperty(PRP_OBJECT_MODE) != null
                     && !pModel.getProperty(PRP_MODE).equals(CMD_PLAYBACK_MODE))
             {
                 createObject(pModel.getProperty(PRP_OBJECT_MODE), e.getPoint());
             }
             else
             {
                 pModel.setSelectedObject(object);
             }
         }
         // if middle mouse button is pressed, show the object radius
         else if (hasSelection && SwingUtilities.isMiddleMouseButton(e))
         {
             Range range = new Range(pModel.getSelectedObject(), "radius");
             ((IDecorable) pModel.getSelectedObject()).putDecor(DECOR_RANGE, range);
         }
         
         pModel.fireRepaint();
     }
     
     
     // unload everything that is not needed anymore as long no mouse button is pressed
     @Override
     public void mouseReleased(MouseEvent e)
     {
         if (SwingUtilities.isLeftMouseButton(e))
         {
             dragDelay = null;
             
             if (pModel.getSelectedObject() != null)
             {
                 ((IDecorable) pModel.getSelectedObject()).removeDecor(DECOR_RANGE);
                 ((IDecorable) pModel.getSelectedObject()).removeDecor(DECOR_ANGLE_VIEWER);
                 pModel.fireRepaint();
             }
         }
         else if (SwingUtilities.isMiddleMouseButton(e) && pModel.getSelectedObject() != null)
         {
             ((IDecorable) pModel.getSelectedObject()).removeDecor(DECOR_RANGE);
             pModel.fireRepaint();
         }
     }
     
     
     @Override
     public void mouseDragged(MouseEvent e)
     {
         // get in-scene cursor position
         Vector cursor = pModel.toTransformedVector(e.getPoint());
         boolean hasSelection = pModel.getSelectedObject() != null;
         
         // if ctrl is pressed modify the velocity vector (except in playback mode)
         if (hasSelection && GuiUtil.isLeftButton(e, true, false, false) && 
                 !pModel.getProperty(PRP_MODE).equals(CMD_PLAYBACK_MODE))
         {
             // set new velocity
             pModel.getSelectedObject().velocity = Util.minus(cursor, pModel.getSelectedObject().getPosition());
             pModel.fireObjectUpdated(pModel.getSelectedObject());
             pModel.fireRepaint();
         }
         // if alt is pressed modify the object's angle (except in playback mode)
         else if (hasSelection && (GuiUtil.isLeftButton(e, false, false, true) || 
                 GuiUtil.isLeftButton(e, true, true, false)) && 
                 !pModel.getProperty(PRP_MODE).equals(CMD_PLAYBACK_MODE))
         {
             double x = cursor.getX() - pModel.getSelectedObject().getX();
             double y = cursor.getY() - pModel.getSelectedObject().getY();
             
             double newAngle = Math.atan2(y, x);
             pModel.getSelectedObject().setRotationAngle(newAngle);
             pModel.fireRepaint();
         }
         // middle mouse button or left mouse + shift button modifies the object's radius
         // (except in playback mode)
         else if (hasSelection && (GuiUtil.isLeftButton(e, false, true, false) ||
                 SwingUtilities.isMiddleMouseButton(e)) && !pModel.getProperty(PRP_MODE).equals(CMD_PLAYBACK_MODE))
         {
             pModel.getSelectedObject().setRadius(Util.distance(pModel.getSelectedObject().getPosition(), cursor));
             pModel.fireObjectUpdated(pModel.getSelectedObject());
             pModel.fireRepaint();
         }
         // right mouse button moves the view offset
         else if (SwingUtilities.isRightMouseButton(e))
         {
             pModel.moveViewOffset(e.getX() - mouseOffset.x, e.getY() - mouseOffset.y);
             mouseOffset.x = e.getPoint().x;
             mouseOffset.y = e.getPoint().y;
             
             // refresh canvas
             pModel.fireRepaint();
         }
         
     }
     
     
     @Override
     public void mouseMoved(MouseEvent e) { }
     
     
     // show decors if an object is added
     @Override
     public void objectAdded(ObjectProperties object)
     {
         if (pModel.isState(STG_SHOW_ARROWS_ALWAYS))
         {
             attachVelocityArrow(object);
         }
         
         if (pModel.isState(STG_DEBUG))
         {
             Box aabb = new Box(object, "getAABB");
             ((IDecorable) object).putDecor("aabb", aabb);
         }
     }
     
     @Override
     public void objectRemoved(ObjectProperties object) { }
     
     
     @Override
     public void groundAdded(de.engine.objects.Ground ground) { }
     
     @Override
     public void groundRemoved(de.engine.objects.Ground ground) { }
     
     
     // show decors if an object is selected
     @Override
     public void objectSelected(ObjectProperties object)
     {
         // get the decor functionality of on object
         IDecorable decorableObject = (IDecorable) object;
         
         // attach velocity arrow
         Arrow arrow = new Arrow(object, "velocity");
         decorableObject.putDecor(DECOR_ARROW, arrow);
         
         if (pModel.isState(STG_DEBUG))
         {
             // attach calculated intersection point with the ground
             Coordinate coord = new Coordinate( object, "last_intersection" );
             decorableObject.putDecor(DECOR_COORDINATE, coord);
             
             // attach calculated closest point to the ground
             Coordinate closestPoint = new Coordinate( object, "closest_point" );
             closestPoint.setColor(Color.BLUE);
             decorableObject.putDecor(DECOR_CLOSEST_POINT, closestPoint);
         }
         
         // show object radius
         Range selection = new Range(object, "radius");
         selection.setBorder(new Color(180, 120, 20));
         
         // remove world velocity arrow, because it has already another one
         // which can be modified by the user
         if (pModel.isState(STG_SHOW_ARROWS_ALWAYS))
         {
             decorableObject.removeDecor(DECOR_MULTIPLE_ARROW);
         }
     }
     
     // remove all the decor stuff if an object is deselected
     @Override
     public void objectDeselected(ObjectProperties object)
     {
         IDecorable decorableObject = (IDecorable) object;
         
         decorableObject.removeDecor(DECOR_ARROW);
         decorableObject.removeDecor(DECOR_COORDINATE);
         decorableObject.removeDecor(DECOR_CLOSEST_POINT);
         
         if (pModel.isState(STG_SHOW_ARROWS_ALWAYS))
         {
             attachVelocityArrow(object);
         }
     }
     
     
     @Override
     public void keyPressed(KeyEvent e) { }
     
     @Override
     public void keyReleased(KeyEvent e) { }
     
     @Override
     public void keyTyped(KeyEvent e)
     {
         // remove an object when the delete key is pressed (127 = Delete Key)
         if (e.getKeyChar() == 127 && pModel.getSelectedObject() != null)
         {
             pModel.removedObject(pModel.getSelectedObject());
             pModel.fireRepaint();
         }
     }
     
     
     // the user can use the mouse wheel to zoom within the scene
     @Override
     public void mouseWheelMoved(MouseWheelEvent e)
     {
         int wheel;
         // zooming isn't linear so there are some calculations needed
         if (pModel.getZoom() < 2.0)
         {
             wheel = (int) (pModel.getZoom() * 10.0 - 10.0);
         }
         else
         {
             wheel = (int) (pModel.getZoom() + 8.0);
         }
         
         
         wheel -= e.getWheelRotation();
         
         if (wheel < -9)
         {
             // set min (0.1x zoom)
             wheel = -9;
         }
         else if (wheel > 18)
         {
             // set max (10x zoom)
             wheel = 18;
         }
         
         
         if (wheel < 10)
         {
             pModel.setZoom(wheel / 10.0 + 1.0, e.getPoint());
         }
         else
         {
             pModel.setZoom(wheel - 8.0, e.getPoint());
         }
         
         pModel.fireRepaint();
     }
     
     
     @Override
     public void objectUpdated(ObjectProperties object) { }
     
     @Override
     public void sceneUpdated(Scene scene) { }
     
     
     /**
      * Creates new objects and adds them to the current scene
      * 
      * @param id - defines the object type
      * @param location - position where the object will be created
      */
     public void createObject(String id, Point location)
     {
         Vector sceneLocation = pModel.toTransformedVector(location);
         
         switch (id)
         {
             case OBJ_CIRCLE:
                 Circle circle = new Circle(pModel, sceneLocation, 8);
                 circle.setMass(10);
                 pModel.addObject( circle );
                 
                 break;
                 
             case OBJ_SQUARE:
                 Square square = new Square(pModel, sceneLocation, 12);
                 square.setMass(10);
                 pModel.addObject( square );
                 
                 break;
                 
             case OBJ_GROUND:
                 pModel.setGround(new Ground(pModel, Ground.DOWNHILL, (int) sceneLocation.getY()));
                 break;
                 
             default:
                 return;
         }
         
         pModel.fireRepaint();
     }
     
     
     // this is a helper function to create the global velocity arrows
     // because this code will be invoked multiple times
     private void attachVelocityArrow(ObjectProperties object)
     {
         Arrow arrow = new Arrow(object, "velocity");
         arrow.setColor(new Color(128, 128, 255));
         arrow.setBorder(new Color(0, 0, 255));
         ((IDecorable) object).putDecor(DECOR_MULTIPLE_ARROW, arrow);
     }
     
     
     @Override
     public void stateChanged(String id, boolean value)
     {
         // wether the global velocity arrow should be attached or not
         if (id.equals(STG_SHOW_ARROWS_ALWAYS))
         {
             for (ObjectProperties object : pModel.getScene().getObjects())
             {
                 if (value)
                 {
                     if (object != pModel.getSelectedObject())
                     {
                         attachVelocityArrow(object);
                     }
                 }
                 else
                 {
                     ((IDecorable) object).removeDecor(DECOR_MULTIPLE_ARROW);
                 }
             }
             pModel.fireRepaint();
         }
     }
     
     @Override
     public void propertyChanged(String id, String value) { }
 }
