 package Mastermind;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 // Custom button to take input for Mastermind game
 public class InputButton extends JComponent implements MouseListener {
     // properties of button
     private int  currColor;
     private Color colors[];
     private boolean mousePressed;
     private boolean editable;
     private boolean visible;
 
     // constructor - initialize values
     public InputButton() {
         this.addMouseListener(this);
 
         currColor = 0;
         mousePressed = false;
         editable = false;
         visible = true;
         colors = new Color[7];
         colors[0] = Color.BLACK;
         colors[1] = Color.RED;
         colors[2] = Color.BLUE;
         colors[3] = Color.GREEN;
         colors[4] = Color.YELLOW;
         colors[5] = Color.ORANGE;
         colors[6] = Color.MAGENTA;
     }
 
     // redraw button
     public void paint(Graphics g) {
         // if mouse was clicked, go to next color
         if (mousePressed) {
             if (currColor == 6) {
                 // and if at end, return to beginning of color list
                 currColor = 1;
             } else {
                 currColor++;
             }
         }
         // if visible, show color change
         if (visible) {
             g.setColor(colors[currColor]);
         }
         // then redraw
         if (currColor == 0 || !visible) {
             g.drawOval(0,0,30,30);;
         } else {
             g.fillOval(0,0,30,30);
         }
     }
 
     // get current color
     public int getCurrColor() {
         return currColor;
     }
 
     //set color and then redraw
     public void setCurrColor(int newColor) {
         currColor = newColor;
         repaint();
     }
 
     //set edit state
     public void setEdit(boolean edit) {
         editable = edit;
     }
 
    // set visible state and redraw
     public void setVis(boolean vis) {
         visible = vis;
        repaint();
     }
 
     // set default size of component
     public Dimension getMinimumSize() {
         return new Dimension(30,30);
     }
     public Dimension getPreferredSize() {
         return new Dimension(30,30);
     }
 
     // if mouse pressed and editable, then redraw
     public void mousePressed(MouseEvent e) {
         if (editable) {
             mousePressed = true;
             repaint();
         }
     }
 
     // keep track of mouse being released
     public void mouseReleased(MouseEvent e) {
         mousePressed = false;
     }
 
     // required when implementing mouselistener
     public void mouseClicked(MouseEvent e) {}
     public void mouseEntered(MouseEvent e) {}
     public void mouseExited(MouseEvent e) {}
 }
