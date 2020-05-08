 package net.robinjam.mandelbrot;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 /**
  * Implements the listeners required to allow the user to select a region of a component, and handles drawing the current selection to the screen.
  * 
  * @author James Robinson
  */
 public class SelectionListener implements MouseListener, MouseMotionListener {
     
     private Point start;
     private Point end;
     private Callback callback;
     
     /**
      * Creates a new instance.
      * 
      * @param callback The object that will be notified when the selection is updated.
      */
     public SelectionListener(Callback callback) {
         this.callback = callback;
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
         start = me.getPoint();
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
        Rectangle selection = getSelection();
        if (selection != null && (selection.width != 0 || selection.height != 0))
            callback.selectionCreated(selection);
         start = null;
         end = null;
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
         end = me.getPoint();
         callback.selectionMoved();
     }
     
     /**
      * Draws the current selection.
      * If there is no selection, this method does nothing.
      * 
      * @param g The Graphics2D object to draw the selection with.
      */
     public void paint(Graphics2D g) {
         if (getSelection() != null) {
             g.setColor(new Color(0.0f, 0.2f, 0.8f, 0.5f));
             g.fill(getSelection());
             g.setColor(new Color(0.0f, 0.2f, 0.8f, 0.8f));
             g.draw(getSelection());
         }
     }
     
     private Rectangle getSelection() {
         if (start == null || end == null) return null;
         int x = Math.min(start.x, end.x);
         int y = Math.min(start.y, end.y);
         int width = Math.abs(start.x - end.x);
         int height = Math.abs(start.y - end.y);
         return new Rectangle(x, y, width, height);
     }
     
     /**
      * Defines an interface that selectable objects must implement to be informed when the selection changes.
      */
     public static interface Callback {
 
         /**
          * Called when the selection region is changed, but before the user releases their mouse.
          */
         public void selectionMoved();
         
         /**
          * Called when the user releases their mouse after making a selection.
          * 
          * @param selection The region the user selected.
          */
         public void selectionCreated(Rectangle selection);
         
     }
     
     // Unused listeners
     
     @Override
     public void mouseClicked(MouseEvent me) {}
 
     @Override
     public void mouseEntered(MouseEvent me) {}
 
     @Override
     public void mouseExited(MouseEvent me) {}
 
     @Override
     public void mouseMoved(MouseEvent me) {}
     
 }
