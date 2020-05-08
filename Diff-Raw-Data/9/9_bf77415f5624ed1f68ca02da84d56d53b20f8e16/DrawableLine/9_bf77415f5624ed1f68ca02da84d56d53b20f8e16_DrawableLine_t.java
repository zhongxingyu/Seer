 package balle.main.drawable;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import balle.world.Line;
 import balle.world.Scaler;
 
 public class DrawableLine implements Drawable {
 
     private Line  line;
     private Color colour;
 
     @Override
     public void draw(Graphics g, Scaler scaler) {
        g.setColor(colour);
         g.drawLine(scaler.m2PX(line.getA().getX()), scaler.m2PY(line.getA().getY()),
                 scaler.m2PX(line.getB().getX()), scaler.m2PY(line.getB().getY()));
     }
 
     public Line getLine() {
         return line;
     }
 
     public void setLine(Line line) {
         this.line = line;
     }
 
     public Color getColour() {
         return colour;
     }
 
     public void setColour(Color colour) {
         this.colour = colour;
     }
 
     public DrawableLine(Line line, Color colour) {
         super();
         this.line = line;
         this.colour = colour;
     }
 
 }
