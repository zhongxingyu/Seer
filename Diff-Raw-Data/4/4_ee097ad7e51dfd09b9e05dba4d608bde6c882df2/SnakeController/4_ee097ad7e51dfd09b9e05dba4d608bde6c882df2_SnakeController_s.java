 package world;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 
 import javax.swing.Timer;
 
 import world.WorldModel.GameState;
 
 public class SnakeController implements Observer, ActionListener {
 	private WorldModel wm;
 	private Timer t;
 	private Random r = new Random();
 	/**
 	 * Constructor method
 	 * @param animPanel
 	 * @param wm the model
 	 */
 	public SnakeController(WorldModel wm) {
 		this.wm = wm;
 		this.wm.addObserver(this);
		System.out.println(wm.getStepDelay());
 		t = new Timer(wm.getStepDelay(), this);
 		t.stop();
 	}
 	/**
 	 * Moves the snake forward. Action fired by the timer.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		moveSnakeForward();
 	}
 	/**
 	 * Computes the snake's next state and update the world model.
 	 */
 	private void moveSnakeForward() {
 		LinkedList<GridPoint> snake = wm.getSnake();
 		GridPoint crt = snake.getFirst();
 		GridPoint next;
 		// compute next point
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
 		// snake's head out of bounds
 		if(	next.getX()<0 || next.getX()>=wm.GRID_WIDTH ||
 			next.getY()<0 || next.getY()>=wm.GRID_HEIGHT) {
 			wm.setState(GameState.GAME_OVER);
 			return;
 		}
 		// snake eats its own tail
 		if(isSnakeOnPoint(next, snake)) {
 			wm.setState(GameState.GAME_OVER);
 			return;
 		}
 		// Add a from "piece" to the snake
 		snake.addFirst(next);
 		// Insect has been eaten, snake grows longer
 		if(next.equals(wm.getInsect())) {
 			wm.setCntEaten(wm.getCntEaten()+1);
 			wm.setScore(wm.getScore()+1);
 			replaceInsect(snake);
 		}
 		else {
 			snake.removeLast();
 		}
 		// Update model --> notify view
 		wm.setSnake(snake);
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
 	/**
 	 * Notified when the cofigs change.
 	 */
 	@Override
 	public void update(Observable o, Object event) {
 	    WorldModel.WorldEvents what = (WorldModel.WorldEvents) event;
 	    switch (what){
 	      case CONFIG_CHANGED:
 	        configChanged();
 	        break;
 	    }
 	}
 	/**
 	 * Starts / stops the simulation.
 	 */
 	private void configChanged() {
 		switch(wm.getState()) {
 		case PLAY:
 			t.setDelay(wm.getStepDelay());
 			t.start();
 			break;
 		default:
 			t.stop();
 			break;
 		}
 	}
 }
 
 
 
