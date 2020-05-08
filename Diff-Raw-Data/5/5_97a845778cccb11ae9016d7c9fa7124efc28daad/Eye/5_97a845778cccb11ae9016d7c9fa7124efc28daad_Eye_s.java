 import processing.core.*;
 
 
 public class Eye {
 
 	/*
 	 * This is a reference to our main class (which extends PApplet) 
 	 * 
 	 * We need this because in Eclipse all our custom classes are
 	 * separate, whereas the processing IDE treats everything as
 	 * an inner class of the main PApplet
 	 * . 
 	 * If we want to use any core processing methods like 
 	 * ellipse(), background() etc. we have to have this, and 
 	 * call parent.ellipse(), parent.background() etc. to use them
 	 * 
 	 * 
 	 */
 	PApplet parent;
 	
 	// The location of the eye
 	int xPos;
 	int yPos;
 	
 	// The size of the various bits
 	float radius;
 	float radius_pupil;
 	float radius_glare;
 	
 	
 	/*
 	 * Eye( PApplet _parent, int _xPos, int _yPos )
 	 * 
 	 * Constructor method sets the x and y positions
 	 */
 	public Eye( PApplet _parent, int _xPos, int _yPos ){
 		
 		parent = _parent;
 		xPos   = _xPos;
 		yPos   = _yPos;
 		
 		// Generate a random value from 30-120 for the radius
 		setRadius( (int) parent.random(30,120) );
 	}
 	
 	
 	/*
 	 * setRadius( int _radius )
 	 * 
 	 * Sets the radius of the eye.
 	 * Also updates the radiuses for the 
 	 * pupil and the glare appropriately 
 	 */
 	public void setRadius( float _radius ){
 		radius       = _radius;
 		radius_pupil = radius/2;
 		radius_glare = radius_pupil/5;
 	}
 	
 	
 	/*
 	 * draw()
 	 * 
 	 * Calculates the position of the pupil based on
 	 * the mouse, then draws all the bits of the eye
 	 */
 	public void draw(){
 		
 		// Calculate the X and Y offsets
 		// from the eye's location (   xPos,   yPos )
 		// to the mouse's location ( mouseX, mouseY )
 		float dx = parent.mouseX-xPos;
 		float dy = parent.mouseY-yPos;
 		
 		// Use these to calculate the angle
 		float angle = PApplet.atan2(dy,dx);
 		
 		// Use the angle to calculate the offsets
 		// from the center to the pupil
		float xOffset = (PApplet.cos(angle)*radius_glare);
		float yOffset = (PApplet.sin(angle)*radius_glare);
 		
 		
 		
 		// Draw the main circle in white
 		parent.fill(255);
 		parent.ellipse(xPos,  yPos, 
 				       radius, radius);		
 		// Draw the pupil in black
 		parent.fill(0);
 		parent.ellipse( xPos+xOffset, yPos+yOffset, 
 				        radius_pupil, radius_pupil);
 		// Draw the glare in white
 		parent.fill(255);
 		parent.ellipse( xPos+xOffset+radius_glare, yPos+yOffset-radius_glare, 
 				        radius_glare, radius_glare );		      
 	}
 	
 }
