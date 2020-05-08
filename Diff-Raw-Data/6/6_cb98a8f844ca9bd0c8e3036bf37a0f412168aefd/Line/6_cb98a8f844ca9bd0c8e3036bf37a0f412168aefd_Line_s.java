 package shapes;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class Line extends OpenShape {
 
   @Override
   public void draw(Graphics g) {
     Color oldColor = g.getColor();
     g.setColor(getLineColor());
     g.drawLine(getX1(), getY1(), getX2(), getY2());
     g.setColor(oldColor);
   }

  @Override
  public boolean containsLocation(int x, int y) {
    // TODO Auto-generated method stub
    return false;
  }
   
   public String toString() {
     return "Line: \n\tx1 = " + getX1() + "\n\ty1 = " + getY1() + 
         "\n\tx2 = " + getX2() + "\n\ty2 = " + getY2();
   }
 
 
 }
