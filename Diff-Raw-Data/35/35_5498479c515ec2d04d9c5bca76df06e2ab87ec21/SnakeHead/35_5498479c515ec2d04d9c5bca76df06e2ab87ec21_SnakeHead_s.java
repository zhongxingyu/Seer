 package org.crockeo.snake.game.components.snake;
 
 import org.crockeo.snake.game.components.Tiles;
 import org.crockeo.snake.game.components.Tile;
 
 /*
  * Class: SnakeHead
  * 
  * Purpose:
  *  The head of the snake
  */
 
 public class SnakeHead extends Tile {
 	/*
 	 * 0 - up
 	 * 1 - down
 	 * 2 - left
 	 * 3 - right
 	 */
 	private int direction;
 	
 	public SnakeHead(int x, int y) { super(Tiles.SNAKE_HEAD, x, y); }
 	
 	public void move() {
		if (direction == 0)
 			moveUp();
		else if (direction == 1)
 			moveDown();
		else if (direction == 2)
 			moveLeft();
		else if (direction == 3)
 			moveRight();
 	}
 	
 	// Accessors
	public int getDirection() { return direction; }
 	
 	// Mutators
 	public void setDirection(int direction) {
		if (((this.direction == 0 && direction == 1) || (this.direction == 1 && direction == 0))
				|| ((this.direction == 2 && direction == 3) || (this.direction == 3 && direction == 2)))
 			return;
 		
 		this.direction = direction;
 	}
 }
