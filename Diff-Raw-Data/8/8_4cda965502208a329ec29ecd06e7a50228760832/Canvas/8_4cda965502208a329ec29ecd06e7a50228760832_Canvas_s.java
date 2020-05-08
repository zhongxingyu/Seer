 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.JComponent;
 import util.Paintable;
 
 
 /**
  * Creates an area of the screen in which the paintable objects will be drawn.
  * 
  * @author Robert C Duvall
  * @author David Winegar
  * @author Zhen Gou
  */
 public class Canvas extends JComponent {
     // default serialization ID
     private static final long serialVersionUID = 1L;
 
     private Iterator<Paintable> myPaintableIterator;
 
     /**
      * Create a panel so that it knows its size.
      * 
      * @param size size of the viewable area
      */
     public Canvas (Dimension size) {
         // set size (a bit of a pain)
         setPreferredSize(size);
         setSize(size);
         // prepare to receive input
         List<Paintable> emptyList = new ArrayList<Paintable>();
         myPaintableIterator = emptyList.iterator();
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
         while (myPaintableIterator.hasNext()) {
             Paintable paintable = myPaintableIterator.next();
             paintable.paint((Graphics2D) pen);
         }
     }
 
     /**
      * Implements the Observer update function by getting the current sprites and then calling
      * repaint() to paint them.
      * 
      * @param arg0 Obeservable object, in this case a DataSource object
      * @param arg1 Object passed in to observer, unused in this class
      *        TODO recomment
      */
     public void update (Iterator<Paintable> iterator) {
         myPaintableIterator = iterator;
         repaint();
     }
 
 }
