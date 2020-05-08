 package world;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
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
 		t = new Timer(wm.getStepDelay(), this); t.stop();
 		
 		LinkedList<GridPoint> snake = new LinkedList<GridPoint>();
 		snake.addFirst(new GridPoint(wm.GRID_WIDTH/2, wm.GRID_HEIGHT/2));
 	    snake.addFirst(new GridPoint(wm.GRID_WIDTH/2-1, wm.GRID_HEIGHT/2));
 	    snake.addFirst(new GridPoint(wm.GRID_WIDTH/2-2, wm.GRID_HEIGHT/2));
 	    snake.addFirst(new GridPoint(wm.GRID_WIDTH/2-2, wm.GRID_HEIGHT/2-1));
 	    snake.addFirst(new GridPoint(wm.GRID_WIDTH/2-2, wm.GRID_HEIGHT/2-2));
 	    
 	    ArrayList<Insect> insects = new ArrayList<Insect>();
 	    for(int i=0; i<wm.INSECTS; i++)
 	    	insects.add(getRandomInsect(snake, insects));
 	    
	    wm.setInsects(insects);	// Update model and view is notified
	    wm.setSnake(snake);
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
 		ArrayList<Insect> insects = wm.getInsects();
 		GridPoint crt = snake.getFirst();
 		GridPoint next;
 		// compute next point
 		switch(wm.getNextDirectionAndAcceptDirectionChanges()) {
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
 		if(isPointOnSnake(next, snake)) {
 			wm.setState(GameState.GAME_OVER);
 			return;
 		}
 		// Add a from "piece" to the snake
 		snake.addFirst(next);
 		// check if insect has been eaten
 		boolean eaten = false;
 		for(Insect ins : insects) {
 			if(ins.equals(next)) {
 				wm.setCntEaten(wm.getCntEaten()+1);
 				wm.setScore(wm.getScore()+ins.getScore());
 				insects.remove(ins);
 				insects.add(getRandomInsect(snake, insects));
 				eaten=true;
 				break;
 			}			
 		}
 		if(!eaten)	// snake didn't eat, doesn't grow longer
 			snake.removeLast();
 		// Update model --> notify view
 		wm.setSnake(snake);
 	}
 	/**
 	 * creates a new insect in a free random place.
 	 */
 	private Insect getRandomInsect(LinkedList<GridPoint> snake, ArrayList<Insect> insects){
 		Insect ins; // the new insect
 		do {
 			ins=new Insect(	r.nextInt(wm.GRID_WIDTH),
 							r.nextInt(wm.GRID_WIDTH),
 							wm.INSECT_SCORES[r.nextInt(wm.INSECT_SCORES.length)]);
 		}while(isPointOnSnake(ins, snake) || isPointOnInsect(ins, insects));
 		return ins;
 	}
 	/**
 	 * check if the snake is on the given point
 	 * @return true if snake is on the point
 	 */
 	private boolean isPointOnSnake(GridPoint p, LinkedList<GridPoint> snake) {
 		for(GridPoint sp : snake) {
 			if(p.equals(sp)) return true;
 		}
 		return false;
 	}
 	/**
 	 * chek if the point is on insect
 	 * @return true if point is on insect
 	 */
 	private boolean isPointOnInsect(GridPoint p, ArrayList<Insect> insects) {
 		for(GridPoint ip : insects) {
 			if(p.equals(ip)) return true;
 		}
 		return false;
 	}
 	/**
 	 * notified when the configs change.
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
 	 * starts / stops the simulation.
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
 
 
 
