 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 
 import javax.swing.Box;
 import javax.swing.JPanel;
 
 public class CaseViewer extends JPanel {
     private final static int MAGNIFIER_SIZE = 512;
 
     private Case theCase;
 
     private OneView c1;
 
     private OneView c2;
 
     private OneView c3;
 
     private OneView c4;
 
     final protected Cursor hiddenCursor = getToolkit().createCustomCursor(
             new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(),
             "null");
 
     final private Box hBox = Box.createHorizontalBox();
 
     final protected MagnifierWindow magnifierWindow;
 
     public CaseViewer() {
         super();
 
         setBackground(null);
 
         magnifierWindow = new MagnifierWindow(this);
         magnifierWindow.setSize(MAGNIFIER_SIZE, MAGNIFIER_SIZE);
 
         setMinimumSize(new Dimension(800, 600));
         setPreferredSize(new Dimension(800, 600));
 
         setLayout(new BorderLayout());
 
         add(hBox);
 
         addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 if (e.getButton() == 2) {
                     updateMagnifierPosition(e);
                     setCursor(hiddenCursor);
                     magnifierWindow.setVisible(true);
                 }
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (e.getButton() == 2) {
                     setCursor(null);
                     magnifierWindow.setVisible(false);
                 }
             }
         });
 
         addMouseMotionListener(new MouseMotionAdapter() {
             @Override
             public void mouseDragged(MouseEvent e) {
                 if (magnifierWindow.isVisible()) {
                     updateMagnifierPosition(e);
                 }
             }
         });
     }
 
     protected void repaintMagnifier(int x, int y) {
         repaint(x - MAGNIFIER_SIZE / 2, y - MAGNIFIER_SIZE / 2, MAGNIFIER_SIZE,
                 MAGNIFIER_SIZE);
     }
 
     public void setCase(Case c) {
         theCase = c;
 
        c1 = new OneView(theCase.getLeftCC(), "LCC");
        c2 = new OneView(theCase.getRightCC(), "RCC");
        c3 = new OneView(theCase.getLeftML(), "LML");
        c4 = new OneView(theCase.getRightML(), "RML");
 
         hBox.removeAll();
         hBox.add(c1);
         hBox.add(Box.createHorizontalStrut(10));
         hBox.add(c2);
         hBox.add(Box.createHorizontalStrut(10));
         hBox.add(c3);
         hBox.add(Box.createHorizontalStrut(10));
         hBox.add(c4);
 
         revalidate();
         
         magnifierWindow.repaint();
     }
 
     protected void updateMagnifierPosition(MouseEvent e) {
         magnifierWindow.setLocation(e.getX() - MAGNIFIER_SIZE / 2, e.getY()
                 - MAGNIFIER_SIZE / 2);
         magnifierWindow.repaint();
     }
 }
