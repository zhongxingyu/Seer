 package world;
 
 import ihm.GamePanel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.Random;
 
 import world.WorldModel.GameState;
 
 public class SnakeController implements ActionListener {
 	private GamePanel animPanel;
 	private WorldModel wm;
 	private Random r = new Random();
 	
 	public SnakeController(GamePanel animPanel, WorldModel wm) {
 		this.animPanel = animPanel;
 		this.wm        = wm;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		moveSnakeForward();
		animPanel.repaint();
 	}
 	/**
 	 * Computes the snake's next state and update the world model.
 	 */
 	private void moveSnakeForward() {
 		LinkedList<GridPoint> snake = wm.getSnake();
 		GridPoint crt = snake.getFirst();
 		GridPoint next;
 		
 		switch(wm.getNextDirection()) {
 			case UP:
 				next = new GridPoint(crt.getX(), crt.getY()-1);
 				break;
 			case DOWN:
 				next = new GridPoint(crt.getX(), crt.getY()+1);
 				break;
 			case LEFT:
 				next = new GridPoint(crt.getX()-1, crt.getY());
 				break;
 			case RIGHT:
 				next = new GridPoint(crt.getX()+1, crt.getY());
 				break;
 			default:
 				next = new GridPoint(crt.getX(), crt.getY());
 		}
 		
 		if(		next.getX()<0 || next.getX()>=wm.GRID_WIDTH ||	// snake's head out of bounds
 				next.getY()<0 || next.getY()>=wm.GRID_HEIGHT) {
 			wm.setState(GameState.GAME_OVER);
 			return;
 		}
 		
 		if(isSnakeOnPoint(next, snake)) {	// snake eats its own tail
 			wm.setState(GameState.GAME_OVER);
 			return;
 		}
 		
 		snake.addFirst(next);
 		
 		// Insect has been eaten, snake grows longer
 		if(next.equals(wm.getInsect())) {
 			replaceInsect(snake);
 		}
 		else {
 			snake.removeLast();
 		}
 	}
 	/**
 	 * checks if the snake is placed on the given point
 	 * @param p point to check
 	 * @param snake linked list representing the snake
 	 * @return true if snake is on p
 	 */
 	private boolean isSnakeOnPoint(GridPoint p, LinkedList<GridPoint> snake) {
 		for(GridPoint sp : snake) {
 			if(sp.equals(p)) {
 				return true;
 			}
 	  }
 	  return false;
 	}
 	/**
 	 * creates a new insect in a random place.
 	 */
 	public void replaceInsect(LinkedList<GridPoint> snake){
 		GridPoint ni; // the new insect
 		do {
 			ni=new GridPoint(r.nextInt(wm.GRID_WIDTH), r.nextInt(wm.GRID_WIDTH));
 		}while(isSnakeOnPoint(ni, snake));
 		wm.setInsect(ni);
 	}
 }
 
 
 
