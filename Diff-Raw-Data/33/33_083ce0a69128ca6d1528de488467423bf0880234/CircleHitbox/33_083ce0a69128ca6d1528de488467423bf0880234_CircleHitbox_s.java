 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 
 
 public class CircleHitbox extends Hitbox {
 	private double radius;
 	
 	public CircleHitbox(double x, double y, double z, double radius, double depth) {
 		super(x, y, z, depth);
 		this.radius = radius;
 	}
 
 	@Override
 	public boolean intersects(Hitbox hb) {
 		if (hb.getShape() == 0) {
 			return Hitbox.intersects(this, (RectangleHitbox) hb);
 		} else if (hb.getShape() == 1) {
 			return Hitbox.intersects((CircleHitbox) hb, this);
 		}
 		return false;
 	}
 
 	@Override
 	public int getShape() {
 		return 1; //Cirlce
 	}
 
 	public double getRadius() {
 		return radius;
 	}
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	@Override
 	public void show(Graphics g, Component observer) {
 		Color before = g.getColor();
 		g.setColor(Color.GREEN);
		g.fillOval((int) (getX() * 100), observer.getSize().height - (int) (getY() * 100 + getZ() * 40), (int)(getRadius()*200), (int)(getRadius()*200));
 		g.setColor(before);
 	}
 }
