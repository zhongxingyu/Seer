 package mygame;
 
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 
 public class CircleF {
 	
 	private Vector2f itsPosition;
 	private float itsRadius;
 	
 	public CircleF() {
 		itsPosition = new Vector2f(0, 0);
 		itsRadius = 0;
 	}
 	public CircleF(Vector2f position, float radius) {
 		itsPosition = position;
 		itsRadius = radius;
 	}
 	
 	public Vector2f getPosition() {
 		return itsPosition;
 	}
 	public float getRadius() {
 		return itsRadius;
 	}
 	
 	public void setPosition(Vector2f value) {
 		itsPosition = value;
 	}
 	public void setRadius(float value) {
 		itsRadius = value;
 	}
 	
 	public boolean collidesWithCircle(CircleF target) {
 		return collidesWithCircle(target, true);
 	}
 	public boolean collidesWithCircle(CircleF target, boolean resolve) {
		Vector2f displacement = itsPosition.subtract(target.getPosition()).normalize();
 		float distance = itsPosition.distance(target.getPosition());
		float overshoot = distance - (itsRadius + target.getRadius());
 		if (overshoot < 0) {
 			if (resolve) {
				itsPosition = target.getPosition().add(displacement.mult(itsRadius + target.getRadius()));
 			}
 			return true;
 		}
 		return false;
 	}
 	public boolean collidesWithRect(RectF target) {
 		return collidesWithRect(target, true);
 	}
 	public boolean collidesWithRect(RectF target, boolean resolve) {
 		float x = itsPosition.getX();
 		float y = itsPosition.getY();
 		float left = x - itsRadius;
 		float right = x + itsRadius;
 		float bottom = y - itsRadius;
 		float top = y + itsRadius;
 		if ((y > target.getBottom()) && (y < target.getTop())) {
 			if ((left < target.getRight()) && (left > target.getLeft())) {
 				if (resolve) {
 					itsPosition.setX(itsPosition.getX() + (target.getRight() - left));
 				}
 				return true;
 			}
 			if ((right < target.getRight()) && (right > target.getLeft())) {
 				if (resolve) {
 					itsPosition.setX(itsPosition.getX() + (target.getLeft() - right));
 				}
 				return true;
 			}
 		}
 		if ((x > target.getLeft()) && (x < target.getRight())) {
 			if ((bottom < target.getTop()) && (bottom > target.getLeft())) {
 				if (resolve) {
 					itsPosition.setY(itsPosition.getY() + (target.getTop() - bottom));
 				}
 				return true;
 			}
 			if ((top < target.getTop()) && (top > target.getLeft())) {
 				if (resolve) {
 					itsPosition.setY(itsPosition.getY() + (target.getBottom() - top));
 				}
 				return true;
 			}
 		}
 		if (itsPosition.distance(target.getTopLeft()) < itsRadius)
 			return true;
 		if (itsPosition.distance(target.getTopRight()) < itsRadius)
 			return true;
 		if (itsPosition.distance(target.getBottomLeft()) < itsRadius)
 			return true;
 		if (itsPosition.distance(target.getBottomRight()) < itsRadius)
 			return true;
 		return false;
 	}
 	
 }
