 package codepath.snake.objects;
 
 import java.awt.Graphics;
 import java.awt.Point;
 
 public class Snake {
 	// This defines the size of the apple to draw
 	private int drawSize;
 	// The number of squares that make up this snake
 	private int length;
 	// The list of coordinates of the snake's body
 	private Point[] pos;
 	// The direction of the snake (left, right, up, down)
 	private String direction;
 
     // This is the constructor where we need to initialize everything
 	// We need to initialize the direction, pos array, and the length
 	// Also need to setup the initial location of the snake
 	public Snake(int drawSize) {
 
 	}
 
 	// Returns the x coordinate position of the snake's head
 	public int getHeadPosX() {
		return -1;
 	}
 
 	// Returns the y coordinate position of the snake's head
 	public int getHeadPosY() {
		return -1;
 	}
 
 	// Increases the length of the snake's tail by 1
 	// Used to make the snake longer once an apple is eaten
 	// Needs to increment length, and instantiate a new array of a larger size
 	public void growTail() {
 		
 	}
 
 	// Returns the direction the snake is headed
 	// Direction is up, down, left or right
 	public String getDirection() {
		return null;
 	}
 
 	// Updates the direction the snake is headed
 	// Accepts up, down, left or right
 	public void setDirection(String direction) {
 		this.direction = direction;
 	}
 
 	// Returns true if the snake's head has hit the body; false otherwise
 	// This is a game over condition when the head runs into any part of the body
 	// We need to check for a collision
 	public boolean hasHitTail() {
		return false;
 	}
 
 	// Moves the snake position coordinates in the direction he is heading.
 	// Needs to move the tail and adjust the head in his direction.
 	public void move() {
 		// First since a snake slides across the tiles,
 		// move each tile (except the head) so that every tile is now equal
 		// to the previous tile (to move all tiles).
 
 
 		// Based on the current direction, we need to
 		// move the head tile towards the next tile in that direction...
 
 	}
 
 	// Given the canvas, draw the snake on screen using known coordinates
 	public void draw(Graphics canvas) {
 		// Set the draw color
 		// For every tile of the snake's body, draw a rectangle at that position
 	}
 }
