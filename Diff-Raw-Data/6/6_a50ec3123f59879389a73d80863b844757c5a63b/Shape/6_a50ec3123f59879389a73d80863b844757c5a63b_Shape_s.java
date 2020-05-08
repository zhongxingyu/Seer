 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 
 public abstract class Shape {
  private double x;
  private double y;
  private double dy;
  private double dx;
  private Color color;
  private Rectangle box;
  
 public Shape()
  {
 	//So whats up ? 
  }
  
  public double getX()
  {
 	 return x;
  }
  
  public double getY()
  {
 	 return y;
  }
  
  public double getDX()
  {
 	 return dx;
  }
  
  public double getDY()
  {
 	 return  dy;
  }
  
  public Color getColor()
  {
 	 return color;
  }
  
  public Rectangle getBoundingBox()
  {
 	 return box;
  }
  
  public void setBoundingBox(Rectangle box)
  {
 	 this.box = box;
  }
  
  public void move()
  {
 	 x += dx; 
 	 y += dy;
 	constrain();  
  }
  
  protected void constrain()
  {
 	double x0 = box.x, y0 = box.y;
 	double x1 = x0 + box.width;
 	double y1 = y0 + box.height;
 	
 	// If outside box, change direction
 	if(x < x0) dx = Math.abs(dx);
 	if(x > x1) dx = -Math.abs(dx);
 	if(y < y0) dy = Math.abs(dy);
 	if(y > y1) dy = -Math.abs(dy);
  }
  
  abstract public void paint(Graphics g);
 }
