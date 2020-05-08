 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.*;
 import java.io.*;
 
 public class Pendulum {
     private static int length = 140;
     private double theta;
     private double vTheta;
     private double mTheta; // the amount theta changes based on magnetism
 
     private Color bobColor;
 
     private int bobWidth  = 20;
     private int bobHeight = 40;
 
     public Pendulum() {
         theta  = 0;
         vTheta = 0;
         mTheta = 0;
         calcColor();
     }
 
     public Pendulum(double t) {
         theta  = t;
         vTheta = 0;
         mTheta = 0;
         calcColor();
     }
 
     // gives the bob a random color
     private void calcColor() {
         float red   = (float)(Math.random());
         float green = (float)(Math.random());
         float blue  = (float)(Math.random());
         bobColor = new Color(red, green, blue);
     }
 
     public void step() {
         vTheta += mTheta + (-Params.gravity * Math.cos(theta));
         theta  += vTheta / Params.timestep;
     }
 
     public void calcNewValues(ArrayList<Pendulum> pendulums) {
         // calculate mTheta for the next step
         mTheta = 0;
         for (Pendulum p : pendulums) {
             if (p.getTheta() != theta) {
                 int direction;
                 double angle;
                 if (theta > p.getTheta()) {
                     direction =  -1;
                     angle = p.getTheta() - theta;
                 } else {
                     direction = 1;
                     angle = theta - p.getTheta();
                 }
 
                //double angle = Math.min(p.getTheta() - theta, theta - p.getTheta());
                double d = Math.tan(angle) * length;
 
                 mTheta += direction * Params.magnetism * (1/(d*d)) * Math.sin(angle);
             }
         }
     }
 
     public void paint(Graphics g) {
         g.drawLine(Params.xPivot, Params.yPivot, xEnd(), yEnd());
         drawBob(g);
     }
 
     private void drawBob(Graphics g) {
         int a = bobWidth  / 2;
         int b = length - bobHeight / 2;
 
         double ax = Math.sqrt(a*a + b*b) * Math.cos(-(theta + Math.atan2(a, b)));
         double ay = Math.sqrt(a*a + b*b) * Math.sin(-(theta + Math.atan2(a, b)));
 
         double bx = Math.sqrt(a*a + b*b) * Math.cos(-(theta - Math.atan2(a, b)));
         double by = Math.sqrt(a*a + b*b) * Math.sin(-(theta - Math.atan2(a, b)));
 
         b = length + bobHeight/2;
 
         double cx = Math.sqrt(a*a + b*b) * Math.cos(-(theta - Math.atan2(a, b)));
         double cy = Math.sqrt(a*a + b*b) * Math.sin(-(theta - Math.atan2(a, b)));
 
         double dx = Math.sqrt(a*a + b*b) * Math.cos(-(theta + Math.atan2(a, b)));
         double dy = Math.sqrt(a*a + b*b) * Math.sin(-(theta + Math.atan2(a, b)));
 
         int[] xs = {xPos(ax), xPos(bx), xPos(cx), xPos(dx)};
         int[] ys = {yPos(ay), yPos(by), yPos(cy), yPos(dy)};
 
         
         g.setColor(bobColor);
         g.fillPolygon(xs, ys, 4);
 
         // reset the graphics color back
         g.setColor(Color.BLACK);
 
     }
 
     // reposition x to be correctly placed for our pivot
     private int xPos(double x) {
         return Params.xPivot + (int)x;
     }
 
     // reposition y to be correctly placed for our pivot
     private int yPos(double y) {
         return Params.yPivot + (int)y;
     }
 
     // calculate end of stick
    private int xEnd() {
         return (int) (Params.xPivot + length * Math.cos(theta));
     }
 
     // calculate end of stick
    private int yEnd() {
         return (int) (Params.yPivot - length * Math.sin(theta));
     }
 
     public double getTheta() { return theta; } 
 }
