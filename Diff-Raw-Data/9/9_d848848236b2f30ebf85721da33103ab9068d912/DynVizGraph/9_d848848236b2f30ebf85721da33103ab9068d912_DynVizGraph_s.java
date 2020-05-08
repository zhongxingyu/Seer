 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import javax.swing.JApplet;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 /**
  *
  * @author gmcwhirt
  */
 public class DynVizGraph extends JApplet {
 
     private int chartWidth = 210;  // The width of the canvas panel
     private int chartHeight = 210; // The height of the canvas panel
 
     private CanvasPanel BRChart;  // JPanel canvas for graphics drawing
     private CanvasPanel DtRChart;
     private CanvasPanel CtRChart;
 
     private int payoffA;
     private int payoffB;
     private int payoffC;
     private int payoffD;
 
     public DynVizGraph(){
         Container c = getContentPane();
 
         setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
         c.setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
 
         try {
             String pAS = getParameter("A");
             payoffA = Integer.parseInt(pAS);
         } catch (NumberFormatException e) {
             payoffA = 0;
         } catch (NullPointerException e) {
             payoffA = 0;
         }
 
         try {
             String pBS = getParameter("B");
             payoffB = Integer.parseInt(pBS);
         } catch (NumberFormatException e) {
             payoffB = 0;
         } catch (NullPointerException e) {
             payoffB = 0;
         }
 
         try {
             String pCS = getParameter("C");
             payoffC = Integer.parseInt(pCS);
         } catch (NumberFormatException e) {
             payoffC = 0;
         } catch (NullPointerException e) {
             payoffC = 0;
         }
 
         try {
             String pDS = getParameter("D");
             payoffD = Integer.parseInt(pDS);
         } catch (NumberFormatException e) {
             payoffD = 0;
         } catch (NullPointerException e) {
             payoffD = 0;
         }
 
         System.out.println(payoffA);
         System.out.println(payoffB);
         System.out.println(payoffC);
         System.out.println(payoffD);
 
         JPanel panel = new JPanel(new GridLayout(1,3));
 
         BRChart = new CanvasPanel();
         DtRChart = new CanvasPanel();
         CtRChart = new CanvasPanel();
 
         panel.setBorder(new LineBorder(Color.LIGHT_GRAY));
         panel.setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
 
         BRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         DtRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         CtRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         panel.add(BRChart);
         panel.add(DtRChart);
         panel.add(CtRChart);
 
         c.add(panel);
 
         Thread BRGen = new Thread(new BRGraphGenerator(payoffA, payoffB, payoffC, payoffD));
         BRGen.start();
     }
     
     private void BRGraphInfo(Line[] lines, Line[] arrows){
         //BRChart
         BRChart.setLines(lines);
         BRChart.setArrows(arrows);
         System.out.println("Set lines");
         //BRChart.repaint();
     }
     
     private void DtRGraphInfo(BufferedImage bi){
         
     }
     
     private void CtRGraphInfo(BufferedImage bi){
         
     }
 
     class Line {
         private int[] coords;
         private Color color;
         private Color color2;
 
         public Line(int c1, int c2, int c3, int c4, Color c, Color cp){
             coords = new int[4];
             coords[0] = c1;
             coords[1] = c2;
             coords[2] = c3;
             coords[3] = c4;
             color = c;
             color2 = cp;
         }
 
         public Line(int c1, int c2, int c3, int c4, Color c){
             coords = new int[4];
             coords[0] = c1;
             coords[1] = c2;
             coords[2] = c3;
             coords[3] = c4;
             color = c;
             color2 = Color.BLACK;
         }
 
         public Line(int c1, int c2, int c3, int c4){
             coords = new int[4];
             coords[0] = c1;
             coords[1] = c2;
             coords[2] = c3;
             coords[3] = c4;
             color = Color.BLACK;
             color2 = Color.BLACK;
         }
 
         public Color getColor(){
             return color;
         }
 
         public Color getColor2(){
             return color2;
         }
 
         public int[] getCoords(){
             return coords;
         }
 
         @Override
         public String toString(){
             return coords.toString();
         }
     }
 
     class BRGraphGenerator implements Runnable {
         private int A, B, C, D;
 
         public BRGraphGenerator(int Ap, int Bp, int Cp, int Dp){
             A = Ap;
             B = Bp;
             C = Cp;
             D = Dp;
         }
 
         @Override
         public void run(){
             Line[] lines;
             Line[] arrows;
 
             //draw stuff
             float qlimf = (float)C / (float)(A + C);
             System.out.println(qlimf);
             int qlim = (int) Math.floor(200f * (1f - qlimf));
             System.out.println(qlim);
 
             lines = new Line[106];
             int index = 0;
             int index2 = 0;
             float brlimx;
             float brlimy;
             int lrespx;
             int lrespy;
 
             if (A + C > 0){
                 lrespx = 0;
                 if (qlimf <= 1f && qlimf >= 0f){
                     lines[index++] = new Line(200, qlim, 200, 0, Color.RED);
                     lines[index++] = new Line(0, qlim, 0, 200, Color.RED);
                     lines[index++] = new Line(0, qlim, 200, qlim, Color.RED);
                     brlimy = qlimf;
                 } else if (qlimf > 1f) {
                     //always play the other
                     lines[index++] = new Line(0, 0, 0, 200, Color.RED);
                     brlimy = 1f;
                 } else {
                     //always play this
                     lines[index++] = new Line(200, 0, 200, 200, Color.RED);
                     brlimy = 0f;
                 }
             } else if (A + C < 0){
                 lrespx = 200;
                 if (qlimf <= 1f && qlimf >= 0f){
                     lines[index++] = new Line(200, qlim, 200, 200, Color.RED);
                     lines[index++] = new Line(0, qlim, 0, 0, Color.RED);
                     lines[index++] = new Line(0, qlim, 200, qlim, Color.RED);
                     brlimy = qlimf;
                 } else if (qlimf > 1f) {
                     //always play this
                     lines[index++] = new Line(200, 0, 200, 200, Color.RED);
                     brlimy = 0f;
                 } else {
                     //always play the other
                     lines[index++] = new Line(0, 0, 0, 200, Color.RED);
                     brlimy = 1f;
                 }
             } else if (0 >= C) {
                 //play this
                 lines[index++] = new Line(200, 0, 200, 200, Color.RED);
                 lrespx = 0;
                 brlimy = 0f;
             } else {
                 //play other
                 lines[index++] = new Line(0, 0, 0, 200, Color.RED);
                 lrespx = 0;
                 brlimy = 1f;
             }
 
             float plimf = (float)D / (float)(B + D);
             int plim = (int) Math.floor(200f * (1 - plimf));
 
             if (B + D > 0){
                 lrespy = 0;
                 if (plimf >= 0f && plimf <= 1f){
                     lines[index++] = new Line(plim, 200, 0, 200, Color.BLUE);
                     lines[index++] = new Line(plim, 0, 200, 0, Color.BLUE);
                     lines[index++] = new Line(plim, 0, plim, 200, Color.BLUE);
                     brlimx = plimf;
                 } else if (plimf > 1f){
                     //play the other
                     lines[index++] = new Line(0, 200, 200, 0, Color.BLUE);
                     brlimx = 1f;
                 } else {
                     //play this
                     lines[index++] = new Line(0, 200, 200, 200, Color.BLUE);
                     brlimx = 0f;
                 }
             } else if (B + D < 0){
                 lrespy = 200;
                 if (plimf >= 0f && plimf <= 1f){
                     lines[index++] = new Line(plim, 200, 200, 200, Color.BLUE);
                     lines[index++] = new Line(plim, 0, 0, 0, Color.BLUE);
                     lines[index++] = new Line(plim, 0, plim, 200, Color.BLUE);
                     brlimx = plimf;
                 } else if (plimf > 1f) {
                     //play this
                     lines[index++] = new Line(0, 200, 200, 200, Color.BLUE);
                     brlimx = 0f;
                 } else {
                     //play the other
                     lines[index++] = new Line(0, 200, 200, 0, Color.BLUE);
                     brlimx = 1f;
                 }
             } else if (0 >= D) {
                 //play this
                 lines[index++] = new Line(0, 200, 200, 200, Color.BLUE);
                 lrespy = 0;
                 brlimx = 0f;
             } else {
                 //play other
                 lines[index++] = new Line(0, 200, 200, 0, Color.BLUE);
                 lrespy = 0;
                 brlimx = 1f;
             }
 
             arrows = new Line[100];
             float step = 0.1f;
             int fmx;
             int fmy;
             int tox;
             int toy;
             for (float i = 0f; i < 1f; i += step){
                 for (float j = 0f; j < 1f; j += step){
                     //response at (i, j)
                     fmx = (int) Math.floor( 200 * (1f - i) );
                     fmy = (int) Math.floor( 200 * (1f - j) );
 
                     if (j < brlimy){
                         tox = lrespx;
                     } else if (j > brlimy) {
                         tox = Math.abs(lrespx - 200);
                     } else {
                         tox = fmx;
                     }
 
                     if (i < brlimx){
                         toy = lrespy;
                     } else if (i > brlimx) {
                         toy = Math.abs(lrespy - 200);
                     } else {
                         toy = fmy;
                     }
 
                     arrows[index2++] = new Line(fmx, fmy, tox, toy, Color.GREEN, Color.BLACK);
                     lines[index++] = new Line(fmx, fmy, fmx, fmy, Color.BLACK);
                 }
             }
 
             BRGraphInfo(lines, arrows);
         }
     }
 
     // Class to provide a drawing surface for graphics display
     class CanvasPanel extends JPanel {
         static final int BR = 1;
         static final int DtR = 2;
         static final int CtR = 3;
 
         private Line[] _lines;
         private Line[] _arrows;
         private int _typ;
 
         // Constructor
         public CanvasPanel(){
             setBackground(Color.white);
             setPreferredSize( new Dimension( chartWidth, chartHeight ) );  // Set its size
             setVisible( true );   // Make the window visible
         }
 
         public void setLines(Line[] lines){
             _lines = lines;
         }
 
         public void setArrows(Line[] arrows){
             _arrows = arrows;
         }
 
         @Override
         public void paintComponent ( Graphics g ){
             super.paintComponent( g );
 
             Graphics2D g2d = (Graphics2D) g;
 
             g2d.setColor(Color.LIGHT_GRAY);
             g2d.drawRect(5, 5, 200, 200);
 
             Color color;
             Color color2;
             int[] coords;
 
             if (_arrows != null && _arrows.length > 0) {
                 for (int i = 0; i < _arrows.length; i++){
                     if (_arrows[i] != null){
                         coords = _arrows[i].getCoords();
                         color = _arrows[i].getColor();
                         color2 = _arrows[i].getColor2();
 
                         drawArrow(g2d, coords[0] + 5, coords[1] + 5, coords[2] + 5, coords[3] + 5, color, color2);
                     }
                 }
             }
 
             if (_lines != null && _lines.length > 0) {
                 System.out.println("We have lines!");
 
                 for (int i = 0; i < _lines.length; i++){
                     if (_lines[i] != null){
                         coords = _lines[i].getCoords();
                         color = _lines[i].getColor();
                         g2d.setColor(color);
                         g2d.drawLine(coords[0] + 5, coords[1] + 5, coords[2] + 5, coords[3] + 5);
                     }
                 }
             }
 
 
         }
 
         /**
          * Draws an arrow on the given Graphics2D context
          * @param g The Graphics2D context to draw on
          * @param x The x location of the "tail" of the arrow
          * @param y The y location of the "tail" of the arrow
          * @param xx The x location of the "head" of the arrow
          * @param yy The y location of the "head" of the arrow
          */
         private void drawArrow( Graphics2D g, int x, int y, int xx, int yy, Color lcolor, Color acolor )
         {
             float arrowWidth = 5.0f ;
             float theta = 0.423f ;
             int[] xPoints = new int[ 3 ] ;
             int[] yPoints = new int[ 3 ] ;
             float[] vecLine = new float[ 2 ] ;
             float[] vecLeft = new float[ 2 ] ;
             float fLength;
             float th;
             float ta;
             float baseX, baseY ;
 
             xPoints[ 0 ] = xx ;
             yPoints[ 0 ] = yy ;
 
             // build the line vector
             vecLine[ 0 ] = (float)xPoints[ 0 ] - x ;
             vecLine[ 1 ] = (float)yPoints[ 0 ] - y ;
 
             // build the arrow base vector - normal to the line
             vecLeft[ 0 ] = -vecLine[ 1 ] ;
             vecLeft[ 1 ] = vecLine[ 0 ] ;
 
             // setup length parameters
             fLength = (float)Math.sqrt( vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1] ) ;
            th = arrowWidth / ( 2.0f * fLength ) ;
            ta = arrowWidth / ( 2.0f * ( (float)Math.tan( theta ) / 2.0f ) * fLength ) ;
 
             // find the base of the arrow
             baseX = ( (float)xPoints[ 0 ] - ta * vecLine[0]);
             baseY = ( (float)yPoints[ 0 ] - ta * vecLine[1]);
 
             // build the points on the sides of the arrow
             xPoints[ 1 ] = (int)( baseX + th * vecLeft[0] );
             yPoints[ 1 ] = (int)( baseY + th * vecLeft[1] );
             xPoints[ 2 ] = (int)( baseX - th * vecLeft[0] );
             yPoints[ 2 ] = (int)( baseY - th * vecLeft[1] );
 
             g.setColor(lcolor);
             g.drawLine( x, y, (int)baseX, (int)baseY ) ;
 
             g.setColor(acolor);
             g.fillPolygon( xPoints, yPoints, 3 ) ;
         }
      } // End canvasPanel class
 
 }
