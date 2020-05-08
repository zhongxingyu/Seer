 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graficador;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Mateo
  */
 public class Graficador extends JFrame {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         Graficador g = new Graficador((int)Float.parseFloat(args[0]), (int)Float.parseFloat(args[args.length-2]));
         int i = 0;
         while (i < args.length) {
             int x = (int)Float.parseFloat(args[i]);
             int y = (int)Float.parseFloat(args[i+1]);
             g.addPoint(x, y, false);
             i += 2;
         }
         try {
             g.drawGraph();
         } catch (Exception e) {
         }
     }
     
     private static int width = 900, height = 600;
     private BufferedImage graph;
     private ArrayList<Point> points, intercepts;
     private int maxY = 0, minY = 0, minX, maxX, domain, range;
     private boolean done = false;
 
     public Graficador(int m, int n) {
         this.domain = n - m + 1; //+1 because interval is [m, n] inclusive
         minX = m;
         maxX = n;
         this.setTitle("Eq2Solver");
         this.setVisible(true);
         this.setResizable(false);
         this.setSize(width, height);
         points = new ArrayList<>(domain);
         intercepts = new ArrayList<>(2);
 
         graph = new BufferedImage(getWidth(), getHeight(), BufferedImage.BITMASK);
     }
 
     public void addPoint(int x, int y, boolean intercept) {
         if (done) {
             return;
         }
 
         Point p = new Point(x, y);
 
         if (y > maxY) {
             maxY = y;
         } else {
             if (y < minY) {
                 minY = y;
             }
         }
 
         if (intercept) {
             intercepts.add(p);
         } else {
             points.add(p);
         }
     }
 
     public void drawGraph() throws Exception {
         if (points.size() + intercepts.size() == 0) {
             throw new Exception("No points have been add to draw");
         }
 
         done = true;
         range = maxY - minY + 1;
         int dx = getWidth() / (domain + 2); //+2 for margin of the graph
         int dy = getHeight() / (range + 2);
 
         Graphics2D g2d = (Graphics2D) graph.getGraphics();
 
         //Regular points
 
         g2d.setColor(Color.black);
         for (int i = 0; i < points.size(); i++) {
             Point p = points.get(i);
             if(i < points.size()-1){
                 Point p2 = points.get(i+1);
                if(p2.x > p.x){
                    g2d.drawLine((p.x - minX + 1) * dx, height - ((p.y - minY + 1) * dy), (p2.x - minX + 1) * dx, height - ((p2.y - minY + 1) * dy));
                }
             }
            g2d.drawString("(" + p.x + ", " + p.y + ")", (p.x - minX + 1) * dx, height - ((p.y - minY + 1) * dy)); //+1 for margin
         }
 
         //Intercepts
         g2d.setColor(Color.red);
         for (int i = 0; i < intercepts.size(); i++) {
             Point p = intercepts.get(i);
             g2d.drawString("(" + p.x + ", " + p.y + ")", (p.x - minX + 1) * dx, height - ((p.y - minY + 1) * dy)); //+1 for margin
         }
 
         g2d.dispose();
         repaint();
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         ((Graphics2D) g).drawImage(graph, 0, 0, getWidth(), getHeight(), this);
         g.dispose();
     }
 }
