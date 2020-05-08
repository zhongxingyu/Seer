 package standard;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 
 import visual.GameVisualizer;
 import framework2.Drawable;
 import framework2.GameState;
 import framework2.Location;
 import framework2.Player;
 import framework2.Unit;
 import framework2.Updateable;
 import framework2.World;
 
 import static standard.Constants.*;
 
 public class UserPlayer implements Player, Updateable, Drawable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7098865388074696915L;
 	private GameVisualizer visual;
 	private GameState state;
 	
 	private Location[] queue = new Location[1];
 	
 	private int x = 0,y = 0;
 	
 	private boolean endInitPhase = false;
 	
 	private ReentrantLock lock = new ReentrantLock();
 	private Condition condition = lock.newCondition();
 	
 	public UserPlayer(GameVisualizer visual){
 		this.visual = visual;
 		state = GameState.UnitPlacement;
 	}
 
 	@Override
 	public Location placeUnit(Unit unit) {
 		lock.lock();
 		while (queue[0] == null) {
 			try {
 				condition.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Location result = queue[0];
 		queue[0] = null;
 		lock.unlock();
 		return result;
 	}
 
 	@Override
 	public Location[] switchUnits() {
 		lock.lock();
 		if(state.equals(GameState.UnitPlacement)){
 			state = GameState.UnitSwitching;
 			queue = new Location[2];
 		}
 		while(queue[0] == null || queue[1] == null){
 			try {
 				condition.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Location[] result = {queue[0], queue[1]};
 		queue[0] = null;
 		queue[1] = null;
 		lock.unlock();
 		
 		return result;
 	}
 
 	@Override
 	public boolean endInitPhase() {
 		return endInitPhase;
 	}
 
 	@Override
 	public void updateWorld(World world) {
 		visual.setWorld(world);
 	}
 
 	@Override
 	public Location[] getMove(World world) {
 		state = GameState.GamePhase;
 		while(queue[0] == null || queue[1] == null){
 			try {
 				condition.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Location[] result = {queue[0], queue[1]};
 		queue[0] = null;
 		queue[1] = null;
 		lock.unlock();
 		
 		return result;
 	}
 
 	@Override
 	public void init(GameContainer gc) {
 		
 	}
 
 	
 	
 	@Override
 	public void draw(GameContainer gc, Graphics g) {
 		g.setColor(Color.decode("#F87217"));
 		g.drawRect(x * CELLSIZE, y * CELLSIZE, CELLSIZE, CELLSIZE);
 		g.drawRect(x * CELLSIZE + 1, y * CELLSIZE + 1, CELLSIZE - 2, CELLSIZE - 2);
 
 		lock.lock();
 
 		if (queue[0] != null) {
 			g.setColor(Color.decode("#348781"));
 			g.drawRect(queue[0].column * CELLSIZE, queue[0].row * CELLSIZE,	CELLSIZE, CELLSIZE);
 			g.drawRect(queue[0].column * CELLSIZE + 1, queue[0].row * CELLSIZE + 1, CELLSIZE - 2, CELLSIZE - 2);
 		}
 		if (!state.equals(GameState.UnitPlacement)) {
 			if (queue[1] != null) {
 				g.setColor(Color.decode("#348781"));
 				g.drawRect(queue[0].column * CELLSIZE, queue[0].row * CELLSIZE,	CELLSIZE, CELLSIZE);
 				g.drawRect(queue[0].column * CELLSIZE + 1, queue[0].row	* CELLSIZE + 1, CELLSIZE - 2, CELLSIZE - 2);
 			}
 		}
 		lock.unlock();
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) {
 		Input input = gc.getInput();
 		
 		if(input.isKeyPressed(Input.KEY_DOWN)){
 			y++;
 			if(y > HEIGHT-1){
 				y = 0;
 			}
 		}
 		if(input.isKeyPressed(Input.KEY_UP)){
 			y--;
 			if(y < 0){
				y = HEIGHT;
 			}
 		}
 		if(input.isKeyPressed(Input.KEY_RIGHT)){
 			x++;
 			if(x > WIDTH-1){
 				x = 0;
 			}
 		}
 		if(input.isKeyPressed(Input.KEY_LEFT)){
 			x--;
 			if(x < 0){
				x = WIDTH;
 			}
 		}
 		
 		if(input.isKeyPressed(Input.KEY_SPACE)){
 			lock.lock();
 			switch(state){
 			case UnitPlacement : 
 				if(queue[0] == null){
 					queue[0] = new Location(x,y);
 					condition.signalAll();
 				} 
 				break;
 			case UnitSwitching :
 			case GamePhase :
 				if(queue[0] == null){
 					queue[0] = new Location(x,y);
 				}
 				else if(queue[1] == null){
 					queue[1] = new Location(x,y);
 					condition.signalAll();
 				}
 				else{
 					condition.signalAll();
 				}
 			}
 			lock.unlock();
 		}
 		
 		if(input.isKeyPressed(Input.KEY_Q)){
 			endInitPhase = true;
 		}
 	}
 
 	
 
 }
