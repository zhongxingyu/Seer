 package io.github.christiangaertner.moebelplaner.input;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 /**
  *
  * @author Christian
  */
 public class Mouse implements MouseListener, MouseMotionListener {
 
     /**
      * Mouse Koordianten
      */
     protected int x, y;
     
     /**
      * Die vorherige Mouse Position
      */
     protected int preX, preY;
     
    protected int buttonClick = -1;
     protected int buttonHold = -1;
 
 
    public void update() {
        buttonClick = -1;
    }
    
    public boolean leftClick() {
        return (click() == 1);
    }
    
    public int click() {
        return buttonClick;
    }
 
     /**
      * Gibt zurück ob die linke MausTaste gedrückt ist
      *
      * @return
      */
     public boolean leftHold() {
         return (hold() == 1);
     }
 
     public int hold() {
         return buttonHold;
     }
 
     /**
      * X-Koordinate
      *
      * @return
      */
     public int x() {
         return x;
     }
 
     /**
      * Y-Koordinate
      *
      * @return
      */
     public int y() {
         return y;
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
         buttonHold = me.getButton();
        buttonClick = me.getButton();
         preX = x - me.getX();
         preY = y - me.getY();
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
         buttonHold = -1;
        buttonClick = -1;
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
     }
 
     @Override
     public void mouseMoved(MouseEvent me) {
         x = me.getX();
         y = me.getY();
     }
 }
