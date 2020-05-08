 public class Character {
     public enum Directions {NORTH,SOUTH,EAST,WEST,NORTHWEST,NORTHEAST,SOUTHWEST,SOUTHEAST};
 
     public boolean isDrawn;
 
     protected float xPosition, yPosition;
     protected boolean alive;
     protected float[] color;//temporary until we have graphics, just color of square
     protected float xVelocity, yVelocity;
     
     private static float[] defaultColor = {1,1,1};
     
     public Character(float x, float y, float[] c) {
 	xPosition = x;
 	yPosition = y;
 	alive = true;
 	isDrawn = false;
 	color = c;
 	xVelocity = 5;
 	yVelocity = 5;
     }
 
     public Character(float x, float y) {
 	this(x,y, defaultColor);
     }
 
     public Character() {
 	this(100,100);
     }
 
     public boolean isAlive() {
 	return alive;
     }
 
     public void kill() {
 	alive = false;
     }
 
     public void move(float x, float y) {
 	xPosition += x;
 	yPosition += y;
     }
     
    public float[] getPosition(){
	float[] result = {xPosition, yPosition};
 	return result;
     }
     public void move(Directions d) {
 	switch(d) {
 	case NORTH:
 	    yPosition += yVelocity;
 	    break;
 	case WEST:
 	    xPosition -= xVelocity;
 	    break;
 	case EAST:
 	    xPosition += xVelocity;
 	    break;
 	case SOUTH:
 	    yPosition -= yVelocity;
 	    break;
 	case NORTHEAST:
 	    xPosition += xVelocity;
 	    yPosition += yVelocity;
 	    break;
 	case NORTHWEST:
 	    xPosition -= xVelocity;
 	    yPosition += yVelocity;
 	    break;
 	case SOUTHEAST:
 	    xPosition += xVelocity;
 	    yPosition -= yVelocity;
 	    break;
 	case SOUTHWEST:
 	    xPosition -= xVelocity;
 	    yPosition -= yVelocity;
 	    break;
 	}
     }
 
     public float getXPos() {
 	return xPosition;
     }
     public float getYPos() {
 	return yPosition;
     }
     public float getXVel() {
 	return xVelocity;
     }
     public float getYVel() {
 	return yVelocity;
     }
     public float[] getColor() {
 	return color;
     }
 
 }
