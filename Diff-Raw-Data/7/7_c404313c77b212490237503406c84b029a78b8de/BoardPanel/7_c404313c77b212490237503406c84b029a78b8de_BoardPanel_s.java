 package org.adjudju.gui.panels;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: alex
  * Date: 14/06/13
  * Time: 00:01
  * To change this template use File | Settings | File Templates.
  */
 public class BoardPanel extends JPanel implements Observer {
     private SquarePanel[][] squares = new SquarePanel[8][8];
     private static final int SIZE = 8;
     private static final Color GREEN = new Color(0, 100, 0);
     private static final Color WHITE = Color.WHITE;
     private SquarePanel selected = null;
 
     public BoardPanel() {
         setLayout(new GridLayout(8, 8));
         setBorder(BorderFactory.createEtchedBorder());
         initComponents();
     }
 
     private void initComponents() {
         for(int i = 0; i < SIZE; i++) {
             for(int j = 0; j < SIZE; j++) {
 
                 if(i % 2 == 0) {
                     if(j % 2 == 0) {
                         squares[i][j] = new SquarePanel(this, Color.WHITE);
                     } else {
                         squares[i][j] = new SquarePanel(this, Color.GREEN);
                     }
                 } else {
                     if(j % 2 == 0) {
                         squares[i][j] = new SquarePanel(this, Color.GREEN);
                     } else {
                         squares[i][j] = new SquarePanel(this, Color.WHITE);
                     }
                 }
 
                 add(squares[i][j]);
             }
         }
     }
 
     @Override
     public void update(Observable o, Object arg) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     public void setSelected(SquarePanel squarePanel) {
         if(selected == null) {
             selected = squarePanel;
         } else {
             selected.resetBackground();
 
            if (selected == squarePanel) {
                selected = null;
            } else {
                 selected = squarePanel;
             }
         }
 
         repaint();
     }
 }
