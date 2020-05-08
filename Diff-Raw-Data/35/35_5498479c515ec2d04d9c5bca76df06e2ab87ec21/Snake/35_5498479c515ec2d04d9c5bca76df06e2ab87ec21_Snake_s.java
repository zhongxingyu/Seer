 package org.crockeo.snake.game.components.snake;
 
 import org.crockeo.snake.game.components.Tile;
 import org.crockeo.snake.game.World;
 import org.crockeo.snake.Config;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 
 import java.util.ArrayList;
 
 /*
  * Class: Snake
  * 
  * Purpose:
  *  Snake container class
  */
 
 public class Snake {
 	private SnakeHead head;
 	private ArrayList<SnakeBody> body;
 	
 	private boolean shouldAdd;
 	
 	public Snake(int x, int y) {
 		head = new SnakeHead(x, y);
 		
 		body = new ArrayList<>();
 		body.add(new SnakeBody(x, y + 1, head));
 		
 		shouldAdd = false;
 	}
 	
 	// Updating the snake
 	public boolean update(World w) {
 		boolean ret = false;
 	
 		head.move();
 		if ((head.getX() < 0 || head.getX() > w.getWidth() - 1)
 			|| (head.getY() < 0 || head.getY() > w.getHeight() - 2))
 			ret = true; // Player has lost
 		
 		for (SnakeBody b: body)
			if (head.on(b))
				ret = true; // Player has lost
 		
 		for (SnakeBody b: body)
			b.move();
 		
 		if (shouldAdd)
 			actuallyAddPart();
 		
 		
 		return ret; // Player has NOT lost! :D
 	}
 	
 	// Drawing the snake
 	public void render(Graphics g, World w) {
 		head.render(g, w);
 		for (SnakeBody b: body)
 			b.render(g, w);
 	}
 	
 	// Handling input
 	public void handleInput(Input i) {
 		if (i.isKeyDown(Config.upKey))
 			head.setDirection(0);
 		else if (i.isKeyDown(Config.downKey))
 			head.setDirection(1);
 		else if (i.isKeyDown(Config.leftKey))
 			head.setDirection(2);
 		else if (i.isKeyDown(Config.rightKey))
 			head.setDirection(3);
 	}
 	
 	// Adding 
 	public void addPart() { shouldAdd = true; }
 	private void actuallyAddPart() {
 		body.add(new SnakeBody(body.get(body.size() - 1).getLastX(),
 				   body.get(body.size() - 1).getLastY(),
 				   body.get(body.size() - 1)));
 		shouldAdd = false;
 	}
 	
 	// Accessors
 	public SnakeHead getHead() { return head; }
 	public SnakeBody[] getBody() { return (SnakeBody[])body.toArray(); }
 	public SnakeBody getBodyPart(int index) { return body.get(index); }
 	
 	// Mutators
 	public Tile getTile(int x, int y) {
 		if (head.getX() == x && head.getY() == y)
 			return head;
 		
 		for (SnakeBody b: body)
 			if (b.getX() == x && b.getY() == y)
 				return b;
 		
 		return null;
 	}
 	
 	public void setPosition(int x, int y) {
 		int dX = head.getX() - x;
 		int dY = head.getY() - y;
 		
 		head.move(dX, dY);
 		for (SnakeBody b: body)
 			b.move(dX, dY);
 	}
 	
 	public void setX(int x) { setPosition(x, head.getY()); }
 	public void setY(int y) { setPosition(head.getX(), y); }
 }
