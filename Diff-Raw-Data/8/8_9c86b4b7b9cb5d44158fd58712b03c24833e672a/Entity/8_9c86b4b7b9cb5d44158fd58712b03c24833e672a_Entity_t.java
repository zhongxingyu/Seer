 package mta13438;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class Entity {
 
 	private Point pos;
 	private float speed;
 	private float orientation;
 
 	public Entity() {
 		setPos(new Point(0,0,0));
 		setSpeed(1);
 		setOrientation(0);
 	}
 
 	public Entity(Point pos, float speed, float orientation) {
 		setPos(pos);
 		setSpeed(speed);
 		setOrientation(orientation);
 	}
 
 	public Point getPos() {
 		return pos;
 	}
 
 	public void setPos(Point pos) {
 		this.pos = pos;
 	}
 
 	public void setPos(float x, float y, float z) {
 		this.pos.setX(x);
 		this.pos.setY(y);
 		this.pos.setZ(z);
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	public float getOrientation() {
 		return orientation;
 	}
 
 	public void setOrientation(float orientation) {
 		this.orientation = orientation;
 	}
 
 	public String toString() {
 		return "Entity [speed=" + speed + ", orientation=" + orientation + "]";
 	}
 
 	public void turnLeft(float delta) {
 		setOrientation(getOrientation() + (0.1f * delta));
 		if (getOrientation() > 2*Math.PI) {
 			setOrientation(0);
 		}
 	}
 
 	public void turnRight(float delta) {
 		setOrientation(getOrientation() - (0.1f * delta));
 		if (getOrientation() < 0) {
 			setOrientation((float)(2*Math.PI));
 		}
 	}
 
 	public void foward(float delta, float minX, float maxX, float minY, float maxY) {
 		float x,y,z;
 
 		if(getPos().getX() + ((getSpeed() * Math.cos(getOrientation())) * delta) >= minX && getPos().getX() + ((getSpeed() * Math.cos(getOrientation())) * delta) <= maxX){
 			x = (float) ((getSpeed() * Math.cos(getOrientation()) * delta) + getPos().getX());
 		}else x = 0.0f + getPos().getX();
 
 		if(getPos().getY() + ((getSpeed() * Math.sin(getOrientation())) * delta) >= minY && getPos().getY() + ((getSpeed() * Math.sin(getOrientation())) * delta) <= maxY){
 			y = (float) ((getSpeed() * Math.sin(getOrientation()) * delta) + getPos().getY());
 		}else y = 0.0f + getPos().getY();
 
 		z = 0.0f + getPos().getZ();
 
 		setPos(x, y, z);
 	}
 
 	public void backward(float delta, float minX, float maxX, float minY, float maxY) {
 		float x,y,z;
 
 		if(getPos().getX() - (float) ((getSpeed() * Math.cos(getOrientation()) * delta)) >= minX && getPos().getX() - (float) ((getSpeed() * Math.cos(getOrientation()) * delta)) <= maxX){
 			x = getPos().getX() - (float) ((getSpeed() * Math.cos(getOrientation()) * delta));
		}else x = getPos().getX() - 0.0f;
 
 		if(getPos().getY() - (float) ((getSpeed() * Math.sin(getOrientation()) * delta)) >= minY && getPos().getY() - (float) ((getSpeed() * Math.sin(getOrientation()) * delta)) <= maxY){
 			y = getPos().getY() - (float) ((getSpeed() * Math.sin(getOrientation()) * delta));
		}else y =getPos().getY() - 0.0f;
 
		z = getPos().getZ() - 0.0f;
 
 		setPos(x, y, z);
 	}
 
 	public void walkSound() {
 		// Something to play the sound
 	}
 
 	public void draw() {
 		glColor3f(0f, 0f, 1.0f);
 		glPointSize(5);
 		glBegin(GL_POINTS);
 		glVertex2f(getPos().getX(), getPos().getY());
 		glEnd();
 
 		/*
 		 * If a circle is preferred instead of a point. glColor3f(1.0f, 0f, 0f);
 		 * glBegin(GL_LINE_STRIP); float f = 0.0f; for(int i = 0; i<30;i++){
 		 * glVertex3f(pos.getX()+(float)Math.cos(f),
 		 * pos.getY()+(float)Math.sin(f), 0); f = (float) (f +(2*Math.PI/30)); }
 		 * glEnd();
 		 */
 	}
 }
