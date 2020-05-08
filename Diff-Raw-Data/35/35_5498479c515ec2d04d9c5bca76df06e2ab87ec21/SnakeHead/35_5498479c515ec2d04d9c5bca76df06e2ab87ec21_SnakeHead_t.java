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
	private int realDirection;
 	private int direction;
 	
 	public SnakeHead(int x, int y) { super(Tiles.SNAKE_HEAD, x, y); }
 	
 	public void move() {
		realDirection = direction;
		
		if (realDirection == 0)
 			moveUp();
		else if (realDirection == 1)
 			moveDown();
		else if (realDirection == 2)
 			moveLeft();
		else if (realDirection == 3)
 			moveRight();
 	}
 	
 	// Accessors
	public int getDirection() { return realDirection; }
 	
 	// Mutators
 	public void setDirection(int direction) {
		if (((this.realDirection == 0 && direction == 1) || (this.realDirection == 1 && direction == 0))
				|| ((this.realDirection == 2 && direction == 3) || (this.realDirection == 3 && direction == 2)))
 			return;
 		
 		this.direction = direction;
 	}
 }
