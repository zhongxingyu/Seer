 package vooga.fighter.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
 
 import javax.swing.JComponent;
 import vooga.fighter.controller.ViewDataSource;
 import vooga.fighter.view.CanvasLayout;
 
 
 /**
  * Creates an area of the screen in which the game will be drawn that supports:
  * <UL>
  * <LI>animation via the Timer
  * <LI>mouse input via the MouseListener
  * <LI>keyboard input via the KeyListener
  * </UL>
  * 
  * @author Robert C Duvall
  * @author Wayne You
  */
 public class Canvas extends JComponent {
     // default serialization ID
     private static final long serialVersionUID = 1L;
 
     // game to be animated
     private ViewDataSource myViewDataSource;
     // current layout of the game
     private CanvasLayout myLayout = null;
 
     /**
      * Create a panel so that it knows its size
      */
     public Canvas (Dimension size) {
         // set size (a bit of a pain)
         setPreferredSize(size);
         setSize(size);
         // prepare to receive input
         setFocusable(true);
         requestFocus();
     }
     
     /**
      * Sets the data source from the controller.
      * @param data
      */
     public void setViewDataSource (ViewDataSource data) {
         myViewDataSource = data;
     }
     
     /**
      * Sets up the layout of the view. Null implies no layout.
      * @param layout
      */
     public void setLayout (CanvasLayout layout) {
         myLayout = layout;
     }
     
     /**
      * Calls java's repaint method.
      */
     public void paint() {
         repaint();
     }
 
     /**
      * Paint the contents of the canvas.
      * 
      * Never called by you directly, instead called by Java runtime
      * when area of screen covered by this container needs to be
      * displayed (i.e., creation, uncovering, change in status)
      * 
      * @param pen used to paint shape on the screen
      */
     @Override
     public void paintComponent (Graphics pen) {
         pen.setColor(Color.WHITE);
         pen.fillRect(0, 0, getSize().width, getSize().height);
         // If there is no defined layout, simply paint things at the locations they are given.
         if (myLayout == null) {
             for (int i = 0; i < myViewDataSource.ObjectNumber(); i++) {
                 myViewDataSource.getPaintable(i).paint((Graphics2D) pen,
                                                  myViewDataSource.getLocation(i),
                                                  myViewDataSource.getSize(i));
             }
         }
         else {
             myLayout.paintComponents((Graphics2D)pen, myViewDataSource, this.getSize());
         }
     }
 
 }
