 package com.blogspot.nikcode.complex.ui;
 
 import com.blogspot.nikcode.complex.ComplexNumber;
 import com.blogspot.nikcode.complex.MandelbrotSetCalculator;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.util.Set;
 import javax.swing.JPanel;
 
 /**
  *
  * @author nik
  */
 public class MandelbrotSetPanel extends JPanel {
     
     private static final int ZOOM = 2;
     private static final int MAX_ZOOM = 8388608;
     private boolean isFirstPaint = true;
     private int screenCenterX;
     private int screenCenterY;
     private int axesCenterX;
     private int axesCenterY;
     private double realStart;
     private double realEnd;
     private double imaginaryStart;
     private double imaginaryEnd;
     private int currentZoom = 1;
     
     public MandelbrotSetPanel() {
         super();
         this.setBackground(Color.WHITE);
         this.addMouseListener(mouseListener());
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D) g;
         if (isFirstPaint) {
             init();
         }
         g.drawLine(0, axesCenterY, getWidth(), axesCenterY);
         g.drawLine(axesCenterX, 0, axesCenterX, getHeight());
         
         Set<ComplexNumber> mandelbrotSet = MandelbrotSetCalculator.computeSet(realStart, realEnd, imaginaryStart, imaginaryEnd, 
                 0.005 / currentZoom, 0.005 / currentZoom);
         for (ComplexNumber complexNumber : mandelbrotSet) {
             double x = axesCenterX + complexNumber.getReal() * getRealSegmentSize();
             double y = axesCenterY + complexNumber.getImaginary() * getImaginarySegmentSize();
             g2d.draw(new Line2D.Double(x, y, x, y));
         }
     }
     
     private MouseListener mouseListener() {
         return new MouseListener() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 MandelbrotSetPanel thisPanel = MandelbrotSetPanel.this;
                 if (thisPanel.currentZoom == MAX_ZOOM) {
                     return;
                 }
                 int dx = e.getX() - axesCenterX;
                 int dy = e.getY() - axesCenterY;
                 thisPanel.axesCenterX = screenCenterX - dx * ZOOM;
                 thisPanel.axesCenterY = screenCenterY - dy * ZOOM;
                 
                 double real = dx / getRealSegmentSize();
                 double imaginary = dy / getImaginarySegmentSize();
                double newRealSegmentsCount = getRealSegmetCount() / ZOOM;
                 double newImaginarySegmentsCount = getImaginarySegmentCount() / ZOOM;
                 thisPanel.realStart = real - newRealSegmentsCount / 2;
                 thisPanel.realEnd = real + newRealSegmentsCount / 2;
                 thisPanel.imaginaryStart = imaginary - newImaginarySegmentsCount / 2;
                 thisPanel.imaginaryEnd = imaginary + newImaginarySegmentsCount / 2;
                 
                 thisPanel.currentZoom *= ZOOM;
                 thisPanel.repaint();
             }
             @Override
             public void mousePressed(MouseEvent e) {
             }
             @Override
             public void mouseReleased(MouseEvent e) {
             }
             @Override
             public void mouseEntered(MouseEvent e) {
             }
             @Override
             public void mouseExited(MouseEvent e) {
             }
         };
     }
     
     private void init() {
         screenCenterX = this.getWidth() / 2;
         screenCenterY = this.getHeight() / 2;
         axesCenterX = screenCenterX;
         axesCenterY = screenCenterY;
         realStart = -2;
         realEnd = 2;
         double firstIterationSegmentSize = getRealSegmentSize();
         double imaginarySegmentCount = getHeight() / firstIterationSegmentSize;
         imaginaryStart = - (imaginarySegmentCount / 2);
         imaginaryEnd = imaginarySegmentCount / 2;
         
         isFirstPaint = false;
     }
     
    private double getRealSegmetCount() {
         return realEnd - realStart;
     }
     
     private double getRealSegmentSize() {
        return getWidth() / getRealSegmetCount();
     }
     
     private double getImaginarySegmentCount() {
         return imaginaryEnd - imaginaryStart;
     }
     
     private double getImaginarySegmentSize() {
         return getHeight() / getImaginarySegmentCount();
     }
 }
