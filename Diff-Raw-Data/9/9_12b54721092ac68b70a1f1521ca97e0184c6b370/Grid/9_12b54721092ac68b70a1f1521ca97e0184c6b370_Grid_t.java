 package view;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.font.TextLayout;
 import util.Paintable;
 
 
 public class Grid implements Paintable {
     private int canvasHeight;
     private int canvasWidth;
     private int myFrequency;
     private final Font myFont = new Font("Default", Font.PLAIN, 12);
     private final int yOffset = 8;
     private final int xOffset = 3;
 
     public Grid (Dimension bounds, int frequency) {
         canvasHeight = (int) bounds.getHeight();
         canvasWidth = (int) bounds.getWidth();
         myFrequency = frequency;
 
     }
 
     @Override
     public void paint (Graphics2D pen) {
 
         paintVerticalLines(pen);
         paintHorizontalLines(pen);
         pen.setStroke(new BasicStroke());
 
     }
 
     public void paintVerticalLines (Graphics2D pen) {
         int segmentSize = canvasWidth / myFrequency;
         Stroke drawingStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
                                                BasicStroke.JOIN_BEVEL, 0, new float[] { 8 }, 0);
         pen.setStroke(drawingStroke);
         pen.setColor(Color.black);
         for (int i = 1; i < myFrequency + 1; ++i) {
             int myX = 0 + i * segmentSize;
             pen.drawLine(myX, 0, myX, canvasHeight);
             TextLayout text = new TextLayout("" + (myX - canvasWidth / 2),
                                              myFont, pen.getFontRenderContext());
             text.draw(pen, myX + xOffset, canvasHeight - yOffset);
 
         }
 
     }
 
     public void paintHorizontalLines (Graphics2D pen) {
         int segmentSize = canvasHeight / myFrequency;
         Stroke drawingStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
                                                BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
         pen.setStroke(drawingStroke);
         pen.setColor(Color.black);
         for (int i = 0; i < myFrequency + 1; ++i) {
             int myY = 0 + i * segmentSize;
             pen.drawLine(0, myY, canvasWidth, myY);
            TextLayout text = new TextLayout("" + (canvasHeight / 2 - myY),
                                              myFont, pen.getFontRenderContext());
             text.draw(pen, 0 + xOffset, myY + yOffset * 2);
 
         }
 
     }
 
 }
